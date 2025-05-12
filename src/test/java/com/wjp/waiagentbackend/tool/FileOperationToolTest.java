package com.wjp.waiagentbackend.tool;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileOperationToolTest {

    @Test
    void readFile() {
        String fileName = "wjp";
        FileOperationTool fileOperationTool = new FileOperationTool();
        String result = fileOperationTool.readFile(fileName);
        assertNotNull(result);

    }

    @Test
    void writeFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "wjp";
        String content = "网址: https://github.com/wjp527";
        String result = fileOperationTool.writeFile(fileName, content);
        assertNotNull(result);

    }
}