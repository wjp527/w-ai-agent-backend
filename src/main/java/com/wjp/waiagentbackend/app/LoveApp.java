package com.wjp.waiagentbackend.app;


import com.wjp.waiagentbackend.advisor.MyLoggerAdvisor;
import com.wjp.waiagentbackend.chatmemory.FileBasedChatMemory;
import com.wjp.waiagentbackend.rag.LoveAppRagCustomAdvisorFactory;
import com.wjp.waiagentbackend.rag.QueryInternationalization;
import com.wjp.waiagentbackend.rag.QueryInternationalizationSDK;
import com.wjp.waiagentbackend.rag.QueryReWriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * 恋爱专家对话应用
 * @Author: wjp
 */
@Component
@Slf4j
public class LoveApp {
    // 客户端
    private final ChatClient chatClient;

    // 系统提示
    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。\n";

    /**
     * 初始化 ChatClient客户端
     * @param dashscopeChatModel
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        // 初始化基于内存的对话记忆
//        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient
                // 设置对话模型
                .builder(dashscopeChatModel)
                // 设置对话记忆
                .defaultSystem(SYSTEM_PROMPT)
                // 设置默认拦截器
                .defaultAdvisors(
                        // 设置对话记忆拦截器
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 自定义日志拦截器，可按需开启
                        new MyLoggerAdvisor()
                        // 自定义违禁词拦截器
//                        new ProhibitedWordsAdvisor()
//                        // 自定义推理增强 Advisor，可按需开启
//                        new ReReadingAdvisor()
                )
                .build();

    }

    /**
     * AI 基础对话方法(支持多轮对话)
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        // 调用prompt方法，传入用户消息
        ChatResponse response = chatClient
                // 开始对话提示
                .prompt()
                // 设置用户的消息内容
                .user(message)
                // 设置对话的参数，包括聊天的ID和检索对话历史的大小
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                )
                // 发送请求并获取响应
                .call()
                // 从响应中提取出ChatResponse对象
                .chatResponse();

        // 从ChatResponse对象中获取对话结果，并提取出文本内容
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions) {

    }

    /**
     * AI 恋爱报告功能（实战结构化输出）
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        // 调用prompt方法，传入用户消息
        LoveReport loveReport = chatClient
                // 开始对话提示
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                // 设置用户的消息内容
                .user(message)
                // 设置对话的参数，包括聊天的ID和检索对话历史的大小
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                )
                // 发送请求并获取响应
                .call()
                .entity(LoveReport.class);

        log.info("loveReport: {}", loveReport);
        return loveReport;
    }


    /**
     * AI 恋爱知识库问答功能
     */


    @Resource
    private VectorStore loveAppVectorStore;

    // RAG知识库
    @Resource
    private Advisor loveAppRegCloudAdvisor;

    // PgVector向量存储
    @Resource
    private VectorStore pgVectorVectorStore;

    // 重写查询
    @Resource
    private QueryReWriter queryReWriter;

    // 国际化[文本翻译]
    @Resource
    private QueryInternationalization queryInternationalization;

    // 国际化[文本翻译 - SDK]
    @Resource
    private QueryInternationalizationSDK queryInternationalizationSDK;

    /**
     * 判断文本是否包含中文
     * @param text
     * @return
     */
      public boolean isChinese(String text) {
      return text != null && text.matches(".*[\\u4e00-\\u9fff].*"); // 简单正则匹配中文字符
  }

    /**
     * AI 恋爱报告功能（实战结构化输出）
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        // 重写对话信息
        String rewrittenMessage = queryReWriter.doQueryRewrite(message);

        // 国际化
//        String result = queryInternationalization.doQueryTransformer(rewrittenMessage);

        String result = rewrittenMessage;
        // 判断该 rewrittenMessage 如果不是中文进行转换
        if (!isChinese(rewrittenMessage)) {
            // SDK 国际化
            result = queryInternationalizationSDK.doTextTranslation(rewrittenMessage);
        }

        ChatResponse response = chatClient
                .prompt()
                // 设置用户的消息内容
                .user(result)
                // 设置对话的参数，包括聊天的ID和检索对话历史的大小【支持多轮对话】
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId).param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启自定义日志
                .advisors(new MyLoggerAdvisor())
                // 应用 RAG 知识库问答[基于本地]
//                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                // 应用 RAG 检索增强服务[基于云服务]
//                .advisors(loveAppRegCloudAdvisor)
                // 应用 RAG 检索增强服务[基于 PgVector 向量存储]
//                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                // 应用自定义的 RAG检索增强服务[文档查询器 + 上下文检索器]
                .advisors(
                        LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(
                                // 本地向量存储
                                loveAppVectorStore,
                                // 检索条件
//                                "单身"
                                "已婚"
                        )
                )
                .call()
                .chatResponse();

        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;

    }
}
