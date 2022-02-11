package com.xiaopeng.jingwei.plugin.asmkit.glidehook;

import android.util.SparseArray;
import com.bumptech.glide.load.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

public class GlideDataManager {
    private static final String TAG = "GlideDataManager";
    public static SparseArray<GlideImageInfo> imageInfoMap = new SparseArray<>(64);

    /**
     * 保存“获取图片数据流过程中”产生的数据
     *
     * @param requestId
     * @param data
     * @param downloadCost
     */
    public static void saveInfoByRequestIdWhenGettingImage(int requestId,
                                                           Object data,
                                                           double downloadCost) {
        long imageSize = 0;
        if (data instanceof ByteBuffer) {
            imageSize = ((ByteBuffer) data).capacity();
        } else if (data instanceof InputStream) {
            try {
                imageSize = ((InputStream) data).available();
            } catch (IOException e) {

            }
        } else {
            imageSize = -1;
        }


        GlideImageInfo imageInfo = imageInfoMap.get(requestId);
        if (imageInfo == null) {
            imageInfo = new GlideImageInfo();
            imageInfoMap.put(requestId, imageInfo);
        }
        setInfoWhenGettingImage(imageInfo, imageSize, downloadCost);

        if (imageSize == -1) {
            imageInfo.error_message = data != null ? data.toString() : "empty Data";
        }
    }

    /**
     * 保存“图片最终加载成功，即将交给view”的数据。即图片加载完成
     *
     * @param requestId
     * @param requestUrlStr
     * @param width
     * @param height
     * @param dataSource
     * @param totalCost
     */
    public static void saveImageInfoByRequestId(int requestId,
                                                String requestUrlStr,
                                                int width,
                                                int height,
                                                DataSource dataSource,
                                                double totalCost) {
        URL url = null;
        GlideImageInfo imageInfo;
        try {
            url = new URL(requestUrlStr);
        } catch (MalformedURLException e) {
//            e.printStackTrace();
        }

        imageInfo = imageInfoMap.get(requestId);
        if (imageInfo != null) {
            imageInfoMap.delete(requestId);
        } else {
            imageInfo = new GlideImageInfo();
        }
        setRealRequestInfo(imageInfo, url, requestUrlStr, width, height, dataSource, totalCost);

//        Log.e(TAG, "imageInfoMap size = " + imageInfoMap.size());
//        ApmDataProxy.getProxy().onGlideDataReport(imageInfo);
    }

    /**
     * 保存“图片最终加载失败，即将抛异常”的数据
     *
     * @param requestId
     * @param requestUrlStr
     * @param width
     * @param height
     * @param errorMsg
     * @param totalCost
     */
    public static void saveFailImageInfoByRequestId(int requestId,
                                                    String requestUrlStr,
                                                    int width,
                                                    int height,
                                                    String errorMsg,
                                                    double totalCost) {
        URL url = null;
        GlideImageInfo imageInfo;
        try {
            url = new URL(requestUrlStr);
        } catch (MalformedURLException e) {
//            e.printStackTrace();
        }

        imageInfo = imageInfoMap.get(requestId);
        if (imageInfo != null) {
            imageInfoMap.delete(requestId);
        } else {
            imageInfo = new GlideImageInfo();
        }
        setFailRealRequestInfo(imageInfo, url, requestUrlStr, width, height, errorMsg, totalCost);

//        Log.e(TAG, "imageInfoMap size = " + imageInfoMap.size());
//        ApmDataProxy.getProxy().onGlideDataReport(imageInfo);
    }


    /**
     * 保存“图片解码阶段”的数据
     * 有些复用的情况不会回调该方法，例如MEMORY_CACHE
     *
     * @param requestId
     * @param targetWidth
     * @param targetHeight
     * @param mineType
     * @param realWidth
     * @param realHeight
     * @param decodeCost
     */
    public static void saveImageDecodeInfoByRequestId(Integer requestId,
                                                      int targetWidth,
                                                      int targetHeight,
                                                      String mineType,
                                                      int realWidth,
                                                      int realHeight,
                                                      double decodeCost) {
        GlideImageInfo imageInfo = imageInfoMap.get(requestId);
        if (imageInfo == null) {
            imageInfo = new GlideImageInfo();
            imageInfoMap.put(requestId, imageInfo);
        }
        imageInfo.setDecodeInfo(targetWidth,
                targetHeight,
                mineType,
                realWidth,
                realHeight,
                decodeCost);
    }


    /**
     * 保存图片加载成功的数据
     *
     * @param info
     * @param url
     * @param requestUrlStr
     * @param width
     * @param height
     * @param dataSource
     * @param totalCost
     */
    private static void setRealRequestInfo(GlideImageInfo info, URL url, String requestUrlStr, int width, int height, DataSource dataSource, double totalCost) {
        if (info == null) {
            return;
        }
        if (url != null) {
            info.host = url.getHost();
            info.original_path = url.getPath();
            info.request_path = url.getPath();
        } else {
            info.original_path = requestUrlStr;
            info.request_path = requestUrlStr;
        }
        info.target_width = width;
        info.target_height = height;
        info.cost_time = totalCost;
        info.cache_type = dataSource.name();
        info.status = 1;
    }

    /**
     * 保存图片加载失败的数据
     *
     * @param info
     * @param url
     * @param requestUrlStr
     * @param width
     * @param height
     * @param errorMsg
     * @param totalCost
     */
    private static void setFailRealRequestInfo(GlideImageInfo info, URL url, String requestUrlStr, int width, int height, String errorMsg, double totalCost) {
        if (info == null) {
            return;
        }
        if (url != null) {
            info.host = url.getHost();
            info.original_path = url.getPath();
            info.request_path = url.getPath();
        } else {
            info.original_path = requestUrlStr;
            info.request_path = requestUrlStr;
        }
        info.target_width = width;
        info.target_height = height;
        info.cost_time = totalCost;
        info.error_message = errorMsg;
        info.status = 0;
    }

    private static void setInfoWhenGettingImage(GlideImageInfo info, long imageSize, double downloadCost) {
        if (info == null) {
            return;
        }
        info.download_cost = downloadCost;
        info.image_size = imageSize;
        info.downloaded_size = imageSize;
    }
}
