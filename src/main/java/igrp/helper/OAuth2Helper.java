package igrp.helper;

import igrp.oauth2.error.OAuth2Error;
import igrp.resource.oauth.PostData;
import igrp.resource.oauth.Error;
/**
 * @author Marcel Iekiny
 * Oct 4, 2017
 */
public final class OAuth2Helper { // Not inherit ...
	
	private OAuth2Helper() {} // Not instantiate ...
	
	/** A set of public static methods ... **/
	
	// Proccess all oauth2 GET request 
	public static Object doGet(String client_id, String response_type, String scope, String redirect_uri) {
		Object result = null;
		if(response_type == null || response_type.isEmpty()) {
			return result;
		}
		switch(response_type) {
			case "code":
			case "token": break;
			default: break;
		}
		return result;
	}
	
	// Proccess all oauth2 POST request
	public static Object doPost(PostData data) {
		Object result = null;
		if(data.getGrant_type() != null)
			switch(data.getGrant_type()) {
			
				case "authorization_code":break;
				
				case "password":break;
				
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
		Object result = null;
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
}
