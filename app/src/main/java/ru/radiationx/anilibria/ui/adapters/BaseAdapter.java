package ru.radiationx.anilibria.ui.adapters;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.MvpDelegate;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by radiationx on 14.09.17.
 */

public abstract class BaseAdapter<E, VH extends BaseViewHolder> extends MvpBaseAdapter<VH> {
    protected ArrayList<E> items = new ArrayList<>();

    public BaseAdapter(MvpDelegate<?> parentDelegate, String childId) {
        super(parentDelegate, childId);
    }

    public void setItems(ArrayList<E> items) {
        clear();
        this.items = items;
    }

    public void bindItems(Collection<? extends E> items) {
        bindItems(items, true);
    }

    public void bindItems(Collection<? extends E> items, boolean clearList) {
        if (clearList)
            clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void clear() {
        items.clear();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public E getItem(int position) {
        return items.get(position);
    }

    protected View inflateLayout(ViewGroup parent, @LayoutRes int id) {
        return LayoutInflater.from(parent.getContext()).inflate(id, parent, false);
    }

    public interface OnItemClickListener<T> {
        void onItemClick(T item);

        boolean onItemLongClick(T item);
    }
}
