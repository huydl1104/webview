package com.ydl.webview.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ydl.webview.R
import kotlinx.android.synthetic.main.activity_js_bridge.*

class JSBridgeActivity :AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_js_bridge)
        webview.loadUrl("https://www.baidu.com/")
    }
}