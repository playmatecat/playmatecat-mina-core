package application.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import application.server.NioTCPServer;

public class ApplicationInitServlet extends HttpServlet{

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
