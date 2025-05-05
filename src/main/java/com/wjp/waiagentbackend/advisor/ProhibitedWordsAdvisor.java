package com.wjp.waiagentbackend.advisor;

import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 违禁词拦截器 Advisor
 * @author wjp
 */
public class ProhibitedWordsAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    /**
     * 在调用之前执行
     * @param advisedRequest
     * @return
     */
    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        // 获取用户参数
        Map<String, Object> advisedUserParams = new HashMap<>(advisedRequest.userParams());
        // 设置违禁词
        advisedUserParams.put("prohibited_words", "nmd,tmd,sb,你妈的,傻逼");

        // 创建新的用户参数
        return AdvisedRequest.from(advisedRequest)
            .userText("""
                {prohibited_words}
                """)
            .userParams(advisedUserParams)
            .build();

    }

    /**
     * 在调用之后执行
     * @param advisedRequest the advised request
     * @param chain the advisor chain
     * @return
     */
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(this.before(advisedRequest));
    }

    /**
     * 在调用之后执行
     * @param advisedRequest the advised request
     * @param chain the chain of advisors to execute
     * @return
     */
    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(this.before(advisedRequest));
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
	}
}
