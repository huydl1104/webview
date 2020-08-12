package com.ydl.webview.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.ydl.webview.R
import kotlinx.android.synthetic.main.activity_first.*


class ThreeActivity : AppCompatActivity() {
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
        val url = "file:///android_asset/callsms.html"
        web_view.loadUrl(url)
    }
}
