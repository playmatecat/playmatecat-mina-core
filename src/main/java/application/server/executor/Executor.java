package application.server.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Executor {
	
	/** cpu数量 **/
    private final static int numberOfCores = Runtime.getRuntime().availableProcessors();

    /** 阻塞系数 **/
    private final static double blockingCoefficient = 0.9;
    
    /**
     * 线程池最大数量
     * 线程数=CPU可用核心数/（1 - 阻塞系数），其中阻塞系数在在0到1范围内。
     * 计算密集型程序的阻塞系数为0，IO密集型程序的阻塞系数接近1。
     */
    private final static int poolMaxSize = (int) (numberOfCores / (1 - blockingCoefficient));
	
    /**concurrent包的线程池**/
	private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(poolMaxSize, poolMaxSize,
		    60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.DiscardOldestPolicy());
	
	private Executor(){}
	
	public static void start(Thread thread) {
		threadPoolExecutor.execute(thread);
	}
}
