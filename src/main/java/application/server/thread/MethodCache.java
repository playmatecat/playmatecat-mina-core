package application.server.thread;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 方法缓存,减少反射获取方法的性能消耗
 * @author blackcat
 *
 */
public class MethodCache {
	public final static ConcurrentHashMap<String, Method> methodMap = new ConcurrentHashMap<String, Method>();
	
	private MethodCache(){};
}
