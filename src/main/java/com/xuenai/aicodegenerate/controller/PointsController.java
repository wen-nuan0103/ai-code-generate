package com.xuenai.aicodegenerate.controller;

import com.mybatisflex.core.paginate.Page;
import com.xuenai.aicodegenerate.annotation.AuthCheck;
import com.xuenai.aicodegenerate.common.BaseResponse;
import com.xuenai.aicodegenerate.common.ResultUtils;
import com.xuenai.aicodegenerate.constant.UserConstant;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.exception.ThrowUtils;
import com.xuenai.aicodegenerate.model.dto.points.AdminChargeRequest;
import com.xuenai.aicodegenerate.model.dto.points.PointsQueryRequest;
import com.xuenai.aicodegenerate.model.entity.Points;
import com.xuenai.aicodegenerate.model.vo.points.PointsVO;
import com.xuenai.aicodegenerate.service.PointsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/points")
public class PointsController {

    @Resource
    private PointsService pointsService;

    @PostMapping("/admin/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<PointsVO>> listPointsLogVOByPage(@RequestBody PointsQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = queryRequest.getCurrent();
        long pageSize = queryRequest.getPageSize();
        // 查询数据
        Page<Points> pointsPage = pointsService.page(Page.of(pageNum, pageSize), pointsService.getQueryWrapper(queryRequest));
        Page<PointsVO> pointsVOPage = new Page<>(pageNum,pageSize,pointsPage.getTotalRow());
        List<PointsVO> pointsVOList = pointsService.getPointsVOList(pointsPage.getRecords());
        pointsVOPage.setRecords(pointsVOList);
        return ResultUtils.success(pointsVOPage);
    }

    @PostMapping("/admin/charge")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> chargePoints(@RequestBody AdminChargeRequest chargeRequest) {
        ThrowUtils.throwIf(chargeRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(chargeRequest.getUserId() == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(chargeRequest.getAmount() == null || chargeRequest.getAmount() == 0, ErrorCode.PARAMS_ERROR);

        Long newBalance = pointsService.chargeBalance(chargeRequest);
        return ResultUtils.success(newBalance);
    }

}
