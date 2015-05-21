package application.server.executor.thread;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.util.ReflectionUtils;

import application.ApplicationContextHolder;

import com.playmatecat.mina.NioTransferAdapter;
import com.playmatecat.utils.json.UtilsJson;


public class MethodExecuteRunnable extends Thread {
	
	private final static Logger logger = Logger.getLogger(MethodExecuteRunnable.class);
	
	/**nio server session**/
	private IoSession session; 
	/**nio 服务端收到的数据**/
	private Object message;
	
	public MethodExecuteRunnable(IoSession session, Object message) {
		this.session = session;
		this.message = message;
	}

	@Override
	public void run() {
		if(!(message instanceof NioTransferAdapter)) {
			return;
		}
		
		NioTransferAdapter nta = (NioTransferAdapter) message;
		
		//请求唯一标码
		String GUID = nta.getGUID();
		//请求的服务名,一般为组件名.方法
		String restServiceName = nta.getRestServiceName();
		
		String ctpName = "get From db by restServiceName";
		String ctpMethodName = "get From db by restServiceName";
		
		ctpName = "userCpt";
		ctpMethodName = "savetestCall";
		
		//根据组件名获得执行类
		Object reflectCpt = ApplicationContextHolder.getApplicationContext().getBean(ctpName);
		//nta.getClazz获得DTO的类型，作为反射调用函数的入参类型
		String result = StringUtils.EMPTY;
		
		TransactionInterceptor transactionInterceptor = (TransactionInterceptor) ApplicationContextHolder.getApplicationContext().getBean("txAdvice");
		try {
			//找出方法,通过spring反射工具，自带缓存
			Method method = ReflectionUtils.findMethod(reflectCpt.getClass(), ctpMethodName, nta.getClazz());
			
			List<Object> argsList = null;
			Object args[] = null;
			//判断是否有参数
			if(nta.getClazz() != null) {
				argsList = new ArrayList<Object>();
				//json数据重新转回对象
				Object argObj = UtilsJson.parseJsonStr2Obj(nta.getJSONdata(), nta.getClazz());
				argsList.add(argObj);
			}
			args = argsList.toArray();
			//执行调用,通过事务拦截器调用(因为读写数据库所以方法都应该有事务)
			//result = (String) transactionInterceptor.invoke(TxMethodProxy.getMethodInvocation(reflectCpt, method, args));
			
			//直接反射测试模式，如果正式用上面注释的方法，用事务
			result = (String) ReflectionUtils.invokeJdbcMethod(method, reflectCpt, args);
		} catch (Throwable e) {
			logger.error("反射方法调用错误", e);
		}
		
		//返回数据，并且设定相同的唯一标码来保证客户端识别是哪次请求
		NioTransferAdapter rtnNta = new NioTransferAdapter(result);
		rtnNta.setGUID(GUID);
		//通过mina传输数据
		session.write(rtnNta);

	}
	
	
}
