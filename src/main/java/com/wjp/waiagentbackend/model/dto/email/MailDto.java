package com.wjp.waiagentbackend.model.dto.email;

import lombok.Data;

/**
 * 邮件发送参数
 */
@Data
public class MailDto {

    private String addressee;

    private String subject;

    private String content;

}

