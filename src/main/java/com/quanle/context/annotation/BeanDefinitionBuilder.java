package com.quanle.context.annotation;

import com.alibaba.druid.util.StringUtils;
import com.quanle.annotation.AutowiredX;
import com.quanle.annotation.ComponentX;
import com.quanle.annotation.RepositoryX;
import com.quanle.annotation.ServiceX;
import com.quanle.annotation.TransactionalX;

import org.junit.Assert;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * @author quanle
 * @date 2020/3/13 1:06 AM
 */
public class BeanDefinitionBuilder {

    private ClassLoader classLoader;
    private Predicate<Class<?>> filter;
    /**
     * 缓存(key: className)
     */
    private Map<String, BeanDefinition> definitionMap;
    /**
     * 缓存(key: beanName)
     */
    private Map<String, BeanDefinition> beanDefinitionMap;
    /**
     * 将内部注入是接口类型的时候，进行延迟构建
     */
    private Set<BeanDefinition> lazyDefinitions;

    public BeanDefinitionBuilder(ClassLoader classLoader, Predicate<Class<?>> filter) {
        Assert.assertNotNull(classLoader);
        Assert.assertNotNull(filter);

        this.classLoader = classLoader;
        this.filter = filter;
        this.definitionMap = new ConcurrentHashMap<>(64);
        this.beanDefinitionMap = new ConcurrentHashMap<>(64);
        this.lazyDefinitions = new HashSet<>(64);
    }

    public BeanDefinition build(String className) {
        Assert.assertNotNull(className);

        if (definitionMap.containsKey(className)) {
            return definitionMap.get(className);
        }

        try {
            Class<?> classType = Class.forName(className);
            if (!filter.test(classType)) {
                String beanName = getBeanName(classType);
                BeanDefinition beanDefinition = new BeanDefinition(className, beanName, classType, classLoader);
                // 缓存beanDefinition
                definitionMap.put(className, beanDefinition);
                beanDefinitionMap.put(beanName, beanDefinition);
                // 构建内部依赖的beanDefinition
                buildDependency(beanName, beanDefinition);
                return beanDefinition;
            }
            return null;
        } catch (Throwable e) {
            System.err.println("BeanDefinitionBuilder@build failed, msg: " + e.getMessage());
            return null;
        }
    }

    private void buildDependency(String beanName, BeanDefinition beanDefinition) {
        boolean hasInterfaceField = false;
        Class<?> classType = beanDefinition.getClassType();
        for (Field field : classType.getDeclaredFields()) {
            if (null == field.getAnnotation(AutowiredX.class)) {
                continue;
            }
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();
            // 判断是否存在接口注入的形式
            if (fieldType.isInterface()) {
                hasInterfaceField = true;
//                beanDefinition.getDependencies().put(fieldName, null);
//            } else {
            }
            beanDefinition.getDependencies().put(fieldName, build(fieldType.getName()));
        }
        if (hasInterfaceField) {
            this.lazyDefinitions.add(beanDefinition);
        }
    }

    private String getBeanName(Class<?> objectInstance) {
        Assert.assertNotNull(objectInstance);
        // 通过注解的value获取别名
        String beanName = null;
        if (null != objectInstance.getAnnotation(ComponentX.class)) {
            beanName = objectInstance.getAnnotation(ComponentX.class).value();
        } else if (null != objectInstance.getAnnotation(ServiceX.class)) {
            beanName = objectInstance.getAnnotation(ServiceX.class).value();
        } else if (null != objectInstance.getAnnotation(RepositoryX.class)) {
            beanName = objectInstance.getAnnotation(RepositoryX.class).value();
        } else if (null != objectInstance.getAnnotation(AutowiredX.class)) {
            beanName = objectInstance.getAnnotation(AutowiredX.class).value();
        } else if (null != objectInstance.getAnnotation(TransactionalX.class)) {
            beanName = objectInstance.getAnnotation(TransactionalX.class).value();
        }
        // 如果注解中没有value属性则根据类型进行驼峰命名
        if (StringUtils.isEmpty(beanName)) {
            beanName = getFirstLowercase(objectInstance.getSimpleName());
        }
        return beanName;
    }

    /**
     * 首字母小写
     *
     * @param name AccountService
     * @return accountService
     */
    private String getFirstLowercase(String name) {
        Assert.assertNotNull(name);
        return String.format("%s%s", Character.toLowerCase(name.charAt(0)), name.substring(1));
    }

    /**
     * 延迟构建
     */
    public void buildLazyBeanDefinitions() {
        // 属性依赖未构建的BeanDefinition
        for (BeanDefinition definition : this.lazyDefinitions) {
            for (Map.Entry<String, BeanDefinition> entity : definition.getDependencies().entrySet()) {
                if (null == entity.getValue()) {
                    String fieldName = entity.getKey();
                    BeanDefinition beanDefinition = beanDefinitionMap.get(fieldName);
                    definition.getDependencies().put(fieldName, beanDefinition);
                }
            }
        }
        // 清空lazyDefinitions
        lazyDefinitions.clear();
    }

    public Map<String, BeanDefinition> getDefinitionMap() {
        return definitionMap;
    }
}
