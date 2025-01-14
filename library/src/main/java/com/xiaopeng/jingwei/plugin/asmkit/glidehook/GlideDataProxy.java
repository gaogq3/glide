package com.xiaopeng.jingwei.plugin.asmkit.glidehook;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.SparseIntArray;
import androidx.annotation.Nullable;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.ResourceCallback;
import com.bumptech.glide.request.SingleRequest;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.util.LogTime;
import com.xiaopeng.jingwei.plugin.asmkit.utils.ReflectUtils;
import java.util.ArrayList;
import java.util.List;


public class GlideDataProxy {

    private static final String TAG = "GlideProxy";
    public static SparseIntArray engineKeyRequestIdMap = new SparseIntArray(64);
    public static SparseIntArray optionsRequestIdMap = new SparseIntArray(64);
    public static SparseIntArray imageReaderOptionsMap = new SparseIntArray(64);


    /**
     * @param engineKeyHashCode
     * @param resourceCallback  因为resourceCallback是SingleRequest类所实现的接口，
     *                          所这个接口的hashCode = SingleRequest对象的hashCode,即requestId
     */
    public static void proxyPutEngineKeyMapToResourceCallback(Integer engineKeyHashCode, ResourceCallback resourceCallback) {
//        Log.d(TAG, String.format("engineKeyHashCode = %d , requestId = % d", engineKeyHashCode, resourceCallback.hashCode()));
        engineKeyRequestIdMap.put(engineKeyHashCode, resourceCallback.hashCode());
    }


    /**
     * @param engineKeyHashCode
     * @param sourceKey
     * @param data
     */
    public static void proxyOnDataFetcherReady(Integer engineKeyHashCode, Key sourceKey, Object data, long startFetchTime) {
//        Log.d(TAG, "proxyOnDataFetcherReady");
        Integer callBackHashCode = engineKeyRequestIdMap.get(engineKeyHashCode);
        GlideDataManager
                .saveInfoByRequestIdWhenGettingImage(callBackHashCode == null ? 0 : callBackHashCode,
                        data,
                        LogTime.getElapsedMillis(startFetchTime));
    }

    /**
     * get and remove
     *
     * @param engineKeyHashCode
     * @return requestId/callbackHashCode
     */
    public static int proxyGetRequestIdByEngineKey(Integer engineKeyHashCode) {
//        Log.d(TAG, String.format("proxyGetRequestIdByEngineKey = %d", engineKeyHashCode));
        int callBackHashCode = engineKeyRequestIdMap.get(engineKeyHashCode, 0);
        engineKeyRequestIdMap.delete(engineKeyHashCode);
        return callBackHashCode;
    }

    /**
     * 图片加载结束，带开始时间
     *
     * @param resource
     * @param model
     * @param target
     * @param dataSource
     * @param startTime
     * @param <R>
     */
    public static <R> void proxyOnResourceReady(int requestId,
                                                R resource,
                                                Object model,
                                                Target<R> target,
                                                int requestWidth,
                                                int requestHeight,
                                                DataSource dataSource,
                                                long startTime) {
//        Log.d(TAG, "proxyOnResourceReady: requestId = " + requestId);
//        Bitmap bitmap = null;
//        if (resource instanceof Bitmap) {
//            bitmap = (Bitmap) resource;
//            double imgSize = byte2MemorySize(bitmap.getByteCount(), MemoryConstants.MB);
//        } else if (resource instanceof BitmapDrawable) {
//            bitmap = ImageUtils.drawable2Bitmap((BitmapDrawable) resource);
//            double imgSize = byte2MemorySize(bitmap.getByteCount(), MemoryConstants.MB);
//        }
        if (resource == null) {
            return;
        }
        GlideDataManager
                .saveImageInfoByRequestId(requestId, model != null ? model.toString() : "url is null", requestWidth, requestHeight, dataSource, LogTime.getElapsedMillis(startTime));
//
//        Log.d(TAG, "Finished loading "
//                + resource.getClass().getSimpleName()
//                + " from "
//                + dataSource
//                + " for "
//                + model
//                + " with size ["
//                + bitmap.getWidth()
//                + "x"
//                + bitmap.getHeight()
//                + "] in "
//                + com.bumptech.glide.util.LogTime.getElapsedMillis(startTime)
//                + " ms");
    }

    /**
     * 图片加载失败，带开始时间
     *
     * @param requestId
     * @param model
     * @param target
     * @param e
     * @param startTime
     * @param <R>
     */
    public static <R> void proxyOnLoadFailed(int requestId, Object model,
                                             Target<R> target,
                                             int requestWidth,
                                             int requestHeight,
                                             GlideException e, long startTime) {
        GlideDataManager.saveFailImageInfoByRequestId(requestId, model != null ? model.toString() : "url is null", requestWidth, requestHeight, e.toString(), LogTime.getElapsedMillis(startTime));
    }

    /**
     * 图片加载结束通过listener回调
     *
     * @param singleRequest
     */
    public static void proxyRequestListener(Object singleRequest) {
//        Log.d(TAG, "proxyRequestListener: ");
        try {
            List<RequestListener> requestListeners = null;
            if (singleRequest instanceof SingleRequest) {
                requestListeners = ReflectUtils.reflect(singleRequest).field("requestListeners").get();
            }
            //可能存在用户没有引入okhttp的情况
            if (requestListeners == null) {
                requestListeners = new ArrayList<>();
                requestListeners.add(new GlideRequestListener());
            } else {
                requestListeners.add(new GlideRequestListener());
            }
            if (singleRequest instanceof SingleRequest) {
                ReflectUtils.reflect(singleRequest).field("requestListeners", requestListeners);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存options的HashCode和requestId的map键值对
     *
     * @param optionsHashCode
     * @param requestId
     */
    public static void proxyPutOptionsToRequestId(Integer optionsHashCode, Integer requestId) {
//        Log.d(TAG, "proxyPutImageReaderMapToRequestId: ");
        optionsRequestIdMap.put(optionsHashCode, requestId);
    }

    /**
     * 保存图片解码阶段结束时的一些信息
     *
     * @param imageReaderHashCode
     * @param sourceWidth
     * @param sourceHeight
     * @param outMimeType
     * @param options
     * @param result
     * @param requestedWidth
     * @param requestedHeight
     * @param startTime
     */
    public static void proxySaveImageInfo(Integer imageReaderHashCode,
                                          int sourceWidth,
                                          int sourceHeight,
                                          String outMimeType,
                                          BitmapFactory.Options options,
                                          Bitmap result,
                                          int requestedWidth,
                                          int requestedHeight,
                                          long startTime) {
        //获取optionsHashCode并移除其在imageReaderOptionsMap中的值
        int optionsHashCode = imageReaderOptionsMap.get(imageReaderHashCode, 0);
        imageReaderOptionsMap.delete(imageReaderHashCode);

        //获取requestId并移除其在optionsRequestIdMap中的值
        int requestId = optionsRequestIdMap.get(optionsHashCode, 0);
        optionsRequestIdMap.delete(optionsHashCode);
        if (requestId != 0) {
            GlideDataManager.saveImageDecodeInfoByRequestId(requestId,
                    requestedWidth,
                    requestedHeight,
                    outMimeType,
                    sourceWidth,
                    sourceHeight,
                    LogTime.getElapsedMillis(startTime));

//            Log.v(TAG, "Decoded "
//                    + "requestId = "
//                    + requestId
//                    + getBitmapString(result)
//                    + " from ["
//                    + sourceWidth
//                    + "x"
//                    + sourceHeight
//                    + "] "
//                    + outMimeType
//                    + " with inBitmap "
//                    + getInBitmapString(options)
//                    + " for ["
//                    + requestedWidth
//                    + "x"
//                    + requestedHeight
//                    + "]"
//                    + ", sample size: "
//                    + options.inSampleSize
//                    + ", density: "
//                    + options.inDensity
//                    + ", target density: "
//                    + options.inTargetDensity
////                + ", thread: "
////                + Thread.currentThread().getName()
//                    + ", duration: "
//                    + LogTime.getElapsedMillis(startTime));
        }
    }


    public static void proxyPutImageReaderOptionsMap(Integer imageReaderHashCode, Integer optionsHashCode) {
//        Log.d("GaoGq", String.format("proxyPutImageReaderOptionsMap:" +
//                "imageReaderHashCode = %d, optionsHashCode = %d", imageReaderHashCode, optionsHashCode));
        imageReaderOptionsMap.put(imageReaderHashCode, optionsHashCode);
    }

    private static String getInBitmapString(BitmapFactory.Options options) {
        return getBitmapString(options.inBitmap);
    }


    @Nullable
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getBitmapString(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        String sizeString = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? " (" + bitmap.getAllocationByteCount() + ")" : "";
        return "["
                + bitmap.getWidth()
                + "x"
                + bitmap.getHeight()
                + "] "
                + bitmap.getConfig()
                + sizeString;
    }
}
