package com.xia.weathersearchmcp.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeatherSearchToolTest {

    @Test
    void getWeather() {
        String result = WeatherSearchTool.getWeather("北京");
        assertNotNull(result);
        System.out.println(result);
    }
}