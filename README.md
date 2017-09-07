# ImageWatcher
## 介绍
ImageWatcher是一个自定义图片查看器，功能包含图片放大缩小、双击恢复初始大小、下滑退出(类似微信朋友圈图片下滑退出功能)和图片共享元素转场动画。  
<img src="https://github.com/AltriaTT/ImageWatcher/blob/master/1504669961865_video.gif" width="270" height="480"/>
## 用法
1. 导入
```
compie 'io.github.altriatt:imagewatcher:1.0.1'
//依赖 建议Glide配置磁盘缓存原图尺寸
compile 'com.github.bumptech.glide:glide:4.0.0'
```
2. 使用

设置Activity的主题：
```
<style name="AppTheme.Translucent">
        <item name="android:windowBackground">@color/translucent_bg</item>
        <item name="android:windowIsTranslucent">true</item>
        <item name="android:windowFullscreen">true</item>//如果不想全屏，去掉这行
        <item name="windowActionBar">false</item>
        <item name="windowNoTitle">true</item>
</style>
```
在布局文件中加入：
```
<com.altria.imagewatcher.view.ImageWatcher
        android:id="@+id/image_watcher"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```
使用方法：
- 绑定资源并显示
```
mImageWatcher.bind(List<String> urls, int index);//index 首先加载图片的索引
mImageWatcher.bind(List<String> urls, int index, ImageViewInfo info);//仅index图片执行共享元素转场动画
mImageWatcher.bind(List<String> urls, int index, List<ImageViewInfo> infoList);//均执行共享元素转场动画
```
- 点击返回按钮和图片下滑退出时的处理操作（必选项）
```
mImageWatcher.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                finish();
                //禁用默认转场动画，如果使用淡入淡出，去掉这行代码
                overridePendingTransition(0, 0);
            }
        });
```
- 处理onBackPressed() （当有共享元素转场动画时，必选项）
```
@Override
    public void onBackPressed() {
        mImageWatcher.exit();
    }
```
- 添加操作视图（可选项）
```
mImageWatcher.bindActionView(); //在右上角添加一个自定义视图
```
- 共享元素转场动画所需信息获取
```
ImageWatcher.createViewInfoBundle(View view);
ImageWatcher.createViewInfoBundle(View... view);
```
## 样例
- 获取VIEW的位置和大小信息
```
Bundle viewInfoBundle = ImageWatcher.createViewInfoBundle(mImageViewFirst, mImageViewSecond);

viewInfoBundle.putStringArrayList("image_url", (ArrayList<String>) urls);
viewInfoBundle.putInt("position", 1);

Intent intent = new Intent(MainActivity.this, ImageWatchActivity.class);
intent.putExtras(viewInfoBundle);

startActivity(intent);
overridePendingTransition(0, 0);
```
- 在ImageWatcher所在Activity获取
```
imageUrl = getIntent().getStringArrayListExtra("image_url");
currentPosition = getIntent().getIntExtra("position", 0);

info = (ImageViewInfo) getIntent().getSerializableExtra("info");
infoList = (ImageViewInfoList) getIntent().getSerializableExtra("info_list");
```
## 联系方式
altria765961241@gmail.com
## 
## Licenses
 Copyright 2017 AltriaTT

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
