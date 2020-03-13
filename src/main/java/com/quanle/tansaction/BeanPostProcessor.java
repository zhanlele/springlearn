package com.quanle.tansaction;

/**
 * @author quanle
 * @date 2020/3/13 12:44 AM
 */
public interface BeanPostProcessor {

    Object postProcessAfterInitialization(String beanName, Object target);
}
