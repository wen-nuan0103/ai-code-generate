package com.xuenai.aicodegenerate.langgraph.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.xuenai.aicodegenerate.langgraph.model.dto.ImageResource;
import com.xuenai.aicodegenerate.langgraph.model.enums.ImageCategoryEnum;
import com.xuenai.aicodegenerate.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class LogoGeneratorTool {

    @Value("${dashscope.api-key}")
    private String dashScopeApiKey;

    @Value("${dashscope.image-model}")
    private String imageModel;

    @Resource
    private CosManager cosManager;

    @Tool("根据描述生成 Logo 设计图片，用于网站品牌标识")
    public List<ImageResource> generateLogo(@P("Logo 设计描述，例如名称、行业、风格等，尽量详细") String description) {
        List<ImageResource> logoList = new ArrayList<>();
        try {
            String prompt = String.format("生成 Logo ，Logo 中禁止包含任何文字！Logo 介绍: %s", description);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("prompt_extend", true);
            parameters.put("watermark", false);
            parameters.put("seed", 12345);
            ImageSynthesisParam params = ImageSynthesisParam.builder()
                    .apiKey(dashScopeApiKey)
                    .model(imageModel)
                    .prompt(prompt)
                    .n(1)
                    .size("768*768")
                    .parameters(parameters)
                    .build();
            ImageSynthesis imageSynthesis = new ImageSynthesis();
            ImageSynthesisResult result = imageSynthesis.call(params);
            if (result != null && result.getOutput() != null && result.getOutput().getResults() != null) {
                result.getOutput().getResults().forEach(image -> {
                    String url = image.get("url");
                    // 注意这个链接只有在24小时内有效，将其下载上传到自己的COS
                    if (StrUtil.isNotBlank(url)) {
                        File temp = FileUtil.createTempFile("logo_", ".jpg", true);
                        HttpUtil.downloadFile(url, temp);
                        String keyName = String.format("/logos/%s/%s", RandomUtil.randomString(5), temp.getName());
                        String cosUrl = cosManager.uploadFile(keyName, temp);
                        FileUtil.del(temp);
                        if (StrUtil.isNotBlank(cosUrl)) {
                            logoList.add(ImageResource.builder()
                                    .category(ImageCategoryEnum.LOGO)
                                    .description(description)
                                    .url(cosUrl)
                                    .build());
                        }
                    }
                });
            }
        } catch (Exception e) {
            log.error("生成 Logo 失败: {}", e.getMessage(), e);
        }

        return logoList;
    }

}
