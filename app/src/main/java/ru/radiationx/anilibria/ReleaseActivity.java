package ru.radiationx.anilibria;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.radiationx.anilibria.data.api.Api;
import ru.radiationx.anilibria.data.api.release.FullRelease;
import ru.radiationx.anilibria.data.api.release.Release;

/**
 * Created by radiationx on 05.11.17.
 */

public class ReleaseActivity extends AppCompatActivity {
    ImageView image;
    TextView title;
    TextView desc;
    TextView info;
    int id = 5207;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release);
        id = getIntent().getIntExtra("release_id", 5207);
        image = findViewById(R.id.full_image);
        title = findViewById(R.id.full_title);
        desc = findViewById(R.id.full_description);
        info = findViewById(R.id.full_info);
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

        String seasonsHtml = "<b>Сезон:</b> " + TextUtils.join(", ", release.getSeasons());
        String voicesHtml = "<b>Голоса:</b> " + TextUtils.join(", ", release.getVoices());
        String typesHtml = "<b>Тип:</b> " + TextUtils.join(", ", release.getTypes());
        String[] arrHtml = {seasonsHtml, voicesHtml, typesHtml};
        String html = TextUtils.join("<br>", arrHtml);
        info.setText(Html.fromHtml(html));
    }
}
