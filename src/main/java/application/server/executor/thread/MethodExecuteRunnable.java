package application.server.executor.thread;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.util.ReflectionUtils;

import com.playmatecat.mina.stucture.NioTransferAdapter;
import com.playmatecat.mina.stucture.RequestServiceAdapter;
import com.playmatecat.mina.stucture.ResponseServiceAdapter;
import com.playmatecat.utils.json.UtilsJson;
import com.playmatecat.utils.spring.UtilsSpringContext;

/**
 * 对mina接收到的方法,执行对应方法的线程执行体
 * @author blackcat
 *
 */
public class MethodExecuteRunnable extends Thread {

    private static final Logger LOGGER = Logger.getLogger(MethodExecuteRunnable.class);

    /** nio server session **/
    private IoSession session;
    /** nio 服务端收到的数据 **/
    private Object message;

    public MethodExecuteRunnable(IoSession session, Object message) {
        this.session = session;
        this.message = message;
    }

    @Override
    public void run() {
        if (!(message instanceof RequestServiceAdapter)) {
            return;
        }

        RequestServiceAdapter reqNta = (RequestServiceAdapter) message;

        // 请求的服务名,一般为组件名.方法
        String restServiceName = reqNta.getRestServiceName();

        String ctpName = "get From db by restServiceName";
        String ctpMethodName = "get From db by restServiceName";

        ctpName = "userCpt";
        ctpMethodName = "savetestCall";
        
        
        // 根据组件名获得执行类
        //Object reflectCpt = ApplicationContextHolder.getApplicationContext().getBean(ctpName);
        Object reflectCpt = UtilsSpringContext.getBean(ctpName);
        // nta.getClazz获得DTO的类型，作为反射调用函数的入参类型
        String result = StringUtils.EMPTY;
        Method method = null;
//        TransactionInterceptor transactionInterceptor 
//            = (TransactionInterceptor) ApplicationContextHolder.getApplicationContext().getBean("txAdvice");
        TransactionInterceptor transactionInterceptor 
            = (TransactionInterceptor) UtilsSpringContext.getBean("txAdvice");
        try {
            if(reflectCpt == null) {
                throw new Exception("服务组件不存在");
            }

            // 找出方法,通过spring反射工具，自带缓存
            method = ReflectionUtils.findMethod(reflectCpt.getClass(), ctpMethodName, reqNta.getClazz());
            
            if(method == null) {
                throw new Exception("服务组件的方法不存在");
            }

            List<Object> argsList = null;
            Object[] args = null;
            // 判断是否有参数
            if (reqNta.getClazz() != null) {
                argsList = new ArrayList<Object>();
                // json数据重新转回对象
                Object argObj = UtilsJson.parseJsonStr2Obj(reqNta.getRequestJsonData(), reqNta.getClazz());
                argsList.add(argObj);
            }
            args = argsList.toArray();
            
            //AspectJExpressionPointcut aj = (AspectJExpressionPointcut)UtilsSpringContext.getBean("pcServiceMethods");

            // 执行调用,通过事务拦截器调用(因为读写数据库所以方法都应该有事务)
            result = (String) transactionInterceptor.invoke(TxMethodProxy.getMethodInvocation(reflectCpt, method, args));
            
            // 直接反射测试模式，如果正式用上面注释的方法，用事务
            //result = (String) ReflectionUtils.invokeJdbcMethod(method, reflectCpt, args);
        } catch (Throwable e) {
            String errorClass = reflectCpt != null ? errorClass = reflectCpt.getClass().getName() : StringUtils.EMPTY;
            String errorMethod = method != null? method.getName() : StringUtils.EMPTY;
            String argsJson = reqNta.getRequestJsonData();
            String errMsg = MessageFormat.format("GUID:{0}, 错误信息:{1}, 类:{2}, 方法:{3}, 参数:{4}", 
                    new Object[]{reqNta.getGUID(), e.getMessage(), errorClass, errorMethod, argsJson});
            String simpleErrMsg =  MessageFormat.format("GUID:{0},错误信息:{1}",
                    new Object[]{reqNta.getGUID(), e.getMessage()});
            LOGGER.error("反射方法调用错误. " + errMsg, e);
            ResponseServiceAdapter rtnNta = new ResponseServiceAdapter(null, reqNta);
            rtnNta.setException(new Exception(simpleErrMsg));
            session.write(rtnNta);
            return;
        }

        // 返回数据，并且设定相同的唯一标码来保证客户端识别是哪次请求
        ResponseServiceAdapter rtnNta = new ResponseServiceAdapter(result, reqNta);

        // 通过mina传输数据
        session.write(rtnNta);

    }

}
