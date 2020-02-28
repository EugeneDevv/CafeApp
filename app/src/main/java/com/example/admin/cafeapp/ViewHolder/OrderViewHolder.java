package com.example.admin.cafeapp.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin.cafeapp.Interface.ItemClickListener;
import com.example.admin.cafeapp.R;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtOrderId,txtOrderStatus,txtOrderPhone,txtOrderAddress,txtOrderDate,txtPaymentState,txtPaymentMethod,txtName,txtTotal;
    public ImageView btn_delete,btn_map;
    private ItemClickListener itemClickListener;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);
        txtOrderAddress=(TextView)itemView.findViewById(R.id.order_address);
        txtOrderId=(TextView)itemView.findViewById(R.id.order_id);
        txtOrderStatus=(TextView)itemView.findViewById(R.id.order_status);
        txtOrderPhone=(TextView)itemView.findViewById(R.id.order_phone);
        txtOrderDate=(TextView)itemView.findViewById(R.id.order_date);
        txtPaymentState=(TextView)itemView.findViewById(R.id.paymentState);
        txtPaymentMethod=(TextView)itemView.findViewById(R.id.paymentMethod);
        txtName=(TextView)itemView.findViewById(R.id.userName);
        txtTotal=(TextView)itemView.findViewById(R.id.totalAmount);
        btn_delete=(ImageView)itemView.findViewById(R.id.btn_delete);
        btn_map=(ImageView)itemView.findViewById(R.id.btn_map);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        if (itemClickListener!=null)
            itemClickListener.onClick(v,getAdapterPosition(),false);

    }
}
