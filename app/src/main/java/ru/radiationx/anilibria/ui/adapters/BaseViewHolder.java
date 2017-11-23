package ru.radiationx.anilibria.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by radiationx on 14.09.17.
 */

public class BaseViewHolder<T> extends RecyclerView.ViewHolder {

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public void bind(T item, int position) {
    }

    public void bind(T item) {
    }

    public void bind(int position) {
    }

    public void bind() {
    }
}
