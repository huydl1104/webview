
package com.ydl.webviewlibrary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


public class X5WebViewClient extends WebViewClient {

    private InterWebListener webListener;
    private BridgeWebView webView;
    private Context mContext ;
    /**
     * 设置监听时间，包括常见状态页面切换，进度条变化等
     * @param listener                          listener
     */
    public void setWebListener(InterWebListener listener){
        this.webListener = listener;
    }

    /**
     * 构造方法
     * @param webView                           需要传进来webview
     * @param context                           上下文
     */
    public X5WebViewClient(BridgeWebView webView, Context context) {
        this.webView = webView;
        this.mContext = context;
        //将js对象与java对象进行映射
        //webView.addJavascriptInterface(new ImageJavascriptInterface(context), "imagelistener");
    }

    /**
     * 这个方法中可以做拦截
     * 主要的作用是处理各种通知和请求事件
     * 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }


        try {
            url = URLDecoder.decode(url, "UTF-8");
            Log.i("yuyu","shouldOverrideUrlLoading  url ->"+url);
            if(url.startsWith("sms:") || url.startsWith("mailto:") ||
                    url.startsWith("tel:")) {
                //类型我目前用到的是微信、支付宝、拨号 三种跳转方式，其他类型自加
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                Log.i("yuyu","shouldOverrideUrlLoading  Uri ->"+Uri.parse(url));
                mContext.startActivity(intent);
                return true;
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }



        // 如果是返回数据
        if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) {
            webView.handlerReturnData(url);
            return true;
        } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) {
            webView.flushMessageQueue();
            return true;
        } else {
            if (this.onCustomShouldOverrideUrlLoading(url)){
                return true;
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }
    }

    protected boolean onCustomShouldOverrideUrlLoading(String url) {
        return false;
    }


    /**
     * 增加shouldOverrideUrlLoading在api>=24时
     * 主要的作用是处理各种通知和请求事件
     * 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
     * @param view                              view
     * @param request                           request
     * @return
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String url = request.getUrl().toString();
            try {
                url = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            //如果是返回数据
            if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) {
                webView.handlerReturnData(url);
                return true;
            } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) {
                webView.flushMessageQueue();
                return true;
            } else {
                if (this.onCustomShouldOverrideUrlLoading(url)){
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, request);
                }
            }
        }else {
            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    /**
     * 作用：开始载入页面调用的，我们可以设定一个loading的页面，告诉用户程序在等待网络响应。
     */
    @Override
    public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
        super.onPageStarted(webView, s, bitmap);
        //设定加载开始的操作
    }

    /**
     * 当页面加载完成会调用该方法
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        if (!X5WebUtils.isConnected(webView.getContext()) && webListener!=null) {
            //隐藏进度条方法
            webListener.hindProgressBar();
        }
        super.onPageFinished(view, url);
        //设置网页在加载的时候暂时不加载图片
        //webView.getSettings().setBlockNetworkImage(false);
        //页面finish后再发起图片加载
        if(!webView.getSettings().getLoadsImagesAutomatically()) {
            webView.getSettings().setLoadsImagesAutomatically(true);
        }
        //这个时候添加js注入方法
        BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.TO_LOAD_JS);
        if (webView.getStartupMessage() != null) {
            for (Message m : webView.getStartupMessage()) {
                //分发message 必须在主线程才分发成功
                webView.dispatchMessage(m);
            }
            webView.setStartupMessage(null);
        }
        //html加载完成之后，添加监听图片的点击js函数
        //addImageClickListener();
        //addImageClickListener(webView);
    }

    /**
     * 请求网络出现error
     * 作用：加载页面的服务器出现错误时（如404）调用。
     * App里面使用webView控件的时候遇到了诸如404这类的错误的时候，若也显示浏览器里面的那种错误提示页面就显得很丑陋，
     * 那么这个时候我们的app就需要加载一个本地的错误提示页面，即webView如何加载一个本地的页面
     */
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String
            failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        if (errorCode == 404) {
            //用javascript隐藏系统定义的404页面信息
            String data = "Page NO FOUND！";
            view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
        } else {
            if (webListener!=null){
                webListener.showErrorView();
            }
        }
    }

    /**
     * 当缩放改变的时候会调用该方法
     */
    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        super.onScaleChanged(view, oldScale, newScale);
        //视频全屏播放按返回页面被放大的问题
        if (newScale - oldScale > 7) {
            //异常放大，缩回去。
            view.setInitialScale((int) (oldScale / newScale * 100));
        }
    }

    // 向主机应用程序报告Web资源加载错误。这些错误通常表明无法连接到服务器。
    // 值得注意的是，不同的是过时的版本的回调，新的版本将被称为任何资源（iframe，图像等）
    // 不仅为主页。因此，建议在回调过程中执行最低要求的工作。
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            X5WebUtils.log("服务器异常"+error.getDescription().toString());
        }
        //ToastUtils.showToast("服务器异常6.0之后");
        //当加载错误时，就让它加载本地错误网页文件
        //mWebView.loadUrl("file:///android_asset/errorpage/error.html");
        if (webListener!=null){
            webListener.showErrorView();
        }
    }


    /**
     * 通知主机应用程序在加载资源时从服务器接收到HTTP错误
     * @param view                              view
     * @param request                           request
     * @param errorResponse                     错误内容
     */
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                    WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
    }


    /**
     * 通知主机应用程序已自动处理用户登录请求
     * @param view                              view
     * @param realm                             数据
     * @param account                           account
     * @param args                              args
     */
    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
        super.onReceivedLoginRequest(view, realm, account, args);
    }

    /**
     * 在加载资源时通知主机应用程序发生SSL错误
     * 作用：处理https请求
     *      webView加载一些别人的url时候，有时候会发生证书认证错误的情况，这时候希望能够正常的呈现页面给用户，
     *      我们需要忽略证书错误，需要调用WebViewClient类的onReceivedSslError方法，
     *      调用handler.proceed()来忽略该证书错误。
     * @param view                              view
     * @param handler                           handler
     * @param error                             error
     */
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
        if (error!=null){
            String url = error.getUrl();
            X5WebUtils.log("onReceivedSslError----异常url----"+url);
        }
        //https忽略证书问题
        if (handler!=null){
            //表示等待证书响应
            handler.proceed();
            // handler.cancel();      //表示挂起连接，为默认方式
            // handler.handleMessage(null);    //可做其他处理
        }
    }

    /**
     * 作用：在加载页面资源时会调用，每一个资源（比如图片）的加载都会调用一次。
     * @param webView                           view
     * @param s                                 s
     */
    @Override
    public void onLoadResource(WebView webView, String s) {
        super.onLoadResource(webView, s);
    }

    /**
     * android与js交互：
     * 首先我们拿到html中加载图片的标签img.
     * 然后取出其对应的src属性
     * 循环遍历设置图片的点击事件
     * 将src作为参数传给java代码
     * 在java回调方法中对界面进行跳转处理，用PhotoView加载大图实现，便于手势的操作
     * 这个循环将所图片放入数组，当js调用本地方法时传入。
     * 当然如果采用方式一获取图片的话，本地方法可以不需要传入这个数组
     * //通过js代码找到标签为img的代码块，设置点击的监听方法与本地的openImage方法进行连接
     * @param webView                       webview
     */
    private void addImageArrayClickListener(WebView webView) {
        webView.loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName(\"img\"); " +
                "var array=new Array(); " +
                "for(var j=0;j<objs.length;j++){" +
                "    array[j]=objs[j].src; " +
                "}"+
                "for(var i=0;i<objs.length;i++)  " +
                "{"
                + "    objs[i].onclick=function()  " +
                "    {  "
                + "        window.imagelistener.openImage(this.src,array);  " +
                "    }  " +
                "}" +
                "})()");
    }

    /**
     * 通过js代码找到标签为img的代码块，设置点击的监听方法与本地的openImage方法进行连接
     * @param webView                       webview
     */
    private void addImageClickListener(WebView webView) {
        webView.loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName(\"img\"); " +
                "for(var i=0;i<objs.length;i++)  " +
                "{"
                + "    objs[i].onclick=function()  " +
                "    {  "
                + "        window.imagelistener.openImage(this.src);  " +
                "    }  " +
                "}" +
                "})()");
    }
}

