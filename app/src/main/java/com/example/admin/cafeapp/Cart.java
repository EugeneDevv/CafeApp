package com.example.admin.cafeapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.cafeapp.Common.Common;
import com.example.admin.cafeapp.Common.Config;
import com.example.admin.cafeapp.Database.MyData;
import com.example.admin.cafeapp.Helper.RecyclerItemTouchHelper;
import com.example.admin.cafeapp.Interface.RecyclerItemTouchHelperListener;
import com.example.admin.cafeapp.Model.DataMessage;
import com.example.admin.cafeapp.Model.Feedback;
import com.example.admin.cafeapp.Model.MyResponse;
import com.example.admin.cafeapp.Model.Order;
import com.example.admin.cafeapp.Model.Request;
import com.example.admin.cafeapp.Model.Token;
import com.example.admin.cafeapp.Model.User;
import com.example.admin.cafeapp.Remote.APIService;
import com.example.admin.cafeapp.Remote.IGoogleService;
import com.example.admin.cafeapp.ViewHolder.CartAdapter;
import com.example.admin.cafeapp.ViewHolder.CartViewHolder;
import com.facebook.accountkit.AccountKit;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

//import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Cart extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener,LocationListener, RecyclerItemTouchHelperListener, RatingDialogListener {

    private static final int PAYPAL_REQUEST_CODE=9999;

    PlaceAutocompleteFragment edtAddress;

    FusedLocationProviderClient fusedLocationProviderClient;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView txtTotalPrice;
    FButton btnPlace;
    private GoogleApiClient googleApiClient;
    final static int REQUEST_LOCATION = 199;


    List<Order> cart=new ArrayList<>();
    CartAdapter adapter;

    //Paypal Service
    static PayPalConfiguration config=new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);

    String address,commemt;

    Place shippingAddress;

    //Location
    LocationRequest mLocationRequest;
    LocationCallback locationCallback;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;


    private static final int UPDATE_INTERVAL=5000;
    private static final int FASTEST_INTERVAL=3000;
    private static final int DISPLACEMENT=10;
    private static final int LOCATION_REQUEST_CODE=9999;
    private static final int PLAY_SERVICES_REQUEST=9997;

    //Declare Google Map API Service
    IGoogleService mGoogleMapService;
    APIService mService;

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
        setContentView(R.layout.activity_cart);

        //Init
        mGoogleMapService=Common.getGoogleMapAPI();

        rootLayout=(RelativeLayout)findViewById(R.id.rootLayout);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Runtime Permission
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]
                    {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },LOCATION_REQUEST_CODE);

        }
        else
        {
            if (checkPlayServices())
            {
                buildGoogleApiClient();
                createLocationRequest();
                buildLocationcallback();
            }
        }

        // Todo Location Already on  ... start
        final LocationManager manager = (LocationManager) Cart.this.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(Cart.this)) {
            Toast.makeText(Cart.this,"GPS Is Enabled",Toast.LENGTH_SHORT).show();
        }
        // Todo Location Already on  ... end

        if(!hasGPSDevice(Cart.this)){
            Toast.makeText(Cart.this,"Gps not Supported",Toast.LENGTH_SHORT).show();
        }

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(Cart.this)) {
            Log.e("Shyam","Gps already enable");
            Toast.makeText(Cart.this,"Please Enable Your GPS",Toast.LENGTH_SHORT).show();
            enableLoc();
        }else{
            Log.e("Shyam","Gps already enabled");
            Toast.makeText(Cart.this,"GPS Is Enabled",Toast.LENGTH_SHORT).show();
        }

        //Init Paypal Service
        Intent intent=new Intent(this,PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        startService(intent);
        //Init Service
        mService=Common.getFCMService();

        //Firebase
        database=FirebaseDatabase.getInstance();
        requests=database.getReference("Requests");

        //Init
        recyclerView=(RecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);



        //Swipe to delete cart item
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback=new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        txtTotalPrice=(TextView)findViewById(R.id.total);
        btnPlace=(FButton)findViewById(R.id.btnPlaceOrder);

        loadListFood();

        if (cart.size()<1)
            showAlert();


        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cart.size()>0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this, "Your Cart Is Empty,Please Add Your Food !!!", Toast.LENGTH_SHORT).show();


            }
        });

    }

    private void showAlert(){
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Hey, "+Common.currentUser.getName());
        alertDialog.setMessage("Your Cart Is Empty, Please Add Your Food");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.setIcon(R.mipmap.unnamed);
        alertDialog.show();
    }

    private void buildLocationcallback() {
        locationCallback=new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location:locationResult.getLocations())
                {

                }
            }
        };
    }

    private void createLocationRequest() {
        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode=GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode!=ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_REQUEST).show();
            }
            else
            {
                Toast.makeText(this, "This Device Not Supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case LOCATION_REQUEST_CODE:
            {
                if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if (checkPlayServices())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
            }
            break;
        }
    }



    private void showAlertDialog() {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One More Step!");
        alertDialog.setMessage("Enter Address Or Select Address Options:");
        alertDialog.setCancelable(false);

        LayoutInflater inflater=this.getLayoutInflater();

        View order_address_comment=inflater.inflate(R.layout.order_address_comment,null);

         edtAddress=(PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        //Hide search icon before fragment
        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        //Set Hint for AutoComlplete EditText
        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("Tap Here To Enter Address");
        //set text size
        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(14);

        //Get Address from placeAutoComplete
        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shippingAddress=place;
            }

            @Override
            public void onError(Status status) {

                Log.e("ERROR",status.getStatusMessage());
            }


        });



        final MaterialEditText edtComment=(MaterialEditText)order_address_comment.findViewById(R.id.edtComment);

        //Radio
        final RadioButton rdiShipToAddress=(RadioButton)order_address_comment.findViewById(R.id.rdiShipToAddress);
        final RadioButton rdiHomeAddress=(RadioButton)order_address_comment.findViewById(R.id.rdiHomeAddress);
        final RadioButton rdiCOD=(RadioButton)order_address_comment.findViewById(R.id.rdiCOD);
        final RadioButton rdiPaypal=(RadioButton)order_address_comment.findViewById(R.id.rdiPaypal);
        final RadioButton rdiBalance=(RadioButton)order_address_comment.findViewById(R.id.rdiBalance);

        //Events Radio

        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    if (Common.currentUser.getHomeAddress() !=null ||
                            !TextUtils.isEmpty(Common.currentUser.getHomeAddress()))
                    {
                        address=Common.currentUser.getHomeAddress();
                        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                .setText(address);
                    }

                    else
                        Toast.makeText(Cart.this, "Please Update Your Address !!!", Toast.LENGTH_SHORT).show();

                }
            }
        });

        rdiShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Ship to this address feature
                if (isChecked)
                {
                  //  if(mLastLocation!=null) {
                        mGoogleMapService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng="+ mLastLocation.getLatitude()+","+mLastLocation.getLongitude()+"&sensor=true&key=AIzaSyDSNO6kfM3L3o2x0fkM-6jTXjI2iFKOfAY"))
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {

                                        //If fetch API
                                        try {
                                            JSONObject jsonObject = new JSONObject(response.body().toString());

                                            JSONArray resultsArray = jsonObject.getJSONArray("results");

                                            JSONObject firstObject = resultsArray.getJSONObject(0);


                                            address = firstObject.getString("formatted_address");  //set this address to edtAddress
                                            ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                                    .setText(address);


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {

                                        Toast.makeText(Cart.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                  //  }
                }
            }
        });

        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_local_grocery_store_black_24dp);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

              //  if(mLastLocation!=null) {
                    //Show Paypal To Payment
                    //Add Check Condition here
                    if (!rdiShipToAddress.isChecked() && !rdiHomeAddress.isChecked()) {

                        if (shippingAddress != null)
                            address = shippingAddress.getAddress().toString();
                        else {
                            Toast.makeText(Cart.this, "Please Enter Address Or Select Address Options !!!", Toast.LENGTH_SHORT).show();
                            //Fix Crash Fragment
                            getFragmentManager().beginTransaction()
                                    .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                    .commit();

                            return;
                        }
                    }
                    if (TextUtils.isEmpty(address)) {
                        Toast.makeText(Cart.this, "Please Enter Address Or Select Address Options !!!", Toast.LENGTH_SHORT).show();
                        //Fix Crash Fragment
                        getFragmentManager().beginTransaction()
                                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();

                        return;
                    }
                    commemt = edtComment.getText().toString();

                    //Check Payment
                    if (!rdiCOD.isChecked() && !rdiPaypal.isChecked() && !rdiBalance.isChecked()) {
                        Toast.makeText(Cart.this, "Please Select Payment Method Options !!!", Toast.LENGTH_SHORT).show();
                        //Fix Crash Fragment
                        getFragmentManager().beginTransaction()
                                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();

                        return;
                    } else if (rdiPaypal.isChecked()) {

                        String formatAmount = txtTotalPrice.getText().toString()
                                .replace("$", "")
                                .replace(",", "");

                        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(formatAmount),
                                "USD",
                                "Food Payment",
                                PayPalPayment.PAYMENT_INTENT_SALE);
                        Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
                        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
                        showRatingDialog();
                    } else if (rdiCOD.isChecked()) {
                        //crete new Request
                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                txtTotalPrice.getText().toString(),
                                "0", //status
                                commemt,
                                "COD",
                                "Unpaid",
                                String.format("%s,%s", mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                                cart
                        );

                        //Submit To Firebase
                        //We will Be Using System.CurrentMilli to key
                        String order_number = String.valueOf(System.currentTimeMillis());
                        requests.child(order_number).setValue(request);

                        //Delete Cart
                        new MyData(getBaseContext()).clearCart(Common.currentUser.getPhone());
                        sendNotificationOrder(order_number);

                        showRatingDialog();
                        Toast.makeText(Cart.this, "Thank You, Order Placed Successfully !!!", Toast.LENGTH_SHORT).show();
                       // finish();
                    } else if (rdiBalance.isChecked()) {
                        double amount = 0;
                        //First we will get total price txtTotalPrice
                        try {
                            amount = Common.formatCurrency(txtTotalPrice.getText().toString(), Locale.US).doubleValue();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        //After receiving total price of this order,just compare with user balance
                        if (Double.parseDouble(Common.currentUser.getBalance().toString()) >= amount) {
                            //crete new Request
                            Request request = new Request(
                                    Common.currentUser.getPhone(),
                                    Common.currentUser.getName(),
                                    address,
                                    txtTotalPrice.getText().toString(),
                                    "0", //status
                                    commemt,
                                    "TasteHub Wallet",
                                    "Paid",
                                    String.format("%s,%s", mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                                    cart
                            );

                            //Submit To Firebase
                            //We will Be Using System.CurrentMilli to key
                            final String order_number = String.valueOf(System.currentTimeMillis());
                            requests.child(order_number).setValue(request);

                            //Delete Cart
                            new MyData(getBaseContext()).clearCart(Common.currentUser.getPhone());

                            //Update Balance
                            double balance = Double.parseDouble(Common.currentUser.getBalance().toString()) - amount;
                            Map<String, Object> update_balance = new HashMap<>();
                            update_balance.put("balance", balance);

                            FirebaseDatabase.getInstance()
                                    .getReference("User")
                                    .child(Common.currentUser.getPhone())
                                    .updateChildren(update_balance)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //Refresh User
                                                FirebaseDatabase.getInstance()
                                                        .getReference("User")
                                                        .child(Common.currentUser.getPhone())
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                Common.currentUser = dataSnapshot.getValue(User.class);
                                                                //Send Orders to Server
                                                                sendNotificationOrder(order_number);

                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });
                                            }
                                        }
                                    });

                            showRatingDialog();
                            Toast.makeText(Cart.this, "Thank You, Order Placed Successfully !!!", Toast.LENGTH_SHORT).show();
                          //  finish();
                        } else {
                            Toast.makeText(Cart.this, "You Have Low Balance,Please Choose Other Payment Options !!!", Toast.LENGTH_SHORT).show();
                        }
                    }


                    //Remove Fragment
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
              //  }

            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //Remove Fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();


            }
        });
        alertDialog.show();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==PAYPAL_REQUEST_CODE)
        {
            if (resultCode==RESULT_OK)
            {
                PaymentConfirmation confirmation=data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation!=null)
                {
                    try {
                        String paymentDetail=confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject=new JSONObject(paymentDetail);

                        //crete new Request
                        Request request=new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                txtTotalPrice.getText().toString(),
                                "0", //status
                                commemt,
                                "Paypal",
                                jsonObject.getJSONObject("response").getString("state"),
                                String.format("%s,%s",mLastLocation.getLatitude(),mLastLocation.getLongitude()),
                                cart
                        );

                        //Submit To Firebase
                        //We will Be Using System.CurrentMilli to key
                        String order_number=String.valueOf(System.currentTimeMillis());
                        requests.child(order_number).setValue(request);

                        //Delete Cart
                        new MyData(getBaseContext()).clearCart(Common.currentUser.getPhone());
                        sendNotificationOrder(order_number);

                        Toast.makeText(Cart.this, "Thank You, Order Placed Successfully !!!", Toast.LENGTH_SHORT).show();
                      //  finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (resultCode==Activity.RESULT_CANCELED)
                Toast.makeText(this, "Payment Cancel !!!", Toast.LENGTH_SHORT).show();
            else if (resultCode==PaymentActivity.RESULT_EXTRAS_INVALID)
                Toast.makeText(this, "Invalid Payment !!!", Toast.LENGTH_SHORT).show();

        }

    }

    private void sendNotificationOrder(final String order_number) {



        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query data=tokens.orderByChild("isServerToken").equalTo(true); //get all node with isServerToken
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    Token serverToken=postSnapShot.getValue(Token.class);
                    Map<String,String>dataSend=new HashMap<>();
                    dataSend.put("title",Common.currentUser.getName());
                    dataSend.put("message","You Have New Order"+" "+order_number);
                    DataMessage dataMessage=new DataMessage(serverToken.getToken(),dataSend);

                    String test=new Gson().toJson(dataMessage);
                    Log.d("Content",test);

                    mService.sendNotification(dataMessage)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code()==200)
                                    {
                                        if (response.body().success == 1) {

                                            Toast.makeText(Cart.this, "Thank You, Order Placed Successfully !!!", Toast.LENGTH_SHORT).show();
                                        //    finish();
                                        } else {
                                            Toast.makeText(Cart.this, "Failed !!!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                    Log.e("ERROR",t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadListFood() {
        cart=new MyData(this).getCarts(Common.currentUser.getPhone());
        adapter=new CartAdapter(cart,this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        //Calculate Total Price
        int total=0;
        for (Order order:cart)
            total+=(Float.parseFloat(order.getPrice()))*(Float.parseFloat(order.getQuantity()));
        Locale locale=new Locale("en","US");
        NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));

        //Animation
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(final int position) {

        AlertDialog.Builder alertDialog=new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Hey, "+Common.currentUser.getName());
        alertDialog.setMessage("Do You Want To Delete This Food?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                cart.remove(position);
                new MyData(Cart.this).clearCart(Common.currentUser.getPhone());
                for (Order item:cart)
                    new MyData(Cart.this).addToCart(item);
                loadListFood();
                Toast.makeText(Cart.this, "Food Deleted Successfully", Toast.LENGTH_SHORT).show();

            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                Toast.makeText(Cart.this, "Cancel", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.setIcon(R.mipmap.unnamed);
        alertDialog.show();


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();
        startLocationUpdates();
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation!=null)
        {
            Log.d("Location","Your Location : "+mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
        }
        else
        {
            Log.d("Location","Could Not Get Your Location");
        }

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);

    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        displayLocation();

    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHolder)
        {
            String name=((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();
            final Order deleteItem=((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex=viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);
            new MyData(getBaseContext()).removeFromCart(deleteItem.getProductId(),Common.currentUser.getPhone());

            //Update txtTotal
            //Calculate Total Price
            int total=0;
            List<Order> orders=new MyData(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for (Order item:orders)
                total+=(Float.parseFloat(item.getPrice()))*(Float.parseFloat(item.getQuantity()));
            Locale locale=new Locale("en","US");
            NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);

            txtTotalPrice.setText(fmt.format(total));

            //Make Snackbar
            Snackbar snackbar=Snackbar.make(rootLayout,name+" "+"Removed From Cart !!!",Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new MyData(getBaseContext()).addToCart(deleteItem);

                    //Update txtTotal
                    //Calculate Total Price
                    int total=0;
                    List<Order> orders=new MyData(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for (Order item:orders)
                        total+=(Float.parseFloat(item.getPrice()))*(Float.parseFloat(item.getQuantity()));
                    Locale locale=new Locale("en","US");
                    NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);

                    txtTotalPrice.setText(fmt.format(total));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }


    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }

    private void enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(Cart.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error","Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(Cart.this, REQUEST_LOCATION);

                              //*  finish();

                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }
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
                .create(Cart.this)
                .show();


    }

    @Override
    public void onNegativeButtonClicked() {
        finish();
    }

    @Override
    public void onNeutralButtonClicked() {
        finish();
    }

    @Override
    public void onPositiveButtonClicked(int i,String s) {

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

                        Toast.makeText(Cart.this, "Thank You For Your Feedback !!!", Toast.LENGTH_SHORT).show();
                    }
                });
        finish();
    }
}
