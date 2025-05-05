package com.wjp.waiagentbackend.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于文件持久化的对话记忆
 */
public class FileBasedChatMemory implements ChatMemory {

    // 指定文件保存目录
    private final String BASE_DIR;
    // 对话客户端
    private static final Kryo kryo = new Kryo();

    /**
     * 设置实例化策略
     */
    static {
        kryo.setRegistrationRequired(false);
        // 设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    // 构造对象时，指定文件保存目录
    public FileBasedChatMemory(String dir) {
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    /**
     * 添加对话
     * @param conversationId
     * @param messages
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> conversationMessages = getOrCreateConversation(conversationId);
        conversationMessages.addAll(messages);
        saveConversation(conversationId, conversationMessages);
    }

    /**
     * 获取对话
     * @param conversationId
     * @param lastN
     * @return
     */
    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<Message> allMessages = getOrCreateConversation(conversationId);
        return allMessages.stream()
                .skip(Math.max(0, allMessages.size() - lastN))
                .toList();
    }

    /**
     * 清空对话
     * @param conversationId
     */
    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 获取对话文件
     * @param conversationId
     * @return
     */
    private List<Message> getOrCreateConversation(String conversationId) {
        // 获取对话文件
        File file = getConversationFile(conversationId);
        // 如果文件不存在，则创建一个空的对话列表
        List<Message> messages = new ArrayList<>();
        if (file.exists()) {
            // 如果文件存在，则从文件中读取对话内容
            try (Input input = new Input(new FileInputStream(file))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    /**
     * 保存对话
     * @param conversationId
     * @param messages
     */
    private void saveConversation(String conversationId, List<Message> messages) {
        // 获取对话文件
        File file = getConversationFile(conversationId);
        // 将对话内容保存到文件中
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取对话文件
     * @param conversationId
     * @return
     */
    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR, conversationId + ".kryo");
    }
}
