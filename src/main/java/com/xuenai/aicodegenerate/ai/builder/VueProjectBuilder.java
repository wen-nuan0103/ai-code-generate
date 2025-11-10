package com.xuenai.aicodegenerate.ai.builder;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VueProjectBuilder {

    /**
     * 异步编译项目
     *
     * @param projectPath 项目路径
     */
    public void buildProjectAsync(String projectPath) {
        Thread.ofVirtual().name("vue-build-" + System.currentTimeMillis()).start(() -> {
            try {
                buildProject(projectPath);
            } catch (Exception e) {
                log.error("异步构建 Vue 项目时发生异常: {}", e.getMessage());
            }
        });
    }

    /**
     * 编译项目
     *
     * @param projectPath 项目路径
     * @return 是否编译完成
     */
    public boolean buildProject(String projectPath) {
        projectPath = projectPath.replace("\\", "/");
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在: {}", projectPath);
            return false;
        }
        File packageJson = new File(projectDir, "package.json");
        if (!packageJson.exists()) {
            log.error("项目目录下没有package.json文件: {}", projectPath);
            return false;
        }
        log.info("开始构建Vue项目: {}", projectPath);
        if (!executeNpmInstall(projectDir)) {
            log.error("npm install执行失败: {}", projectPath);
            return false;
        }
        if (!executeNpmRunBuild(projectDir)) {
            log.error("npm run build执行失败: {}", projectPath);
            return false;
        }
        File distDir = new File(projectDir, "dist");
        if (!distDir.exists() || !distDir.isDirectory()) {
            log.error("构建后的dist目录不存在: {}", projectPath);
            return false;
        }
        log.info("Vue项目构建完成: {}", projectPath);
        return true;
    }

    /**
     * 执行命令
     *
     * @param workingDir     执行命令的目录
     * @param command        命令
     * @param timeoutSeconds 执行命令超时时间
     * @return 是否执行完成
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            Process process = RuntimeUtil.exec(null, workingDir, command.split("\\s+"));
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时: {}", command);
                process.destroyForcibly();
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return true;
            }
            log.error("命令执行失败: {}, 错误码: {}", command, exitCode);
            return false;
        } catch (Exception e) {
            log.error("命令执行失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 执行安装依赖命令
     *
     * @param projectDir 项目目录
     * @return 是否安装完成
     */
    private boolean executeNpmInstall(File projectDir) {
        String command = String.format("%s install", buildCommand("npm"));
        return executeCommand(projectDir, command, 300);
    }

    /**
     * 执行构建命令
     *
     * @param projectDir 项目目录
     * @return 是否构建完成
     */
    private boolean executeNpmRunBuild(File projectDir) {
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 300);
    }

    /**
     * 构建执行命令
     *
     * @param baseCommand 基础命令
     * @return 构建后的命令
     */
    private String buildCommand(String baseCommand) {
        if (isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }

    /**
     * 判断当前操作系统是否为Windows
     *
     * @return 是否
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

}
