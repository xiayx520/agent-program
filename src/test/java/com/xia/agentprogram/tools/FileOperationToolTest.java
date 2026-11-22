package com.xia.agentprogram.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationToolTest {

    @Test
    void readFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "懒癌日报.txt";
        String result = tool.readFile(fileName);
        assertNotNull(result);
    }

    @Test
    void writeFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "懒癌日报.txt";
        String content = "懒癌学习交流社区";
        String result = tool.writeFile(fileName, content);
        assertNotNull(result);
    }
}