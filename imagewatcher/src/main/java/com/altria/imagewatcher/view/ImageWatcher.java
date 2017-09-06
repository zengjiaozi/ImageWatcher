package com.altria.imagewatcher.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.altria.imagewatcher.R;
import com.altria.imagewatcher.adapter.ImageAdapter;
import com.altria.imagewatcher.base.OnDismissListener;
import com.altria.imagewatcher.entity.ImageViewInfo;
import com.altria.imagewatcher.entity.ImageViewInfoList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by altria on 17-6-12.
 */

public class ImageWatcher extends FrameLayout {

    private View rootView;
    private ViewPager viewPager;
    private TextView imageIndex;
    private List<String> urls = new ArrayList<>();
    private int index;
    private ImageAdapter imageAdapter;
    private OnDismissListener onDismissListener;
    private FrameLayout actionView;
    private String currentImageUrl;
    private int currentItemPosition;
    private ImageView mBtnBack;
    private Context context;
    private ImageViewInfo info;
    private List<ImageViewInfo> infoList;

    public ImageWatcher(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        rootView = LayoutInflater.from(context).inflate(R.layout.layout_main, null);
        initView();
    }

    private void initView() {
        viewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        imageIndex = (TextView) rootView.findViewById(R.id.image_index);
        actionView = (FrameLayout) rootView.findViewById(R.id.action_view);
        mBtnBack = rootView.findViewById(R.id.btn_back);
        addView(rootView);
    }

    /**
     * 资源绑定 单组转场动画
     */
    public void bind(List<String> urls, int index, ImageViewInfo info) {
        this.urls.clear();
        this.urls.addAll(urls);

        this.info = info;
        this.index = index;

        init();
    }

    /**
     * 无转场动画
     * @param urls
     * @param index
     */
    public void bind(List<String> urls, int index){
        this.urls.clear();
        this.urls.addAll(urls);

        this.index = index;

        init();
    }

    /**
     * 全组转场动画
     * @param urls
     * @param index
     * @param infoList
     */
    public void bind(List<String> urls, int index, List<ImageViewInfo> infoList){
        this.urls.clear();
        this.urls.addAll(urls);

        this.infoList = infoList;
        this.index = index;

        init();
    }

    public void dismiss() {
        onDismissListener.onDismiss();
    }

    /**
     * 绑定图片操作视图
     */
    public void bindActionView(View view) {
        actionView.addView(view);
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    private void init() {
        imageAdapter = new ImageAdapter(urls, this);
        imageAdapter.setInfo(info);
        imageAdapter.setInfoList(infoList);
        imageAdapter.setFirstLoadPosition(index);
        currentImageUrl = imageAdapter.getImageUrl(index);

        viewPager.setAdapter(imageAdapter);

        viewPager.setCurrentItem(index);
        imageIndex.setText((index + 1) + " / " + urls.size());

        currentItemPosition = index;

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentItemPosition = position;
                if (urls.size() != 0) {
                    currentImageUrl = imageAdapter.getImageUrl(position);
                }
                imageIndex.setText((position + 1) + " / " + urls.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //返回
        mBtnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onDismissListener != null)
                    imageAdapter.exitImageWatcher(currentItemPosition);
            }
        });
    }

    /**
     * 获取当前图片地址
     *
     * @return
     */
    public String getCurrentImageUrl() {
        return currentImageUrl;
    }

    /**
     * 切换至下一张图片
     *
     * @return 是否还有下一张图片
     */
    public boolean toNextImage() {
        boolean hasNext = true;
        if (currentItemPosition + 1 >= urls.size()) {
            hasNext = false;
        } else {
            viewPager.setCurrentItem(++currentItemPosition);
        }
        return hasNext;
    }

    public static Bundle createViewInfoBundle(View view) {
        Bundle b = new Bundle();

        int[] screenLocation = new int[2];
        view.getLocationOnScreen(screenLocation);
        int left = screenLocation[0];
        int top = screenLocation[1];
        int width = view.getWidth();
        int height = view.getHeight();

        b.putSerializable("info", new ImageViewInfo(left, top, width, height));
        return b;
    }

    public static Bundle createViewInfoBundle(View... view) {
        Bundle b = new Bundle();
        List<ImageViewInfo> imageViewInfos = new ArrayList<>();

        for (View v : view) {
            int[] screenLocation = new int[2];
            v.getLocationOnScreen(screenLocation);
            int left = screenLocation[0];
            int top = screenLocation[1];
            int width = v.getWidth();
            int height = v.getHeight();

            imageViewInfos.add(new ImageViewInfo(left, top, width, height));
        }

        b.putSerializable("info_list", new ImageViewInfoList(imageViewInfos));
        return b;
    }
}
