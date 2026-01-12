package com.xuenai.aicodegenerate.model.vo.points;

import com.xuenai.aicodegenerate.model.entity.Points;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class PointsVO extends Points implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否收入
     */
    private Boolean isIncome;
    
    /**
     * 关联的用户信息
     */
    private String userAccount;
    
    private String userName;
    
    private String avatar;
    
}
