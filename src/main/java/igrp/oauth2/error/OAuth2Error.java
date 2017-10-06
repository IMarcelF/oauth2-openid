package igrp.oauth2.error;
/**
 * @author Marcel Iekiny
 * Oct 6, 2017
 */
public enum OAuth2Error {
	
	UNSUPPORTED_OVER_HTTP(400, "OAuth 2.0 only supports calls over HTTPS."),
	VERSION_REJECTED(400, "An unsupported version of OAuth was supplied."),
	PARAMETER_ABSENT(400, "A required parameter is missing from the request."),
	PARAMETER_REJECTED(400, "A provided parameter is too long."),
	INVALID_CLIENT(400, "An invalid Client ID was provided."),
	INVALID_REQUEST(400, "An invalid request parameter was provided."),
	UNSUPPORTED_RESPONSE_TYPE(400, "The provided response_type is supported for this request. You may have provided a response type that doesn’t match the request."),
	UNSUPPORTED_GRANT_TYPE(400, "The provided grant_type is not supported. You may have provided a grant type that doesn’t match the request."),
	INVALID_REDIRECT_URI(400, "The provided redirect_URI does not match the one provided with the original authorization request."),
	UNSUPPORTED_REDIRECT_URI(400, "The provided redirect_URI is not supported for this request type."),
	INVALID_PARAM(400, "A provided request parameter is invalid."),
	INVALID_REFRESH_TOKEN(400, "The provided refresh token is invalid."),
	
	TOKEN_EXPIRED(401, "The provided refresh token has expired."),
	INVALID_CALLBACK(401,"The redirect_uri provided with this request uses an unsupported port or does not match the Client ID (Consumer Key)."),
	UNDERAGE_USER(401, "The user who must authorize access is a minor and cannot authorize access."),
	INVALID_CLIENT_SECRET(401, "An invalid Client Secret was provided."),
	INVALID_GRANT(401, "An invalid or expired token was provided."),
	
	ACCOUNT_NOT_AUTHORIZED(403, "The user has not authorized requesting Client ID (Consumer Key)."),
	
	INTERNAL_ERROR;
	
	OAuth2Error(){}
	
	OAuth2Error(int status, String description){
		this.status = status;
		this.description = description;
	}
	
	private String description;
	private int status;
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
}
