package com.xia.agentprogram.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "恋爱常见问题和回答.pdf";
        String content = "如何提升自身魅力吸引潜在伴侣？\n" +
                "提升自身魅力需从多方面入手。外在形象上，保持良好的个人卫生，选择适合自己身材与风格的着装，定期锻炼塑造健康体魄。内在修养方面，培养广泛兴趣爱好，如阅读提升知识储备，学习乐器增添艺术气质。积极参加社交活动，锻炼沟通与表达能力，以自信、热情的态度展现自我。比如小李原本内向且不注重穿着，通过学习穿搭、参与户外运动并加入读书俱乐部，逐渐变得开朗自信，吸引了不少异性目光。\n" +
                "推荐课程：[《单身魅力提升全攻略》]，该课程从形象打造、气质培养到社交技巧提升，全方位助力单身人士散发独特魅力，吸引心仪对象。\n";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}