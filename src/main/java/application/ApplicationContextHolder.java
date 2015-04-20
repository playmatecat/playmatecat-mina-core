package application;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import application.server.thread.MethodExecutePool;

/**
 * spring配置文件加载器
 * @author root
 *
 */
public class ApplicationContextHolder {

	private static Logger logger = Logger
			.getLogger(ApplicationContextHolder.class);

	private static ApplicationContext ctx = null;

	// private static XmlWebApplicationContext mvcContext = null;
	
	private ApplicationContextHolder(){}
	
	/**
	 * 加载spring配置文件
	 */
	public static final void init() {
		if (ctx == null) {
			logger.info("[Spring] init application contexts...");
			long start = System.currentTimeMillis();
			// 从classpath中加载
			ctx = new ClassPathXmlApplicationContext("classpath*:/config/spring/application*.xml");
			long end = System.currentTimeMillis();
			logger.info("[Spring] init application contexts complate in " + (end - start) + "ms.");
			
			//加载完成后启动自己写的多线程方法扫描器
			Thread scanner = new Thread(new Runnable() {
				public void run() {
					MethodExecutePool.keepScanExcuteMethod();
				}
			});
			scanner.start();
		}
	}
	
	public static ApplicationContext getApplicationContext() {
		return ctx;
	}
}
