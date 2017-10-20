package cn.transpad.transpadui.http;


import java.io.Serializable;

/**
 * @author 刘昆  (liukun@100tv.com)
 * @since  2014-04-22
 */

public final class Error implements Serializable {

	private final int status;
	private final String reason;
	private final boolean networkError;
	
	public Error(boolean networkError, int status, String reason) {
		this.networkError = networkError;
		this.status = status;
		this.reason = reason;
	}
	
	/** 此方法用以判断错误是否是由于网络无法连接导致 */
	public boolean isNetworkError() {
		return networkError;
	}
	
	/** 如果错误不是由于网络连接导致，可通过此方法获得服务器返回的HTTP响应状态码 */
	public int getStatus() {
		return status;
	}
	
	/** 如果错误不是由于网络连接导致，可通过此方法获得服务器返回的HTTP原因短语 */
	public String getReason() {
		return reason;
	}
	
	@Override
	public String toString() {
		if (isNetworkError()) {
			return "Network Error";
		} else {
			return "ErrorStatus: " + status + " Reason: " + reason;
		}
		
	}
	

}
