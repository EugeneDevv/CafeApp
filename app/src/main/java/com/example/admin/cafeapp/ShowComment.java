package com.example.admin.cafeapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.example.admin.cafeapp.Common.Common;
import com.example.admin.cafeapp.Model.Rating;
import com.example.admin.cafeapp.ViewHolder.ShowCommentViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ShowComment extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference ratingTbl;

    SwipeRefreshLayout mSwipeRefreshLayout;

    FirebaseRecyclerAdapter<Rating,ShowCommentViewHolder> adapter;

    String foodId="";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter!=null)
            adapter.stopListening();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Vahika.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_show_comment);

        //Firebase
        database=FirebaseDatabase.getInstance();
        ratingTbl=database.getReference("Rating");

        recyclerView=(RecyclerView)findViewById(R.id.recyclerComment);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Swipe Layout
        mSwipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getIntent() !=null)
                    foodId=getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                if (!foodId.isEmpty() && foodId!=null)
                {
                    //Create request Query
                    Query query=ratingTbl.orderByChild("foodId").equalTo(foodId);

                    FirebaseRecyclerOptions<Rating> options=new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query,Rating.class)
                            .build();
                    adapter=new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowCommentViewHolder holder, int position, @NonNull Rating model) {

                            holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                            holder.txtComment.setText(model.getComment());
                            holder.txtUserName.setText(model.getName());
                        }

                        @NonNull
                        @Override
                        public ShowCommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                            View view=LayoutInflater.from(viewGroup.getContext())
                                    .inflate(R.layout.show_comment_layout,viewGroup,false);
                            return new ShowCommentViewHolder(view);
                        }
                    };

                    loadComment(foodId);
                }
            }
        });

        //Thread to load comment on first launch
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);

                if (getIntent() !=null)
                    foodId=getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                if (!foodId.isEmpty() && foodId!=null)
                {
                    //Create request Query
                    Query query=ratingTbl.orderByChild("foodId").equalTo(foodId);

                    FirebaseRecyclerOptions<Rating> options=new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query,Rating.class)
                            .build();
                    adapter=new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowCommentViewHolder holder, int position, @NonNull Rating model) {

                            holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                            holder.txtComment.setText(model.getComment());
                            holder.txtUserName.setText(model.getName());
                        }

                        @NonNull
                        @Override
                        public ShowCommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                            View view=LayoutInflater.from(viewGroup.getContext())
                                    .inflate(R.layout.show_comment_layout,viewGroup,false);
                            return new ShowCommentViewHolder(view);
                        }
                    };

                    loadComment(foodId);
                }


            }
        });
    }

    private void loadComment(String foodId) {
        adapter.startListening();
        recyclerView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
