package ru.radiationx.anilibria.ui.releases;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.PresenterType;

import java.util.ArrayList;

import ru.radiationx.anilibria.App;
import ru.radiationx.anilibria.R;
import ru.radiationx.anilibria.data.api.releases.ReleaseItem;

/**
 * Created by radiationx on 05.11.17.
 */

public class ReleasesFragment extends MvpAppCompatFragment implements ReleaseView, ReleaseAdapter.ItemListener {
    private final static int START_PAGE = 1;
    private int currentPage = START_PAGE;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private ReleaseAdapter adapter;
    @InjectPresenter(tag = "ReleasesTag", type = PresenterType.GLOBAL)
    ReleasesPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("SUKA", "onCreate: "+this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("SUKA", "onDestroy: "+this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_releases, container, false);
        refreshLayout = view.findViewById(R.id.swipe_refresh);
        recyclerView = view.findViewById(R.id.recycler_view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        adapter = new ReleaseAdapter(getMvpDelegate());
        adapter.setListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

        refreshLayout.setOnRefreshListener(() -> {
            Log.e("SUKA", "setOnRefreshListener");
            currentPage = START_PAGE;
            presenter.getReleases(currentPage);
        });
    }

    @Override
    public void showReleases(ArrayList<ReleaseItem> releases) {
        Log.e("SUKA", "showReleases");
        if (currentPage == START_PAGE) {
            adapter.addAll(releases);
        } else {
            adapter.insertMore(releases);
        }
    }

    @Override
    public void onLoadMore() {
        Log.e("SUKA", "onLoadMore");
        currentPage++;
        presenter.getReleases(currentPage);
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        refreshLayout.setRefreshing(refreshing);
    }

    @Override
    public void onItemClick(ReleaseItem item) {
        Log.d("SUKA", "ON ITEM CLICK");
        Bundle args = new Bundle();
        args.putInt("release_id", item.getId());
        App.get().getRouter().navigateTo("ReleaseFragment", args);
    }

    @Override
    public boolean onItemLongClick(ReleaseItem item) {
        return false;
    }
}
