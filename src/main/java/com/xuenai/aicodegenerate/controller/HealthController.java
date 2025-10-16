package com.xuenai.aicodegenerate.controller;

import com.xuenai.aicodegenerate.common.BaseResponse;
import com.xuenai.aicodegenerate.common.ResultUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/health")
@RestController
public class HealthController {
    
    @RequestMapping("/")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
    
}
