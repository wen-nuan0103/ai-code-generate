package com.xuenai.aicodegenerate.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xuenai.aicodegenerate.model.dto.points.AdminChargeRequest;
import com.xuenai.aicodegenerate.model.dto.points.PointsQueryRequest;
import com.xuenai.aicodegenerate.model.entity.Points;
import com.xuenai.aicodegenerate.model.enums.UserPointsTypeEnum;
import com.xuenai.aicodegenerate.model.vo.points.PointsVO;

import java.util.List;

public interface PointsService extends IService<Points> {

    /**
     * 管理员人工充值/扣款
     *
     * @param chargeRequest 请求 DTO
     * @return 充值/扣款后的余额
     */
    Long chargeBalance(AdminChargeRequest chargeRequest);

    /**
     * 批量获取脱敏展示 VO
     *
     * @param pointsList 用户积分日志列表
     * @return
     */
    List<PointsVO> getPointsVOList(List<Points> pointsList);

    /**
     * 获取脱敏展示 VO
     *
     * @param points 用户积分
     * @return
     */
    PointsVO getPointsVO(Points points);

    /**
     * 通过查询请求转换为QueryWrapper
     *
     * @param queryRequest 请求
     * @return
     */
    QueryWrapper getQueryWrapper(PointsQueryRequest queryRequest);

    /**
     * 通用积分变动接口（包含：改余额 + 记流水）
     *
     * @param userId 用户ID
     * @param points 变动积分
     * @param type   类型枚举
     * @param refId  关联ID 
     */
    void changePoints(Long userId, Long points, UserPointsTypeEnum type, Long refId,String BizNo,String remark);

}
