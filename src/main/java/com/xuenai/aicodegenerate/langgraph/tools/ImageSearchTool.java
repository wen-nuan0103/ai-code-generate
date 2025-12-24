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
public class ImageSearchTool {

    private static final String PEXELS_API_URL = "https://api.pexels.com/v1/search";

    @Value("${pexels.api-key}")
    private String pexelsApiKey;

    /**
     * 搜索图片
     *
     * @param queryEn       英文关键词（用于发给 Pexels API，搜索更准）
     * @param descriptionZh 中文描述（用于保存到数据库或前端展示，体验更好）
     */
    @Tool("搜索内容相关的图片，用于网站内容展示")
    public List<ImageResource> searchContentImages(@P("英文搜索关键词") String queryEn, @P("中文描述用途") String descriptionZh) {
        List<ImageResource> imageList = new ArrayList<>();
        int searchCount = 12;

        try (HttpResponse response = HttpRequest.get(PEXELS_API_URL).header("Authorization", pexelsApiKey).form("query", queryEn).form("per_page", searchCount).form("page", 1).execute()) {

            if (response.isOk()) {
                JSONObject result = JSONUtil.parseObj(response.body());
                if (result.containsKey("photos")) {
                    JSONArray photos = result.getJSONArray("photos");
                    for (int i = 0; i < photos.size(); i++) {
                        JSONObject photo = photos.getJSONObject(i);
                        JSONObject src = photo.getJSONObject("src");
                        String finalDesc = StrUtil.isNotBlank(descriptionZh) ? descriptionZh : photo.getStr("alt", queryEn);

                        imageList.add(ImageResource.builder().category(ImageCategoryEnum.CONTENT).description(finalDesc).url(src.getStr("medium")).build());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Pexels API 调用失败: {}", e.getMessage(), e);
        }
        return imageList;
    }
}