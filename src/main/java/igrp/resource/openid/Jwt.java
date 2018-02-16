package igrp.resource.openid;
/**
 * Iekiny Marcel
 * Feb 9, 2018
 */
public class Jwt {
	
	private String sub;
	private String iss;
	private String aud;
	private long iat;
	private long exp;
	private String nonce;
	
	/*private Integer id;
	private 
	
	`client_id` varchar(80) NOT NULL,
	  `subject` varchar(80) DEFAULT NULL,
	  `public_key` varchar(2000) NOT NULL,
	  `id` int(11) NOT NULL
	*/
	
	public String getSub() {
		return sub;
	}
	
	public void setSub(String sub) {
		this.sub = sub;
	}
	
	public String getIss() {
		return iss;
	}
	
	public void setIss(String iss) {
		this.iss = iss;
	}
	
	public String getAud() {
		return aud;
	}
	
	public void setAud(String aud) {
		this.aud = aud;
	}
	
	public long getIat() {
		return iat;
	}
	
	public void setIat(long iat) {
		this.iat = iat;
	}
	
	public long getExp() {
		return exp;
	}
	public void setExp(long exp) {
		this.exp = exp;
	}
	
	public String getNonce() {
		return nonce;
	}
	
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	
}
