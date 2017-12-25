package ru.radiationx.anilibria.ui.fragments.release.details;
/* Created by radiationx on 18.11.17. */

import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;

import ru.radiationx.anilibria.R;
import ru.radiationx.anilibria.entity.app.release.ReleaseFull;
import ru.radiationx.anilibria.ui.adapters.BaseAdapter;
import ru.radiationx.anilibria.ui.adapters.BaseViewHolder;

public class ReleaseAdapter extends BaseAdapter<ReleaseFull, BaseViewHolder> {
    private static final int RELEASE_HEAD_LAYOUT = 1;
    private static final int RELEASE_EPISODE_LAYOUT = 2;
    private ReleaseListener releaseListener;
    private ReleaseFull release;
    private ColorFilter accentFilter;
    private int accentColor = 0;
    private int tagColor = 0;
    private int tagColorPress = 0;
    private int tagColorText = 0;
    private float tagRadius = 0;

    public void setReleaseListener(ReleaseListener releaseListener) {
        this.releaseListener = releaseListener;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        tagColor = ContextCompat.getColor(recyclerView.getContext(), R.color.release_tag_color);
        tagColorPress = ContextCompat.getColor(recyclerView.getContext(), R.color.release_tag_color_press);
        tagColorText = ContextCompat.getColor(recyclerView.getContext(), R.color.white);
        tagRadius = 2 * recyclerView.getContext().getResources().getDisplayMetrics().density;
        accentColor = ContextCompat.getColor(recyclerView.getContext(), R.color.colorAccent);
        accentFilter = new PorterDuffColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
    }

    public void setRelease(ReleaseFull release) {
        Log.d("SUKA", "ADAPTER SET RELEASE " + release.getEpisodes().size());
        this.release = release;
    }

    @Override
    public int getItemCount() {
        if (release == null) {
            return 0;
        }
        return 1 + release.getEpisodes().size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return RELEASE_HEAD_LAYOUT;
        }
        return RELEASE_EPISODE_LAYOUT;
    }

    @Override
    public ReleaseFull getItem(int position) {
        return release;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case RELEASE_HEAD_LAYOUT: {
                return new ReleaseHeadHolder(inflateLayout(parent, R.layout.item_release_head_new));
            }
            case RELEASE_EPISODE_LAYOUT: {
                return new EpisodeItemHolder(inflateLayout(parent, R.layout.item_release_episode));
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case RELEASE_HEAD_LAYOUT: {
                ((ReleaseHeadHolder) holder).bind(release);
                break;
            }
            case RELEASE_EPISODE_LAYOUT: {
                int index = position - 1;
                ((EpisodeItemHolder) holder).bind(release.getEpisodes().get(index));
                break;
            }
        }

    }

    class ReleaseHeadHolder extends BaseViewHolder<ReleaseFull> {
        ImageView image;
        TextView title;
        TextView desc;
        TextView info;
        Button torrentButton;
        Button watchAllButton;
        ProgressBar imageProgress;
        TagView tagContainer;


        ReleaseHeadHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.full_image);
            title = itemView.findViewById(R.id.full_title);
            desc = itemView.findViewById(R.id.full_description);
            info = itemView.findViewById(R.id.full_info);
            torrentButton = itemView.findViewById(R.id.full_button_torrent);
            watchAllButton = itemView.findViewById(R.id.full_button_watch_all);
            imageProgress = itemView.findViewById(R.id.full_image_progress);
            tagContainer = itemView.findViewById(R.id.full_tags);
            torrentButton.setOnClickListener(v -> {
                if (releaseListener != null) {
                    releaseListener.onClickTorrent(release.getTorrentLink());
                }
            });
            tagContainer.setOnTagClickListener((tag, i) -> {
                if (releaseListener != null) {
                    releaseListener.onClickTag(tag.text);
                }
            });
            watchAllButton.setOnClickListener(view -> {
                if (releaseListener != null) {
                    releaseListener.onClickWatchAll();
                }
            });
        }

        @Override
        public void bind(ReleaseFull release) {
            /*ImageLoader.getInstance().displayImage(release.getImage(), image, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    imageProgress.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    imageProgress.setVisibility(View.GONE);
                }
            });*/
            title.setText(release.getTitle());
            desc.setText(Html.fromHtml(release.getDescription()));
            torrentButton.setEnabled(true);
            if (tagContainer.getTags().isEmpty()) {
                for (String genre : release.getGenres()) {
                    Tag tag = new Tag(genre);
                    tag.layoutColor = tagColor;
                    tag.layoutColorPress = tagColorPress;
                    tag.tagTextColor = tagColorText;
                    tag.radius = tagRadius;
                    tagContainer.addTag(tag);
                }
            }

            String original = release.getOriginalTitle();
            String seasonsHtml = "<b>Сезон:</b> " + TextUtils.join(", ", release.getSeasons());
            String voicesHtml = "<b>Голоса:</b> " + TextUtils.join(", ", release.getVoices());
            String typesHtml = "<b>Тип:</b> " + TextUtils.join(", ", release.getTypes());
            String[] arrHtml = {original, seasonsHtml, voicesHtml, typesHtml};
            String html = TextUtils.join("<br>", arrHtml);
            info.setText(Html.fromHtml(html));

            boolean hasEpisodes = !release.getEpisodes().isEmpty();
            boolean hasMoonwalk = release.getMoonwalkLink() != null;
            watchAllButton.setEnabled(hasEpisodes || hasMoonwalk);

            watchAllButton.setVisibility(hasEpisodes || hasMoonwalk ? View.VISIBLE : View.GONE);
        }
    }

    class EpisodeItemHolder extends BaseViewHolder<ReleaseFull.Episode> {
        TextView title;
        ImageButton qualitySd;
        ImageButton qualityHd;

        EpisodeItemHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            qualitySd = itemView.findViewById(R.id.quality_sd);
            qualityHd = itemView.findViewById(R.id.quality_hd);
            qualitySd.setColorFilter(accentFilter);
            qualityHd.setColorFilter(accentFilter);
            qualitySd.setOnClickListener(v -> {
                if (releaseListener != null) {
                    releaseListener.onClickSd(release.getEpisodes().get(getLayoutPosition() - 1), getLayoutPosition() - 1);
                }
            });
            qualityHd.setOnClickListener(v -> {
                if (releaseListener != null) {
                    releaseListener.onClickHd(release.getEpisodes().get(getLayoutPosition() - 1), getLayoutPosition() - 1);
                }
            });
            itemView.setOnClickListener(view -> {
                if (releaseListener != null) {
                    releaseListener.onClickEpisode(release.getEpisodes().get(getLayoutPosition() - 1), getLayoutPosition() - 1);
                }
            });
        }

        @Override
        public void bind(ReleaseFull.Episode item) {
            title.setText(item.getTitle());
        }
    }

    interface ReleaseListener {
        void onClickSd(ReleaseFull.Episode episode, int position);

        void onClickHd(ReleaseFull.Episode episode, int position);

        void onClickEpisode(ReleaseFull.Episode episode, int position);

        void onClickTorrent(String url);

        void onClickTag(String text);

        void onClickWatchAll();
    }
}
