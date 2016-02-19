package com.example.myapplication.widget.adbannerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.myapplication.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import java.net.URL;

/**
 * Created by sid on 2016/2/19.
 */
public class AdBannerView extends RelativeLayout{

    private static final int GET_BANNER_MES = 0X001;
    private static final int START_WEBVIEW = 0X002;
    private static final int FINISH_WEBVIEW = 0X003;
    private WebView vAdbanner;
    private ImageView vAdbannerHidePic;
    private View that;
    private ForJs controller;
    private Context mContext;

    private Handler handler;
    private int mForwardAppPic;
    private String mForwardAppName;

    /**
     * 指定链接地址显示广告位
     * 需判断链接地址是否存在及内容高度是否大于0
     */
    public void show(final String url) {
        if(url == null || url.length() == 0) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                //判断页面是否存在
                try {
//                    URL url2 =  new URL(url);
//                    url2.openConnection();//判断链接是否正常

                    Message msg = new Message();
                    msg.what = START_WEBVIEW;
                    msg.arg1 = HttpStatus.SC_OK;
                    msg.obj = url;
                    handler.sendMessage(msg);

                } catch (Exception e) {

                }
            }
        }).start();
    }

    public AdBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        that = this;
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.tt_adbanner_view, this);
        vAdbanner = (WebView)findViewById(R.id.tt_adbanner);
        vAdbannerHidePic = (ImageView)findViewById(R.id.tt_adbanner_hide_pic);
        //设置不可见
        setVisibility(View.GONE);

        initHandler();
        init(context,attrs);
    }


    private void initHandler() {
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {


                if(msg.what == FINISH_WEBVIEW) {
                    //显示
                    that.setVisibility(View.VISIBLE);
                }

                //开始装载页面
                if(msg.what == START_WEBVIEW && msg.arg1 == HttpStatus.SC_OK) {
                    controller = new ForJs();
                    String url = (String) msg.obj;
                    synCookies(url);
                    vAdbanner.getSettings().setJavaScriptEnabled(true);// 支持js
                    vAdbanner.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                    vAdbanner.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
                    vAdbanner.addJavascriptInterface(controller, "controller");// 向页面注入控制器
                    vAdbanner.getSettings().setUseWideViewPort(true);// 自适应大小

                    vAdbanner.setWebViewClient(new WebViewClient() {
                        /**
                         * {@inheritDoc}
                         */
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            //此处做点击跳转页面
                            return true;
                        }

                        /**
                         * 处理https证书问题 {@inheritDoc}
                         */
                        @Override
                        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                            handler.proceed();// 接受证书
                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
//                            view.loadUrl("javascript:window.controller.setDocumentBodyHeight(document.body.children[0].offsetHeight);");
                            //此处可与页面做交互
                            view.loadUrl("javascript:window.controller.setDocumentBodyHeight(1);");
                            super.onPageFinished(view, url);
                        }
                    });
                    vAdbannerHidePic.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            that.setVisibility(View.GONE);
                        }
                    });
                    vAdbanner.loadUrl(url);
//                    vAdbanner.loadUrl("file:///android_asset/index.html");访问本地文件
                }
            }
        };
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.tt_adbannerview);
        if(a == null){
            return;
        }
//        mForwardAppPic = a.getInt(R.styleable.tt_adbannerview_tt_adbbaner_forward_pic, -1);
        mForwardAppName = a.getString(R.styleable.tt_adbannerview_tt_adbbaner_forward_title);
    }

    /**
     * 同步一下cookie
     */
    private void synCookies(String url) {
        String cookies = "";
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeAllCookie();
        cookieManager.setCookie(url, cookies);// cookies是在HttpClient中获得的cookie
    }

    /**
     * js回调内部类. <br>
     */
    class ForJs {
        /**
         * 设置页面body高度. <br>
         * 图片不能读取的时候服务端body被置为0
         */
        @JavascriptInterface
        public void setDocumentBodyHeight(int height) {
            if (height != 0) {// 高度不为0，展示广告页面
                Message msg = new Message();
                msg.what = FINISH_WEBVIEW;
                handler.sendMessage(msg);
            }
        }
    }
}
