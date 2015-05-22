package application.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import application.server.NioTCPServer;

/**
 * 自定义spring上下文加载器
 * @author blackcat
 *
 */
public class ApplicationInitServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void destroy() {
        NioTCPServer.destory();
    }

    @Override
    public void init() throws ServletException {
        try {
            NioTCPServer.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
