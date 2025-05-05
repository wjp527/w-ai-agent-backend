package com.wjp.waiagentbackend.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * HTTP 方式调用请求
 */
public class HttpAiInvoke {
    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    public static void main(String[] args) {
        // 替换为你的 API Key
        String apiKey = TestApiKey.API_KEY;

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "qwen-plus");

        // 构建 input 部分
        JSONObject input = new JSONObject();
        JSONObject[] messages = new JSONObject[2];

        JSONObject systemMessage = new JSONObject();
        systemMessage.set("role", "system");
        systemMessage.set("content", "You are a helpful assistant.");

        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", "你是谁？");

        messages[0] = systemMessage;
        messages[1] = userMessage;

        input.set("messages", messages);
        requestBody.set("input", input);

        // 构建 parameters 部分
        JSONObject parameters = new JSONObject();
        parameters.set("result_format", "message");
        requestBody.set("parameters", parameters);

        // 发送请求
        String result = HttpRequest.post(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .execute()
                .body();

        // 打印响应结果
        System.out.println("Response: " + result);

        // 如果需要解析响应结果
        JSONObject response = JSONUtil.parseObj(result);
        System.out.println("Parsed response: " + response);
    }
}
