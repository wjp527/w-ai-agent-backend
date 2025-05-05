package com.wjp.waiagentbackend.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;
    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String answer = loveApp.doChat("您好，我是程序员鱼皮", chatId);
        // 第二轮
        loveApp.doChat("我的另一半是编程导航", chatId);
        // 第三轮
        loveApp.doChat("我的另一半名字叫什么", chatId);

    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "您好，我是程序员鱼皮,想让另一半(编程导航)更爱我，但我不知道该怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }
}