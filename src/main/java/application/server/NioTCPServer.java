package application.server;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

/**
 * mina NIO服务端
 * 
 * @author blackcat
 *
 */
public class NioTCPServer {

    private static final Logger LOGGER = Logger.getLogger(NioTCPServer.class);

    /** 服务端口 **/
    private static final int PORT = 8501;
    /** 服务IP **/
    private static final String ADDRESS = "127.0.0.1";
    /** 超时 **/
    private static final int TIME_OUT = 60;
    /** 缓冲大小 **/
    private static final int BUFF_SZIE = 1024;
    /** sockect停滞时间 **/
    private static final int IDLE_TIME = 10;

    /** minaIO接收 **/
    private static NioSocketAcceptor acceptor;

    /**
     * 程序入口
     * 
     * @param args
     * @throws Exception
     */
    public static void init() throws Exception {
        LOGGER.info("starting nio server");

        // @STEP1 创建一个nio的接收器，并且绑定端口
        /*
         * 通过这个接收器，我们可以直接定义一个处理类，并进行端口绑定(必须配置完其他设置才能绑定)
         */
        acceptor = new NioSocketAcceptor();

        // @STEP2 创建一个过滤器链配置.注意链式顺序
        /*
         * 这个过滤器会记录所有的信息，例如连接SESSION创建，收到消息等。
         * 另一个过滤器ProtocolCodecFilter数据解析器，可以将二进制或者protocol类型的特殊数据转换成message
         * object对象 这里使用了TextLine factory因为这里会处理文本消息
         */
        // acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        // //数据解析器
        // acceptor.getFilterChain().addLast("codec",
        // new ProtocolCodecFilter(new
        // TextLineCodecFactory(Charset.forName("UTF-8"))) );

        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));

        // @STEP3 指定请求处理器
        acceptor.setHandler(new ServerHandler());

        // @STEP4 添加接收器配置
        // 读取缓冲区
        acceptor.getSessionConfig().setReadBufferSize(BUFF_SZIE);
        // 必须设定最大值，保证一定数据量后会立刻返回
        // acceptor.getSessionConfig().setMaxReadBufferSize(128);
        // io空停滞时间
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, IDLE_TIME);
        // 超时时间
        acceptor.getSessionConfig().setWriteTimeout(TIME_OUT);

        try {
            // 绑定ip&端口
            acceptor.bind(new InetSocketAddress(ADDRESS, PORT));
        } catch (Exception e) {
            LOGGER.error("端口绑定异常", e);
            acceptor.unbind();
        }

        LOGGER.info("[Nio Server]finshed boot server!!");

    }

    public static void destory() {
        Map<Long, IoSession> sessionMap = acceptor.getManagedSessions();
        Iterator<Long> keys = sessionMap.keySet().iterator();
        while (keys.hasNext()) {
            IoSession peekSeesion = sessionMap.get(keys.next());
            peekSeesion.close(true);
        }
        acceptor.dispose(true);
        acceptor.unbind();
    }

}
