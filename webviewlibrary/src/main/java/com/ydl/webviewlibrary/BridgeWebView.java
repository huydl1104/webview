
package com.ydl.webviewlibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.tencent.smtt.sdk.WebView;

import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("SetJavaScriptEnabled")
public class BridgeWebView extends WebView implements WebViewJavascriptBridge{

	public static final String TO_LOAD_JS = "WebViewJavascriptBridge.js";
	private long uniqueId = 0;
	Map<String, CallBackFunction> responseCallbacks = new HashMap<>();
	Map<String, BridgeHandler> messageHandlers = new HashMap<>();
	BridgeHandler defaultHandler = new DefaultHandler();
	private List<Message> startupMessage = new ArrayList<>();
	public List<Message> getStartupMessage() {
		return startupMessage;
	}

	public void setStartupMessage(List<Message> startupMessage) {
		this.startupMessage = startupMessage;
	}

	public BridgeWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BridgeWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public BridgeWebView(Context context) {
		super(context);
		init();
	}

	/**
	 * 默认处理程序，处理js发送的没有指定处理程序名称的消息，
	 * 如果js消息有处理程序名，它将由本机注册的命名处理程序处理
	 * @param handler							handler
	 */
	public void setDefaultHandler(BridgeHandler handler) {
       this.defaultHandler = handler;
	}

    private void init() {
		this.setVerticalScrollBarEnabled(false);
		this.setHorizontalScrollBarEnabled(false);
		this.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
		this.setWebViewClient(generateBridgeWebViewClient());
	}

    protected X5WebViewClient generateBridgeWebViewClient() {
        return new X5WebViewClient(this,getContext());
    }

    /**
     * 获取到CallBackFunction data执行调用并且从数据集移除
     * @param url						url链接
     */
	public void handlerReturnData(String url) {
		String functionName = BridgeUtil.getFunctionFromReturnUrl(url);
		if (functionName!=null){
			CallBackFunction f = responseCallbacks.get(functionName);
			String data = BridgeUtil.getDataFromReturnUrl(url);
			if (f != null) {
				f.onCallBack(data);
				responseCallbacks.remove(functionName);
			}
		}
	}

	@Override
	public void send(String data) {
		send(data, null);
	}

	@Override
	public void send(String data, CallBackFunction responseCallback) {
		doSend(null, data, responseCallback);
	}

    /**
     * 保存message到消息队列
     */
	private void doSend(String handlerName, String data, CallBackFunction responseCallback) {
		Message m = new Message();
		if (!TextUtils.isEmpty(data)) {
			m.setData(data);
		}
		if (responseCallback != null) {
			String callbackStr = String.format(BridgeUtil.CALLBACK_ID_FORMAT,
					++uniqueId + (BridgeUtil.UNDERLINE_STR +
							SystemClock.currentThreadTimeMillis()));
			responseCallbacks.put(callbackStr, responseCallback);
			m.setCallbackId(callbackStr);
		}
		if (!TextUtils.isEmpty(handlerName)) {
			m.setHandlerName(handlerName);
		}
		queueMessage(m);
	}

    /**
     * list<message> != null 添加到消息集合否则分发消息
     */
	private void queueMessage(Message m) {
		if (startupMessage != null) {
			startupMessage.add(m);
		} else {
			dispatchMessage(m);
		}
	}

    /**
     * 分发message 必须在主线程才分发成功
     * @param m Message
     */
	public void dispatchMessage(Message m) {
        String messageJson = m.toJson();
        //增加非空判断的逻辑
        if (messageJson!=null){
			//escape special characters for json string  为json字符串转义特殊字符
			messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
			messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");
			messageJson = messageJson.replaceAll("(?<=[^\\\\])(\')", "\\\\\'");
			messageJson = messageJson.replaceAll("%7B", URLEncoder.encode("%7B"));
			messageJson = messageJson.replaceAll("%7D", URLEncoder.encode("%7D"));
			messageJson = messageJson.replaceAll("%22", URLEncoder.encode("%22"));
			String javascriptCommand = String.format(
					BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA, messageJson);
			// 必须要找主线程才会将数据传递出去 --- 划重点
			if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
				this.loadUrl(javascriptCommand);
			}
		}
    }

    /**
     * 刷新消息队列
     */
	public void flushMessageQueue() {
		if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
			loadUrl(BridgeUtil.JS_FETCH_QUEUE_FROM_JAVA, new CallBackFunction() {
				@Override
				public void onCallBack(String data) {
					// deserializeMessage 反序列化消息
					List<Message> list = null;
					try {
						list = Message.toArrayList(data);
					} catch (Exception e) {
                        e.printStackTrace();
						return;
					}
					if (list == null || list.size() == 0) {
						return;
					}
					for (int i = 0; i < list.size(); i++) {
						Message m = list.get(i);
						String responseId = m.getResponseId();
						// 是否是response  CallBackFunction
						if (!TextUtils.isEmpty(responseId)) {
							CallBackFunction function = responseCallbacks.get(responseId);
							String responseData = m.getResponseData();
							function.onCallBack(responseData);
							responseCallbacks.remove(responseId);
						} else {
							CallBackFunction responseFunction = null;
							// if had callbackId 如果有回调Id
							final String callbackId = m.getCallbackId();
							if (!TextUtils.isEmpty(callbackId)) {
								responseFunction = new CallBackFunction() {
									@Override
									public void onCallBack(String data) {
										Message responseMsg = new Message();
										responseMsg.setResponseId(callbackId);
										responseMsg.setResponseData(data);
										queueMessage(responseMsg);
									}
								};
							} else {
								responseFunction = new CallBackFunction() {
									@Override
									public void onCallBack(String data) {
										// do nothing
									}
								};
							}
							// BridgeHandler执行
							BridgeHandler handler;
							if (!TextUtils.isEmpty(m.getHandlerName())) {
								handler = messageHandlers.get(m.getHandlerName());
							} else {
								handler = defaultHandler;
							}
							if (handler != null){
								handler.handler(m.getData(), responseFunction);
							}
						}
					}
				}
			});
		}
	}


	public void loadUrl(String jsUrl, CallBackFunction returnCallback) {
		this.loadUrl(jsUrl);
        // 添加至 Map<String, CallBackFunction>
		responseCallbacks.put(BridgeUtil.parseFunctionName(jsUrl), returnCallback);
	}

	/**
	 * register handler,so that javascript can call it
	 * 注册处理程序,以便javascript调用它
	 * @param handlerName 					handlerName
	 * @param handler 						BridgeHandler
	 */
	public void registerHandler(String handlerName, BridgeHandler handler) {
		if (handler != null) {
            // 添加至 Map<String, BridgeHandler>
			messageHandlers.put(handlerName, handler);
		}
	}

	/**
	 * 解绑注册js操作，一般可以在onDestory中处理
	 * @param handlerName					方法name
	 */
	public void unregisterHandler(String handlerName) {
		if (handlerName != null) {
			messageHandlers.remove(handlerName);
		}
	}

	/**
	 * call javascript registered handler
	 * 调用javascript处理程序注册
     * @param handlerName handlerName
	 * @param data data
	 * @param callBack CallBackFunction
	 */
	public void callHandler(String handlerName, String data, CallBackFunction callBack) {
        doSend(handlerName, data, callBack);
	}

}
