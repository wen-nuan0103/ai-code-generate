package com.xuenai.aicodegenerate.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ProjectDownloadService {
    
    void downloadProjectAsZip(String projectPath, String projectName, HttpServletResponse response);
    
}
