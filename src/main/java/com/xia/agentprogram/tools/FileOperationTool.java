package com.xia.agentprogram.tools;

import cn.hutool.core.io.FileUtil;
import com.xia.agentprogram.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(name = "readFile", description = "Read the content of a file")
    public String readFile(@ToolParam(description = "The name of the file to read") String fileName) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        }catch (Exception e){
            return "Error reading file: " + e.getMessage();
        }
    }

    @Tool(name = "writeFile", description = "Write content to a file")
    public String writeFile(@ToolParam(description = "The name of the file to write") String fileName,
                           @ToolParam(description = "The content to write to the file") String content) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "File written successfully to: " + filePath;
        }catch (Exception e){
            return "Error writing file: " + e.getMessage();
        }
    }


}
