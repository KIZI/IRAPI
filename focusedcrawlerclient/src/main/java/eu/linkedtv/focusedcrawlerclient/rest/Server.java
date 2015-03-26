package eu.linkedtv.focusedcrawlerclient.rest;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.zeromq.ZMQ;

import com.sun.jersey.spi.resource.Singleton;

@Path("/api/v0/")
@Singleton
public class Server {
	
	private final String server = "tcp://*:6666";

	@GET
	@Path("/status")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getStatus() throws JSONException {
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket socket = context.socket(ZMQ.REQ);
		socket.connect(server);
		JSONObject request = new JSONObject();
		request.put("operation", "status");
		socket.send(request.toString().getBytes(), 0);
		byte[] reply = socket.recv(0);
		socket.close();
		context.term();
		return Response.status(200).entity(new String(reply)).build();
	}

	@GET
	@Path("/tasks")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response addTask(
			@DefaultValue("") @QueryParam(value = "domain_source") String domainSource,
			@DefaultValue("") @QueryParam(value = "query") String query)
			throws JSONException {
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket socket = context.socket(ZMQ.REQ);
		socket.connect(server);
		JSONObject request = new JSONObject();
		request.put("operation", "add");
		request.put("domain_source", domainSource);
		request.put("query", query);
		socket.send(request.toString().getBytes(), 0);
		byte[] reply = socket.recv(0);
		socket.close();
		context.term();
		return Response.status(200).entity(new String(reply)).build();
	}
	
	@GET
	@Path("/candidates")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getCandidates(
			@DefaultValue("") @QueryParam(value = "domain_source") String domainSource,
			@DefaultValue("") @QueryParam(value = "query") String query)
			throws JSONException {
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket socket = context.socket(ZMQ.REQ);
		socket.connect(server);
		JSONObject request = new JSONObject();
		request.put("operation", "candidates");
		request.put("domain_source", domainSource);
		request.put("query", query);
		socket.send(request.toString().getBytes(), 0);
		byte[] reply = socket.recv(0);
		socket.close();
		context.term();
		return Response.status(200).entity(new String(reply)).build();
	}
	
	@GET
	@Path("/metadata")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getMetadata(
			@DefaultValue("") @QueryParam(value = "url") String url)
			throws JSONException {
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket socket = context.socket(ZMQ.REQ);
		socket.connect(server);
		JSONObject request = new JSONObject();
		request.put("operation", "metadata");
		request.put("url", url);
		socket.send(request.toString().getBytes(), 0);
		byte[] reply = socket.recv(0);
		socket.close();
		context.term();
		return Response.status(200).entity(new String(reply)).build();
	}

}
