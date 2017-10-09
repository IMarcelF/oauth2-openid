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
import org.apache.log4j.Logger;

import igrp.resource.oauth.PostData;
import igrp.resource.oauth.Token;

@Path("/oauth2")
public class OAuth2 {
	
	/**
	 This method handle all oauth2 get request
	 For response-type: "code" | "token"
	 * */
	@GET
	@Path("/authorization")
   // @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public void authorizationCodeOrImplicit(@QueryParam("request") String request){
		
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
	public Token generateToken(
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
		
		//insert your code here
		
		return new Token();
	}
	
	@POST
	@Path("/token")
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON) // Produce always json response
	public Token generateToken(PostData data) {
		//insert your code here
		return new Token();
	}
	
}
