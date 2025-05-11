package com.wjp.waiagentbackend.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MyTikaDocumentReaderTest {

    @Resource
    private MyTikaDocumentReader myTikaDocumentReader;
    @Test
    void loadText() throws Exception {
        List<Document> documents = myTikaDocumentReader.loadWordDocuments();
        System.out.println("documents = " + documents);
    }
}