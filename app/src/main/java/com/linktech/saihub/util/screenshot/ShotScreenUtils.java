/*
 * *******************************************************************
 *   @项目名称: BHex Android
 *   @文件名称: ShotScreenUtils.java
 *   @Date: 19-5-23 下午5:46
 *   @Author: ppzhao
 *   @Description:
 *   @Copyright（C）: 2019 BlueHelix Inc.   All rights reserved.
 *   注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的.
 *  *******************************************************************
 */

package com.linktech.saihub.util.screenshot;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.linktech.saihub.util.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShotScreenUtils {


    /**
     * Activity截图（带空白的状态栏）
     *
     * @param activity
     * @return
     */
    public static Bitmap shotScreen(Activity activity) {
        try {
            View view = activity.getWindow().getDecorView();
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            view.setDrawingCacheEnabled(false);
            view.destroyDrawingCache();
            return bitmap;
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
            return null;
        }
    }

    /**
     * Activity截图（去掉状态栏）
     *
     * @param activity
     * @return
     */
    public static Bitmap shotActivityNoBar(Activity activity) {
        // 获取windows中最顶层的view
        View view = activity.getWindow().getDecorView();
        view.buildDrawingCache();

        // 获取状态栏高度
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeights = rect.top;
        Display display = activity.getWindowManager().getDefaultDisplay();

        // 获取屏幕宽和高
        int widths = display.getWidth();
        int heights = display.getHeight();

        // 允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);

        // 去掉状态栏
        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0,
                statusBarHeights, widths, heights - statusBarHeights);

        // 销毁缓存信息
        view.destroyDrawingCache();

        return bmp;
    }

    /**
     * Fragment截图
     *
     * @param fragment
     * @return
     */
    public static Bitmap getFragmentBitmap(Fragment fragment) {
        View v = fragment.getView();
        v.buildDrawingCache(false);
        return v.getDrawingCache();
    }

    /**
     * 绘制View的方式只适用于静态View，SurfaceView、IjkVideoView等播放视频的View，不能截取到；
     * 此外，某些动画类View也不能截取到。针对上述情况，可获取SurfaceView的一帧生成bitmap，将bitmap进行合并，同理动画类View也可以合并。
     * 例如，最底层播放视频、中间层播放动画、最上层是普通的view，可将bitmap合并
     *
     * @param background
     * @param midBitmap
     * @param foreground
     * @return
     */
    public static Bitmap combineBitmapInCenter(Bitmap background, Bitmap midBitmap, Bitmap foreground) {
        if (!background.isMutable()) {
            background = background.copy(Bitmap.Config.ARGB_8888, true);
        }
        Paint paint = new Paint();
        Canvas canvas = new Canvas(background);
        int bw = background.getWidth();
        int bh = background.getHeight();

        int mw = midBitmap.getWidth();
        int mh = midBitmap.getHeight();
        int mx = (mw - bw) / 2;
        int my = (mh - bh) / 2;
        canvas.drawBitmap(midBitmap, mx, my, paint);

        int fw = foreground.getWidth();
        int fh = foreground.getHeight();
        int fx = (fw - bw) / 2;
        int fy = (fh - bh) / 2;
        canvas.drawBitmap(foreground, fx, fy, paint);

//        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.save();
        canvas.restore();
        return background;
    }

    /**
     * 保存bitmap到本地
     *
     * @param saveFileName
     * @param bitmap
     */
    public static void saveBitmapToFile(String saveFileName, Bitmap bitmap) {

        if (TextUtils.isEmpty(saveFileName) || bitmap == null) {
            return;
        }

        try {
            File f = new File(saveFileName);
            f.createNewFile();
            FileOutputStream fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            LogUtils.i("ScreenShotUtil", "保存失败");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通知系统相册更新
     *
     * @param context
     * @param fileName
     */
    public static void AlbumScan(Context context, String fileName) {
        MediaScannerConnection.scanFile(context.getApplicationContext(), new String[]{fileName}, new String[]{"image/*"}, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
//                ToastUtils.showShort(path+" : "+uri.getPath());
            }
        });
    }

}
