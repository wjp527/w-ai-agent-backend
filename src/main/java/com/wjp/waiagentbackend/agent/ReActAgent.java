package com.wjp.waiagentbackend.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct (Reasoning and Acting)模式的代理抽象类
 * 实现思考+行动的两个步骤的智能体
 *
 * @author wjp
 */
@EqualsAndHashCode(callSuper = true) // 用于自动生成 equals 和 hashCode方法
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent{

    /**
     * 执行当前状态并决定下一步行动
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     * @return 行动执行结果
     */
    public abstract String act();
    @Override
    public String step() {
        try {
            Boolean think = think();
            if(!think) {
                return "思考完毕 - 无需行动";
            }
            return act();
        } catch (Exception e) {
            // 记录异常日志
            log.error("步骤执行失败: " + e.getMessage());
            return "步骤执行失败: " + e.getMessage();
        }
    }
}
