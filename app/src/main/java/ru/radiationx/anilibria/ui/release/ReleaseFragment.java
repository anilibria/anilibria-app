package ru.radiationx.anilibria.ui.release;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.radiationx.anilibria.R;
import ru.radiationx.anilibria.data.api.Api;
import ru.radiationx.anilibria.data.api.release.FullRelease;

/**
 * Created by radiationx on 16.11.17.
 */

public class ReleaseFragment extends Fragment {
    ImageView image;
    TextView title;
    TextView desc;
    TextView info;
    int id = 5207;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("SUKA", "onCreate: " + this);
        Bundle arguments = getArguments();
        if (arguments != null) {
            id = arguments.getInt("release_id");
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
        Api.get().Release().getRelease(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onload);
    }

    private void onload(FullRelease release) {
        String imageUrl = "https://www.anilibria.tv/" + release.getImage();
        ImageLoader.getInstance().displayImage(imageUrl, image);
        title.setText(release.getTitle());
        desc.setText(release.getDescription());
        info.setText("palehchi paren'");

        String original = release.getOriginalTitle();
        String seasonsHtml = "<b>Сезон:</b> " + TextUtils.join(", ", release.getSeasons());
        String voicesHtml = "<b>Голоса:</b> " + TextUtils.join(", ", release.getVoices());
        String typesHtml = "<b>Тип:</b> " + TextUtils.join(", ", release.getTypes());
        String[] arrHtml = {original, seasonsHtml, voicesHtml, typesHtml};
        String html = TextUtils.join("<br>", arrHtml);
        info.setText(Html.fromHtml(html));
    }
}
