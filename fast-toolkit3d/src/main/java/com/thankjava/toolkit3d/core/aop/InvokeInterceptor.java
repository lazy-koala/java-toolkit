package com.thankjava.toolkit3d.core.aop;

import java.lang.reflect.Method;

import com.thankjava.toolkit.bean.aop.entity.AopArgs;
import com.thankjava.toolkit.bean.aop.entity.AopCache;
import com.thankjava.toolkit.bean.aop.entity.AopConfig;
import com.thankjava.toolkit.core.aop.util.AopScanner;

import com.thankjava.toolkit.core.aop.util.BreakMethod;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

class InvokeInterceptor implements MethodInterceptor {


    private Class<?> implementObjectClass;

    public InvokeInterceptor(Class<?> implementObjectClass) {
        this.implementObjectClass = implementObjectClass;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {

        AopConfig aopConfig = AopCache.getAopConfig(obj, method, args);
        if (aopConfig == null) {
            AopScanner.scanner(implementObjectClass, obj.getClass());
            aopConfig = AopCache.getAopConfig(obj, method, args);
        }

        if (!aopConfig.isUsedAop()) {
            return proxy.invokeSuper(obj, args);
        }

        AopArgs aopArgs = new AopArgs(args);
        Object invokeReturn = null;

        BreakMethod.invokeBeforeCutPoint(aopConfig, aopArgs);

        if (aopArgs.isInvokeProxyMethod()) {
            invokeReturn = proxy.invokeSuper(obj, aopArgs.getInvokeArgs());
        }

        aopArgs.setReturnResult(invokeReturn);

        BreakMethod.invokeAfterCutPoint(aopConfig, aopArgs);

        return aopArgs.getReturnResult();
    }


}