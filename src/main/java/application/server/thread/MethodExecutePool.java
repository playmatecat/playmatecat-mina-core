package application.server.thread;

import java.util.LinkedList;

import org.apache.log4j.Logger;



public class MethodExecutePool {
	
	private final static Logger logger = Logger.getLogger(MethodExecutePool.class);
	
	public static int runningThreadCount = 0;
	
	
	/**任务缓冲列队**/
	private static LinkedList<Runnable> queueTaskList = new LinkedList<Runnable>();
	
	/**任务扫描是否已经唯一开启过**/
	private static boolean isScanInited = false;
	
	
	/** cpu数量 **/
	private final static int numberOfCores = Runtime.getRuntime().availableProcessors();

	/** 阻塞系数 **/
	private final static double blockingCoefficient = 0.9;
	
	/**
	 * 线程池最大数量
	 * 线程数=CPU可用核心数/（1 - 阻塞系数），其中阻塞系数在在0到1范围内。
	 * 计算密集型程序的阻塞系数为0，IO密集型程序的阻塞系数接近1。
	 */
	private final static int MAX_POOL_SIZE = (int) (numberOfCores / (1 - blockingCoefficient));

	
	
	
	private MethodExecutePool(){};
	
	public static void execute(Runnable runnable) {
		//加入到任务列队
		queueTaskList.add(runnable);
		
	}
	
//	/**
//	 * 本方法只能在spring启动时被执行唯一一次,即扫描器只有唯一一个
//	 * 循环不断的扫描列队中是否有任务,有则执行
//	 */
//	public static void keepScanExcuteMethod() {
//		if(!isScanInited) {
//			isScanInited = true;
//			try {
//				while(true) {
//					Thread.sleep(1);
//					if(runningThreadCount < MAX_POOL_SIZE) {
//						Runnable peekTask = queueTaskList.pollFirst();
//						if(peekTask != null) {
//							Thread thread = new Thread(peekTask);
//							runningThreadCount++;
//							thread.start();
//						}
//					}
//				}
//			} catch (Exception e) {
//				logger.error("spring方法任务线程扫描器异常", e);
//			}
//		}
//	}
	
	
	/**
	 * 本方法只能在spring启动时被执行唯一一次,即扫描器只有唯一一个
	 * 循环不断的扫描列队中是否有任务,有则执行
	 */
	public static void keepScanExcuteMethod() {
		if(!isScanInited) {
			isScanInited = true;
			try {
				while(true) {
					Thread.sleep(1);
					if(runningThreadCount < MAX_POOL_SIZE) {
						Runnable peekTask = queueTaskList.pollFirst();
						Thread thread = new Thread(peekTask);
						thread.start();
					}
				}
			} catch (Exception e) {
				logger.error("spring方法任务线程扫描器异常", e);
			}
		}
	}
	
	
}
