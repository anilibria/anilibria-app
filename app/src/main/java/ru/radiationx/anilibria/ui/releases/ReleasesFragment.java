package ru.radiationx.anilibria.ui.releases;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ru.radiationx.anilibria.R;
import ru.radiationx.anilibria.data.api.releases.ReleaseItem;

/**
 * Created by radiationx on 05.11.17.
 */

public class ReleasesFragment extends Fragment implements ReleasesContract.View, ReleaseAdapter.ItemListener {
    private final static int START_PAGE = 1;
    private int currentPage = START_PAGE;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private ReleasesContract.Presenter presenter;
    private ReleaseAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new ReleasesPresenter(this);
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
        presenter.onCreate(this);
        adapter = new ReleaseAdapter();
        adapter.setListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        presenter.getReleases(currentPage);

        refreshLayout.setOnRefreshListener(() -> {
            currentPage = START_PAGE;
            presenter.getReleases(currentPage);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onCreate(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public void showReleases(ArrayList<ReleaseItem> releases) {
        if (currentPage == START_PAGE) {
            adapter.addAll(releases);
        } else {
            adapter.insertMore(releases);
        }
    }

    @Override
    public void onLoadMore() {
        currentPage++;
        presenter.getReleases(currentPage);
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        refreshLayout.setRefreshing(refreshing);
    }
}
