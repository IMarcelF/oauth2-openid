package igrp.helper;

import java.net.URISyntaxException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import javax.ws.rs.core.Response;

import igrp.oauth2.error.OAuth2Error;
import igrp.resource.oauth.PostData;
import igrp.resource.OAuthAccessToken;
import igrp.resource.OAuthClient;
import igrp.resource.OAuthRefreshToken;
import igrp.resource.User;
import igrp.resource.oauth.Error;
/**
 * @author Marcel Iekiny
 * Oct 4, 2017
 */
public final class OAuth2Helper { // Not inherit ...
	
	private OAuth2Helper() {} // Not instantiate ...
	
	/** A set of public static fields ... **/
	public static String idpUrl = "http://localhost:8080/IGRP/webapps?r=igrp/login/login"; // In this case use IGRP-Framework login page
	
	/** A set of public static methods ... **/
	// Proccess all oauth2 GET request 
	public static Object doGet(String client_id, String response_type, String scope, String redirect_uri) {
		String url = OAuth2Helper.idpUrl + "&oauth=1";
		if(response_type == null || response_type.isEmpty()) {
			return Response.status(400).build();
		}
		switch(response_type) {
			case "code": 
			case "token": 
				url += (response_type != null && !response_type.isEmpty() ? "&response_type=" + response_type : "");
				url += (client_id != null && !client_id.isEmpty() ? "&client_id=" + client_id : "");
			try {
				url += (redirect_uri != null && !redirect_uri.isEmpty() ? "&redirect_uri=" + URLEncoder.encode(redirect_uri, "utf-8") : "");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
				url += (scope != null && !scope.isEmpty() ? "&scope=" + scope : "");
			try {
				return Response.temporaryRedirect(new URI(url)).build();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			default:// Do nothing yet ...
		}
		return Response.status(400).build();
	}
	
	// Proccess all oauth2 POST request
	public static Object doPost(PostData data) {
		Object result = null;
		if(data.getGrant_type() != null)
			switch(data.getGrant_type()) {
			
				case "authorization_code":break;
				case "password": result = resourceOwnerPasswordGrant(data.getUsername(), data.getPassword(), data.getClient_id(), 
											data.getClient_secret(), data.getScope()); break;
				
				case "refresh_token":break;
				
				case "client_credentials": // Not set yet
				default: result = new Error(OAuth2Error.UNSUPPORTED_GRANT_TYPE.name(), OAuth2Error.UNSUPPORTED_GRANT_TYPE.getDescription()); break;
			}
		else
			result = new Error(OAuth2Error.UNSUPPORTED_GRANT_TYPE.name(), OAuth2Error.UNSUPPORTED_GRANT_TYPE.getDescription());
		return result;
	}
	
	// OAuth2 response_type=code
	public static void authorizationCodeGrant(String client_id, String scope, String redirect_uri) {
		
	}
	
	// OAuth2 grant_type=authorization_code
	public static Object swapCodeByToken(String code, String client_id, String client_secret, String redirect_uri) {
		Object result = null;
		return result;
	}
	
	// OAuth2 response_type=token & grant_type=implicit
	public static void implicitGrant(String client_id, String scope, String redirect_uri) {
		
	}
	
	// OAuth2 grant_type=password
	public static Object resourceOwnerPasswordGrant(String username, String password, String client_id, String client_secret, String scope) { 
		Object result = new Error(OAuth2Error.INTERNAL_ERROR.name(), OAuth2Error.INTERNAL_ERROR.getDescription()); // Default error
		
		User user = null;
		try {
			user = (User) DAOHelper.getInstance().getEntityManager().createQuery("select t from User t where t.user_name = :_u").setParameter("_u", username).getSingleResult();
		}catch(Exception e) {
			return new Error(OAuth2Error.INTERNAL_ERROR.name(), OAuth2Error.INTERNAL_ERROR.getDescription());
		}
		if(!user.getPass_hash().equals(password))
			return new Error(OAuth2Error.INTERNAL_ERROR.name(), OAuth2Error.INTERNAL_ERROR.getDescription());
				
		OAuthClient client = null;
		try {
			client = (OAuthClient) DAOHelper.getInstance().getEntityManager().createQuery("select t from OAuthClient t where t.client_id = :_c").setParameter("_c", client_id).getSingleResult();
		}catch(Exception e) {
			return new Error(OAuth2Error.INVALID_CLIENT.name(), OAuth2Error.INVALID_CLIENT.getDescription());
		}
		if(!client.getClient_secret().equals(client_secret))
			return new Error(OAuth2Error.INVALID_CLIENT_SECRET.name(), OAuth2Error.INVALID_CLIENT_SECRET.getDescription());
		
		OAuthAccessToken accessToken;
		OAuthRefreshToken refreshToken;
		
		return result;
	}
	
	// OAuth2 grant_type=client_credentials
	public static Object clientCredentialsGrant(String client_id, String client_secret) {
		Object result = null;
		return result;
	}
	
	// OAuth2 grant_type=refresh_token
	public static Object refreshToken(String refresh_token, String scope, String client_id, String client_secret) {
		Object result = null;
		return result;
	}
	
	/** Aux. algorithm **/
	private static String generateAccessToken() {
		return java.util.UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	private static String generateRefreshToken() {
		return java.util.UUID.randomUUID().toString().replaceAll("-", "");
	}
}
