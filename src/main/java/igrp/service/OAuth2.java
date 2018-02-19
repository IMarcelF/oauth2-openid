package igrp.service;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.swing.JOptionPane;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import igrp.helper.OAuth2Helper;
import igrp.oauth2.util.OAuth2Error;
import igrp.resource.oauth2.Error;
import igrp.resource.oauth2.PostData;

@Path("/oauth2")
public class OAuth2 {
	
	/**
	 This method handle all oauth2 get request
	 For response_type: "code" | "token"
	 * */
	@GET
	@Path("/authorization")
	public Response authorizationCodeOrImplicit(
				@QueryParam("response_type") String response_type,
				@QueryParam("client_id") String client_id,
				@QueryParam("scope")@DefaultValue("") String scope,
				@QueryParam("redirect_uri")@DefaultValue("") String redirect_uri,
				@QueryParam("authorize")@DefaultValue("") String authorize,
				@QueryParam("userId")@DefaultValue("") String userId,
				@QueryParam("state")@DefaultValue("") String state,
				@QueryParam("nonce")@DefaultValue("") String nonce,
				@Context HttpServletRequest request
			){
		String url = request.getRequestURL().toString().replace(request.getRequestURI() + "", "");
		try {
			Cookie []cookies = request.getCookies();
			for(Cookie cookie : cookies)
				System.out.println(cookie.getName() + " - " + cookie.getValue());
		}catch(Exception e) {}
		
		return (Response) OAuth2Helper.doGet(client_id, response_type, scope, redirect_uri, authorize, url, userId);
	}
	
	/**
	 The following methods handle all oauth2 post request ...
	 For grant_type: "authorization_code" | "password" | "refresh_token" | "client_credentials"
	 For two consumes media-type: "application/x-www-form-urlencoded" & "application/json"
	 * */	
	@POST
	@Path("/token")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON) // Produce always json response
	public Response generateToken(
			@FormParam("grant_type") String grant_type, // Required !!!
			@FormParam("code") @DefaultValue("") String code,
			@FormParam("redirect_uri")@DefaultValue("") String redirect_uri, 
			@FormParam("scope")@DefaultValue("") String scope, 
			@FormParam("username")@DefaultValue("") String username, 
			@FormParam("password")@DefaultValue("") String password,
			@FormParam("refresh_token")@DefaultValue("") String refresh_token, 
			@FormParam("client_id")@DefaultValue("") String client_id, 
			@FormParam("client_secret")@DefaultValue("") String client_secret
			) {
		Object result = OAuth2Helper.doPost(new PostData(grant_type, code, redirect_uri, scope, username, password, refresh_token, client_id, client_secret));
		if(result instanceof Error) {
			Error error = (Error) result;
			int status = OAuth2Error.valueOf(error.getError()).getStatus();
		return Response.status(status).entity(error).build();
		}
		return Response.status(200).entity(result).build();
	}
	
	@POST
	@Path("/token")
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON) // Produce always json response 
	public Response generateToken(PostData data) {
		Object result = OAuth2Helper.doPost(data);
		if(result instanceof Error) {
			Error error = (Error) result;
			int status = OAuth2Error.valueOf(error.getError()).getStatus();
		return Response.status(status).entity(error).build();
		}
		return Response.status(200).entity(result).build();
	}
	
	/**
	 For OAuth2-OpenId user endpoint purpose ... 
	 * */
	@GET
	@Path("/userinfo")
	public Response userInfo(@HeaderParam(value = "Authorization") String token){
		
		return null;
	}
	
}
