package com.xuenai.aicodegenerate.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xuenai.aicodegenerate.annotation.AuthCheck;
import com.xuenai.aicodegenerate.common.BaseResponse;
import com.xuenai.aicodegenerate.common.ResultUtils;
import com.xuenai.aicodegenerate.constant.UserConstant;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.exception.ThrowUtils;
import com.xuenai.aicodegenerate.model.dto.chat.history.ChatHistoryQueryRequest;
import com.xuenai.aicodegenerate.model.entity.ChatHistory;
import com.xuenai.aicodegenerate.model.entity.User;
import com.xuenai.aicodegenerate.service.ChatHistoryService;
import com.xuenai.aicodegenerate.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 对话历史 控制层。
 *
 * @author 小菜
 */
@RestController
@RequestMapping("/chat-history")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 根据appId分页查询对话历史
     *
     * @param appId          appId
     * @param pageNum        页码
     * @param lastCreateTime 最后创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId, @RequestParam(defaultValue = "10") int pageNum, @RequestParam(required = false) LocalDateTime lastCreateTime, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.listChatHistoryByPage(appId, pageNum, lastCreateTime, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 导出对话历史为Markdown格式
     *
     * @param appId   appId
     * @param request 请求
     * @return Markdown格式字符串
     */
    @GetMapping("/{appId}/export")
    public BaseResponse<String> exportMarkdown(@PathVariable Long appId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String markdown = chatHistoryService.exportChatToMarkdown(appId, loginUser);
        return ResultUtils.success(markdown);
    }

    /**
     * 管理员分页查询所有对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = chatHistoryQueryRequest.getCurrent();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }


}
