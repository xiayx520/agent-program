package com.xia.agentprogram.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TerminalOperationTool {

    /**
     * 命令执行超时时间（秒），避免命令挂起导致 Agent 调用永久阻塞
     */
    private static final long TIMEOUT_SECONDS = 30;

    /**
     * 是否为 Windows 系统，用于选择对应的命令解释器
     */
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    /**
     * 命令输出编码：JDK 18+ 默认 charset 为 UTF-8，而中文 Windows 的 cmd 输出为 GBK，
     * 使用 native.encoding 匹配系统真实编码，避免中文输出乱码
     */
    private static final Charset OUTPUT_CHARSET = Charset.forName(
            System.getProperty("native.encoding", Charset.defaultCharset().name()));

    @Tool(description = "Execute a command in the terminal")
    public String executeTerminalCommand(@ToolParam(description = "Command to execute in the terminal") String command) {
        StringBuilder output = new StringBuilder();
        try {
            // 根据操作系统选择命令解释器：Windows 用 cmd，其他系统用 sh
            ProcessBuilder builder = IS_WINDOWS
                    ? new ProcessBuilder("cmd.exe", "/c", command)
                    : new ProcessBuilder("sh", "-c", command);
            // 合并错误流到标准输出，确保错误信息（如命令不存在）也能被捕获
            builder.redirectErrorStream(true);

            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), OUTPUT_CHARSET))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            // 限时等待命令执行完成，超时则强制终止进程
            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                output.append("Command execution timed out after ").append(TIMEOUT_SECONDS).append(" seconds");
                return output.toString();
            }
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                output.append("Command execution failed with exit code: ").append(exitCode);
            }
        } catch (IOException e) {
            log.error("执行终端命令失败, command: {}", command, e);
            return "Error executing command: " + e.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("执行终端命令被中断, command: {}", command, e);
            return "Command execution interrupted: " + e.getMessage();
        }
        return output.toString();
    }

}
