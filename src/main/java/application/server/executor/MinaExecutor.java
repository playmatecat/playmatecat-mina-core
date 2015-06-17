package application.server.executor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程执行器
 * @author blackcat
 *
 */
public class MinaExecutor {

    // /** cpu数量 **/
    // private final static int numberOfCores =
    // Runtime.getRuntime().availableProcessors();
    //
    // /** 阻塞系数 **/
    // private final static double blockingCoefficient = 0.9;

    /**
     * 线程池最大数量 线程数=CPU可用核心数/（1 - 阻塞系数），其中阻塞系数在在0到1范围内。
     * 计算密集型程序的阻塞系数为0，IO密集型程序的阻塞系数接近1。
     */
    // private final static int poolMaxSize = (int) (numberOfCores / (1 -
    // blockingCoefficient));
    /**
     * 对 linuxthreads 这个值一般是 1024，对于 nptl 则没有硬性的限制，仅仅受限于系统的资源 这个系统的资源主要就是线程的
     * stack 所占用的内存，用 ulimit -s 可以查看默认的线程栈大小，一般情况下，这个值是 8M 这个值和理论完全相符，因为 32 位
     * linux 下的进程用户空间是 3G 的大小，也就是 3072M，用 3072M 除以 8M 得 384 为了突破内存的限制，可以有两种方法 用
     * ulimit -s 1024 减小默认的栈大小
     * 
     * by blackcat:由于上层tomcat访问线程数量可知,所以对下层而言线程数量可控,因此设置到内存能承载的大小就OK了
     * 所以设定大小大致为JVM内存大小/12M(8M分割,多余4M部分给其他程序运行使用)
     */
    private static final int POOL_MAX_SIZE = (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024 / 12);

    /** concurrent包的线程池 **/
    private static ThreadPoolExecutor threadPoolExecutor 
        = new ThreadPoolExecutor(POOL_MAX_SIZE, POOL_MAX_SIZE, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.DiscardOldestPolicy());

    private MinaExecutor() {
    }

    public static void start(Thread thread) {
        threadPoolExecutor.execute(thread);
    }
    
    public static void destory() {
        threadPoolExecutor.shutdown();
        try {
            Thread.sleep(1000);
            threadPoolExecutor.getActiveCount();
            if(threadPoolExecutor.getActiveCount() != 0) {
                Thread.sleep(5000);
            } else {
                return;
            }
        } catch (Exception e) {
            //do nothing
        }
        
        //如果最终依然有线程未终止,则强制终止所有线程(包括之前已经发起还在执行中的任务线程)
        threadPoolExecutor.shutdownNow();
    }
}
