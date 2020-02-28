package com.example.admin.cafeapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.admin.cafeapp.Common.Common;
import com.example.admin.cafeapp.Database.MyData;
import com.example.admin.cafeapp.Interface.ItemClickListener;
import com.example.admin.cafeapp.Model.Favourites;
import com.example.admin.cafeapp.Model.Food;
import com.example.admin.cafeapp.Model.Order;
import com.example.admin.cafeapp.ViewHolder.FoodViewHolder;
import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SearchActivity extends AppCompatActivity {

    //Search Functionality
    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> suggestList=new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;
    SwipeRefreshLayout swipeRefreshLayout;


    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;

    //Favourites
    MyData localDB;

    //Facebook Share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    //Create target from picasso
    Target target=new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //Create photo from bitmap
            SharePhoto photo=new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (ShareDialog.canShow(SharePhotoContent.class))
            {
                SharePhotoContent content=new SharePhotoContent.Builder()
                        .addPhoto(photo).build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

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
        setContentView(R.layout.activity_search);

        //Init Facebook
        callbackManager=CallbackManager.Factory.create();
        shareDialog=new ShareDialog(this);

        //Firebase
        database=FirebaseDatabase.getInstance();
        foodList=database.getReference("Foods");

        //LocalDB
        localDB=new MyData(this);

        recyclerView=(RecyclerView)findViewById(R.id.recycler_search);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Search
        materialSearchBar=(MaterialSearchBar)findViewById(R.id.searchBar);


        loadSuggest(); //Function to load suggest from firebase


        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //when user type their text,we will change suggest list
                List<String> suggest=new ArrayList<String>();
                for (String search:suggestList)
                {
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                // materialSearchBar.setLastSuggestions(suggest);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //when search bar is close
                //Restore original adapter
                if (!enabled)
                    recyclerView.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //when Search finished
                //show result of search bar
                startSearch(text);

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        //Load all food
        loadAllFoods();
    }

    private void loadAllFoods() {

        //create query by category id
        Query searchByName=foodList;
        //create options with query
        FirebaseRecyclerOptions<Food> foodOptions=new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName,Food.class).build();

        adapter= new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.food_item,viewGroup,false);
                return new FoodViewHolder(itemView);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder holder, final int position, @NonNull final Food model) {
                holder.food_name.setText(model.getName());
                holder.food_price.setText(String.format("$ %s",model.getPrice().toString()));
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.food_image);

                //Quick Cart

                holder.quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        boolean isExists=new MyData(getBaseContext()).checkFoodExists(adapter.getRef(position).getKey(),Common.currentUser.getPhone());

                        if (!isExists) {

                            new MyData(getBaseContext()).addToCart(new Order(
                                    Common.currentUser.getPhone(),
                                    adapter.getRef(position).getKey(),
                                    model.getName(),
                                    "1",
                                    model.getPrice(),
                                    model.getDiscount(),
                                    model.getImage()
                            ));

                        }
                        else
                        {
                            new MyData(getBaseContext()).increaseCart(Common.currentUser.getPhone(),adapter.getRef(position).getKey());
                        }
                        Toast.makeText(SearchActivity.this, "Added To Cart Successfully", Toast.LENGTH_SHORT).show();
                    }
                });


                //Add Favourites
                if (localDB.isFavourites(adapter.getRef(position).getKey(),Common.currentUser.getPhone()))
                    holder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);

                //Click To Share
                holder.share_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.with(getApplicationContext())
                                .load(model.getImage())
                                .into(target);
                    }
                });

                //Click To change state of Favourites
                holder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Favourites favourites=new Favourites();
                        favourites.setFoodId(adapter.getRef(position).getKey());
                        favourites.setFoodName(model.getName());
                        favourites.setFoodDescription(model.getDescription());
                        favourites.setFoodImage(model.getImage());
                        favourites.setFoodDiscount(model.getDiscount());
                        favourites.setFoodMenuId(model.getMenuId());
                        favourites.setUserPhone(Common.currentUser.getPhone());
                        favourites.setFoodPrice(model.getPrice());

                        if (!localDB.isFavourites(adapter.getRef(position).getKey(),Common.currentUser.getPhone())) {

                            localDB.addToFavourites(favourites);
                            holder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(SearchActivity.this, ""+model.getName()+" "+"Was Added To Favourites", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            new MyData(getBaseContext()).removeFromFavourites(adapter.getRef(position).getKey(),Common.currentUser.getPhone());
                            holder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(SearchActivity.this, ""+model.getName()+"Was Removed From Favourites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                final Food local = model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent foodDetail=new Intent(SearchActivity.this,FoodDetail.class);
                        foodDetail.putExtra("FoodId",adapter.getRef(position).getKey()); //send food id to new activity
                        startActivity(foodDetail);

                    }
                });



            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();
    }

    private void startSearch(CharSequence text) {

        //create query by name
        Query SearchByName=foodList.orderByChild("name").equalTo(text.toString());
        //create options with query
        FirebaseRecyclerOptions<Food> foodOptions=new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(SearchByName,Food.class).build();
        searchAdapter=new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {
                holder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(holder.food_image);

                final Food local=model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //start activity
                        Intent foodDetail=new Intent(SearchActivity.this,FoodDetail.class);
                        foodDetail.putExtra("FoodId",searchAdapter.getRef(position).getKey());
                        startActivity(foodDetail);

                    }
                });

            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView=LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.food_item,viewGroup,false);
                return new FoodViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter);
        searchAdapter.notifyDataSetChanged();

    }

    private void loadSuggest() {

        foodList.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                        {
                            Food item=postSnapshot.getValue(Food.class);
                            suggestList.add(item.getName());//Add Name Of To Food List
                        }
                        materialSearchBar.setLastSuggestions(suggestList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStop() {

        if (adapter!=null)
            adapter.stopListening();
        if (searchAdapter!=null)
            searchAdapter.stopListening();
        super.onStop();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        loadAllFoods();
    }

}
