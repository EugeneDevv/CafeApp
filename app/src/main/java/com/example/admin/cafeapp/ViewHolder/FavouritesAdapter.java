package com.example.admin.cafeapp.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.admin.cafeapp.Common.Common;
import com.example.admin.cafeapp.Database.MyData;
import com.example.admin.cafeapp.FoodDetail;
import com.example.admin.cafeapp.FoodList;
import com.example.admin.cafeapp.Interface.ItemClickListener;
import com.example.admin.cafeapp.Model.Favourites;
import com.example.admin.cafeapp.Model.Food;
import com.example.admin.cafeapp.Model.Order;
import com.example.admin.cafeapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesViewHolder> {

    private Context context;
    private List<Favourites> favouritesList;

    public FavouritesAdapter(Context context,List<Favourites> favouritesList)
    {
        this.context=context;
        this.favouritesList=favouritesList;
    }

    @NonNull
    @Override
    public FavouritesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView=LayoutInflater.from(context)
                .inflate(R.layout.favourites_item,viewGroup,false);
        return new FavouritesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FavouritesViewHolder holder, final int i) {

        holder.food_name.setText(favouritesList.get(i).getFoodName());
        holder.food_price.setText(String.format("$ %s",favouritesList.get(i).getFoodPrice().toString()));
        Picasso.with(context).load(favouritesList.get(i).getFoodImage()).into(holder.food_image);

        //Quick Cart

        holder.quick_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isExists=new MyData(context).checkFoodExists(favouritesList.get(i).getFoodId(),Common.currentUser.getPhone());

                if (!isExists) {

                    new MyData(context).addToCart(new Order(
                            Common.currentUser.getPhone(),
                            favouritesList.get(i).getFoodId(),
                            favouritesList.get(i).getFoodName(),
                            "1",
                            favouritesList.get(i).getFoodPrice(),
                            favouritesList.get(i).getFoodDiscount(),
                            favouritesList.get(i).getFoodImage()
                    ));

                }
                else
                {
                    new MyData(context).increaseCart(Common.currentUser.getPhone(),favouritesList.get(i).getFoodId());
                }
                Toast.makeText(context, "Added To Cart Successfully", Toast.LENGTH_SHORT).show();
            }
        });


        final Favourites local = favouritesList.get(i);
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                //Start new Activity
                Intent foodDetail=new Intent(context,FoodDetail.class);
                foodDetail.putExtra("FoodId",favouritesList.get(i).getFoodId()); //send food id to new activity
                context.startActivity(foodDetail);

            }
        });
    }

    @Override
    public int getItemCount() {
        return favouritesList.size();
    }

    public void removeItem( int position)
    {
        favouritesList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Favourites item, int position)
    {
        favouritesList.add(position,item);
        notifyItemInserted(position);
    }

    public Favourites getItem(int position)
    {
        return favouritesList.get(position);
    }
}
