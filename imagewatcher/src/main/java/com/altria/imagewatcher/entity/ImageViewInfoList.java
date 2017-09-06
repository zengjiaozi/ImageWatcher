package com.altria.imagewatcher.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by altria on 17-8-23.
 */

public class ImageViewInfoList implements Serializable{
    private List<ImageViewInfo> imageViewInfoList;

    public ImageViewInfoList(List<ImageViewInfo> imageViewInfoList) {
        this.imageViewInfoList = imageViewInfoList;
    }

    public List<ImageViewInfo> getImageViewInfoList() {
        return imageViewInfoList;
    }

    public void setImageViewInfoList(List<ImageViewInfo> imageViewInfoList) {
        this.imageViewInfoList = imageViewInfoList;
    }
}
