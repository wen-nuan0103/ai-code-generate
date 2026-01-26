package com.xuenai.aicodegenerate.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.xuenai.aicodegenerate.model.dto.ai.model.AiModelAddRequest;
import com.xuenai.aicodegenerate.model.dto.ai.model.AiModelQueryRequest;
import com.xuenai.aicodegenerate.model.dto.ai.model.AiModelUpdateRequest;
import com.xuenai.aicodegenerate.model.entity.AiModel;
import com.xuenai.aicodegenerate.model.vo.ai.AiModelVO;

import java.util.List;

public interface AiModelService extends IService<AiModel> {


    /**
     * 分页查询
     *
     * @param request 查询条件
     * @return
     */
    Page<AiModel> listAiModelByPage(AiModelQueryRequest request);

    /**
     * 添加模型
     *
     * @param request 添加请求
     */
    void addAiModel(AiModelAddRequest request);

    /**
     * 更新模型
     *
     * @param request 更新请求
     * @return
     */
    boolean updateAiModel(AiModelUpdateRequest request);

    /**
     * 获取VO列表 (用于下拉框)
     *
     * @param searchText
     * @return
     */
    List<AiModelVO> listAiModelVO(String searchText);

    /**
     * 根据模型能力获取对应的模型
     *
     * @param type 模型能力
     * @return
     */
    List<AiModel> listAiModelByType(String type);


    /**
     * 获取模型价格配置
     *
     * @param modelCode 模型编码
     * @param modelType 模型类型
     * @return
     */
    AiModel getModelPriceConfig(String modelCode, String modelType);

}
