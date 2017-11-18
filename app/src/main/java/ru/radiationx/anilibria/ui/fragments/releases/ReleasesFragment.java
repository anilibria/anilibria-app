package ru.radiationx.anilibria.ui.fragments.releases;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;

import java.util.ArrayList;

import ru.radiationx.anilibria.R;
import ru.radiationx.anilibria.data.api.releases.ReleaseItem;
import ru.radiationx.anilibria.ui.fragments.BaseFragment;

/* Created by radiationx on 05.11.17. */

public class ReleasesFragment extends BaseFragment implements ReleasesView, ReleasesAdapter.ItemListener {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private ReleasesAdapter adapter;
    @InjectPresenter/*(tag = "ReleasesTag", type = PresenterType.GLOBAL)*/
            ReleasesPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("SUKA", "onCreate: " + this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("SUKA", "onDestroy: " + this);
    }

    @Override
    public void onCreateView(LayoutInflater inflater, @Nullable Bundle savedInstanceState) {
        baseInflateFragment(inflater, R.layout.fragment_releases);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.e("SUKA", "onViewCreated");
        toolbar.setTitle(R.string.fragment_title_releases);
        Log.e("SUKA", "onViewCreated title " + toolbar.getTitle());
        refreshLayout = view.findViewById(R.id.swipe_refresh);
        recyclerView = view.findViewById(R.id.recycler_view);
        adapter = new ReleasesAdapter(getMvpDelegate());
        adapter.setListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

        refreshLayout.setOnRefreshListener(() -> {
            Log.e("SUKA", "setOnRefreshListener");
            presenter.refreshReleases();
        });
        toolbar.getMenu().add("Поиск")
                .setIcon(R.drawable.ic_toolbar_search)
                .setOnMenuItemClickListener(item -> {
                    Toast.makeText(getContext(), "Временно не поддерживается", Toast.LENGTH_SHORT).show();
                    return false;
                })
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public void showReleases(ArrayList<ReleaseItem> releases) {
        Log.e("SUKA", "showReleases");
        adapter.bindItems(releases);
    }

    @Override
    public void insertMore(ArrayList<ReleaseItem> releases) {
        Log.e("SUKA", "insertMore");
        adapter.insertMore(releases);
    }

    @Override
    public void onLoadMore() {
        Log.e("SUKA", "onLoadMore");
        presenter.loadMore();
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        refreshLayout.setRefreshing(refreshing);
    }

    @Override
    public void onItemClick(ReleaseItem item) {
        presenter.onItemClick(item);
    }

    @Override
    public boolean onItemLongClick(ReleaseItem item) {
        return presenter.onItemLongClick(item);
    }
}
