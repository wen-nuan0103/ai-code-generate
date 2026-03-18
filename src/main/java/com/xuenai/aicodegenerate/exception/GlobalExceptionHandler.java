package com.xuenai.aicodegenerate.exception;

import cn.hutool.json.JSONUtil;
import com.xuenai.aicodegenerate.common.BaseResponse;
import com.xuenai.aicodegenerate.common.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        if (handleSseError(e.getCode(), e.getMessage())) {
            return null;
        }
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        if (handleSseError(ErrorCode.SYSTEM_ERROR.getCode(), "系统错误")) {
            return null;
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    private boolean handleSseError(int errorCode, String errorMessage) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return false;
        }

        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        boolean sseRequest = (accept != null && accept.contains("text/event-stream"))
                || uri.contains("/chat/gen/code");

        if (!sseRequest) {
            return false;
        }

        if (response == null || response.isCommitted()) {
            return true;
        }

        try {
            response.setContentType("text/event-stream");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");

            Map<String, Object> errorData = Map.of(
                    "error", true,
                    "code", errorCode,
                    "message", errorMessage
            );
            String errorJson = JSONUtil.toJsonStr(errorData);
            String sseData = "event: business-error\ndata: " + errorJson + "\n\n"
                    + "event: done\ndata: {}\n\n";

            response.getOutputStream().write(sseData.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
        } catch (IOException | IllegalStateException ex) {
            log.warn("Failed to write SSE error response", ex);
        }
        return true;
    }
}
