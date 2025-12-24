package com.xuenai.aicodegenerate.langgraph.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xuenai.aicodegenerate.langgraph.model.dto.ImageResource;
import com.xuenai.aicodegenerate.langgraph.model.enums.ImageCategoryEnum;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Deprecated
@Slf4j
@Component
public class UndrawIllustrationTool {

    private static final String UNDRAW_API_URL = "https://undraw.co/_next/data/SNHhDZgzZi3Ah8uuKvVO7/search/%s.json?term=%s";

    /**
     * 搜索插画
     * @param queryEn 英文关键词（Undraw 对英文支持最好）
     * @param descriptionZh 中文描述（用于前端展示和语义理解）
     */
    @Tool("搜索插画图片，用于网站美化和装饰")
    public List<ImageResource> searchIllustrations(
            @P("英文搜索关键词") String queryEn,
            @P("中文描述用途") String descriptionZh
    ) {
        List<ImageResource> imageList = new ArrayList<>();
        int searchCount = 12; // 获取前12张供选择
        String apiUrl = String.format(UNDRAW_API_URL, queryEn, queryEn);

        try (HttpResponse response = HttpRequest.get(apiUrl).timeout(10000).execute()) {
            if (!response.isOk()) {
                log.warn("Undraw API 请求失败，状态码: {}", response.getStatus());
                return imageList;
            }

            JSONObject result = JSONUtil.parseObj(response.body());
            JSONObject pageProps = result.getJSONObject("pageProps");
            if (pageProps == null) {
                return imageList;
            }

            JSONArray initialResults = pageProps.getJSONArray("initialResults");
            if (initialResults == null || initialResults.isEmpty()) {
                log.info("Undraw 未搜索到相关插画: {}", queryEn);
                return imageList;
            }

            int actualCount = Math.min(searchCount, initialResults.size());
            for (int i = 0; i < actualCount; i++) {
                JSONObject illustration = initialResults.getJSONObject(i);
                String title = illustration.getStr("title", "插画");
                String media = illustration.getStr("image", "");
                if (StrUtil.isBlank(media)) {
                    continue;
                }
                // 优先使用传入的中文描述，如果没传则用原版标题兜底
                String finalDesc = StrUtil.isNotBlank(descriptionZh) ? descriptionZh : title;

                imageList.add(ImageResource.builder()
                        .category(ImageCategoryEnum.ILLUSTRATION)
                        .description(finalDesc)
                        .url(media)
                        .build());
            }
        } catch (Exception e) {
            log.error("搜索插画失败：{}", e.getMessage(), e);
        }
        return imageList;
    }
}