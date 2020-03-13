package com.quanle.tansaction.impl;

import com.quanle.annotation.AutowiredX;
import com.quanle.annotation.ComponentX;
import com.quanle.annotation.TransactionalX;
import com.quanle.tansaction.BeanPostProcessor;
import com.quanle.utils.TransactionManager;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;

/**
 * @author quanle
 * @date 2020/3/13 12:44 AM
 */
@ComponentX
public class TransactionPostProcessor implements BeanPostProcessor {
    @AutowiredX
    private TransactionManager transactionManager;

    @Override
    public Object postProcessAfterInitialization(String beanName, Object target) {
        Object proxy = target;
        if (null != target.getClass().getAnnotation(TransactionalX.class)) {
            if (target.getClass().getInterfaces().length > 0) {
                proxy = jdkProxy(target);
            } else {
                proxy = cglibProxy(target);
            }
        }
        return proxy;
    }

    private Object cglibProxy(Object target) {
        return Enhancer.create(target.getClass(),
                (MethodInterceptor) (obj, method, args, proxy) -> doProxyMethod(target, method, args));
    }

    private Object jdkProxy(Object target) {
        return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(),
                (proxy, method, args) -> doProxyMethod(target, method, args));
    }

    private Object doProxyMethod(Object target, Method method, Object[] args) throws SQLException,
            InvocationTargetException, IllegalAccessException {

        Object retValue;
        transactionManager.beginTransaction();
        try {
            retValue = method.invoke(target, args);
        } catch (Throwable e) {
            System.err.println(String.format("\n method[%s] errorMsg[%s] \n", method.getName(), e.getMessage()));
            transactionManager.rollback();
            throw e;
        }
        transactionManager.commit();
        return retValue;
    }
}
