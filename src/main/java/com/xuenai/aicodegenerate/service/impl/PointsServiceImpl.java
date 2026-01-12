package com.xuenai.aicodegenerate.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.exception.ThrowUtils;
import com.xuenai.aicodegenerate.mapper.PointsMapper;
import com.xuenai.aicodegenerate.mapper.UserMapper;
import com.xuenai.aicodegenerate.model.dto.points.AdminChargeRequest;
import com.xuenai.aicodegenerate.model.dto.points.PointsQueryRequest;
import com.xuenai.aicodegenerate.model.entity.Points;
import com.xuenai.aicodegenerate.model.entity.User;
import com.xuenai.aicodegenerate.model.enums.UserPointsTypeEnum;
import com.xuenai.aicodegenerate.model.vo.points.PointsVO;
import com.xuenai.aicodegenerate.service.PointsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PointsServiceImpl extends ServiceImpl<PointsMapper, Points> implements PointsService {

    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long chargeBalance(AdminChargeRequest chargeRequest) {
        Long userId = chargeRequest.getUserId();
        Long amount = chargeRequest.getAmount();

        this.changePoints(userId, amount,
                amount > 0 ? UserPointsTypeEnum.SYSTEM_ADD : UserPointsTypeEnum.SYSTEM_DEDUCT
                , null,
                "admin_charge_" + UUID.randomUUID().toString()
                , chargeRequest.getRemark());

        User user = QueryChain.of(User.class)
                .where(User::getId)
                .eq(userId).one();

        return user.getPoints();
    }

    @Override
    public List<PointsVO> getPointsVOList(List<Points> pointsList) {
        if (CollUtil.isEmpty(pointsList)) {
            return new ArrayList<>();
        }
        Set<Long> userIds = pointsList.stream().map(Points::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectListByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        return pointsList.stream().map(this::getPointsVO)
                .peek(points -> {
                    User user = userMap.get(points.getUserId());
                    if (user != null) {
                        points.setUserAccount(user.getUserAccount());
                        points.setUserName(user.getUserName());
                        points.setAvatar(user.getAvatar());
                    }
                    UserPointsTypeEnum typeEnum = UserPointsTypeEnum.getEnumByValue(points.getType());
                    points.setIsIncome(typeEnum.isIncome());
                }).collect(Collectors.toList());
    }

    @Override
    public PointsVO getPointsVO(Points points) {
        if (points == null) {
            return null;
        }
        PointsVO pointsVO = new PointsVO();
        BeanUtil.copyProperties(points, pointsVO);
        return pointsVO;
    }

    @Override
    public QueryWrapper getQueryWrapper(PointsQueryRequest queryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (queryRequest == null) {
            return queryWrapper;
        }
        String account = queryRequest.getUserAccount();
        if (StrUtil.isNotBlank(account)) {
            User user = QueryChain.of(User.class)
                    .where(User::getUserAccount)
                    .eq(account).one();
            if (user != null) {
                queryWrapper.eq("user_id", user.getId());
            }
        }

        Integer type = queryRequest.getType();
        String bizNo = queryRequest.getBizNo();
        String startTime = queryRequest.getCreateTimeStart();
        String endTime = queryRequest.getCreateTimeEnd();
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        queryWrapper
                .eq("type", type)
                .eq("biz_no", bizNo)
                .ge("create_time", startTime)
                .le("create_time", endTime);

        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("create_time", false);
        }

        return queryWrapper;
    }


    @Override
    public void changePoints(Long userId, Long points, UserPointsTypeEnum type, Long refId, String BizNo, String remark) {
        if (points == 0) {
            return;
        }

        User user = userMapper.selectOneById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        long newPoints = user.getPoints() + points;
        boolean updateResult = UpdateChain.of(User.class)
                .setRaw(User::getPoints, "points + " + points)
                .where(User::getId).eq(userId)
                .update();

        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新积分失败");
        
        Points log = new Points();
        log.setUserId(userId);
        log.setAmount(points);
        log.setType(type.getValue());
        log.setCurrentPoints(newPoints);
        log.setRefId(refId);
        log.setBizNo(BizNo);
        log.setRemark(remark);

        this.save(log);
    }

}
