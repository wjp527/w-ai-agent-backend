package com.wjp.waiagentbackend.tool;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PDFGenerationToolTest {

    @Test
    public void testGeneratePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "wjp.pdf";
        String content = "Github: https://www.github.com/wjp527";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}
