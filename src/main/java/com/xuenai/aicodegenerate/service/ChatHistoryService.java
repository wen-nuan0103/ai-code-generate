package com.xuenai.aicodegenerate.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xuenai.aicodegenerate.model.dto.chat.history.ChatHistoryQueryRequest;
import com.xuenai.aicodegenerate.model.entity.ChatHistory;
import com.xuenai.aicodegenerate.model.entity.User;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author 小菜
 */
public interface ChatHistoryService extends IService<ChatHistory> {


    /**
     * 创建对话历史
     *
     * @param appId       应用 ID
     * @param userId      用户 ID
     * @param message     消息内容
     * @param messageType 消息类型
     * @return 是否添加成功
     */
    boolean createChatHistory(Long appId, Long userId, String message, String messageType);


    /**
     * 创建包含思考过程的聊天记录
     *
     * @param appId           应用ID
     * @param userId          用户ID
     * @param message         正文内容（最终代码/回复）
     * @param thinkingMessage 思考过程（JSON字符串）
     * @param messageType     消息类型
     * @return 是否成功
     */
    boolean createChatHistoryWithThinking(Long appId, Long userId, String message, String thinkingMessage, String messageType);
    
    /**
     * 根据应用 ID 删除对话历史
     *
     * @param appId 应用 ID
     * @return 是否删除成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 获取查询条件
     *
     * @param historyQueryRequest 查询请求
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest historyQueryRequest);

    /**
     * 将对话历史加载到内存中
     *
     * @param appId      应用 ID
     * @param chatMemory 内存
     * @param maxCount   加载最大数量
     * @return 加载的数量
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

    /**
     * 分页获取历史记录
     *
     * @param appId          应用 ID
     * @param pageSize       每页大小
     * @param lastCreateTine 最后创建时间
     * @param LoginUser      登录用户
     * @return 分页结果
     */
    Page<ChatHistory> listChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTine, User LoginUser);

    /**
     * 将历史记录导出为 Markdown
     *
     * @param appId     应用 ID
     * @param loginUser 登录用户
     * @return Markdown 文本
     */
    String exportChatToMarkdown(Long appId, User loginUser);

    /**
     * 根据应用 ID 获取对话历史数量
     * @param appId 应用 ID
     * @return 对话历史数量
     */
    Long countByAppId(Long appId);
}
