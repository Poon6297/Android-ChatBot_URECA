package com.example.poon6.ureca_shared_v1.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.poon6.ureca_shared_v1.Model.Card;
import com.example.poon6.ureca_shared_v1.R;

import java.util.ArrayList;
import java.util.List;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.CardViewHolder> {

    private RequestManager mRequestManager;
    private List<Card> mDataSet = new ArrayList<>();

    public ChildAdapter(RequestManager requestManager) {
        this.mRequestManager = requestManager;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.bind(mDataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public class CardViewHolder extends ParentAdapter.BaseViewHolder<Card> {

        private ImageView image;
        private TextView title;

        public CardViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
        }

        @Override
        public void bind(Card data) {
            mRequestManager.load(data.getUri())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(image);
            title.setText(data.getTitle());
        }
    }

    public void add(List<Card> dataSet) {
        mDataSet.addAll(dataSet);
    }
}
