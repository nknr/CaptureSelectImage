package com.example.pickfile.view.adapter;

import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;

public class ImageBindAdapter {

    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, String url) {
        if (url != null)
            Glide.with(view.getContext()).load(url).into(view);
    }
}
