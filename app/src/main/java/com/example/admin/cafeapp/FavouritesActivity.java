package com.example.admin.cafeapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.RelativeLayout;

import com.example.admin.cafeapp.Common.Common;
import com.example.admin.cafeapp.Database.MyData;
import com.example.admin.cafeapp.Helper.RecyclerItemTouchHelper;
import com.example.admin.cafeapp.Interface.RecyclerItemTouchHelperListener;
import com.example.admin.cafeapp.Model.Favourites;
import com.example.admin.cafeapp.Model.Food;
import com.example.admin.cafeapp.Model.Order;
import com.example.admin.cafeapp.ViewHolder.CartAdapter;
import com.example.admin.cafeapp.ViewHolder.CartViewHolder;
import com.example.admin.cafeapp.ViewHolder.FavouritesAdapter;
import com.example.admin.cafeapp.ViewHolder.FavouritesViewHolder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FavouritesActivity extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FavouritesAdapter adapter;
    RelativeLayout rootLayout;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Vahika.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_favourites);

        rootLayout=(RelativeLayout)findViewById(R.id.root_layout);
        recyclerView=(RecyclerView)findViewById(R.id.recycler_fav);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Swipe to delete cart item
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback=new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        loadFavourites();
    }

    private void showAlert(){
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(FavouritesActivity.this);
        alertDialog.setTitle("Hey, "+Common.currentUser.getName());
        alertDialog.setMessage("You Have No Order, Please Order Something");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(FavouritesActivity.this,Home.class));
            }
        });

        alertDialog.setIcon(R.mipmap.unnamed);
        alertDialog.show();
    }

    private void loadFavourites() {
        adapter=new FavouritesAdapter(this,new MyData(this).getAllFavourites(Common.currentUser.getPhone()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

        if (viewHolder instanceof FavouritesViewHolder)
        {
            String name=((FavouritesAdapter)recyclerView.getAdapter()).getItem(position).getFoodName();
            final Favourites deleteItem=((FavouritesAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex=viewHolder.getAdapterPosition();

            adapter.removeItem(viewHolder.getAdapterPosition());
            new MyData(getBaseContext()).removeFromFavourites(deleteItem.getFoodId(),Common.currentUser.getPhone());

            //Make Snackbar
            Snackbar snackbar=Snackbar.make(rootLayout,name+" "+"Removed From Favourites !!!",Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new MyData(getBaseContext()).addToFavourites(deleteItem);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
