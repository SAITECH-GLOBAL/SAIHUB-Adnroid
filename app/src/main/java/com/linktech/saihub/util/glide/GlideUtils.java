package com.linktech.saihub.util.glide;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class GlideUtils {

    public static RequestOptions getErrorOptions(int errorRes) {
        return new RequestOptions()
                .skipMemoryCache(false)
                .placeholder(errorRes)
//                .placeholder(R.mipmap.icon_token_default)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .error(errorRes);
    }


    public static void loadMsg(Context context, String url, ImageView imageView, int errorRes) {
        Glide.with(context)
                .load(url)
                .apply(getErrorOptions(errorRes))
                .into(imageView);
    }
}
