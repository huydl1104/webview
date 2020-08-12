package com.ydl.webview.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.ydl.webview.R
import com.ydl.webviewlibrary.X5WebChromeClient
import kotlinx.android.synthetic.main.activity_first.*

class PhotoActivity :AppCompatActivity(){


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

    fun handleFinish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition()
        } else {
            finish()
        }
    }


    override fun onDestroy() {
        try {
            if (web_view != null) {
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
        val url = "file:///android_asset/upload_photo.html"
        web_view.loadUrl(url)
    }


    /**
     * 上传图片之后的回调
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == X5WebChromeClient.FILE_CHOOSER_RESULT_CODE) {
            web_view.getX5WebChromeClient().uploadMessage(intent, resultCode)
        } else if (requestCode == X5WebChromeClient.FILE_CHOOSER_RESULT_CODE_5) {
            web_view.getX5WebChromeClient().uploadMessageForAndroid5(intent, resultCode)
        }
    }
}
