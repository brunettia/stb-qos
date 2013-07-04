package ch.wingo.pris.WS.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.wingo.pris.WebSocketsCentralisation;


@Path("/stb")
@Consumes(MediaType.TEXT_PLAIN)
public class QOSResource {
	
	public QOSResource(){}
	
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/ping/{macAddr}")
	@GET
	public String getPing(@PathParam("macAddr") String macAddr){
		return WebSocketsCentralisation.getInstance().ping(macAddr);
	}
	
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/iperf/{macAddr}")
	@GET
	public String getIperf(@PathParam("macAddr") String macAddr){
		return WebSocketsCentralisation.getInstance().iperf(macAddr);
	}
	
	@Path("/reboot/{macAddr}")
	@GET
	public void reboot(@PathParam("macAddr") String macAddr){
		WebSocketsCentralisation.getInstance().reboot(macAddr);
	}
	
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/isMute/{macAddr}")
	@GET
	public String isMute(@PathParam("macAddr") String macAddr){
		return WebSocketsCentralisation.getInstance().isMute(macAddr);
	}
	
}
