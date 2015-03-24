package eu.linkedtv.irapi.search.errorhandling;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

	Logger logger = LoggerFactory.getLogger(ValidationExceptionMapper.class);

	@Override
	public Response toResponse(final ValidationException ex) {
		logger.debug("ValidationException mapper: " + ex.getClass().getName() + ", message: " + ex.getMessage());

		ResponseBuilder r = Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN)
				.entity(ex.getMessage());
		return r.build();
	}
}