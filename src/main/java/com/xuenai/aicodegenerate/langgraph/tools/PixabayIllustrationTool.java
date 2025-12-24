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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PixabayIllustrationTool {

    private static final String PIXABAY_API_URL = "https://pixabay.com/api/";

    @Value("${pixabay.api-key}")
    private String pixabayApiKey;

    /**
     * 搜索插画 (使用 Pixabay API)
     *
     * @param queryEn       英文关键词 (Pixabay 对英文支持最好)
     * @param descriptionZh 中文描述 (用于前端展示和语义理解)
     */
    @Tool("搜索插画图片，用于网站美化和装饰")
    public List<ImageResource> searchIllustrations(@P("英文搜索关键词") String queryEn, @P("中文描述用途") String descriptionZh) {
        List<ImageResource> imageList = new ArrayList<>();
        int searchCount = 12; // 获取前12张供选择

        try (HttpResponse response = HttpRequest.get(PIXABAY_API_URL).form("key", pixabayApiKey).form("q", queryEn).form("image_type", "illustration")     // 关键：指定 vector (矢量图) 或 illustration (插画)
                .form("safesearch", "true")       // 开启安全搜索
                .form("per_page", searchCount)    // 每页数量
                .timeout(10000).execute()) {

            if (!response.isOk()) {
                log.warn("Pixabay API 请求失败，状态码: {}", response.getStatus());
                return imageList;
            }

            JSONObject result = JSONUtil.parseObj(response.body());
            JSONArray hits = result.getJSONArray("hits");
            if (hits == null || hits.isEmpty()) {
                log.info("Pixabay 未搜索到相关插画: {}", queryEn);
                return imageList;
            }

            for (int i = 0; i < hits.size(); i++) {
                JSONObject hit = hits.getJSONObject(i);
                String imageUrl = hit.getStr("webformatURL");
                String tags = hit.getStr("tags");

                if (StrUtil.isBlank(imageUrl)) {
                    continue;
                }

                String finalDesc = StrUtil.isNotBlank(descriptionZh) ? descriptionZh : tags;

                imageList.add(ImageResource.builder().category(ImageCategoryEnum.ILLUSTRATION).description(finalDesc).url(imageUrl).build());
            }
        } catch (Exception e) {
            log.error("搜索 Pixabay 插画失败：{}", e.getMessage(), e);
        }
        return imageList;
    }
}