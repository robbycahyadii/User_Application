package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ChatListAdapter extends BaseAdapter {

    private Context context;
    private List<ChatMessage> chatMessages;

    public ChatListAdapter(Context context, List<ChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
    }

    @Override
    public int getCount() {
        return chatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        }

        ChatMessage chatMessage = chatMessages.get(position);

        TextView senderTextView = convertView.findViewById(R.id.senderTextView);
        TextView messageTextView = convertView.findViewById(R.id.messageTextView);

        senderTextView.setText(chatMessage.getSender());
        messageTextView.setText(chatMessage.getMessage());

        return convertView;
    }
}
