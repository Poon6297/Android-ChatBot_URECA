package com.example.poon6.ureca_shared_v1.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.poon6.ureca_shared_v1.Model.Card;
import com.example.poon6.ureca_shared_v1.Model.ChatMessage;
import com.example.poon6.ureca_shared_v1.Model.QuickReply;
import com.example.poon6.ureca_shared_v1.R;

import java.util.ArrayList;
import java.util.List;

import ai.api.model.ResponseMessage;

public class ParentAdapter extends RecyclerView.Adapter<ParentAdapter.BaseViewHolder> {

    private static final String ERROR_TAG = "error";

    private static final int MY_MESSAGE = 0;
    private static final int BOT_MESSAGE = 1;
    private static final int BOT_QUICK_REPLY = 2;
    private static final int BOT_CARD = 3;

    private Context mContext;
    private View.OnClickListener quickReplyListener;

    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

    private List<Object> mDataSet = new ArrayList<>();

    //  to record the position of quick reply view in mDataSet
    private int quickReplyPosition;

    public ParentAdapter(Context context, View.OnClickListener quickReplyListener) {
        this.mContext = context;
        this.quickReplyListener = quickReplyListener;
        this.quickReplyPosition = -1;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        BaseViewHolder vh = null;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case MY_MESSAGE:
                v = layoutInflater.inflate(R.layout.user_query_layout, parent, false);
                vh = new SpeechViewHolder(v);
                break;
            case BOT_MESSAGE:
                v = layoutInflater.inflate(R.layout.bot_reply_layout, parent, false);
                vh = new SpeechViewHolder(v);
                break;
            case BOT_QUICK_REPLY:
                v = layoutInflater.inflate(R.layout.quick_reply_layout, parent, false);
                vh = new QuickReplyViewHolder(v);
                break;
            case BOT_CARD:
                v = layoutInflater.inflate(R.layout.card_list_layout, parent, false);
                vh = new CardListViewHolder(v);
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.bind(mDataSet.get(position));
    }


    @Override
    public int getItemViewType(int position) {
        int viewType = -1;
        if (mDataSet.get(position) instanceof ChatMessage) {
            ChatMessage item = (ChatMessage) mDataSet.get(position);

            if (item.isMine() && !item.isImage()) {
                viewType = MY_MESSAGE;
            } else {
                viewType = BOT_MESSAGE;
            }
        } else if (mDataSet.get(position) instanceof QuickReply) {
            viewType = BOT_QUICK_REPLY;
        } else if (mDataSet.get(position) instanceof  List) {
            viewType = BOT_CARD;
        }
        return viewType;
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public static abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {
        public BaseViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(T data);
    }

    public class SpeechViewHolder extends BaseViewHolder<ChatMessage> {

        private TextView mTextView;

        public SpeechViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.text);
        }

        @Override
        public void bind(ChatMessage data) {
            mTextView.setText(data.getContent());
        }
    }

    public class QuickReplyViewHolder extends BaseViewHolder<QuickReply> {

        private Button mBtn1;
        private Button mBtn2;
        private Button mBtn3;

        public QuickReplyViewHolder(View itemView) {
            super(itemView);
            mBtn1 = itemView.findViewById(R.id.btn1);
            mBtn2 = itemView.findViewById(R.id.btn2);
            mBtn3 = itemView.findViewById(R.id.btn3);
        }

        @Override
        public void bind(QuickReply data) {
            mBtn1.setText(data.getButton1());
            mBtn2.setText(data.getButton2());
            mBtn3.setText(data.getButton3());

            mBtn1.setOnClickListener(quickReplyListener);
            mBtn2.setOnClickListener(quickReplyListener);
            mBtn3.setOnClickListener(quickReplyListener);
        }
    }

    public class CardListViewHolder extends BaseViewHolder<List<Card>> {

        private RecyclerView cardListHolder;
        private LinearLayoutManager layoutManager;
        private ChildAdapter childAdapter;

        public CardListViewHolder(View itemView) {
            super(itemView);

            cardListHolder = itemView.findViewById(R.id.horizontal_rv);
            layoutManager = new LinearLayoutManager(cardListHolder.getContext(), LinearLayoutManager.HORIZONTAL, false);
            childAdapter = new ChildAdapter(Glide.with(mContext));

            cardListHolder.setLayoutManager(layoutManager);
            cardListHolder.setAdapter(childAdapter);
            cardListHolder.setRecycledViewPool(viewPool);
            layoutManager.setInitialPrefetchItemCount(4);
        }

        @Override
        public void bind(List<Card> data) {
            childAdapter.add(data);
        }
    }



    public void add(boolean isImage, boolean isMine, final String data) {
        ChatMessage chatMessage = new ChatMessage(isImage, isMine, data);
        mDataSet.add(chatMessage);
        notifyDataSetChanged();
    }

    public void add(List<String> dataList) {
        if (quickReplyPosition != -1) {
            mDataSet.remove(quickReplyPosition);
        }
        quickReplyPosition = mDataSet.size();

        QuickReply quickReply = new QuickReply(dataList);
        mDataSet.add(quickReply);
        notifyDataSetChanged();
    }

    public void addCard(List<ResponseMessage.ResponseCard> dataList) {
        List<Card> cardList = new ArrayList<>();

        for (ResponseMessage.ResponseCard data : dataList) {
            Card card = new Card(data.getImageUrl(), data.getTitle());
            cardList.add(card);
        }

        mDataSet.add(cardList);
        notifyDataSetChanged();
    }

    public void removeQuickReplyView() {
        mDataSet.remove(quickReplyPosition);
        notifyDataSetChanged();
        this.quickReplyPosition = -1;
    }
}
