package igrp.service;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.StringTokenizer;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * @author Marcel Iekiny
 * Aug 23, 2017
 */
//@Provider
public class AuthenticationFilter implements ContainerRequestFilter{

	public void filter(ContainerRequestContext requestContext) throws IOException {
		//System.out.println("Filter executado ...");
		List<String> authHeader = requestContext.getHeaders().get("Authorization");
		if(authHeader!= null && authHeader.size() > 0){
			String authToken = authHeader.get(0);
			authToken = authToken.replaceFirst("Basic ", "");
			String decodeString = new String(Base64.getDecoder().decode(authToken));
			System.out.println(decodeString);
			System.out.println(decodeString);
			StringTokenizer token = new StringTokenizer(decodeString, ":");
			String username = token.nextToken();
			String password = token.nextToken();
			if("IMarcelF".equals(username) && "softwaredeveloper".equals(password)){
				return;
			}
		}
		Response response = Response.status(Response.Status.UNAUTHORIZED)
				.entity("<root>You are not authorized ...</root>")
				.build();
		requestContext.abortWith(response);
	}

}
