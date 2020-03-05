package com.example.programablergbled.Utils;

import android.content.Context;
import android.graphics.ImageFormat;
import android.media.Image;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Size;

import com.example.android.basicrenderscript.ScriptC_utils;

public class ImageUtils {

    private static Allocation mOutputAllocation;
    private static Allocation mPrevAllocation;
    private static Allocation mInputNormalAllocation;


    public static byte[] getByteFromImg(Image img, Context context){
        RenderScript rs = RenderScript.create(context);
        ScriptC_utils script = new ScriptC_utils(rs);

        mInputNormalAllocation.ioReceive();

        createAllcation(rs,new Size(img.getWidth(),img.getHeight()));


        //script.forEach_yuv2rgbFrames(mPrevAllocation,mOutputAllocation);
        byte[] b = new byte[mOutputAllocation.getBytesSize()];
        mOutputAllocation.copyTo(b);
        return b;
    }

    private static void createAllcation(RenderScript rs, Size dimensions) {
        Type.Builder yuvTypeBuilder = new Type.Builder(rs, Element.YUV(rs));
        yuvTypeBuilder.setX(dimensions.getWidth());
        yuvTypeBuilder.setY(dimensions.getHeight());
        yuvTypeBuilder.setYuvFormat(ImageFormat.YUV_420_888);

        Type.Builder rgbTypeBuilder = new Type.Builder(rs, Element.RGBA_8888(rs));
        rgbTypeBuilder.setX(dimensions.getWidth());
        rgbTypeBuilder.setY(dimensions.getHeight());
        mPrevAllocation = Allocation.createTyped(rs, rgbTypeBuilder.create(),
                Allocation.USAGE_SCRIPT);
        mOutputAllocation = Allocation.createTyped(rs, rgbTypeBuilder.create(),
                Allocation.USAGE_IO_OUTPUT | Allocation.USAGE_SCRIPT);

    }
}
