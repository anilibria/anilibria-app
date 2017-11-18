package ru.radiationx.anilibria.ui.fragments.release;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.nostra13.universalimageloader.core.ImageLoader;

import ru.radiationx.anilibria.R;
import ru.radiationx.anilibria.data.api.release.FullRelease;

/**
 * Created by radiationx on 16.11.17.
 */

public class ReleaseFragment extends MvpAppCompatFragment implements ReleaseView {
    public final static String ARG_ID = "release_id";
    private ImageView image;
    private TextView title;
    private TextView desc;
    private TextView info;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_release, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        image = view.findViewById(R.id.full_image);
        title = view.findViewById(R.id.full_title);
        desc = view.findViewById(R.id.full_description);
        info = view.findViewById(R.id.full_info);
        presenter.loadRelease(id);
    }


    @Override
    public void setRefreshing(boolean refreshing) {

    }

    @Override
    public void showRelease(FullRelease release) {
        ImageLoader.getInstance().displayImage(release.getImage(), image);
        title.setText(release.getTitle());
        desc.setText(release.getDescription());
    }

    @Override
    public void showRelease(FullRelease release, String infoText) {
        showRelease(release);
        info.setText(Html.fromHtml(infoText));
    }
}
