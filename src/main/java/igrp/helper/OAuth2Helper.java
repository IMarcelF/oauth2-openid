package igrp.helper;
/**
 * @author Marcel Iekiny
 * Oct 4, 2017
 */
public final class OAuth2Helper { // Not inherit ...
	
	private OAuth2Helper() {} // Not instantiate ...
	
	/*
	 * * A set of public static methods ... **/
	
	// OAuth2 response_type=code
	public static void authorizationCodeGrant(String client_id, String scope, String redirect_uri) {
		
	}
	
	// OAuth2 grant_type=authorization_code
	public static void swapCodeByToken(String code, String client_id, String client_secret, String redirect_uri) {
		
	}
	
	// OAuth2 response_type=token & grant_type=implicit
	public static void implicitGrant(String client_id, String scope, String redirect_uri) {
		
	}
	
	// OAuth2 grant_type=password
	public static void resourceOwnerPasswordGrant(String username, String password, String client_id, String client_secret, String scope) {
		
	}
	
	// OAuth2 grant_type=client_credentials
	public static void clientCredentilasGrant(String client_id, String client_secret) {
		
	}
}
