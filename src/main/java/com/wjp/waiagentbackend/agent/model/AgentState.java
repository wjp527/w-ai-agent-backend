package com.wjp.waiagentbackend.agent.model;

/**
 * 代理执行状态的枚举
 */
public enum AgentState {

    /**
     * 空间状态
     */
    IDLE,

    /**
     * 运行中的状态
     */
    RUNNING,

    /**
     * 已完成的状态
      */
    FINISHED,

    /**
     * 错误状态
     */
    ERROR

}
