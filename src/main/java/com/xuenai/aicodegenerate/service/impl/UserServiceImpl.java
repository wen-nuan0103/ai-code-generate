package com.xuenai.aicodegenerate.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xuenai.aicodegenerate.constant.UserConstant;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.manager.CosManager;
import com.xuenai.aicodegenerate.mapper.UserMapper;
import com.xuenai.aicodegenerate.model.dto.user.UserLoginRequest;
import com.xuenai.aicodegenerate.model.dto.user.UserQueryRequest;
import com.xuenai.aicodegenerate.model.dto.user.UserRegisterRequest;
import com.xuenai.aicodegenerate.model.dto.user.UserUpdateRequest;
import com.xuenai.aicodegenerate.model.entity.User;
import com.xuenai.aicodegenerate.model.enums.UserPointsTypeEnum;
import com.xuenai.aicodegenerate.model.enums.UserRoleEnum;
import com.xuenai.aicodegenerate.model.vo.user.LoginUserVO;
import com.xuenai.aicodegenerate.model.vo.user.UserVO;
import com.xuenai.aicodegenerate.service.PointsService;
import com.xuenai.aicodegenerate.service.UserService;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.xuenai.aicodegenerate.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户表 服务层实现。
 *
 * @author 小菜
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private CosManager cosManager;
    
    @Resource
    private PointsService pointsService;

    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        Boolean isAdminLogin = userLoginRequest.getIsAdminLogin();
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("password", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        if (isAdminLogin) {
            if (!UserConstant.ADMIN_ROLE.equals(user.getRole())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "非管理员禁止登录后台");
            }
        }
        // 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public long userRegister(UserRegisterRequest request) {
        String userAccount = request.getUserAccount();
        String userPassword = request.getUserPassword();
        String checkPassword = request.getCheckPassword();
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        if (userAccount.length() < 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度过短");
        }

        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }

        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
        }

        // 查询用户是否存在
        User existsUser = this.queryChain().where(User::getUserAccount).eq(userAccount).one();
        if (existsUser != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        
        // 加密密码
        String encryptPassword = getEncryptPassword(userPassword);
        User user = new User();
        User inviter = null;

        String inviteCode = request.getInviteCode();
        if (StringUtils.isNotBlank(inviteCode)) {
            inviter = this.queryChain()
                    .where(User::getShareCode)
                    .eq(inviteCode)
                    .one();
            if (inviter == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邀请码无效"); 
            }
        }
        
        user.setUserAccount(userAccount);
        user.setPassword(encryptPassword);
        user.setUserName(UUID.randomUUID().toString().substring(0, 6));
        user.setRole(UserRoleEnum.USER.getValue());
        user.setShareCode(UUID.randomUUID().toString().substring(0, 6));
        if (inviter != null) {
            user.setInviteUser(inviter.getId());
        }
        user.setPoints(0L);
        boolean result = this.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }

        Long userId = user.getId();
        // 注册奖励
        pointsService.changePoints(
                userId,
                UserPointsTypeEnum.REGISTER.getPoints(),
                UserPointsTypeEnum.REGISTER,
                null,
                "register_charge_" + UUID.randomUUID().toString(),
                "注册赠送"
        );
        // 邀请奖励 
        if (inviter != null) {
            pointsService.changePoints(
                    inviter.getId(),
                    UserPointsTypeEnum.INVITE.getPoints(),
                    UserPointsTypeEnum.INVITE,
                    userId,
                    "inviter_charge_" + UUID.randomUUID().toString(),
                    "邀请用户注册赠送"
            );
            
            pointsService.changePoints(
                    userId,
                    UserPointsTypeEnum.INVITE.getPoints(),
                    UserPointsTypeEnum.INVITE,
                    inviter.getId(),
                    "invited_charge_" + UUID.randomUUID().toString(),
                    "被邀请注册赠送"
            );
        }
        

        return userId;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }


    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }


    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        if (StrUtil.isNotBlank(user.getPhone())) {
            user.setPhone(DesensitizedUtil.mobilePhone(user.getPhone()));
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getRole();
        Integer userStatus = userQueryRequest.getUserStatus();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create().eq("id", id).eq("role", userRole).like("user_account", userAccount).like("user_name", userName).like("profile", userProfile).eq("user_status",userStatus).orderBy(sortField, "ascend".equals(sortOrder));
    }


    @Override
    public String getEncryptPassword(String password) {
        final String SALT = "snow";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getRole());
    }

    @Override
    public boolean updateInfo(UserUpdateRequest request) {
        User user = new User();
        BeanUtil.copyProperties(request, user);
        String avatar = user.getAvatar();
        if (StringUtils.isNotBlank(avatar) && avatar.contains("/temp/")) {
            String tempKey = extractKeyFromUrl(avatar);
            String realKey = tempKey.replace("temp/", "/");
            cosManager.moveObject(tempKey, realKey);
            String url = avatar.replace("temp/", "/");
            user.setAvatar(url);
        }
        return this.updateById(user);
    }


    /**
     * 从 URL 中提取 COS 的 Key
     *
     * @param url cos url
     * @return cos key（文件路径）
     */
    private String extractKeyFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            if (path == null) {
                return url;
            }
            if (path.startsWith("/")) {
                return path.substring(1);
            }
            return path;
        } catch (URISyntaxException e) {

            return url;
        }
    }
}
