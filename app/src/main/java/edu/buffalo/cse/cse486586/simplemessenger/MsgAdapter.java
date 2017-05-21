package edu.buffalo.cse.cse486586.simplemessenger;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by weiyijiang on 5/21/17.
 */

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {
    private List<Message> msgList;
    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout left;
        LinearLayout right;
        TextView leftMsg;
        TextView rightMsg;

        public ViewHolder(View view) {
            super(view);
            left = (LinearLayout) view.findViewById(R.id.left_layout);
            right = (LinearLayout) view.findViewById(R.id.right_layout);
            leftMsg = (TextView) view.findViewById(R.id.left_msg);
            rightMsg = (TextView) view.findViewById(R.id.right_msg);
        }
    }

    public MsgAdapter(List<Message> list) {
        this.msgList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message msg = this.msgList.get(position);
        if (msg.getType() == Message.TYPE_RECEIVED) {
            holder.left.setVisibility(View.VISIBLE);
            holder.right.setVisibility(View.GONE);
            holder.leftMsg.setText(msg.getContent());
        } else {
            holder.left.setVisibility(View.GONE);
            holder.right.setVisibility(View.VISIBLE);
            holder.rightMsg.setText(msg.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }
}
