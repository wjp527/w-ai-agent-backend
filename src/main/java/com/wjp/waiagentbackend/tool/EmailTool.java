package com.wjp.waiagentbackend.tool;

import com.wjp.waiagentbackend.config.MailConfig;
import com.wjp.waiagentbackend.model.dto.email.MailDto;
import jakarta.annotation.Resource;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import opennlp.tools.util.StringUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 发送邮箱工具
 * @author wjp
 */
@Component
public class EmailTool {

    // 注入邮件配置
    @Autowired
    private JavaMailSender javaMailSender;

    //发件人邮箱
    @Value("${spring.mail.username}")
    private String from;

    @Resource
    private MailConfig mailConfig;

    @Tool(description = "Send a email")
    public String sendEmail(@ToolParam(description = "Die parameter, an die die e-mail geschickt wird, einschließlich der adresse, " +
            "der subjekt und des inhalts\n") @RequestBody MailDto mail) throws AddressException {
        //判断文件格式是否正确
        //获取收件人邮箱
        String addressee = mail.getAddressee();
        // 校验邮箱是否合法
        if(!addressee.contains("@qq.com")){
            return "Briefkästen sind illegal.\n";
        }


        String subject = mail.getSubject();
        String content = mail.getContent();

        if(StringUtil.isEmpty(subject)) {
            mail.setSubject(mailConfig.getSubject());
        } else {
            mail.setSubject(subject);
        }
        if(StringUtil.isEmpty(content)) {
            mail.setContent(mailConfig.getContentPrefix() + mailConfig.getContentSuffix());
        } else {
            mail.setContent(content);
        }


        // 设置邮件内容
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        // 发件人邮箱,与配置文件中保持一致,所以直接从配置文件绑定过来了
        mailMessage.setFrom(String.valueOf(new InternetAddress("π" + "<" + from + ">")));
        // 收件人
        mailMessage.setTo(mail.getAddressee());
        // 标题
        mailMessage.setSubject(mail.getSubject());
        // 内容, 第一个参数为邮箱内容, 第二个参数为是否启用html格式,
        // 如果开启, 那么第一个参数如果有html标签, 会自动识别, 邮件样式更好看
        mailMessage.setText(mail.getContent());

        // 发送邮件
        javaMailSender.send(mailMessage);

        return "Send email successfully.";
    }
}
