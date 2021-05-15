package com.example.flickrfindr.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.flickrfindr.Model.PhotoItemModel;
import com.example.flickrfindr.R;
import com.hololo.library.photoviewer.PhotoViewer;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.ViewHolder> {
    Context context;

    boolean isPlaceHolder;

    ArrayList<PhotoItemModel> photoItemModelList;

    public MainActivityAdapter(Context context, boolean isPlaceHolder, ArrayList<PhotoItemModel> photoItemModelList) {
        this.context = context;
        this.isPlaceHolder = isPlaceHolder;
        this.photoItemModelList = photoItemModelList;
    }

    public void addItemsToAdapter(boolean isPlaceHolder, ArrayList<PhotoItemModel> photoItemModelList) {
        if(this.photoItemModelList == null) {
            this.photoItemModelList = new ArrayList<>();
        }

        int size = this.photoItemModelList.size();

        this.isPlaceHolder = isPlaceHolder;
        this.photoItemModelList.addAll(photoItemModelList);
        notifyItemRangeChanged(size, size + 25);
    }

    public void clearList() {
        if(photoItemModelList == null) {
            photoItemModelList = new ArrayList<>();
        }

        photoItemModelList.clear();

        notifyDataSetChanged();
    }

    @NotNull
    @Override
    public MainActivityAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view;

        if (isPlaceHolder) {
            view = LayoutInflater.from(context).inflate(R.layout.item_flickr_placeholder, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_flickr, parent, false);
        }

        return new MainActivityAdapter.ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NotNull MainActivityAdapter.ViewHolder holder, final int position) {
        if (isPlaceHolder) {
            return;
        }

        PhotoItemModel photoItemModel = photoItemModelList.get(position);

        Glide.with(context)
                .load(photoItemModel.getImageURL())
                .error(R.drawable.ic_error)
                .centerCrop()
                .into(holder.ivPhoto);

        holder.tvTitle.setText(photoItemModel.getTitle());

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(photoItemModel.getImageURL());

                new PhotoViewer.Builder(context)
                        .url(arrayList)
                        .build()
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (isPlaceHolder) {
            return 25;
        } else {
            if (photoItemModelList == null) {
                return 0;
            } else {
                return photoItemModelList.size();
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView ivPhoto;
        TextView tvTitle;

        public ViewHolder(View itemView, int itemType) {
            super(itemView);

            if (isPlaceHolder) {
                return;
            }

            view = itemView;
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}
