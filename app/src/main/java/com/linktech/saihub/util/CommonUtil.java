package com.linktech.saihub.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.TextUtils;
import android.view.View;

import com.linktech.saihub.R;
import com.linktech.saihub.manager.wallet.btc.Base58Check;


public class CommonUtil {

    public static String getChannel(Context context) {
        //return getAppMetaData(context, "UMENG_CHANNEL");
        String channel = "official";
//        String channel = WalleChannelReader.getChannel(context);
        if (TextUtils.isEmpty(channel)) {
            channel = "official";
        }
        return channel;
    }

    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            // 当前应用的版本名称
            versionName = info.versionName;
            // 当前版本的版本号
            //int versionCode = info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }


    public static String getPackageName(Context context) {
        String packName = "";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            // 当前应用的版本名称
            //			String versionName = info.versionName;
            // 当前版本的版本号
            //			int versionCode = info.versionCode;
            // 当前版本的包名
            packName = info.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packName;
    }


    /**
     * 复制文本
     *
     * @param context
     * @param copyContent
     */
    public static void copyText(Context context, String copyContent) {
        if (!TextUtils.isEmpty(copyContent)) {
            // 得到剪贴板管理器
            ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setText(copyContent);
            ToastUtils.INSTANCE.shortToast(context.getString(R.string.content_copy));
        }
    }

    public static void copyText(Context context, String copyContent, String toastStr) {
        if (!TextUtils.isEmpty(copyContent)) {
            // 得到剪贴板管理器
            ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            //把文本封装到ClipData中
            ClipData clip = ClipData.newPlainText("simple text", copyContent);
            // Set the clipboard's primary clip.
            cmb.setPrimaryClip(clip);
//            cmb.setText(copyContent);
            ToastUtils.INSTANCE.shortToast(toastStr);
        }
    }

    /**
     * 粘贴
     *
     * @param context
     * @return
     */
    public static String pasteText(Context context) {
        final ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = cm.getPrimaryClip();
        //  ClipData 里保存了一个ArryList 的 Item 序列， 可以用 getItemCount() 来获取个数
        if (data != null && data.getItemCount() > 0) {
            ClipData.Item item = data.getItemAt(0);
            return item.getText().toString();// 注意 item.getText 可能为空
        } else {
            return "";
        }
    }

    /**
     * 按矩形等比裁切图片
     */
    public static Bitmap ImageCropRect(Bitmap bitmap, View rl) {
        try {
            int w = bitmap.getWidth(); // 得到图片的宽，高
            int h = bitmap.getHeight();
            int rw = rl.getMeasuredWidth();
            int rh = rl.getMeasuredHeight();
            if (w * h * rw * rh == 0) {
                return null;
            }
            float ww = 0, wh = 0;
            if (1f * w / h > 1f * rw / rh) {
                wh = h;
                ww = 1f * rw / rh * h;
            } else if (1f * w / h < 1f * rw / rh) {
                ww = w;
                wh = 1f * rh / rw * w;
            } else {
                ww = w;
                wh = h;
            }

            float retX = 0, retY = 0;
            retX = (w - ww) / 2;
            retY = (h - wh) / 2;

            //下面这句是关键
            return Bitmap.createBitmap(bitmap, (int) retX, (int) retY, (int) ww, (int) wh, null, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 切对应高度的图片
     */
    public static Bitmap ImageCropHeightRect(Bitmap bitmap, int mainH, int tabH) {
        try {
            int w = bitmap.getWidth(); // 得到图片的宽，高
            int h = bitmap.getHeight();

            //下面这句是关键
            return Bitmap.createBitmap(bitmap, 0, mainH, w, tabH, null, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 把一个bitmap压缩，压缩到指定大小
     *
     * @param bm
     * @param width
     * @param height
     * @return
     */
    public static Bitmap scaleBitmap(Bitmap bm, float width, float height) {
        if (bm == null) {
            return null;
        }
        int bmWidth = bm.getWidth();
        int bmHeight = bm.getHeight();
        float scaleWidth = width / bmWidth;
        float scaleHeight = height / bmHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        if (scaleWidth == 1 && scaleHeight == 1) {
            return bm;
        } else {
            Bitmap resizeBitmap = Bitmap.createBitmap(bm, 0, 0, bmWidth,
                    bmHeight, matrix, false);
            bm.recycle();//回收图片内存
            bm.setDensity(240);
            return resizeBitmap;
        }
    }


    /**
     * @param
     * @描述 快速模糊化处理bitmap
     * @作者 tll
     * @时间 2016/12/5 19:22
     */
    public static Bitmap fastblur(Bitmap sentBitmap, int radius) {

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int temp = 256 * divsum;
        int dv[] = new int[temp];
        for (i = 0; i < temp; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
                        | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return (bitmap);
    }


    /**
     * 版本号比较
     * <p>
     * compareTo()方法返回值为int类型，就是比较两个值，如：x.compareTo(y)。如果前者大于后者，返回1，前者等于后者则返回0，前者小于后者则返回-1
     *
     * @param s1
     * @param s2
     * @return 范围可以是"2.20.", "2.10.0" ,".20","2.10.0",2.1.3 ，3.7.5，10.2.0
     */

    public static int compareVersion(String s1, String s2) {
        String[] s1Split = s1.split("\\.", -1);
        String[] s2Split = s2.split("\\.", -1);

        int len1 = s1Split.length;

        int len2 = s2Split.length;

        int lim = Math.min(len1, len2);

        int i = 0;

        while (i < lim) {
            int c1 = "".equals(s1Split[i]) ? 0 : Integer.parseInt(s1Split[i]);

            int c2 = "".equals(s2Split[i]) ? 0 : Integer.parseInt(s2Split[i]);

            if (c1 != c2) {
                return c1 - c2;
            }
            i++;

        }

        return len1 - len2;
    }

    public static byte[] byteMerger(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }

    /**
     * @param ypub
     * @return
     */
    public static String ypubToXpub(String ypub) {
        byte[] decode = Base58Check.base58ToBytes(ypub);
        byte[] bytes = new byte[decode.length - 4];
        System.arraycopy(decode, 4, bytes, 0, decode.length - 4);
        byte[] bytes1 = hexToByteArray("0488b21e");
        byte[] byteMerger = byteMerger(bytes1, bytes);
        return Base58Check.bytesToBase58(byteMerger);
    }


    public static byte[] hexToByteArray(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            //奇数
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            //偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }


    public static byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }
}



