package com.xuenai.aicodegenerate.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xuenai.aicodegenerate.model.dto.app.AppQueryRequest;
import com.xuenai.aicodegenerate.model.entity.App;
import com.xuenai.aicodegenerate.model.entity.User;
import com.xuenai.aicodegenerate.model.vo.app.AppVO;

/**
 * 应用 服务层。
 *
 * @author 小菜
 */
public interface AppService extends IService<App> {

    /**
     * 校验应用
     *
     * @param app 应用
     * @param add 是否为创建校验
     */
    void validApp(App app, boolean add);

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
    Page<AppVO> getAppVOPage(Page<App> appPage);

    /**
     * 分页获取当前用户创建的应用
     *
     * @param appQueryRequest 查询请求
     * @param loginUser 当前登录用户
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

}
