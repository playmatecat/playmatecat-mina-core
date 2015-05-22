package application.server.executor.thread;

import java.lang.reflect.Method;

import org.springframework.aop.framework.ProxyFactory;

/**
 * 事务代理类
 * @author blackcat
 *
 */
class TxMethodProxy {
    private TxMethodProxy() {
    }

    public static TxMethodInvocation getMethodInvocation(Object target, Method method, Object[] args) throws Throwable {
        ProxyFactory proxyFactory = new ProxyFactory(target);
        TxMethodInvocation txMethodInvocation = new TxMethodInvocation(proxyFactory.getProxy(), 
                target, method, args, proxyFactory.getTargetClass(),
                proxyFactory.getInterceptorsAndDynamicInterceptionAdvice(method, proxyFactory.getTargetClass()));
        return txMethodInvocation;
    }

}
