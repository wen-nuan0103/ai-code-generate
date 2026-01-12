package com.xuenai.aicodegenerate.model.dto.points;

import com.xuenai.aicodegenerate.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 积分流水查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PointsQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID 
     * 用于排查某个具体用户的流水
     */
    private String userAccount;

    /**
     * 业务单号 
     * 用于根据 biz_no 排查某笔特定交易
     */
    private String bizNo;

    /**
     * 变动类型
     * 1-注册 2-签到 5-充值 101-AI消耗 等
     */
    private Integer type;

    /**
     * 搜索开始时间 (字符串格式: "2026-01-01 00:00:00")
     */
    private String createTimeStart;

    /**
     * 搜索结束时间
     */
    private String createTimeEnd;

    /**
     * 排序字段 
     */
    private String sortField;

    /**
     * 排序顺序 (ascend / descend)
     */
    private String sortOrder;
}