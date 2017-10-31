package ru.radiationx.anilibria;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import ru.radiationx.anilibria.api.releases.ReleaseItem;

/**
 * Created by radiationx on 31.10.17.
 */

public class ReleaseAdapter extends BaseAdapter<ReleaseItem, ReleaseAdapter.DeviceItemHolder> {


    @Override
    public ReleaseAdapter.DeviceItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflateLayout(parent, R.layout.item_release);
        return new ReleaseAdapter.DeviceItemHolder(v);
    }

    @Override
    public void onBindViewHolder(ReleaseAdapter.DeviceItemHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    class DeviceItemHolder extends BaseViewHolder<ReleaseItem> {
        ImageView image;
        TextView title;
        TextView desc;

        DeviceItemHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_image);
            title = itemView.findViewById(R.id.item_title);
            desc = itemView.findViewById(R.id.item_desc);
        }

        @Override
        public void bind(ReleaseItem item, int position) {
            title.setText(item.getTitle());
            desc.setText(item.getDescription());
            ImageLoader.getInstance().displayImage("https://www.anilibria.tv/" + item.getImage(), image);
        }
    }
}
