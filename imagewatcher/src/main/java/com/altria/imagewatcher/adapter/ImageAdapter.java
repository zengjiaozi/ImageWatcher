package com.altria.imagewatcher.adapter;

import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;

import com.altria.imagewatcher.R;
import com.altria.imagewatcher.base.OnExitListener;
import com.altria.imagewatcher.entity.ImageTag;
import com.altria.imagewatcher.entity.ImageViewInfo;
import com.altria.imagewatcher.view.ImageWatcher;
import com.altria.imagewatcher.view.ScaleImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by altria on 17-6-13.
 */

public class ImageAdapter extends PagerAdapter {
    private List<String> urls;
    private SparseArray<View> views = new SparseArray<>();
    private ImageWatcher imageWatcher;
    private ImageViewInfo info;
    private int firstLoadPosition;
    private List<ImageViewInfo> infoList;
    private boolean firstLoad = true;
    private long DEFAULT_DURATION = 500;
    private Map<Integer, ImageTag> imageTagMap = new HashMap<>();

    public ImageAdapter(List<String> urls, ImageWatcher imageWatcher) {
        this.urls = urls;
        this.imageWatcher = imageWatcher;

        imageTagMap.clear();
        firstLoad = true;
        info = null;
        infoList = null;
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));//删除页卡
        views.remove(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        final View inflate = LayoutInflater.from(container.getContext()).inflate(R.layout.item_image, null);
        container.addView(inflate);

        views.put(position, inflate);

        View bgView = inflate.findViewById(R.id.bg_view);
        final ScaleImageView image = (ScaleImageView) inflate.findViewById(R.id.image);

        image.setBgView(bgView);
        image.setExitListener(new OnExitListener() {
            @Override
            public void exit() {
                imageWatcher.dismiss();
            }
        });

        if (info == null && infoList == null) {
            Glide.with(container.getContext())
                    .asBitmap()
                    .load(urls.get(position))
                    .into(image);
        } else {
            Glide.with(container.getContext().getApplicationContext())
                    .asBitmap()
                    .load(urls.get(position))
                    .into(new SimpleTarget<Bitmap>() {

                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                            setImageTag(position, new ImageTag(resource.getWidth(), resource.getHeight()));

                            image.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                                @Override
                                public boolean onPreDraw() {
                                    // remove previous listener
                                    image.getViewTreeObserver().removeOnPreDrawListener(this);
                                    if (firstLoad && info != null) {
                                        firstLoad = false;

                                        //准备场景
                                        prepareScene(image, position, info);
                                        //播放动画
                                        runEnterAnimation(image);
                                    }else if (firstLoad){
                                        firstLoad = false;

                                        //准备场景
                                        prepareScene(image, position, infoList.get(position));
                                        //播放动画
                                        runEnterAnimation(image);
                                    }else {
                                        //获取转场动画信息
                                        getImageTagInfo(image, position, infoList.get(position));
                                    }

                                    return true;
                                }
                            });

                            image.setImageBitmap(resource);
                        }
                    });
        }

        return inflate;
    }

    private void runEnterAnimation(ScaleImageView image) {
        image.animate()
                .setDuration(DEFAULT_DURATION)
                .setInterpolator(new LinearInterpolator())
                .scaleX(1f)
                .scaleY(1f)
                .translationX(0)
                .translationY(0)
                .start();
    }

    private void runExitAnimation(ScaleImageView image, int position, ImageViewInfo imageViewInfo) {
        ImageTag imageTag = getImageTag(position);
     //   Log.e("TAG", imageViewInfo.getLeft()+" "+imageTag.getLeft()+" "+(imageViewInfo.getLeft() - imageTag.getLeft()));
        image.animate()
                .setDuration(DEFAULT_DURATION)
                .setInterpolator(new LinearInterpolator())
                .scaleX(imageTag.getScaleX())
                .scaleY(imageTag.getScaleY())
                .translationX(imageViewInfo.getLeft() - imageTag.getLeft())
                .translationY(imageViewInfo.getTop() - (imageTag.getTop() + ((image.getHeight() - ((image.getWidth() / imageTag.getWidth()) * imageTag.getHeight())) * imageTag.getScaleY()) / 2F))
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        imageWatcher.dismiss();
                    }
                })
                .start();
    }

    private void prepareScene(ScaleImageView image, int position, ImageViewInfo info) {
        ImageTag imageTag = getImageTag(position);

        //缩放到起始view大小
        float scaleX = (float) info.getWidth() / (float) image.getWidth();
        float scaleY = (float) info.getHeight() / (((float) image.getWidth() / imageTag.getWidth()) * imageTag.getHeight());
        image.setScaleX(scaleX);
        image.setScaleY(scaleY);
        imageTag.setScaleX(scaleX);
        imageTag.setScaleY(scaleY);
        //   Log.e("TAG", "scale:" + scaleX +" "+scaleY +" "+imagwWidth+" "+imageHeight);
        //   Log.e("TAG", "info:"+info.getLeft() + " " + info.getTop() + " " + info.getWidth() + " " + info.getHeight());
        int[] screenLocation = new int[2];
        image.getLocationOnScreen(screenLocation);
        imageTag.setLeft(screenLocation[0]);
        imageTag.setTop(screenLocation[1]);
        //     Log.e("TAG", "location:"+screenLocation[0] + " " + screenLocation[1]);

        //移动到起始view位置
        float deltaX = info.getLeft() - screenLocation[0];
        float deltaY = info.getTop() - (screenLocation[1] + ((image.getHeight() - ((image.getWidth() / imageTag.getWidth()) * imageTag.getHeight())) * scaleY) / 2F);
        image.setTranslationX(deltaX);
        image.setTranslationY(deltaY);
        //     Log.e("TAG","deltaY:"+deltaY);
    }

    private void getImageTagInfo(ScaleImageView image, int position, ImageViewInfo info) {
        ImageTag imageTag = getImageTag(position);

        //缩放到起始view大小
        float scaleX = (float) info.getWidth() / (float) image.getWidth();
        float scaleY = (float) info.getHeight() / (((float) image.getWidth() / imageTag.getWidth()) * imageTag.getHeight());
        image.setScaleX(scaleX);
        image.setScaleY(scaleY);
        imageTag.setScaleX(scaleX);
        imageTag.setScaleY(scaleY);

        int[] screenLocation = new int[2];
        image.getLocationOnScreen(screenLocation);
        if (position > firstLoadPosition){//预加载后一页
            imageTag.setLeft(screenLocation[0] - image.getWidth());
        }else {//预加载前一页
            imageTag.setLeft(screenLocation[0] + image.getWidth());
        }
        imageTag.setTop(screenLocation[1]);

        image.setScaleX(1.0F);
        image.setScaleY(1.0F);
    }

    public String getImageUrl(int position) {
        return urls.get(position);
    }

    public void setInfo(ImageViewInfo info) {
        this.info = info;
    }

    public void setFirstLoadPosition(int firstLoadPosition) {
        this.firstLoadPosition = firstLoadPosition;
    }

    public void setInfoList(List<ImageViewInfo> infoList) {
        this.infoList = infoList;
    }

    public void exitImageWatcher(int position) {
        views.get(position).findViewById(R.id.bg_view).setVisibility(View.GONE);
        if (infoList == null && info != null) {
            if (position != firstLoadPosition){
                imageWatcher.dismiss();
            }else {
                runExitAnimation((ScaleImageView) views.get(position).findViewById(R.id.image), position, info);
            }
        }else if (infoList != null && info == null){
            runExitAnimation((ScaleImageView) views.get(position).findViewById(R.id.image), position, infoList.get(position));
        }else {
            imageWatcher.dismiss();
        }

    }

    public void setImageTag(int key, ImageTag tag){
        imageTagMap.put(key, tag);
    }

    public ImageTag getImageTag(int key){
        return imageTagMap.get(key);
    }
}
