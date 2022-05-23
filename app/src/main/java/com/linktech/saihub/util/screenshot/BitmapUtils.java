/*
 * *******************************************************************
 *   @项目名称: BHex Android
 *   @文件名称: BitmapUtils.java
 *   @Date: 19-5-22 下午9:52
 *   @Author: ppzhao
 *   @Description:
 *   @Copyright（C）: 2019 BlueHelix Inc.   All rights reserved.
 *   注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的.
 *  *******************************************************************
 */

package com.linktech.saihub.util.screenshot;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.linktech.saihub.R;
import com.linktech.saihub.util.LogUtils;
import com.linktech.saihub.util.PixelUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class BitmapUtils {
    /**
     * 合成新的bitmap
     *
     * @param context
     * @param filePath
     * @param bitmap
     * @return
     */
    public static Bitmap concatBitmap(Context context, String filePath, Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int navHeight = getHeightWithNav(context) - getHeightWithoutNav(context);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(filePath, options);
        int width = options.outWidth;
        int height = options.outHeight - navHeight;
        int max = 1024 * 1024;
        int sampleSize = 1;
        while (width / sampleSize * height / sampleSize > max) {
            sampleSize *= 2;
        }
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;

        Bitmap srcBmp = BitmapFactory.decodeFile(filePath, options);
        //先计算bitmap的宽高，因为bitmap的宽度和屏幕宽度是不一样的，需要按比例拉伸
        double ratio = 1.0 * bitmap.getWidth() / srcBmp.getWidth();
        int additionalHeight = (int) (bitmap.getHeight() / ratio);
        Bitmap scaledBmp = Bitmap.createScaledBitmap(bitmap, srcBmp.getWidth(), additionalHeight, false);
        //到这里图片拉伸完毕

        //这里开始拼接，画到Canvas上
        Bitmap result = Bitmap.createBitmap(srcBmp.getWidth(), srcBmp.getHeight() - navHeight / sampleSize + additionalHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas();
        canvas.setBitmap(result);
        canvas.drawBitmap(srcBmp, 0, 0, null);
        //这里需要做个判断，因为一些系统是有导航栏的，所以截图时有导航栏，这里需要把导航栏遮住
        //计算出导航栏高度，然后draw时往上偏移一段距离

        double navRatio = 1.0 * PixelUtils.getScreenWidth() / srcBmp.getWidth();
        canvas.drawBitmap(scaledBmp, 0, srcBmp.getHeight() - (int) (navHeight / navRatio), null);
        bitmap.recycle();
        return result;
    }

    public static Bitmap concatBitmap(Context context, boolean isVerticalScreen, Bitmap srcBmp, Bitmap attachBitmap) {
        if (srcBmp == null || attachBitmap == null) {
            return null;
        }
        int navHeight = getHeightWithNav(context) - getHeightWithoutNav(context);
        //这里开始拼接，画到Canvas上
        Bitmap result;
        if (isVerticalScreen) {
            //叠加bitmap
            result = Bitmap.createBitmap(srcBmp.getWidth(), srcBmp.getHeight() - navHeight, Bitmap.Config.RGB_565);
        } else {
            //追加bitmap
            result = Bitmap.createBitmap(srcBmp.getWidth(), srcBmp.getHeight() - navHeight + attachBitmap.getHeight(), Bitmap.Config.RGB_565);
        }

        //这里需要做个判断，因为一些系统是有导航栏的，所以截图时有导航栏，这里需要把导航栏遮住
        //计算出导航栏高度，然后draw时往上偏移一段距离

        Canvas canvas = new Canvas(result);
//        canvas.drawColor(SkinColorUtil.getWhite(context));
        canvas.drawColor(context.getResources().getColor(R.color.white));
        canvas.drawBitmap(srcBmp, 0, 0, null);
        //这里需要做个判断，因为一些系统是有导航栏的，所以截图时有导航栏，这里需要把导航栏遮住
        //计算出导航栏高度，然后draw时往上偏移一段距离

        double navRatio = 1.0 * PixelUtils.getScreenWidth() / srcBmp.getWidth();

        LogUtils.d("BitmapUtils", "navHeight==>:" + navHeight + "==navRatio==:" + navRatio + "==x==" + ((navHeight / navRatio) - attachBitmap.getHeight()));
        if (isVerticalScreen) {
            //叠加bitmap
            canvas.drawBitmap(attachBitmap, 0, srcBmp.getHeight() - (int) (navHeight / navRatio) - attachBitmap.getHeight(), null);
        } else {
            //追加bitmap
            canvas.drawBitmap(attachBitmap, 0, srcBmp.getHeight() - (int) (navHeight / navRatio), null);
        }

        attachBitmap.recycle();
        return result;
    }

    public static Bitmap concatBitmap2(Context context, boolean isVerticalScreen, Bitmap srcBmp, Bitmap attachBitmap) {
        if (srcBmp == null || attachBitmap == null) {
            return null;
        }
        Bitmap result;
        int width = Math.max(srcBmp.getWidth(), attachBitmap.getWidth());
        int height = srcBmp.getHeight() + attachBitmap.getHeight();
        result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(result);
//        canvas.drawColor(SkinColorUtil.getWhite(context));
        canvas.drawColor(context.getResources().getColor(R.color.white));
        canvas.drawBitmap(srcBmp, 0, 0, null);

        canvas.drawBitmap(attachBitmap, 0, srcBmp.getHeight(), null);
        return result;
    }

    /**
     * 获取屏幕高度，不包括navigation
     */
    public static int getHeightWithNav(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display d = windowManager.getDefaultDisplay();
        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            d.getRealMetrics(realDisplayMetrics);
        } else {
            try {
                Method method = d.getClass().getDeclaredMethod("getRealMetrics");
                method.setAccessible(true);
                method.invoke(d, realDisplayMetrics);
            } catch (NoSuchMethodException e) {

            } catch (InvocationTargetException e) {

            } catch (IllegalAccessException e) {

            }
        }
        return realDisplayMetrics.heightPixels;
    }

    /**
     * 获取屏幕高度，包括navigation
     *
     * @return
     */
    public static int getHeightWithoutNav(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display d = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    /**
     * 第一种
     * 通过 DrawingCache 方法来截取普通的view，获取它的视图（Bitmap)
     * 这个方法适用于view 已经显示在界面上了，可以获得view 的宽高实际大小，进而通过DrawingCache 保存为bitmap
     *
     * @param view
     * @return
     */
    public static Bitmap createBitmap(View view) {
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    /**
     * 第一种
     * 通过 DrawingCache 方法来截取普通的view，获取它的视图（Bitmap)
     * 这个方法适用于view 已经显示在界面上了，可以获得view 的宽高实际大小，进而通过DrawingCache 保存为bitmap
     *
     * @param view
     * @return
     */
    public static Bitmap createBitmap(View view, int width, int height) {
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        Bitmap bmp = changeBitmapSize(bitmap, width, height);
        return bmp;
    }

    public static Bitmap changeBitmapSize(Bitmap bitmap, int newWidth, int newHeight) {
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        LogUtils.e("width", "width:" + width);
        LogUtils.e("height", "height:" + height);

        //设置想要的大小
//        int newWidth=1080;
//        int newHeight=1920;

        //计算压缩的比率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        //按原比例缩放，取最合适的缩放比
        float scale = scaleWidth > scaleHeight ? scaleHeight : scaleWidth;

        //获取想要缩放的matrix
        Matrix matrix = new Matrix();
//        matrix.postScale(scaleWidth,scaleHeight);
        matrix.postScale(scale, scale);

        //获取新的bitmap
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        bitmap.getWidth();
        bitmap.getHeight();

        LogUtils.e("newWidth", "newWidth" + bitmap.getWidth());
        LogUtils.e("newHeight", "newHeight" + bitmap.getHeight());

        return bitmap;
    }

    public static Bitmap changeBitmapScale(Bitmap bitmap) {
        if (bitmap.isRecycled()) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        LogUtils.e("width", "width:" + width);
        LogUtils.e("height", "height:" + height);
        float scale = 0;
        if (width > height) {
            //计算压缩的比率
            scale = ((float) width) / height;
        } else {
            scale = ((float) height) / width;
        }
        //获取想要缩放的matrix
        Matrix matrix = new Matrix();
//        matrix.postScale(scaleWidth,scaleHeight);
        matrix.postScale(scale, scale);

        //获取新的bitmap
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width > height ? height : width, width > height ? height : width, matrix, true);
        bitmap.getWidth();
        bitmap.getHeight();
        LogUtils.e("newWidth", "newWidth" + bitmap.getWidth());
        LogUtils.e("newHeight", "newHeight" + bitmap.getHeight());
        return bitmap;
    }


    /**
     * 第二种
     * 如果要截取的view 没有在屏幕上显示完全的，例如要截取的是超过一屏的 scrollview ，通过上面这个方法是获取不到bitmap的，
     * 需要使用下面方法，传的view 是scrollview 的子view（LinearLayout）等， 当然完全显示的view（第一种情况的view） 也可以使用这个方法截取。
     *
     * @param v
     * @return
     */
    public static Bitmap createBitmap2(View v) {
        Bitmap bmp = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);
        v.draw(c);
        return bmp;
    }

    /**
     * 第三种
     * view完全没有显示在界面上，通过inflate 转化的view，这时候通过 DrawingCache 是获取不到bitmap 的，也拿不到view 的宽高，以上两种方法都是不可行的。
     * 第三种方法通过measure、layout 去获得view 的实际尺寸。
     * View view = LayoutInflater.from(this).inflate(R.layout.view_inflate, null, false);
     * //这里传值屏幕宽高，得到的视图即全屏大小
     * createBitmap3(view, getScreenWidth(), getScreenHeight());
     *
     * @param v
     * @param width
     * @param height
     * @return
     */
    public static Bitmap createBitmap3(View v, int width, int height) {
        //测量使得view指定大小
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        v.measure(measuredWidth, measuredHeight);
        //调用layout方法布局后，可以得到view的尺寸大小
        v.layout(v.getLeft(), v.getTop(), v.getMeasuredWidth(), v.getMeasuredHeight());
//        Bitmap bmp = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.RGB_565);
        Bitmap bmp = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);
        v.draw(c);
        return bmp;
    }

    /**
     * 第三种
     * view完全没有显示在界面上，通过inflate 转化的view，这时候通过 DrawingCache 是获取不到bitmap 的，也拿不到view 的宽高，以上两种方法都是不可行的。
     * 第三种方法通过measure、layout 去获得view 的实际尺寸。
     * View view = LayoutInflater.from(this).inflate(R.layout.view_inflate, null, false);
     * //这里传值屏幕宽高，得到的视图即全屏大小
     * createBitmap3(view, getScreenWidth(), getScreenHeight());
     *
     * @param v
     * @param width
     * @param height
     * @return
     */
    public static Bitmap createBitmap3(View v, int left, int top, int width, int height) {
        v.setVisibility(View.INVISIBLE);
        //测量使得view指定大小
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        v.measure(measuredWidth, measuredHeight);
        //调用layout方法布局后，可以得到view的尺寸大小
        v.layout(left, top, v.getMeasuredWidth(), v.getMeasuredHeight());
//        Bitmap bmp = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.RGB_565);
        Bitmap bmp = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);
        v.draw(c);
        return bmp;
    }

    /**
     * 保存bitmap
     *
     * @param bitmap
     */
    public static String saveBitmap(Context context, Bitmap bitmap) {
        FileOutputStream fos;
        String path = "";
        try {
            String fileName = System.currentTimeMillis() + ".jpg";
            String appName = context.getResources().getString(R.string.app_name);
            File root = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "SAIHUBshare");
            if (!root.exists()) {
                root.mkdir();
            }
            //Log.d("BitmapUtil:","root==:"+root.getAbsolutePath());

            File file;
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + fileName);
                // 通过 MediaStore API 插入file 为了拿到系统裁剪要保存到的uri（因为App没有权限不能访问公共存储空间，需要通过 MediaStore API来操作）
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//                uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                file = new File(root, fileName);
//                uri = Uri.parse("file://" + root + "/" + fileName);
            }
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            path = file.getPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    public static File getCropFile(Context context, Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);

        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String path = cursor.getString(columnIndex);
            cursor.close();
            return new File(path);
        }


        return null;
    }

    public static Bitmap getBitmapByres(Context context, int resDraw) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resDraw, null);
        Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
        return bitmap;
    }

    public static Bitmap getBitmapByres2(Context context, int resDraw) {
        Bitmap bitmap = ((BitmapDrawable) context.getResources().getDrawable(resDraw)).getBitmap();
        return bitmap;
    }

    public static Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    /**
     * 读取一个缩放后的图片，限定图片大小，避免OOM
     *
     * @param uri       图片uri，支持“file://”、“content://”
     * @param maxWidth  最大允许宽度
     * @param maxHeight 最大允许高度
     * @return 返回一个缩放后的Bitmap，失败则返回null
     */
    public static Bitmap decodeUri(Context context, Uri uri, int maxWidth, int maxHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //只读取图片尺寸
        resolveUri(context, uri, options);

        //计算实际缩放比例
        int scale = 1;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if ((options.outWidth / scale > maxWidth &&
                    options.outWidth / scale > maxWidth * 1.4) ||
                    (options.outHeight / scale > maxHeight &&
                            options.outHeight / scale > maxHeight * 1.4)) {
                scale++;
            } else {
                break;
            }
        }

        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;//读取图片内容
        options.inPreferredConfig = Bitmap.Config.RGB_565; //根据情况进行修改
        Bitmap bitmap = null;
        try {
            bitmap = resolveUriForBitmap(context, uri, options);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    private static void resolveUri(Context context, Uri uri, BitmapFactory.Options options) {
        if (uri == null) {
            return;
        }

        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_CONTENT.equals(scheme) ||
                ContentResolver.SCHEME_FILE.equals(scheme)) {
            InputStream stream = null;
            try {
                stream = context.getContentResolver().openInputStream(uri);
                BitmapFactory.decodeStream(stream, null, options);
            } catch (Exception e) {
                Log.w("resolveUri", "Unable to open content: " + uri, e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Log.w("resolveUri", "Unable to close content: " + uri, e);
                    }
                }
            }
        } else if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            Log.w("resolveUri", "Unable to close content: " + uri);
        } else {
            Log.w("resolveUri", "Unable to close content: " + uri);
        }
    }

    private static Bitmap resolveUriForBitmap(Context context, Uri uri, BitmapFactory.Options options) {
        if (uri == null) {
            return null;
        }

        Bitmap bitmap = null;
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_CONTENT.equals(scheme) ||
                ContentResolver.SCHEME_FILE.equals(scheme)) {
            InputStream stream = null;
            try {
                stream = context.getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(stream, null, options);
            } catch (Exception e) {
                Log.w("resolveUriForBitmap", "Unable to open content: " + uri, e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Log.w("resolveUriForBitmap", "Unable to close content: " + uri, e);
                    }
                }
            }
        } else if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            Log.w("resolveUriForBitmap", "Unable to close content: " + uri);
        } else {
            Log.w("resolveUriForBitmap", "Unable to close content: " + uri);
        }

        return bitmap;
    }


}
