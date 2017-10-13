package igrp.service;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import igrp.helper.OAuth2Helper;
import igrp.oauth2.error.OAuth2Error;
import igrp.resource.oauth.PostData;
import igrp.resource.oauth.Error;

@Path("/oauth2")
public class OAuth2 {
	
	/**
	 This method handle all oauth2 get request
	 For response-type: "code" | "token"
	 * */
	@GET
	@Path("/authorization")
	public Response authorizationCodeOrImplicit(
				@QueryParam("response_type") String response_type,
				@QueryParam("client_id") String client_id,
				@QueryParam("scope")@DefaultValue("") String scope,
				@QueryParam("redirect_uri")@DefaultValue("") String redirect_uri,
				@QueryParam("authorize")@DefaultValue("") String authorize
			){
		return (Response) OAuth2Helper.doGet(client_id, response_type, scope, redirect_uri, authorize);
	}
	
	/**
	 The following methods handle all oauth2 post request ...
	 For grant-type: "authorization-code" | "password" | "refresh_token" | "client_credentials"
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
}
