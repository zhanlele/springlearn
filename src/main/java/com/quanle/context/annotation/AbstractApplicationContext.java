package com.quanle.context.annotation;

import com.quanle.tansaction.BeanPostProcessor;

import org.junit.Assert;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author quanle
 * @date 2020/3/13 1:04 AM
 */
public abstract class AbstractApplicationContext {
    /**
     * spring bean 的一级缓存, 单例池 （bean name, object）
     */
    private Map<String, Object> singletonObjects;
    /**
     * spring bean 的二级缓存，用于存储还未完全实例化的对象（bean name, object）
     */
    private Map<String, Object> earlySingletonObjects;
    /**
     * spring bean 的三级缓存，用于暴露出已经实例化的bean (bean name, BeanDefinition)
     */
    private Map<String, Object> beanDefinitionRegistry;
    /**
     * 正在创建的bean
     */
    private Set<String> singletonsCurrentlyInCreation;
    /**
     * 后置处理类
     */
    private List<BeanPostProcessor> beanPostProcessors;

    public AbstractApplicationContext() {
        this.singletonObjects = new ConcurrentHashMap<>(16);
        this.earlySingletonObjects = new ConcurrentHashMap<>(16);
        this.beanDefinitionRegistry = new ConcurrentHashMap<>(16);
        this.singletonsCurrentlyInCreation = new CopyOnWriteArraySet<>();
        this.beanPostProcessors = new CopyOnWriteArrayList<>();
    }

    void preInstantiateSingletons(Map<String, BeanDefinition> beanDefinitionMap) {
        Assert.assertNotNull(beanDefinitionMap);

        // 实例化bean对象
        beanDefinitionMap.values().forEach(this::doCreateBean);
        // 执行后置增强生成事务的代理对象
        applyBeanPostProcessor();
    }

    private void applyBeanPostProcessor() {
        for (Map.Entry<String, Object> singletonBean : singletonObjects.entrySet()) {
            for (BeanPostProcessor postProcessor : beanPostProcessors) {
                Object proxy = postProcessor.postProcessAfterInitialization(singletonBean.getKey(), singletonBean.getValue());
                singletonObjects.put(singletonBean.getKey(), proxy);
            }
        }
    }

    private Object doCreateBean(BeanDefinition beanDefinition) {
        try {
            // 1. 实例化
            Object instance = createInstance(beanDefinition);
            // 2. 缓存object, 对正在创建的spring bean 进行打标
            this.beanDefinitionRegistry.put(beanDefinition.getBeanName(), instance);
            this.singletonsCurrentlyInCreation.add(beanDefinition.getBeanName());
            // 3. 填充属性
            populateBean(instance, beanDefinition.getDependencies());
            // 4. 属性填充完毕说明bean已经完成创建
            this.singletonsCurrentlyInCreation.remove(beanDefinition.getBeanName());
            // 5. 将二级缓存中的已完成创建的bean移动到一级缓存
            this.singletonObjects.put(beanDefinition.getBeanName(), instance);
            this.earlySingletonObjects.remove(beanDefinition.getBeanName());
            // 6. 对beanPostProcessor的类进行打标
            if (Arrays.asList(instance.getClass().getInterfaces()).contains(BeanPostProcessor.class)) {
                beanPostProcessors.add((BeanPostProcessor) instance);
            }
            // 7. return
            return instance;
        } catch (Exception e) {
            System.err.println("AbstractApplicationContext@doCreateBean exception, msg: " + e.getMessage());
            return null;
        }
    }

    private void populateBean(Object instance, Map<String, BeanDefinition> dependencies) throws NoSuchFieldException, IllegalAccessException {
        for (Map.Entry<String, BeanDefinition> e : dependencies.entrySet()) {
            String fieldName = e.getKey();
            BeanDefinition definition = e.getValue();
            Object fieldInstance = getBean(fieldName, definition);
            // 属性赋值
            Field instanceField = instance.getClass().getDeclaredField(fieldName);
            instanceField.setAccessible(true);
            instanceField.set(instance, fieldInstance);
        }
    }

    private Object getBean(String beanName, BeanDefinition definition) {
        Assert.assertNotNull(beanName);

        Object singleton = getSingleton(beanName);
        if (null == singleton) {
            Assert.assertNotNull(definition);
            singleton = doCreateBean(definition);
        }
        return singleton;
    }

    private Object getSingleton(String beanName) {
        Object singletonObject = this.singletonObjects.get(beanName);
        if (null == singletonObject && singletonsCurrentlyInCreation.contains(beanName)) {
            singletonObject = this.earlySingletonObjects.get(beanName);
            if (null == singletonObject) {
                singletonObject = this.beanDefinitionRegistry.get(beanName);
                if (null != singletonObject) {
                    this.earlySingletonObjects.put(beanName, singletonObject);
                    this.beanDefinitionRegistry.remove(beanName);
                }
            }
        }
        return singletonObject;
    }

    private Object createInstance(BeanDefinition beanDefinition) throws Exception {
        return beanDefinition.getClassType().getConstructor().newInstance();
    }

    public Object getBean(String beanName) {
        return this.singletonObjects.get(beanName);
    }
}
