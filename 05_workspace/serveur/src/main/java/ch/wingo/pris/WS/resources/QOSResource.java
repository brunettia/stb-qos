package ch.wingo.pris.WS.resources;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.wingo.pris.WebSocketsCentralisation;


@Path("/stb")
//@Path("/test")
@Consumes(MediaType.APPLICATION_JSON)
public class QOSResource {
	
	public QOSResource(){}
	
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/hello")
	@GET
	public String helloWorld(){
		return "Hello World";
	}
	
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/ping/{macAddr}")
	@GET
	public Response getPing(@PathParam("macAddr") String macAddr, @QueryParam("cmd") String cmd){
		System.out.println("WebService : Ping");
		String json = WebSocketsCentralisation.getInstance().ping(macAddr, cmd);
		return Response.ok(json).header("Access-Control-Allow-Origin", "*").build();
	}
	
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/iperf/{macAddr}")
	@GET
	public Response getIperf(@PathParam("macAddr") String macAddr, @QueryParam("cmd") String cmd){
		System.out.println("WebService : Iperf");
		String json = WebSocketsCentralisation.getInstance().iperf(macAddr, cmd);
		return Response.ok(json).header("Access-Control-Allow-Origin", "*").build();
	}
	
	@Path("/reboot/{macAddr}")
	@GET
	public Response reboot(@PathParam("macAddr") String macAddr){
		System.out.println("WebService : Reboot");
		WebSocketsCentralisation.getInstance().reboot(macAddr);
		return Response.ok("reboot done").header("Access-Control-Allow-Origin", "*").build();
	}
	
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/isMute/{macAddr}")
	@GET
	public Response isMute(@PathParam("macAddr") String macAddr){
		System.out.println("WebService : IsMute");
		String json = WebSocketsCentralisation.getInstance().isMute(macAddr);
		return Response.ok(json).header("Access-Control-Allow-Origin", "*").build();
	}
	
}
