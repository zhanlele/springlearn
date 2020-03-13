package com.quanle.context.annotation;

import com.quanle.annotation.AutowiredX;
import com.quanle.annotation.ComponentX;
import com.quanle.annotation.RepositoryX;
import com.quanle.annotation.ServiceX;
import com.quanle.annotation.TransactionalX;

import org.junit.Assert;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author quanle
 * @date 2020/3/13 1:05 AM
 */
public class AnnotationConfigApplicationContext extends AbstractApplicationContext {
    private String basePackage;
    private BeanDefinitionBuilder definitionBuilder;

    public AnnotationConfigApplicationContext() {
        super();
    }

    public void scanPackage(String basePackage) {
        scanPackage(basePackage, this::classFilter);
    }

    public void scanPackage(String basePackage, Predicate<Class<?>> classFilter) {
        this.basePackage = basePackage;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        this.definitionBuilder = new BeanDefinitionBuilder(classLoader, classFilter);

        String rootPath = basePackage.replaceAll("\\.", "/");
        URL rootUrl = classLoader.getResource(rootPath);
        scanPackage(rootUrl, classFilter);
    }

    private void scanPackage(URL rootUrl, Predicate<Class<?>> filter) {
        Assert.assertNotNull(rootUrl);
        File file = new File(rootUrl.getFile());
        Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(f -> {
            if (f.isDirectory()) {
                scanDirectory(f);
            } else if (isClass(f.getName())) {
                scanClass(f);
            }
        });

        // build finish
        definitionBuilder.buildLazyBeanDefinitions();
        Map<String, BeanDefinition> definitionMap = definitionBuilder.getDefinitionMap();

        // 创建springBean
        preInstantiateSingletons(definitionMap);
    }

    private boolean classFilter(Class<?> clazz) {
        // 内部类过滤
        if (clazz.isAnonymousClass()) {
            return true;
            // 无自定义注解的类过滤
        } else {
            return !matchAnyAnnotation(clazz);
        }
    }

    private boolean matchAnyAnnotation(Class<?> clazz) {
        boolean match = false;
        if (null != clazz.getAnnotation(AutowiredX.class)) {
            match = true;
        }
        if (null != clazz.getAnnotation(ComponentX.class)) {
            match = true;
        }
        if (null != clazz.getAnnotation(ServiceX.class)) {
            match = true;
        }
        if (null != clazz.getAnnotation(RepositoryX.class)) {
            match = true;
        }
        if (null != clazz.getAnnotation(TransactionalX.class)) {
            match = true;
        }
        return match;
    }

    private boolean isClass(String name) {
        return name.endsWith(".class");
    }

    private void scanClass(File file) {
        if (file.isDirectory()) {
            scanDirectory(file);
        } else {
            doScanClass(file);
        }
    }

    /**
     * 构建BeanDefinition
     *
     * @param file class对象
     */
    private void doScanClass(File file) {
        // 将根据绝对路径 -> 转换成类的全类名
        final String filePathWithDot = file.getAbsolutePath().replace(File.separator, ".");
        int packageIndex = filePathWithDot.indexOf(basePackage);
        String className = filePathWithDot.substring(packageIndex).replace(".class", "");
        // 将bean的定义信息放在容器list容器中
        definitionBuilder.build(className);
    }

    private void scanDirectory(File file) {
        Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(this::scanClass);
    }
}
