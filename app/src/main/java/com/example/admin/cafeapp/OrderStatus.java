package com.example.admin.cafeapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.example.admin.cafeapp.Common.Common;
import com.example.admin.cafeapp.Interface.ItemClickListener;
import com.example.admin.cafeapp.Model.Order;
import com.example.admin.cafeapp.Model.Request;
import com.example.admin.cafeapp.ViewHolder.FoodViewHolder;
import com.example.admin.cafeapp.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OrderStatus extends AppCompatActivity {

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;

    FirebaseDatabase database;
    DatabaseReference requests;


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
        setContentView(R.layout.activity_order_status);

        //Firebase
        database=FirebaseDatabase.getInstance();
        requests=database.getReference("Requests");

        recyclerView=(RecyclerView)findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if (getIntent()==null)
            loadOrders(Common.currentUser.getPhone());
        else
        {
            if (getIntent().getStringExtra("userPhone")==null)
                loadOrders(Common.currentUser.getPhone());
            else
                loadOrders(getIntent().getStringExtra("userPhone"));
        }
    }

    private void showAlert(){
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Hey, "+Common.currentUser.getName());
        alertDialog.setMessage("You Have No Order, Please Order Something");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(OrderStatus.this,Home.class));
            }
        });

        alertDialog.setIcon(R.mipmap.unnamed);
        alertDialog.show();
    }

    private void loadOrders(String phone) {

        Query getOrderByUser=requests.orderByChild("phone").equalTo(phone);
        FirebaseRecyclerOptions<Request>orderOptions=new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(getOrderByUser,Request.class).build();

            adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(orderOptions) {
                @Override
                protected void onBindViewHolder(@NonNull OrderViewHolder holder, final int position, @NonNull Request model) {
                    holder.txtOrderId.setText(adapter.getRef(position).getKey());
                    holder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                    holder.txtOrderAddress.setText(model.getAddress());
                    holder.txtOrderPhone.setText(model.getPhone());
                    holder.txtPaymentMethod.setText(model.getPaymentMethod());
                    holder.txtPaymentState.setText(model.getPaymentState());
                    holder.txtName.setText(model.getName());
                    holder.txtTotal.setText(model.getTotal());
                    holder.txtOrderDate.setText(Common.getDate(Long.parseLong(adapter.getRef(position).getKey())));

                    holder.btn_map.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Common.currentKey=adapter.getRef(position).getKey();

                            if (adapter.getItem(position).getStatus().equals("2"))
                                startActivity(new Intent(OrderStatus.this,TrackingOrder.class));
                            else
                                Toast.makeText(OrderStatus.this, "Please Wait Your Order Is Not Forwarded To Shipper Yet", Toast.LENGTH_SHORT).show();
                        }
                    });
                    holder.btn_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (adapter.getItem(position).getStatus().equals("0"))
                                deleteOrder(adapter.getRef(position).getKey());
                            else
                                Toast.makeText(OrderStatus.this, "You Can't Delete Order Now !!!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @NonNull
                @Override
                public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                    View itemView = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.order_layout, viewGroup, false);
                    return new OrderViewHolder(itemView);
                }
            };

            adapter.startListening();
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);

        //Animation
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    private void deleteOrder(final String key) {

        AlertDialog.Builder alertDialog=new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Hey, "+Common.currentUser.getName());
        alertDialog.setMessage("Do You Want To Delete Order?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                requests.child(key)
                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(OrderStatus.this, new StringBuilder("Order"+" ")
                                .append(key)
                                .append(" "+"Has Been Deleted !!!").toString(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OrderStatus.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                Toast.makeText(OrderStatus.this, "Cancel", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.setIcon(R.mipmap.unnamed);
        alertDialog.show();

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        loadOrders(Common.currentUser.getPhone());

    }
}
