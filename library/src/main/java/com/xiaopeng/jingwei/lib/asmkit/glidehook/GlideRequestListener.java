package com.xiaopeng.jingwei.lib.asmkit.glidehook;

import androidx.annotation.Nullable;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class GlideRequestListener<R> implements RequestListener<R> {
    private static final String TAG = "GlideRequestListener";
    public static boolean isGlideApmOpen = true;

    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<R> target, boolean isFirstResource) {
        return false;
    }

    @Override
    public boolean onResourceReady(R resource, Object model, Target<R> target, DataSource dataSource, boolean isFirstResource) {
        try {
            if (isGlideApmOpen) {
//                Bitmap bitmap;
//                if (resource instanceof Bitmap) {
//                    bitmap = (Bitmap) resource;
//                    double imgSize = byte2MemorySize(bitmap.getByteCount(), MemoryConstants.MB);
//                    Log.v(TAG, "load finish url = " + model.toString() + ", DataSource = " + dataSource);
////                    GlideDataManager.saveImageInfo(model.toString(), imgSize, bitmap.getWidth(), bitmap.getHeight(), "Glide");
//                } else if (resource instanceof BitmapDrawable) {
//                    bitmap = ImageUtils.drawable2Bitmap((BitmapDrawable) resource);
//                    double imgSize = byte2MemorySize(bitmap.getByteCount(), MemoryConstants.MB);
//                    Log.v(TAG, "load finish url = " + model.toString() + ", DataSource = " + dataSource);
////                    GlideDataManager.saveImageInfo(model.toString(), imgSize, bitmap.getWidth(), bitmap.getHeight(), "Glide");
//                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
