package application.server.thread;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import application.ApplicationContextHolder;

import com.playmatecat.mina.NioTransferAdapter;


public class MethodExecuteRunnable extends Thread {
	
	private final static Logger logger = Logger.getLogger(MethodExecuteRunnable.class);
	
	/**nio server session**/
	private IoSession session; 
	/**nio 服务端收到的数据**/
	private Object message;
	/**调用bean方法最大执行时间**/
	
	public MethodExecuteRunnable(IoSession session, Object message) {
		this.session = session;
		this.message = message;
	}

	@Override
	public void run() {
		NioTransferAdapter nta = (NioTransferAdapter) message;
		
		//请求唯一标码
		String GUID = nta.getGUID();
		//请求的服务名
		String restServiceName = nta.getRestServiceName();
		
		String ctpName = "get From db by restServiceName";
		String ctpMethodName = "get From db by restServiceName";
		
		ctpName = "userCpt";
		ctpMethodName = "savetestCall";
		
		//获得组件名
		Object reflectCpt = ApplicationContextHolder.getApplicationContext().getBean(ctpName);
		//nta.getClazz获得DTO的类型，作为反射调用函数的入参类型
		String result = StringUtils.EMPTY;
		
		TransactionInterceptor transactionInterceptor = (TransactionInterceptor) ApplicationContextHolder.getApplicationContext().getBean("txAdvice");
		try {
			if (nta.getClazz() != null) {
				// 有参调用
				// 反射方法用了很多时间！！所以用缓存
				// 尝试从缓存中取得需要执行方法,找不到再用反射
				String keyName = ctpMethodName + nta.getClazz().getName();
				Method method = MethodCache.methodMap.get(keyName);
				if (method == null) {
					method = reflectCpt.getClass().getMethod(ctpMethodName,
							nta.getClazz());
					MethodCache.methodMap.put(keyName, method);
				}
				
				List<Object> argsList = new ArrayList<Object>();
				argsList.add(nta.getJSONdata());
				argsList.add(nta.getClazz());
				result = (String) transactionInterceptor.invoke(TxMethodProxy.getMethodInvocation(reflectCpt, method, argsList.toArray()));
			} else {
				// 无参调用
				Method method = reflectCpt.getClass().getMethod(ctpMethodName);
				result = (String) transactionInterceptor.invoke(TxMethodProxy.getMethodInvocation(reflectCpt, method, null));
			}
		} catch (Throwable e) {
			logger.error("反射方法调用错误", e);
		}
		
		//返回数据，并且设定相同的唯一标码来保证客户端识别是哪次请求
		NioTransferAdapter rtnNta = new NioTransferAdapter(result);
		rtnNta.setGUID(GUID);
		session.write(rtnNta);

		MethodExecutePool.runningThreadCount--;

	}
	
	
}
