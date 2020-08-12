
package com.ydl.webviewlibrary;


/**
 * js接口
 */
public interface WebViewJavascriptBridge {
	
	void send(String data);
	void send(String data, CallBackFunction responseCallback);

}
