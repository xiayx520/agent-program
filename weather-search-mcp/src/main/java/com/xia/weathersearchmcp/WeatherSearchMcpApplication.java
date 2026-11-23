package com.xia.weathersearchmcp;

import com.xia.weathersearchmcp.tools.WeatherSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


//mvn -version 显示 Java version: 21.x；
//mvn clean compile 成功，不再出现"类文件具有错误的版本 61.0"。
//pom.xml 本身不需要任何改动，java.version=21 和 Spring AI 1.1.8 的依赖声明都是正确的，纯属本机 JDK/Maven/IDEA 环境级别配置问题。
@SpringBootApplication
public class WeatherSearchMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherSearchMcpApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider weatherSearchTools(WeatherSearchTool weatherSearchTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(weatherSearchTool)
                .build();
    }

}
