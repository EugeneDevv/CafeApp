package com.example.admin.cafeapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.admin.cafeapp.Common.Common;
import com.example.admin.cafeapp.Helper.DirectionJSONParser;
import com.example.admin.cafeapp.Model.Request;
import com.example.admin.cafeapp.Model.ShippingInformation;
import com.example.admin.cafeapp.Remote.IGoogleService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;
import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.admin.cafeapp.Cart.REQUEST_LOCATION;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback,ValueEventListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    DatabaseReference requests,shippingOrder;
    FirebaseDatabase database;
    Request currentOrder;
    IGoogleService mService;
    Marker shippingMarker;
    Polyline polyline;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Todo Location Already on  ... start
        final LocationManager manager = (LocationManager) TrackingOrder.this.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(TrackingOrder.this)) {
            Toast.makeText(TrackingOrder.this,"GPS Is Enabled",Toast.LENGTH_SHORT).show();
        }
        // Todo Location Already on  ... end

        if(!hasGPSDevice(TrackingOrder.this)){
            Toast.makeText(TrackingOrder.this,"Gps not Supported",Toast.LENGTH_SHORT).show();
        }

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(TrackingOrder.this)) {
            Log.e("Shyam","Gps already enable");
            Toast.makeText(TrackingOrder.this,"Please Enable Your GPS",Toast.LENGTH_SHORT).show();
            enableLoc();
        }else{
            Log.e("Shyam","Gps already enabled");
            Toast.makeText(TrackingOrder.this,"GPS Is Enabled",Toast.LENGTH_SHORT).show();
        }

        //Check Permission
        if ( ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.CALL_PHONE
            }, Common.REQUEST_CODE);
        }

        database=FirebaseDatabase.getInstance();
        requests=database.getReference("Requests");
        shippingOrder=database.getReference("ShippingOrders");
        mService=Common.getGoogleMapAPI();
        shippingOrder.addValueEventListener(this);



    }

    @Override
    protected void onStop() {
        shippingOrder.removeEventListener(this);
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        boolean isSuccess=mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.my_map_style));
        if (!isSuccess)
            Log.d("ERROR","Map Style Load Failed !!!");
        mMap.getUiSettings().setZoomControlsEnabled(true);
        trackingLocation();
    }

    private void trackingLocation() {

            requests.child(Common.currentKey)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            currentOrder = dataSnapshot.getValue(Request.class);
                            //If Order has address
                                if (currentOrder.getAddress() != null && !currentOrder.getAddress().isEmpty()) {

                                    mService.getLocationFromAddress(new StringBuilder("https://maps.googleapis.com/maps/api/geocode/json?sensor=false&key=AIzaSyDSNO6kfM3L3o2x0fkM-6jTXjI2iFKOfAY&address=")
                                            .append(currentOrder.getAddress()).toString())
                                            .enqueue(new Callback<String>() {
                                                @Override
                                                public void onResponse(Call<String> call, Response<String> response) {
                                                    try {
                                                        JSONObject jsonObject = new JSONObject(response.body());

                                                        String lat = ((JSONArray) jsonObject.get("results"))
                                                                .getJSONObject(0)
                                                                .getJSONObject("geometry")
                                                                .getJSONObject("location")
                                                                .get("lat").toString();

                                                        String lng = ((JSONArray) jsonObject.get("results"))
                                                                .getJSONObject(0)
                                                                .getJSONObject("geometry")
                                                                .getJSONObject("location")
                                                                .get("lng").toString();

                                                        LatLng location = new LatLng(Double.parseDouble(lat),
                                                                Double.parseDouble(lng));

                                                        mMap.addMarker(new MarkerOptions().position(location)
                                                                .title("Order Destination")
                                                                .icon(BitmapDescriptorFactory.defaultMarker()));

                                                        //set shipper location
                                                        shippingOrder.child(Common.currentKey)
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        final ShippingInformation shippingInformation = dataSnapshot.getValue(ShippingInformation.class);
                                                                        LatLng shipperLocation = new LatLng(shippingInformation.getLat(), shippingInformation.getLng());

                                                                            ImageView btnCall = (ImageView) findViewById(R.id.btnCall);
                                                                            btnCall.setOnClickListener(new View.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(View v) {

                                                                                    Intent intent = new Intent(Intent.ACTION_CALL);
                                                                                    intent.setData(Uri.parse("tel:" + shippingInformation.getShipperPhone()));
                                                                                    if (ActivityCompat.checkSelfPermission(TrackingOrder.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                                                                                        return;
                                                                                    }
                                                                                    startActivity(intent);
                                                                                }
                                                                            });

                                                                            if (shippingMarker == null) {
                                                                                shippingMarker = mMap.addMarker(
                                                                                        new MarkerOptions()
                                                                                                .position(shipperLocation)
                                                                                                .title("Shipper " + shippingInformation.getShipperPhone())
                                                                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                                                                );
                                                                            } else {
                                                                                shippingMarker.setPosition(shipperLocation);
                                                                            }

                                                                            //Update Camera
                                                                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                                                                    .target(shipperLocation)
                                                                                    .zoom(16)
                                                                                    .bearing(0)
                                                                                    .tilt(45)
                                                                                    .build();
                                                                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                                                            //Draw Routes
                                                                            if (polyline != null)
                                                                                polyline.remove();

                                                                            mService.getDirections(shipperLocation.latitude + "," + shipperLocation.longitude,
                                                                                    currentOrder.getAddress(), "AIzaSyDSNO6kfM3L3o2x0fkM-6jTXjI2iFKOfAY")
                                                                                    .enqueue(new Callback<String>() {
                                                                                        @Override
                                                                                        public void onResponse(Call<String> call, Response<String> response) {
                                                                                            new ParserTask().execute(response.body().toString());
                                                                                        }

                                                                                        @Override
                                                                                        public void onFailure(Call<String> call, Throwable t) {

                                                                                        }
                                                                                    });


                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    }
                                                                });

                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<String> call, Throwable t) {

                                                }
                                            });
                                }
                                //If order has latLng
                                else if (currentOrder.getLatLng() != null && !currentOrder.getLatLng().isEmpty()) {
                                    mService.getLocationFromAddress(new StringBuilder("https://maps.googleapis.com/maps/api/geocode/json?sensor=false&key=AIzaSyDSNO6kfM3L3o2x0fkM-6jTXjI2iFKOfAY&latlng=")
                                            .append(currentOrder.getLatLng()).toString())
                                            .enqueue(new Callback<String>() {
                                                @Override
                                                public void onResponse(Call<String> call, Response<String> response) {
                                                    try {
                                                        JSONObject jsonObject = new JSONObject(response.body());

                                                        String lat = ((JSONArray) jsonObject.get("results"))
                                                                .getJSONObject(0)
                                                                .getJSONObject("geometry")
                                                                .getJSONObject("location")
                                                                .get("lat").toString();

                                                        String lng = ((JSONArray) jsonObject.get("results"))
                                                                .getJSONObject(0)
                                                                .getJSONObject("geometry")
                                                                .getJSONObject("location")
                                                                .get("lng").toString();

                                                        LatLng location = new LatLng(Double.parseDouble(lat),
                                                                Double.parseDouble(lng));

                                                        mMap.addMarker(new MarkerOptions().position(location)
                                                                .title("Order Destination")
                                                                .icon(BitmapDescriptorFactory.defaultMarker()));

                                                        //set shipper location
                                                        shippingOrder.child(Common.currentKey)
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        final ShippingInformation shippingInformation = dataSnapshot.getValue(ShippingInformation.class);
                                                                        LatLng shipperLocation = new LatLng(shippingInformation.getLat(), shippingInformation.getLng());

                                                                        ImageView btnCall = (ImageView) findViewById(R.id.btnCall);
                                                                        btnCall.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View v) {

                                                                                Intent intent = new Intent(Intent.ACTION_CALL);
                                                                                intent.setData(Uri.parse("tel:" + shippingInformation.getShipperPhone()));
                                                                                if (ActivityCompat.checkSelfPermission(TrackingOrder.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                                                                                    return;
                                                                                }
                                                                                startActivity(intent);
                                                                            }
                                                                        });

                                                                        if (shippingMarker == null) {
                                                                            shippingMarker = mMap.addMarker(
                                                                                    new MarkerOptions()
                                                                                            .position(shipperLocation)
                                                                                            .title("Shipper " + shippingInformation.getShipperPhone())
                                                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                                                            );
                                                                        } else {
                                                                            shippingMarker.setPosition(shipperLocation);
                                                                        }

                                                                        //Update Camera
                                                                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                                                                .target(shipperLocation)
                                                                                .zoom(16)
                                                                                .bearing(0)
                                                                                .tilt(45)
                                                                                .build();
                                                                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                                                        //Draw Routes
                                                                        if (polyline != null)
                                                                            polyline.remove();

                                                                        mService.getDirections(shipperLocation.latitude + "," + shipperLocation.longitude,
                                                                                currentOrder.getLatLng(), "AIzaSyDSNO6kfM3L3o2x0fkM-6jTXjI2iFKOfAY")
                                                                                .enqueue(new Callback<String>() {
                                                                                    @Override
                                                                                    public void onResponse(Call<String> call, Response<String> response) {
                                                                                        new ParserTask().execute(response.body().toString());
                                                                                    }

                                                                                    @Override
                                                                                    public void onFailure(Call<String> call, Throwable t) {

                                                                                    }
                                                                                });
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    }
                                                                });

                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<String> call, Throwable t) {

                                                }
                                            });
                                }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        trackingLocation();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>> {
        AlertDialog mDialog=new SpotsDialog.Builder().setContext(TrackingOrder.this).build();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please Wait . . .");
            try {
                mDialog.show();
            } catch (WindowManager.BadTokenException e) {
                Log.e("WindowManagerBad ", e.toString());
            }

        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String,String>>> routes=null;
            try {
                jsonObject=new JSONObject(strings[0]);
                DirectionJSONParser parser=new DirectionJSONParser();
                routes=parser.parse(jsonObject);


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();
           // ArrayList points=null;
          //  PolylineOptions lineOptions=null;
            ArrayList<LatLng> points = new ArrayList<LatLng>();
            PolylineOptions lineOptions = new PolylineOptions();
            MarkerOptions markerOptions = new MarkerOptions();
            for (int i=0;i<lists.size();i++)
            {
                points=new ArrayList();
                lineOptions=new PolylineOptions();
                List<HashMap<String,String>> path=lists.get(i);

                for (int j=0;j<path.size();j++)
                {
                    HashMap<String,String>point=path.get(j);
                    double lat=Double.parseDouble(point.get("lat"));
                    double lng=Double.parseDouble(point.get("lng"));

                    LatLng position=new LatLng(lat,lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
            }
            if (points.size()!=0)
                polyline= mMap.addPolyline(lineOptions);
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
            googleApiClient = new GoogleApiClient.Builder(TrackingOrder.this)
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
                                status.startResolutionForResult(TrackingOrder.this, REQUEST_LOCATION);

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
}
