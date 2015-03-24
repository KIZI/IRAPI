package eu.linkedtv.irapi.search.errorhandling;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class IrapiExceptionMapper implements ExceptionMapper<IrapiException> {

	Logger logger = LoggerFactory.getLogger(IrapiExceptionMapper.class);

	@Override
	public Response toResponse(final IrapiException ex) {
		String msg = "IrapiException mapper: exception:" + ex.getCause().getClass().getSimpleName() + ", status: "
				+ ex.getStatus() + ", message: " + ex.getMessage();
		logger.error(msg, ex.getCause());
		ResponseBuilder r = Response.status(ex.getStatus()).type(MediaType.TEXT_PLAIN).entity(msg);
		return r.build();
	}
}