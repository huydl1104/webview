package com.ydl.webview.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.sdk.WebView
import com.ydl.webview.R
import com.ydl.webviewlibrary.BridgeWebView
import com.ydl.webviewlibrary.X5WebChromeClient
import com.ydl.webviewlibrary.X5WebViewClient
import kotlinx.android.synthetic.main.activity_first.*


class CopyActivity : AppCompatActivity() {
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (web_view.canGoBack()) {
                web_view.goBack()
                return true
                //退出网页
            } else {
                handleFinish()
            }
        }
        return false
    }

    private fun handleFinish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        try {
            if (web_view != null) {
                web_view.stopLoading()
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
//        val url = "https://juejin.im/post/5d401cabf265da03a53a12fe"
        val url = "https://github.com/"
        web_view.loadUrl(url)
        web_view.setOnLongClickListener { handleLongImage() }
        val webViewClient = MyX5WebViewClient(web_view, this)
        web_view.setWebViewClient(webViewClient)
        val webChromeClient = MyX5WebChromeClient(this)
        web_view.setWebChromeClient(webChromeClient)
    }

    private inner class MyX5WebViewClient(
        webView: BridgeWebView?,
        context: Context?
    ) :
        X5WebViewClient(webView, context) {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            url: String?
        ): Boolean {
            Log.d("网络拦截--------1------", url)
            return super.shouldOverrideUrlLoading(view, url)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest
        ): Boolean {
            Log.d("网络拦截--------2------", request.url.toString())
            return super.shouldOverrideUrlLoading(view, request)
        }
    }

    private inner class MyX5WebChromeClient
    /**
     * 构造方法
     *
     * @param activity 上下文
     */(activity: Activity?) : X5WebChromeClient(activity)

    /**
     * 长按图片事件处理
     */
    private fun handleLongImage(): Boolean {
        val hitTestResult: WebView.HitTestResult = web_view.getHitTestResult()
        // 如果是图片类型或者是带有图片链接的类型
        if (hitTestResult.type == WebView.HitTestResult.IMAGE_TYPE ||
            hitTestResult.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) { // 弹出保存图片的对话框
            AlertDialog.Builder(this@CopyActivity)
                .setItems(arrayOf("查看大图", "保存图片到相册"),
                    DialogInterface.OnClickListener { dialog, which ->
                        val picUrl = hitTestResult.extra
                        //获取图片
                        Log.e("picUrl", picUrl)
                    })
                .show()
            return true
        }
        return false
    }
}