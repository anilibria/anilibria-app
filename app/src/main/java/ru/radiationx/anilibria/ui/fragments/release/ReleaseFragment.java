package ru.radiationx.anilibria.ui.fragments.release;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.radiationx.anilibria.App;
import ru.radiationx.anilibria.R;
import ru.radiationx.anilibria.Screens;
import ru.radiationx.anilibria.data.api.release.FullRelease;
import ru.radiationx.anilibria.ui.fragments.BaseFragment;
import ru.radiationx.anilibria.utils.Utils;

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
    private ProgressBar imageProgress;
    private TagView tagContainer;
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
        imageProgress = findViewById(R.id.full_image_progress);
        tagContainer = findViewById(R.id.full_tags);

        fixToolbarInsets();
        setMarqueeTitle();
        toolbar.setNavigationOnClickListener(v -> App.get().getRouter().backTo(Screens.RELEASES_LIST));
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow_back);
        toolbar.getMenu().add("Копировать ссылку")
                .setOnMenuItemClickListener(item -> {
                    presenter.onCopyLinkClick();
                    return false;
                });
        toolbar.getMenu().add("Поделиться")
                .setOnMenuItemClickListener(item -> {
                    presenter.onShareClick();
                    return false;
                });

        torrentButton.setEnabled(false);
        torrentButton.setOnClickListener(v -> presenter.onTorrentClick());

        tagContainer.setOnTagClickListener((tag, i) -> {
            Toast.makeText(getContext(), "Временно не поддерживается", Toast.LENGTH_SHORT).show();
        });

        presenter.loadRelease(id);
    }


    @Override
    public void setRefreshing(boolean refreshing) {

    }

    @Override
    public void showRelease(FullRelease release) {
        toolbar.setTitle(String.format("%s / %s", release.getTitle(), release.getOriginalTitle()));
        ImageLoader.getInstance().displayImage(release.getImage(), image, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                imageProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                imageProgress.setVisibility(View.GONE);
            }
        });
        title.setText(release.getTitle());
        desc.setText(release.getDescription());
        torrentButton.setEnabled(true);
        /*String[] tagsArray = release.getGenres().toArray(new String[release.getGenres().size()]);
        tagContainer.addTags(tagsArray);*/
        for(String genre:release.getGenres()){
            Tag tag = new Tag(genre);
            tag.layoutColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);
            tag.layoutColorPress = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
            tag.tagTextColor = ContextCompat.getColor(getContext(), R.color.white);
            tag.radius = 10;
            tagContainer.addTag(tag);
        }
    }

    @Override
    public void showRelease(FullRelease release, String infoText) {
        showRelease(release);
        info.setText(Html.fromHtml(infoText));
    }

    @Override
    public void loadTorrent(String url) {
        Utils.externalLink(url);
    }

    @Override
    public void shareRelease(String text) {
        Utils.shareText(text);
    }

    @Override
    public void copyLink(String url) {
        Utils.externalLink(url);
    }
}
