package com.xuenai.aicodegenerate.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xuenai.aicodegenerate.model.dto.user.UserLoginRequest;
import com.xuenai.aicodegenerate.model.dto.user.UserQueryRequest;
import com.xuenai.aicodegenerate.model.dto.user.UserRegisterRequest;
import com.xuenai.aicodegenerate.model.dto.user.UserUpdateRequest;
import com.xuenai.aicodegenerate.model.entity.User;
import com.xuenai.aicodegenerate.model.vo.user.LoginUserVO;
import com.xuenai.aicodegenerate.model.vo.user.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户表 服务层。
 *
 * @author 小菜
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     *
     * @param userLoginRequest 登录请求DTO
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);


    /**
     * 用户注册
     *
     * @param request
     * @return 用户ID
     */
    long userRegister(UserRegisterRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);


    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏展示 VO
     *
     * @param user 用户
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 批量获取脱敏展示 VO
     *
     * @param userList 用户列表
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 通过查询请求转换为QueryWrapper
     *
     * @param userQueryRequest 查询请求
     * @return
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 获取加密后的密码
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String password);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 更新用户信息
     *
     * @param request 用户修改信息DTO
     * @return 是否修改成功
     */
    boolean updateInfo(UserUpdateRequest request);

}
