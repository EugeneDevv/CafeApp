package com.example.admin.cafeapp.ViewHolder;


import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.admin.cafeapp.Cart;
import com.example.admin.cafeapp.Common.Common;
import com.example.admin.cafeapp.Database.MyData;
import com.example.admin.cafeapp.Interface.ItemClickListener;
import com.example.admin.cafeapp.Model.Order;
import com.example.admin.cafeapp.R;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



public class CartAdapter extends RecyclerView.Adapter<CartViewHolder> {

    private List<Order>listData=new ArrayList<>();
    private Cart cart;

    public CartAdapter(List<Order> listData, Cart cart){
        this.listData=listData;
        this.cart=cart;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater=LayoutInflater.from(cart);
        View itemView=inflater.inflate(R.layout.cart_layout,viewGroup,false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder cartViewHolder, final int i) {

        Picasso.with(cart.getBaseContext())
                .load(listData.get(i).getImage())
                .resize(70,70)
                .centerCrop()
                .into(cartViewHolder.cart_image);

        cartViewHolder.btn_quantity.setNumber(listData.get(i).getQuantity());
        cartViewHolder.btn_quantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order=listData.get(i);
                order.setQuantity(String.valueOf(newValue));
                new MyData(cart).updateCart(order);

                //Calculate Total Price
                int total=0;
                List<Order> orders=new MyData(cart).getCarts(Common.currentUser.getPhone());
                for (Order item:orders)
                    total+=(Float.parseFloat(order.getPrice()))*(Float.parseFloat(item.getQuantity()));
                Locale locale=new Locale("en","US");
                NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);

                cart.txtTotalPrice.setText(fmt.format(total));
            }
        });

        Locale locale=new Locale("en","US");
        NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);
        float price=(Float.parseFloat(listData.get(i).getPrice()))*(Float.parseFloat(listData.get(i).getQuantity()));
        cartViewHolder.txt_cart_name.setText(listData.get(i).getProductName());
        cartViewHolder.txt_price.setText(fmt.format(price));


    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public Order getItem(int position)
    {
        return listData.get(position);
    }

    public void removeItem( int position)
    {
        listData.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Order item, int position)
    {
        listData.add(position,item);
        notifyItemInserted(position);
    }
}
