package com.quanle.context.annotation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author quanle
 * @date 2020/3/13 1:06 AM
 */
public class BeanDefinition {
    /**
     * 类的全类名 com.quanle.servlet.TransferServlet
     */
    private String className;
    /**
     * 类的名称, 首先根据自定义注解中的value来声明，如果没有则以驼峰命名
     */
    private String beanName;
    /**
     * classType 反射后的字节码对象
     */
    private Class<?> classType;
    /**
     * 类加载器
     */
    private ClassLoader classLoader;
    /**
     * 内部需要进行bean属性注入的依赖
     */
    private Map<String, BeanDefinition> dependencies;

    public BeanDefinition(String className, String beanName, Class<?> classType, ClassLoader classLoader) {
        this.className = className;
        this.beanName = beanName;
        this.classType = classType;
        this.classLoader = classLoader;
        this.dependencies = new HashMap<>();
    }

    public BeanDefinition() {
        this.dependencies = new HashMap<>();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Class<?> getClassType() {
        return classType;
    }

    public void setClassType(Class<?> classType) {
        this.classType = classType;
    }

    public Map<String, BeanDefinition> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Map<String, BeanDefinition> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BeanDefinition that = (BeanDefinition) o;
        return Objects.equals(className, that.className) &&
                Objects.equals(beanName, that.beanName) &&
                Objects.equals(classType, that.classType) &&
                Objects.equals(dependencies, that.dependencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, beanName, classType, dependencies);
    }
}
