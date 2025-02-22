package com.example.sm_project.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sm_project.Models.Message;
import com.example.sm_project.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private final Context mContext;
    private final List<Message> messages;
    private final String userId;

    private final onMessageClickListener onMessageClickListener;

    public MessageAdapter(Context mContext, List<Message> messages, String userId, onMessageClickListener onMessageClickListener){
        this.mContext = mContext;
        this.messages = messages;
        this.userId = userId;

        this.onMessageClickListener = onMessageClickListener;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {

        View view;

        if(viewType == MSG_TYPE_RIGHT){
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
        }else{
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
        }

        return new ViewHolder(view, onMessageClickListener);

    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Message message = messages.get(position);

        holder.messageTextView.setText(message.getMessageContext());
        holder.messageTimeTextView.setText(message.getMessageTime());
    }

    @Override
    public int getItemCount() {
        return messages == null ? 0 : messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView messageTextView, messageTimeTextView;
        public onMessageClickListener onMessageClickListener;

        public ViewHolder(@NonNull View itemView, onMessageClickListener onMessageClickListener) {
            super(itemView);

            messageTextView = itemView.findViewById(R.id.message_text_view);
            messageTimeTextView = itemView.findViewById(R.id.message_time_text_view);

            this.onMessageClickListener = onMessageClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onMessageClickListener.onMessageClick(getAdapterPosition());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).getSender().equals(userId)){
            return MSG_TYPE_RIGHT;
        }
        return MSG_TYPE_LEFT;
    }

    public interface onMessageClickListener{
        void onMessageClick(int position);
    }
}
