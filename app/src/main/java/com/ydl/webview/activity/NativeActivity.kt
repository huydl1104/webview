package com.ydl.webview.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.ydl.webview.R
import com.ydl.webviewlibrary.BridgeHandler
import com.ydl.webviewlibrary.CallBackFunction
import com.ydl.webviewlibrary.DefaultHandler
import com.ydl.webviewlibrary.InterWebListener
import kotlinx.android.synthetic.main.activity_native_view.*


class NativeActivity : AppCompatActivity() {
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (web_view.canGoBack() && event.keyCode ==
            KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            web_view.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        if (web_view != null) {
            web_view.clearHistory()
            val parent = web_view.getParent() as ViewGroup
            parent?.removeView(web_view)
            web_view.destroy()
            //mWebView = null;
        }
        super.onDestroy()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onResume() {
        super.onResume()
        if (web_view != null) {
            web_view.getSettings().setJavaScriptEnabled(true)
        }
    }

    override fun onStop() {
        super.onStop()
        if (web_view != null) {
            web_view.getSettings().setJavaScriptEnabled(false)
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_view)
        initView()
    }

    private fun initView() {
        web_view.loadUrl("file:///android_asset/demo.html")
        web_view.getX5WebChromeClient().setWebListener(interWebListener)
        web_view.getX5WebViewClient().setWebListener(interWebListener)
        btn!!.setOnClickListener {
             web_view.callHandler("functionInJs", "data from Java") { data ->
                Log.i("java调用web----", "reponse data from js $data")
             }
            /*具体看demo.html的这段代码
                        bridge.registerHandler("functionInJs", function(data, responseCallback) {
                            document.getElementById("show").innerHTML = ("data from Java: = " + data);
                            if (responseCallback) {
                                var responseData = "Javascript Says Right back aka!";
                                responseCallback(responseData);
                            }
                        });*/
        }
        //js交互方法
        initWebViewBridge()
    }

    private val interWebListener: InterWebListener = object : InterWebListener {
        override fun showTitle(title: String?) {

        }

        override fun hindProgressBar() {
            pb!!.visibility = View.GONE
        }

        override fun showErrorView() {}
        override fun startProgress(newProgress: Int) {
            pb!!.progress = newProgress
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //这个是处理回调逻辑
        web_view.getX5WebChromeClient().uploadMessageForAndroid5(data, resultCode)
    }

    @JavascriptInterface
    fun initWebViewBridge() {
        web_view.setDefaultHandler(DefaultHandler())
        //js调用
        web_view.registerHandler("toPhone", object : BridgeHandler {
            override fun handler(data: String?, function: CallBackFunction?) {}
        })
        web_view.registerHandler("submitFromWeb", object : BridgeHandler {
            override fun handler(data: String, function: CallBackFunction) {
                Toast.makeText(this@NativeActivity, data, Toast.LENGTH_LONG).show()
                Log.i("registerHandler", "handler = submitFromWeb, data from web = $data")
                function.onCallBack("submitFromWeb exe, response data  from Java")
            }
        })
        web_view.callHandler("functionInJs", "yudl") { data ->
            Toast.makeText(this@NativeActivity, data, Toast.LENGTH_LONG).show()
        }
    }
}