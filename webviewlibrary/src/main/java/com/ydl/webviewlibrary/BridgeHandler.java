
package com.ydl.webviewlibrary;

/**
 * 自定义接口，处理消息回调逻辑
 */
public interface BridgeHandler {

	/**
	 * 处理消息
	 */
	void handler(String data, CallBackFunction function);

}
