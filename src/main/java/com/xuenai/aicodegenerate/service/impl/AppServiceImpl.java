package com.xuenai.aicodegenerate.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xuenai.aicodegenerate.ai.core.AiCodeGenerateFacade;
import com.xuenai.aicodegenerate.ai.mode.ProjectInfoResult;
import com.xuenai.aicodegenerate.constant.AppConstant;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.exception.ThrowUtils;
import com.xuenai.aicodegenerate.mapper.AppMapper;
import com.xuenai.aicodegenerate.model.dto.app.AppAddRequest;
import com.xuenai.aicodegenerate.model.dto.app.AppQueryRequest;
import com.xuenai.aicodegenerate.model.entity.App;
import com.xuenai.aicodegenerate.model.entity.User;
import com.xuenai.aicodegenerate.model.enums.ChatHistoryMessageTypeEnum;
import com.xuenai.aicodegenerate.model.enums.CodeGenerateTypeEnum;
import com.xuenai.aicodegenerate.model.vo.app.AppVO;
import com.xuenai.aicodegenerate.model.vo.user.UserVO;
import com.xuenai.aicodegenerate.service.AppService;
import com.xuenai.aicodegenerate.service.ChatHistoryService;
import com.xuenai.aicodegenerate.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author 小菜
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {


    @Resource
    private AiCodeGenerateFacade aiCodeGenerateFacade;

    @Resource
    private UserService userService;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Override
    public Flux<String> chatToGenerateCode(Long appId, String message, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR, "应用 ID 错误");
        ThrowUtils.throwIf(message == null, ErrorCode.PARAMS_ERROR, "提示词不能为空");

        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        ThrowUtils.throwIf(!Objects.equals(app.getUserId(), loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限操作该应用");

        String type = app.getCodeGeneratorType();
        CodeGenerateTypeEnum generatorTypeEnum = CodeGenerateTypeEnum.getEnumByValue(type);
        ThrowUtils.throwIf(generatorTypeEnum == null, ErrorCode.PARAMS_ERROR, "应用代码生成类型错误");

        chatHistoryService.createChatHistory(appId, loginUser.getId(), message, ChatHistoryMessageTypeEnum.USER.getValue());

        Flux<String> content = aiCodeGenerateFacade.generateStreamAndSaveCode(message, generatorTypeEnum, appId);
        StringBuilder builder = new StringBuilder();

        return content.map(chunk -> {
                    builder.append(chunk);
                    return chunk;
                })
                .doOnComplete(() -> {
                    String aiMessage = builder.toString();
                    if (StrUtil.isNotBlank(aiMessage)) {
                        chatHistoryService.createChatHistory(appId, loginUser.getId(), aiMessage, ChatHistoryMessageTypeEnum.AI.getValue());
                    }
                })
                .doOnError(error -> {
                    String errorMessage = "AI 回复失败: " + error.getMessage();
                    chatHistoryService.createChatHistory(appId, loginUser.getId(), errorMessage, ChatHistoryMessageTypeEnum.AI.getValue());
                });
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR, "应用 ID 错误");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        ThrowUtils.throwIf(!Objects.equals(app.getUserId(), loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限操作该应用");

        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }

        String type = app.getCodeGeneratorType();
        String sourceDirName = String.format("%s_%s", type, appId);
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(), ErrorCode.SYSTEM_ERROR, "代码生成目录不存在");
        String deployDir = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDir), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败: " + e.getMessage());
        }

        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean result = this.updateById(updateApp);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }

    @Override
    public void validApp(App app, boolean add) {
        if (app == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String initPrompt = app.getInitPrompt();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "应用初始化提示词不能为空");
        }
        if (StrUtil.isNotBlank(initPrompt) && initPrompt.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用初始化提示词过长");
        }
    }

    @Override
    public long createApp(AppAddRequest appAddRequest, User loginUser) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);

        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);

        this.validApp(app, true);

        app.setCodeGeneratorType(CodeGenerateTypeEnum.MULTI_FILE.getValue());
        app.setUserId(loginUser.getId());
        String initPrompt = appAddRequest.getInitPrompt();
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        boolean result = this.save(app);

        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        asyncGenerateProjectInfo(app);

        return app.getId();
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (appQueryRequest == null) {
            return queryWrapper;
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGeneratorType = appQueryRequest.getCodeGeneratorType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer deployStatus = appQueryRequest.getDeployStatus();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        Integer currentStatus = appQueryRequest.getCurrentStatus();
        Long version = appQueryRequest.getVersion();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        queryWrapper.eq("id", id).like("app_name", appName).like("cover", cover).like("init_prompt", initPrompt).eq("code_generator_type", codeGeneratorType).eq("deploy_key", deployKey).eq("priority", priority).eq("deploy_key", deployKey).eq("deploy_status", deployStatus).eq("priority", priority).eq("current_status", currentStatus).eq("version", version).eq("user_id", userId).orderBy(sortField, "ascend".equals(sortOrder));
        return queryWrapper;
    }

    @Override
    public AppVO getAppVO(App app) {
        AppVO appVO = getVo(app);
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            appVO.setUser(userService.getUserVO(user));
        }
        return appVO;
    }

    @Override
    public Page<AppVO> getAppVOPage(Page<App> appPage, boolean isMyApp) {
        List<App> appList = appPage.getRecords();
        Page<AppVO> appVOPage = new Page<>(appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow());
        if (CollUtil.isEmpty(appList)) {
            return appVOPage;
        }
        List<AppVO> appVOList = null;
        if (isMyApp) {
            appVOList = appList.stream().map(this::getVo).collect(Collectors.toList());
        } else {
            Set<Long> userIds = appList.stream().map(App::getUserId).collect(Collectors.toSet());
            Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, userService::getUserVO));
            appVOList = appList.stream().map(app -> {
                AppVO appVO = getVo(app);
                UserVO userVO = userVOMap.get(app.getUserId());
                appVO.setUser(userVO);
                return appVO;
            }).collect(Collectors.toList());
        }
        appVOPage.setRecords(appVOList);
        return appVOPage;
    }

    @Override
    public Page<AppVO> listMyAppVOByPage(AppQueryRequest appQueryRequest, User loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        appQueryRequest.setUserId(loginUser.getId());
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), this.getQueryWrapper(appQueryRequest));
        return this.getAppVOPage(appPage, true);
    }

    @Override
    public Page<AppVO> listAppVOByPage(AppQueryRequest appQueryRequest) {
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        // 查询精选应用（优先级大于0的应用）
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest);
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        return this.getAppVOPage(appPage, false);
    }

    @Override
    public boolean removeById(Serializable id) {
        if (id == null) return false;
        long appId = Long.parseLong(id.toString());
        if (appId <= 0) return false;

        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            log.error("删除应用历史记录失败（应用 ID 为:{}）: {}", appId, e.getMessage());
        }

        return super.removeById(id);
    }

    /**
     * 返回应用VO（不包含用户信息）
     *
     * @param app 应用
     * @return 应用VO
     */
    private AppVO getVo(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        return appVO;
    }

    /**
     * 异步线程创建项目信息
     *
     * @param app 应用信息
     */
    @Async("aiExecutor")
    public void asyncGenerateProjectInfo(App app) {
        try {
            String userMessage = app.getInitPrompt();
            ProjectInfoResult info = aiCodeGenerateFacade.generateProjectInfo(app.getId(),userMessage);

            App reviseApp = new App();
            reviseApp.setId(app.getId());
            reviseApp.setAppName(info.getName());
            reviseApp.setTags(info.getTags());
            this.updateById(reviseApp);
        } catch (Exception e) {
            log.error("生成项目信息失败: {}", e.getMessage());
        }
    }
}
