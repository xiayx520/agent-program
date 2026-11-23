package com.xia.weathersearchmcp.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;


@Service
public class WeatherSearchTool {

    @Tool(name = "WeatherSearchTool", description = "Search for weather information")
    public static String getWeather(@ToolParam(description = "The city to search for weather information") String city) {
        return "Weather in " + city + " is sunny with a temperature of 25 degrees Celsius.";
    }
}
