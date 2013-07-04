package ch.wingo.pris;

import java.util.Date;

import javax.servlet.annotation.WebServlet;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

@SuppressWarnings("serial")
@WebServlet(name="PRIS WebSockets", urlPatterns="/register")
public class RemoteSTBServlet extends WebSocketServlet {
 
    @Override
    public void configure(WebSocketServletFactory factory) {
    	int idleTimeout = Integer.parseInt(getServletContext().getInitParameter("idleTimeout"));
    	System.out.println(new Date()+" Servlet: configured ! Timeout set to "+idleTimeout+" ms");
    	// timeout 10 minutes
    	factory.getPolicy().setIdleTimeout(idleTimeout);
        factory.register(RemoteSTBSocket.class);
    }
}
