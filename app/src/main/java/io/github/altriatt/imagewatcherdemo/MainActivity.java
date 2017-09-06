package io.github.altriatt.imagewatcherdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.altria.imagewatcher.view.ImageWatcher;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<String> urls = new ArrayList<>();
    private ImageView mImageViewFirst;
    private ImageView mImageViewSecond;
    private RequestOptions optionsDiskAll = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urls.add("http://wx1.sinaimg.cn/mw690/5f2f3c0bly1fj7580g7v4j20qo141e0y.jpg");
        urls.add("http://wx3.sinaimg.cn/mw690/5f2f3c0bly1fj6sygwmycj23vc2kwu13.jpg");

        findView();
        initView();
        setListener();
    }

    private void setListener() {
        mImageViewFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取当前View的位置大小信息
                Bundle viewInfoBundle = new Bundle();
                viewInfoBundle.putStringArrayList("image_url", (ArrayList<String>) urls);
                viewInfoBundle.putInt("position", 0);

                Intent intent = new Intent(MainActivity.this, ImageWatchActivity.class);
                intent.putExtras(viewInfoBundle);

                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        mImageViewSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取当前View的位置大小信息
                Bundle viewInfoBundle = ImageWatcher.createViewInfoBundle(mImageViewFirst, mImageViewSecond);
                viewInfoBundle.putStringArrayList("image_url", (ArrayList<String>) urls);
                viewInfoBundle.putInt("position", 1);

                Intent intent = new Intent(MainActivity.this, ImageWatchActivity.class);
                intent.putExtras(viewInfoBundle);

                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    private void initView() {
        Glide.with(this).load(urls.get(0)).apply(optionsDiskAll).into(mImageViewFirst);
        Glide.with(this).load(urls.get(1)).apply(optionsDiskAll).into(mImageViewSecond);
    }

    private void findView() {
        mImageViewFirst = findViewById(R.id.image_first);
        mImageViewSecond = findViewById(R.id.image_second);
    }
}
