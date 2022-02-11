package com.xiaopeng.jingwei.lib.asmkit.glidehook;

import com.bumptech.glide.load.DataSource;
import com.xiaopeng.jingwei.plugin.asmkit.dataproxy.ApmDataProxy;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

public class GlideDataManager {
    private static final String TAG = "GlideDataManager";
    public static ConcurrentHashMap<Integer, com.xiaopeng.jingwei.plugin.asmkit.glidehook.GlideImageInfo> imageInfoMap = new ConcurrentHashMap<>();

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


        com.xiaopeng.jingwei.plugin.asmkit.glidehook.GlideImageInfo imageInfo;
        if (imageInfoMap.containsKey(requestId)) {
            imageInfo = imageInfoMap.get(requestId);
            if (imageInfo == null) {
                imageInfo = new com.xiaopeng.jingwei.plugin.asmkit.glidehook.GlideImageInfo();
            }
            setInfoWhenGettingImage(imageInfo, imageSize, downloadCost);
        } else {
            imageInfo = new com.xiaopeng.jingwei.plugin.asmkit.glidehook.GlideImageInfo();
            setInfoWhenGettingImage(imageInfo, imageSize, downloadCost);
            imageInfoMap.put(requestId, imageInfo);
        }
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
        com.xiaopeng.jingwei.plugin.asmkit.glidehook.GlideImageInfo imageInfo;
        try {
            url = new URL(requestUrlStr);
        } catch (MalformedURLException e) {

        }

        if (imageInfoMap.containsKey(requestId)) {
            imageInfo = imageInfoMap.remove(requestId);
            if (imageInfo == null) {
                imageInfo = new com.xiaopeng.jingwei.plugin.asmkit.glidehook.GlideImageInfo();
            }
            setRealRequestInfo(imageInfo, url, requestUrlStr, width, height, dataSource, totalCost);
        } else {
            imageInfo = new com.xiaopeng.jingwei.plugin.asmkit.glidehook.GlideImageInfo();
            setRealRequestInfo(imageInfo, url, requestUrlStr, width, height, dataSource, totalCost);
            imageInfoMap.put(requestId, imageInfo);
        }

        ApmDataProxy.getProxy().onGlideDataReport(imageInfo);
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
        com.xiaopeng.jingwei.plugin.asmkit.glidehook.GlideImageInfo imageInfo;
        try {
            url = new URL(requestUrlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (imageInfoMap.containsKey(requestId)) {
            imageInfo = imageInfoMap.remove(requestId);
            if (imageInfo == null) {
                imageInfo = new com.xiaopeng.jingwei.plugin.asmkit.glidehook.GlideImageInfo();
            }
            setFailRealRequestInfo(imageInfo, url, requestUrlStr, width, height, errorMsg, totalCost);
        } else {
            imageInfo = new com.xiaopeng.jingwei.plugin.asmkit.glidehook.GlideImageInfo();
            setFailRealRequestInfo(imageInfo, url, requestUrlStr, width, height, errorMsg, totalCost);
            imageInfoMap.put(requestId, imageInfo);
        }

        ApmDataProxy.getProxy().onGlideDataReport(imageInfo);
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
        if (!imageInfoMap.containsKey(requestId)) {
            com.xiaopeng.jingwei.plugin.asmkit.glidehook.GlideImageInfo imageInfo = new com.xiaopeng.jingwei.plugin.asmkit.glidehook.GlideImageInfo();
            imageInfo.setDecodeInfo(targetWidth,
                    targetHeight,
                    mineType,
                    realWidth,
                    realHeight,
                    decodeCost);
            imageInfoMap.put(requestId, imageInfo);
        } else {
            com.xiaopeng.jingwei.plugin.asmkit.glidehook.GlideImageInfo imageInfo = imageInfoMap.get(requestId);
            if (imageInfo != null) {
                imageInfo.setDecodeInfo(targetWidth,
                        targetHeight,
                        mineType,
                        realWidth,
                        realHeight,
                        decodeCost);
            }
        }
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
    private static void setRealRequestInfo(
        com.xiaopeng.jingwei.plugin.asmkit.glidehook.GlideImageInfo info, URL url, String requestUrlStr, int width, int height, DataSource dataSource, double totalCost) {
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
    private static void setFailRealRequestInfo(
        com.xiaopeng.jingwei.plugin.asmkit.glidehook.GlideImageInfo info, URL url, String requestUrlStr, int width, int height, String errorMsg, double totalCost) {
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

    private static void setInfoWhenGettingImage(
        com.xiaopeng.jingwei.plugin.asmkit.glidehook.GlideImageInfo info, long imageSize, double downloadCost) {
        if (info == null) {
            return;
        }
        info.download_cost = downloadCost;
        info.image_size = imageSize;
        info.downloaded_size = imageSize;
    }
}
