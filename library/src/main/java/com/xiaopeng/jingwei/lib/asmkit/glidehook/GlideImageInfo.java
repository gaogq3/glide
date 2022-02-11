package com.xiaopeng.jingwei.lib.asmkit.glidehook;

public class GlideImageInfo {
    public String host = "";
    public String original_path = "";
    public String request_path = "";
    public int target_width = 0;
    public int target_height = 0;
    public int real_width = 0;
    public int real_height = 0;
    public long image_size = 0;
    public long downloaded_size = 0;
    public String mime_type = "";
    public String cache_type = "";
    public double download_cost = 0;
    public double decode_cost = 0;
    public double cost_time = 0;
    public int status = 0;
    public String error_message = "";

    public void setDecodeInfo(int targetWidth,
                              int targetHeight,
                              String mineType,
                              int realWidth,
                              int realHeight,
                              double decodeCost) {
        target_width = targetWidth;
        target_height = targetHeight;
        mime_type = mineType;
        real_width = realWidth;
        real_height = realHeight;
        decode_cost = decodeCost;
    }

    @Override
    public String toString() {
        return "GlideImageInfo{" +
                "host='" + host + '\'' +
                ", original_path='" + original_path + '\'' +
                ", request_path='" + request_path + '\'' +
                ", target_width=" + target_width +
                ", target_height=" + target_height +
                ", real_width=" + real_width +
                ", real_height=" + real_height +
                ", image_size=" + image_size +
                ", downloaded_size=" + downloaded_size +
                ", mime_type='" + mime_type + '\'' +
                ", cache_type='" + cache_type + '\'' +
                ", download_cost=" + download_cost +
                ", decode_cost=" + decode_cost +
                ", cost_time=" + cost_time +
                ", status=" + status +
                ", error_message='" + error_message + '\'' +
                '}';
    }
}
