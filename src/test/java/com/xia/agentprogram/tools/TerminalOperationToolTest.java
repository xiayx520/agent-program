package com.xia.agentprogram.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TerminalOperationToolTest {

    @Test
    void executeTerminalCommand() {
        TerminalOperationTool tool = new TerminalOperationTool();
        // 按操作系统选择列目录命令：Windows 用 dir，其他系统用 ls -l
        String command = System.getProperty("os.name").toLowerCase().contains("win") ? "dir" : "ls -l";
        String result = tool.executeTerminalCommand(command);
        System.out.println(result);
        assertNotNull(result);
        assertFalse(result.contains("failed"), "命令不应执行失败: " + result);
    }
}