
package com.ydl.webviewlibrary;


/**
 * 默认的BridgeHandler
 */
public class DefaultHandler implements BridgeHandler{

	@Override
	public void handler(String data, CallBackFunction function) {
		if(function != null){
			function.onCallBack("DefaultHandler response data");
		}
	}

}
