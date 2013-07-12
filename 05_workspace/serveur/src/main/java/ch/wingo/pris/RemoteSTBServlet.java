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
    	// getting the idleTimeout parameter from web.xml
    	int idleTimeout = Integer.parseInt(getServletContext().getInitParameter("idleTimeout"));
    	System.out.println(new Date()+" Servlet: configured ! Timeout set to "+idleTimeout+" ms");
    	// setting the timeout
    	factory.getPolicy().setIdleTimeout(idleTimeout);
    	// creating a new Socket
        factory.register(RemoteSTBSocket.class);
    }
}
