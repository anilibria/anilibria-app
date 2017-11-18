package ru.radiationx.anilibria.ui.fragments;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.arellomobile.mvp.MvpAppCompatFragment;

import java.lang.reflect.Field;

import ru.radiationx.anilibria.R;

/* Created by radiationx on 18.11.17. */

public class BaseFragment extends MvpAppCompatFragment {
    protected CoordinatorLayout coordinatorLayout;
    protected AppBarLayout appBarLayout;
    protected CollapsingToolbarLayout toolbarLayout;
    protected Toolbar toolbar;
    protected ViewGroup fragmentContent;
    protected View view;

    //Pls no override
    @Nullable
    @Override
    @Deprecated
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main_base, container, false);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fragmentContent = (ViewGroup) findViewById(R.id.fragment_content);
        onCreateView(inflater, savedInstanceState);
        return view;
    }

    /* Override and call baseInflateFragment */
    public void onCreateView(LayoutInflater inflater, @Nullable Bundle savedInstanceState) {
    }

    protected void fixToolbarInsets() {
        toolbar.setContentInsetStartWithNavigation(0);
        toolbar.setContentInsetEndWithActions(0);
        toolbar.setContentInsetsAbsolute(0, 0);
    }

    protected void setMarqueeTitle() {
        try {
            TextView toolbarTitleView;
            Field field = toolbar.getClass().getDeclaredField("mTitleTextView");
            field.setAccessible(true);
            toolbarTitleView = (TextView) field.get(toolbar);

            toolbarTitleView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            toolbarTitleView.setHorizontallyScrolling(true);
            toolbarTitleView.setMarqueeRepeatLimit(3);
            toolbarTitleView.setSelected(true);
            toolbarTitleView.setHorizontalFadingEdgeEnabled(true);
            toolbarTitleView.setFadingEdgeLength((int) (getContext().getResources().getDisplayMetrics().density * 8));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void baseInflateFragment(LayoutInflater inflater, @LayoutRes int res) {
        inflater.inflate(res, fragmentContent, true);
    }

    public final <T extends View> T  findViewById(@IdRes int id) {
        return view.findViewById(id);
    }
}
