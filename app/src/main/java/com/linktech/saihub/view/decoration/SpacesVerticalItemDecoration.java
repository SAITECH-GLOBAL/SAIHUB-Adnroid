package com.linktech.saihub.view.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class SpacesVerticalItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public SpacesVerticalItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildPosition(view) != 0) {
            outRect.top = space;
        }
    }
}
