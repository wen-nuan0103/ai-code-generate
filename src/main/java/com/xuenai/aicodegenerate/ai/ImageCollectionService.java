package com.xuenai.aicodegenerate.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 图片收集服务
 */
public interface ImageCollectionService {

    /**
     * 收集项目图片
     *
     * @param userMessage 用户消息
     * @return 生成的项目信息
     */
    @SystemMessage(fromResource = "prompt/image-collection-system-prompt.txt")
    String collectImage(@UserMessage String userMessage);

}
