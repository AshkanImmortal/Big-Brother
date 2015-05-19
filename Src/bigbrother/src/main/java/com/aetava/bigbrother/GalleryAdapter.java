package com.aetava.bigbrother;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.aetava.bigbrother.model.Image;

public class GalleryAdapter extends ArrayAdapter<Image> {

    private final Context context;

    public GalleryAdapter(Context context) {
        super(context, 0);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.gallery_item, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.thumbnail_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Image image = getItem(position);

        if (holder.image == null || !holder.image.equals(image)) {
            ((PhotoActivity)context).mImageFetcher.loadImage(image.uri, holder.imageView);
            holder.image = image;
        }
        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        Image image;
    }
}


