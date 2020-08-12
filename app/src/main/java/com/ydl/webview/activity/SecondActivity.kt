package com.ydl.webview.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.ydl.webview.R
import com.ydl.webviewlibrary.InterWebListener
import kotlinx.android.synthetic.main.activity_web_view.*

class SecondActivity :AppCompatActivity(){

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (webview.canGoBack() && event.keyCode ==
            KeyEvent.KEYCODE_BACK && event.repeatCount == 0
        ) {
            webview.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        if (webview != null) {
            webview.clearHistory()
            val parent = webview.getParent() as ViewGroup
            parent?.removeView(webview)
            webview.destroy()
            //mWebView = null;
        }
        super.onDestroy()
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onResume() {
        super.onResume()
        if (webview != null) {
            webview.getSettings().setJavaScriptEnabled(true)
        }
    }

    override fun onStop() {
        super.onStop()
        if (webview != null) {
            webview.getSettings().setJavaScriptEnabled(false)
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        initData()
        initView()
    }


    fun initData() {
        val intent = intent
        if (intent != null) {
           val url = intent.getStringExtra("url")
        }
    }

    fun initView() {
        webview.loadUrl("https://x5.tencent.com/docs/webview.html")
        webview.getX5WebChromeClient().setWebListener(interWebListener)
        webview.getX5WebViewClient().setWebListener(interWebListener)
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

}
