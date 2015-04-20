package application;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.aop.framework.ReflectiveMethodInvocation;

public class TxMethodInvocation extends ReflectiveMethodInvocation{

	public TxMethodInvocation(Object proxy, Object target, Method method,
			Object[] arguments, Class<?> targetClass,
			List<Object> interceptorsAndDynamicMethodMatchers) {
		super(proxy, target, method, arguments, targetClass,
				interceptorsAndDynamicMethodMatchers);
	}
	
//	protected final Method method;
//	protected final Object target;
//
//	public Object[] getArguments() {
//		return (this.arguments != null ? this.arguments : new Object[0]);
//	}
//
//	public Object proceed() throws Throwable {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public Object getThis() {
//		return this.target;
//	}
//
//	public AccessibleObject getStaticPart() {
//		return this.method;
//	}
//
//	public Method getMethod() {
//		return this.method;
//	}

}
