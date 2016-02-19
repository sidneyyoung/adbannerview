package com.example.myapplication;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import com.example.myapplication.widget.adbannerview.AdBannerView;

public class MainActivity extends Activity {

    //广告布局
    private AdBannerView adBannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }


    private void init() {
        adBannerView = (AdBannerView)this.findViewById(R.id.adbanner_layout);
        adBannerView.show("https://www.baidu.com/?tn=94026736_hao_pg");
    }
}
