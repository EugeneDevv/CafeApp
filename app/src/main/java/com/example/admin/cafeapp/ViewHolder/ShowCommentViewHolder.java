package com.example.admin.cafeapp.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.admin.cafeapp.R;

public class ShowCommentViewHolder extends RecyclerView.ViewHolder {

    public TextView txtUserName,txtComment;
    public RatingBar ratingBar;

    public ShowCommentViewHolder(@NonNull View itemView) {
        super(itemView);

        txtComment=(TextView)itemView.findViewById(R.id.txtComment);
        txtUserName=(TextView)itemView.findViewById(R.id.txtUserName);
        ratingBar=(RatingBar)itemView.findViewById(R.id.ratingBar);
    }
}
