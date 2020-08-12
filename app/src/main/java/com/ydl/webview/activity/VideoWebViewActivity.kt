package com.ydl.webview.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.ydl.webview.R
import com.ydl.webviewlibrary.VideoWebListener
import com.ydl.webviewlibrary.X5WebChromeClient
import com.ydl.webviewlibrary.X5WebViewClient
import kotlinx.android.synthetic.main.activity_first.*

class VideoWebViewActivity :AppCompatActivity(){

    private var x5WebChromeClient: X5WebChromeClient? = null
    private var x5WebViewClient: X5WebViewClient? = null
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) { //全屏播放退出全屏
            if (x5WebChromeClient != null && x5WebChromeClient!!.inCustomView()) {
                x5WebChromeClient!!.hideCustomView()
                return true
                //返回网页上一页
            } else if (web_view.canGoBack()) {
                web_view.goBack()
                return true
                //退出网页
            } else {
                handleFinish()
            }
        }
        return false
    }

    fun handleFinish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition()
        } else {
            finish()
        }
    }


    override fun onDestroy() {
        try {
            if (x5WebChromeClient != null) {
                x5WebChromeClient?.removeVideoView()
            }
            //有音频播放的web页面的销毁逻辑
            //在关闭了Activity时，如果Webview的音乐或视频，还在播放。就必须销毁Webview
            //但是注意：webview调用destory时,webview仍绑定在Activity上
            //这是由于自定义webview构建时传入了该Activity的context对象
            //因此需要先从父容器中移除webview,然后再销毁webview:
            if (web_view != null) {
                val parent = web_view.getParent() as ViewGroup
                parent?.removeView(web_view)
                web_view.removeAllViews()
                web_view.destroy()
            }
        } catch (e: Exception) {
            Log.e("X5WebViewActivity", e.message)
        }
        super.onDestroy()
    }


    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        val movieUrl = "https://haokan.baidu.com/v?vid=13224380988329734322&pd=bjh&fr=bjhauthor&type=video"
        web_view.loadUrl(movieUrl)
        x5WebChromeClient = web_view.getX5WebChromeClient()
        x5WebViewClient = web_view.getX5WebViewClient()
        x5WebChromeClient?.setVideoWebListener(object : VideoWebListener {
           override fun showVideoFullView() { //视频全频播放时监听
            }

            override fun hindVideoFullView() { //隐藏全频播放，也就是正常播放视频
            }

            override fun showWebView() { //显示webView
            }

            override fun hindWebView() { //隐藏webView
            }
        })
    }
}
