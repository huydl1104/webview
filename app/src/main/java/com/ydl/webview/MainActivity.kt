package com.ydl.webview

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import com.tencent.smtt.sdk.VideoActivity
import com.ydl.webview.activity.*
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button11.setOnClickListener(this)
        button22.setOnClickListener(this)
        button33.setOnClickListener(this)
        button44.setOnClickListener(this)
        button55.setOnClickListener(this)
        button66.setOnClickListener(this)
        button77.setOnClickListener(this)
        button88.setOnClickListener(this)
        button99.setOnClickListener(this)

        val permission =  RxPermissions(this)
        permission.request(Manifest.permission.INTERNET).subscribe(object :Observer<Boolean>{
            override fun onComplete() {

            }

            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: Boolean) {
                Log.e("yuyu","onNext  intent -->$t")
            }

            override fun onError(e: Throwable) {
            }

        })
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.button11->{startActivity(Intent(this, JSBridgeActivity::class.java))}
            R.id.button22->{startActivity(Intent(this, FirstActivity::class.java))}
            R.id.button33->{startActivity(Intent(this, SecondActivity::class.java))}
            R.id.button44->{startActivity(Intent(this, ThreeActivity::class.java))}
            R.id.button55->{startActivity(Intent(this, VideoWebViewActivity::class.java))}
            R.id.button66->{startActivity(Intent(this, PhotoActivity::class.java))}
            R.id.button77->{startActivity(Intent(this, NativeActivity::class.java))}
            R.id.button88->{startActivity(Intent(this, CopyActivity::class.java))}
            R.id.button99->{startActivity(Intent(this, FileDisplayActivity::class.java))}
        }
    }

}
