package io.github.altriatt.imagewatcherdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.altria.imagewatcher.base.OnDismissListener;
import com.altria.imagewatcher.entity.ImageViewInfo;
import com.altria.imagewatcher.entity.ImageViewInfoList;
import com.altria.imagewatcher.view.ImageWatcher;

import java.util.ArrayList;

/**
 * Created by altria on 17-9-4.
 */

public class ImageWatchActivity extends AppCompatActivity {

    private ArrayList<String> imageUrl;
    private int currentPosition;
    private ImageViewInfo info;
    private ImageViewInfoList infoList;
    private ImageWatcher mImageWatcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_watch);

        imageUrl = getIntent().getStringArrayListExtra("image_url");
        currentPosition = getIntent().getIntExtra("position", 0);

        info = (ImageViewInfo) getIntent().getSerializableExtra("info");
        infoList = (ImageViewInfoList) getIntent().getSerializableExtra("info_list");

        findView();
        initView();
        setListener();
    }

    private void setListener() {
        // mImageWatcher.bindActionView(); 在右上角添加一个自定义视图

        //点击返回按钮  图片下拉推出 时的处理操作
        mImageWatcher.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    private void initView() {
        if (info == null && infoList == null){
            mImageWatcher.bind(imageUrl, currentPosition);
        }else if (info == null){
            mImageWatcher.bind(imageUrl, currentPosition, infoList.getImageViewInfoList());
        }else {
            mImageWatcher.bind(imageUrl, currentPosition, info);
        }

    }

    private void findView() {
        mImageWatcher = findViewById(R.id.image_watcher);
    }


    @Override
    public void onBackPressed() {
        mImageWatcher.exit();
    }
}
