package ru.radiationx.anilibria.ui.fragments.release;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.nostra13.universalimageloader.core.ImageLoader;

import ru.radiationx.anilibria.App;
import ru.radiationx.anilibria.R;
import ru.radiationx.anilibria.Screens;
import ru.radiationx.anilibria.data.api.release.FullRelease;
import ru.radiationx.anilibria.ui.fragments.BaseFragment;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by radiationx on 16.11.17.
 */

public class ReleaseFragment extends BaseFragment implements ReleaseView {
    public final static String ARG_ID = "release_id";
    private ImageView image;
    private TextView title;
    private TextView desc;
    private TextView info;
    private Button torrentButton;
    private int id = -1;

    @InjectPresenter
    ReleasePresenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("SUKA", "onCreate: " + this);
        Bundle arguments = getArguments();
        if (arguments != null) {
            id = arguments.getInt(ARG_ID, id);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("SUKA", "onDestroy: " + this);
    }

    @Override
    public void onCreateView(LayoutInflater inflater, @Nullable Bundle savedInstanceState) {
        baseInflateFragment(inflater, R.layout.fragment_release);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setTitle(R.string.fragment_title_release);
        image = findViewById(R.id.full_image);
        title = findViewById(R.id.full_title);
        desc = findViewById(R.id.full_description);
        info = findViewById(R.id.full_info);
        torrentButton = findViewById(R.id.full_button_torrent);

        fixToolbarInsets();
        setMarqueeTitle();
        toolbar.setNavigationOnClickListener(v -> App.get().getRouter().backTo(Screens.RELEASES_LIST));
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow_back);
        toolbar.getMenu().add("Копировать ссылку");
        toolbar.getMenu().add("Поделиться");

        torrentButton.setEnabled(false);
        torrentButton.setOnClickListener(v -> presenter.onTorrentButtonClick());

        presenter.loadRelease(id);
    }


    @Override
    public void setRefreshing(boolean refreshing) {

    }

    @Override
    public void showRelease(FullRelease release) {
        toolbar.setTitle(String.format("%s / %s", release.getTitle(), release.getOriginalTitle()));
        ImageLoader.getInstance().displayImage(release.getImage(), image);
        title.setText(release.getTitle());
        desc.setText(release.getDescription());
        torrentButton.setEnabled(true);
    }

    @Override
    public void showRelease(FullRelease release, String infoText) {
        showRelease(release);
        info.setText(Html.fromHtml(infoText));
    }

    @Override
    public void startLoadTorrent(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(FLAG_ACTIVITY_NEW_TASK);
        App.get().startActivity(Intent.createChooser(intent, "Открыть в").addFlags(FLAG_ACTIVITY_NEW_TASK));
    }
}
