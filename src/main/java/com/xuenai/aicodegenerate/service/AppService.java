package com.xuenai.aicodegenerate.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xuenai.aicodegenerate.model.dto.app.AppAddRequest;
import com.xuenai.aicodegenerate.model.dto.app.AppQueryRequest;
import com.xuenai.aicodegenerate.model.entity.App;
import com.xuenai.aicodegenerate.model.entity.User;
import com.xuenai.aicodegenerate.model.vo.app.AppVO;
import reactor.core.publisher.Flux;

/**
 * 应用 服务层。
 *
 * @author 小菜
 */
public interface AppService extends IService<App> {


    /**
     * 通过对话生成代码
     *
     * @param appId     应用 ID
     * @param message   对话信息
     * @param loginUser 用户
     * @return 响应式流
     */
    Flux<String> chatToGenerateCode(Long appId, String message, User loginUser);

    /**
     * 部署应用
     *
     * @param appId     应用 ID
     * @param loginUser 用户
     * @return 访问应用的地址
     */
    String deployApp(Long appId, User loginUser);

    /**
     * 下线应用
     *
     * @param appId     应用 ID
     * @param loginUser 用户
     * @return 是否下线成功
     */
    boolean offlineApp(Long appId, User loginUser);


    /**
     * 校验应用
     *
     * @param app 应用
     * @param add 是否为创建校验
     */
    void validApp(App app, boolean add);

    /**
     * 创建应用
     *
     * @param appAddRequest 添加请求
     * @param loginUser     用户
     * @return 应用 ID
     */
    long createApp(AppAddRequest appAddRequest, User loginUser);

    /**
     * 获取查询条件
     *
     * @param appQueryRequest 查询请求
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用封装
     *
     * @param app 应用
     * @return AppVO
     */
    AppVO getAppVO(App app);

    /**
     * 分页获取应用封装
     *
     * @param appPage 应用分页
     * @return AppVO分页
     */
    Page<AppVO> getAppVOPage(Page<App> appPage, boolean isMyApp);

    /**
     * 分页获取当前用户创建的应用
     *
     * @param appQueryRequest 查询请求
     * @param loginUser       当前登录用户
     * @return AppVO分页
     */
    Page<AppVO> listMyAppVOByPage(AppQueryRequest appQueryRequest, User loginUser);

    /**
     * 分页获取精选应用
     *
     * @param appQueryRequest 查询请求
     * @return AppVO分页
     */
    Page<AppVO> listAppVOByPage(AppQueryRequest appQueryRequest);

    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用 ID
     * @param appUrl 应用访问 URL
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);

}
