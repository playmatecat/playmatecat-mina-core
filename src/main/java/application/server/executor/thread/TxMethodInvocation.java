package application.server.executor.thread;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.aop.framework.ReflectiveMethodInvocation;

/**
 * 事务方法调用类
 * @author root
 *
 */
class TxMethodInvocation extends ReflectiveMethodInvocation {

    public TxMethodInvocation(Object proxy, Object target, Method method, Object[] arguments, Class<?> targetClass,
            List<Object> interceptorsAndDynamicMethodMatchers) {
        super(proxy, target, method, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
    }

}
