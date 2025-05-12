package com.wjp.waiagentbackend.model.dto.email;

import lombok.Data;

@Data
public class MailDto {

    private String addressee;

    private String subject;

    private String content;

}

