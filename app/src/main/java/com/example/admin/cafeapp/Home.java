package com.example.admin.cafeapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.example.admin.cafeapp.Common.Common;
import com.example.admin.cafeapp.Database.MyData;
import com.example.admin.cafeapp.Interface.ItemClickListener;
import com.example.admin.cafeapp.Model.Banner;
import com.example.admin.cafeapp.Model.Category;
import com.example.admin.cafeapp.Model.Feedback;
import com.example.admin.cafeapp.Model.Rating;
import com.example.admin.cafeapp.Model.Token;
import com.example.admin.cafeapp.Model.User;
import com.example.admin.cafeapp.ViewHolder.MenuViewHolder;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

//import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RatingDialogListener {

    FirebaseDatabase database;
    FirebaseRecyclerAdapter adapter;
    DatabaseReference category;
    TextView txtFullName;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    SwipeRefreshLayout swipeRefreshLayout;

    CounterFab fab;
    private long backPressedTime;

    //Slider
    HashMap<String,String> image_list;
    SliderLayout mSlider;

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
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        //View
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (Common.isConnectedToInternet(getBaseContext()))
                    loadMenu();
                else {
                    Toast.makeText(getBaseContext(), "Check Your Connection !!!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        //Default load for 1st time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (Common.isConnectedToInternet(getBaseContext()))
                    loadMenu();
                else {
                    Toast.makeText(getBaseContext(), "Check Your Connection !!!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        //Init firebase
        database=FirebaseDatabase.getInstance();
        category=database.getReference("Category");

        FirebaseRecyclerOptions<Category>options=new FirebaseRecyclerOptions.Builder<Category>().setQuery(category,Category.class).build();
        adapter= new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.menu_item,viewGroup,false);
                return new MenuViewHolder(itemView);
            }

            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder holder, int position, @NonNull Category model) {

                holder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.imageView);
                final Category clickItem=model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Get Category Id and send to new activity
                        Intent foodList=new Intent(Home.this,FoodList.class);
                        // Category ID is key so we just get key of this item
                        foodList.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(foodList);
                    }
                });

            }
        };

        //Init Paper
        Paper.init(this);

         fab = (CounterFab) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent cartIntent=new Intent(Home.this,Cart.class);
               startActivity(cartIntent);
            }
        });

        fab.setCount(new MyData(this).getCountCart(Common.currentUser.getPhone()));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Set Name For User
        View headerView=navigationView.getHeaderView(0);
        txtFullName=(TextView)headerView.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.currentUser.getName());

        //Load Menu
        recycler_menu=(RecyclerView)findViewById(R.id.recycler_menu);
        recycler_menu.setLayoutManager(new GridLayoutManager(this,2));
        LayoutAnimationController controller=AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),
                R.anim.layout_slide_from_bottom);
        recycler_menu.setLayoutAnimation(controller);

        //News System
        final CheckBox ckb_subrcribe_new=(CheckBox)findViewById(R.id.ckb_sub_new);
        FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);

        updateToken(FirebaseInstanceId.getInstance().getToken());

        //SetUpSlider
        setupSlider();

    }

    private void setupSlider() {
        mSlider=(SliderLayout)findViewById(R.id.slider);
        image_list=new HashMap<>();

        final DatabaseReference banners=database.getReference("Banner");

        banners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    Banner banner=postSnapShot.getValue(Banner.class);
                    //We Will concatstring name and id like
                    //pizza _01 => and we will use PIZZA for showing description,01  for food  id to click
                    image_list.put(banner.getName()+"@@@"+banner.getId(),banner.getImage());
                }
                for (String key:image_list.keySet())
                {
                    String[] keySplit=key.split("@@@");
                    String nameOfFood=keySplit[0];
                    String idOfFood=keySplit[1];

                    //Create Slider
                    final TextSliderView textSliderView=new TextSliderView(getBaseContext());
                    textSliderView
                            .description(nameOfFood)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent=new Intent(Home.this,FoodDetail.class);
                                    //we will send food id to food detail
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);

                                }
                            });
                    //Add Extra bundle
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodId",idOfFood);

                    mSlider.addSlider(textSliderView);

                    //Remove after event finishes
                    banners.removeEventListener(this);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(4000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new MyData(this).getCountCart(Common.currentUser.getPhone()));
        if (adapter!=null)
            adapter.startListening();

    }

    private void updateToken(String token) {
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference("Tokens");
        Token data=new Token(token,false); //false becausenthis token is sent from client app
        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }

    private void loadMenu() {



        adapter.startListening();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

        //Animation
        recycler_menu.getAdapter().notifyDataSetChanged();
        recycler_menu.scheduleLayoutAnimation();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mSlider.startAutoCycle();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        loadMenu();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        mSlider.stopAutoCycle();
    }

    @Override
    public void onBackPressed() {

       if (backPressedTime + 2000 > System.currentTimeMillis()){
           super.onBackPressed();
           return;


       }
       else {
           Toast.makeText(this, "Press Back Again To Exit !!!", Toast.LENGTH_SHORT).show();

       }
       backPressedTime=System.currentTimeMillis();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId()==R.id.menu_search)
            startActivity(new Intent(Home.this,SearchActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_cart) {
            Intent cartIntent=new Intent(Home.this,Cart.class);
            startActivity(cartIntent);

        } else if (id == R.id.nav_orders) {
            Intent orderIntent=new Intent(Home.this,OrderStatus.class);
            startActivity(orderIntent);

        } else if (id == R.id.nav_log_out) {

            AlertDialog.Builder alertDialog=new AlertDialog.Builder(Home.this);
            alertDialog.setTitle("Hey, "+Common.currentUser.getName());
            alertDialog.setMessage("Do You Want To Sign Out?");
            alertDialog.setCancelable(false);

            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Delete Remember user & Password
                    AccountKit.logOut();
                    //Log Out
                    Intent signIn=new Intent(Home.this,MainActivity.class);
                    signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(signIn);
                }
            });
            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Toast.makeText(Home.this, "Cancel", Toast.LENGTH_SHORT).show();
                }
            });
            alertDialog.setIcon(R.mipmap.unnamed);
            alertDialog.show();


        }
        else if (id==R.id.nav_feedback){
            showRatingDialog();
        }
        else if (id==R.id.nav_Favourites){
            startActivity(new Intent(Home.this,FavouritesActivity.class));
        }
        else if (id==R.id.nav_profile){
          //  startActivity(new Intent(Home.this,Profile.class));
          showProfileDialog();
        }
        else if (id==R.id.nav_share){
            shareAPK();

        }
        else if (id==R.id.nav_help){
            startActivity(new Intent(Home.this,HelpActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void shareAPK() {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Taste Hub:Food Delivery Expert");
            String sAux = "\nLet Me Recommend You Taste Hub Application\n\n";
            sAux = sAux + "https://drive.google.com/open?id=1-3Y7w05blyAx1vRHFcwx_TmZxDcTvPIr\n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "Select Share Method"));
        } catch(Exception e) {
        }

    }

    private void showProfileDialog() {
        final AlertDialog.Builder alertDialog=new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Update Your Profile");
        alertDialog.setMessage("Enter Your Name & Address");
        alertDialog.setCancelable(false);

        LayoutInflater inflater=LayoutInflater.from(this);
        final View layout_profile=inflater.inflate(R.layout.profile_layout,null);

        final MaterialEditText edtName=(MaterialEditText)layout_profile.findViewById(R.id.Profile_name);
        final MaterialEditText edtHomeAddress=(MaterialEditText)layout_profile.findViewById(R.id.Profile_address);
        alertDialog.setView(layout_profile);

        edtName.setText(Common.currentUser.getName());
        edtHomeAddress.setText(Common.currentUser.getHomeAddress());

        //Button
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(edtName.getText().toString().trim().length()==0 || edtHomeAddress.getText().toString().trim().length()==0){
                    Toast.makeText(Home.this, "Please Enter All Details !!!", Toast.LENGTH_SHORT).show();

                }

               else {
                   //show Dialog
                   final ACProgressFlower waitingDialog = new ACProgressFlower.Builder(Home.this)
                           .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                           .themeColor(Color.WHITE)
                           .text("Loading. . .")
                           .fadeColor(Color.DKGRAY).build();
                   waitingDialog.show();

                   //Update Name
                   Map<String, Object> update_profile = new HashMap<>();
                   update_profile.put("name", edtName.getText().toString());
                   update_profile.put("homeAddress", edtHomeAddress.getText().toString());


                   FirebaseDatabase.getInstance()
                           .getReference("User")
                           .child(Common.currentUser.getPhone())
                           .updateChildren(update_profile)
                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   //Dismiss Dialog
                                   waitingDialog.dismiss();
                                   if (task.isSuccessful())
                                       Toast.makeText(Home.this, "Profile Successfully Updated !!!", Toast.LENGTH_SHORT).show();

                               }
                           });

                    //Refresh Driver Data
                    DatabaseReference userInformation=FirebaseDatabase.getInstance().getReference("User");
                    userInformation.child(Common.currentUser.getPhone())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    Common.currentUser=dataSnapshot.getValue(User.class);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
               }

            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(Home.this, "Cancel", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.setIcon(R.drawable.ic_person_black_24dp);
        alertDialog.show();
    }

    private void showSettingDialog() {

        AlertDialog.Builder alertDialog=new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Notification Setting");
        alertDialog.setCancelable(false);

        LayoutInflater inflater=LayoutInflater.from(this);
        View layout_setting=inflater.inflate(R.layout.setting_layout,null);



        //Add Code remember state of checkbox
        Paper.init(this);
        String isSubscribe=Paper.book().read("sub_new");

        final CheckBox ckb_subrcribe_new=(CheckBox)layout_setting.findViewById(R.id.ckb_sub_new);

        Paper.book().write("sub_new","true");
        if (isSubscribe==null || TextUtils.isEmpty(isSubscribe) || isSubscribe.equals("false"))
            ckb_subrcribe_new.setChecked(false);
        else
            ckb_subrcribe_new.setChecked(true);



        alertDialog.setView(layout_setting);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (ckb_subrcribe_new.isChecked())
                {
                    ckb_subrcribe_new.setChecked(true);
                    FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);
                    //Write Value
                    Paper.book().write("sub_new","true");
                    Toast.makeText(Home.this, "News Is Enabled !!!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    ckb_subrcribe_new.setChecked(false);
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.topicName);
                    //write value
                    Paper.book().write("sub_new","false");
                    Toast.makeText(Home.this, "News Is Disabled !!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(Home.this, "Cancel", Toast.LENGTH_SHORT).show();

            }
        });

        alertDialog.setIcon(R.drawable.ic_settings_black_24dp);
        alertDialog.show();

    }


    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int i, String s) {


        //Set Rating and Upload To Firebase
        final Feedback feedback=new Feedback(Common.currentUser.getName(),
                Common.currentUser.getHomeAddress(),
                Common.currentUser.getDOB(),
                Common.currentUser.getEmailAddress(),
                Common.currentUser.getGender(),
                String.valueOf(i),
                s);

        FirebaseDatabase.getInstance().getReference("Feedback").child(Common.currentUser.getPhone())
        .push()
                .setValue(feedback)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Toast.makeText(Home.this, "Thank You For Your Feedback !!!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRatingDialog() {

        new AppRatingDialog.Builder()
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .setWindowAnimation(R.anim.item_animation_slide_from_right)
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad","Not Good","Quite Ok","Very Good","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate This App !!!")
                .setDescription("Please Select Some Stars And Give Your Feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Write Your Feedback Here . . .")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(Home.this)
                .show();


    }
}
