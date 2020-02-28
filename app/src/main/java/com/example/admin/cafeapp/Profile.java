package com.example.admin.cafeapp;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.cafeapp.Common.Common;
import com.example.admin.cafeapp.Model.User;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.szagurskii.patternedtextwatcher.PatternedTextWatcher;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;
import dmax.dialog.SpotsDialog;
import info.hoang8f.widget.FButton;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Profile extends AppCompatActivity {
    MaterialEditText edtName,edtHomeAddress,edtEmail,edtDOB;
    MaterialSpinner spinner;
    TextView txt1,txt2;

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
        setContentView(R.layout.activity_profile);

        txt1=(TextView)findViewById(R.id.txtTag);
        txt2=(TextView)findViewById(R.id.txtText);

        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/TabarraShadowFFP.otf");
        txt1.setTypeface(tf);
        txt2.setTypeface(tf);

        FButton register=(FButton)findViewById(R.id.btnRegister);
        edtName=(MaterialEditText)findViewById(R.id.Profile_name);
        edtHomeAddress=(MaterialEditText)findViewById(R.id.Profile_address);
        edtEmail=(MaterialEditText)findViewById(R.id.Profile_email);
        edtDOB=(MaterialEditText)findViewById(R.id.Profile_DOB);
        edtDOB.addTextChangedListener(new PatternedTextWatcher("##-##-####"));

        spinner = (MaterialSpinner)findViewById(R.id.genderSpinner);
        spinner.setItems("Male","Female","Other");

        Animation animation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.blink);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(400);

        ImageView splash = (ImageView) findViewById(R.id.img_profile);
        splash.startAnimation(animation);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtName.getText().toString().trim().length()==0 ||
                        edtHomeAddress.getText().toString().trim().length()==0 ||
                        edtEmail.getText().toString().trim().length()==0 ||
                        edtDOB.getText().toString().trim().length()==0 ||
                        spinner.getText().toString().trim().length()==0){
                    Toast.makeText(Profile.this, "Please Enter All Details !!!", Toast.LENGTH_SHORT).show();
                }
                else if (!isValidEmail((CharSequence) edtEmail.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Enter Correct Email Address", Toast.LENGTH_SHORT).show();
                }

                else {
                    //Update Name
                    Map<String, Object> update_profile = new HashMap<>();
                    update_profile.put("name", edtName.getText().toString());
                    update_profile.put("homeAddress", edtHomeAddress.getText().toString());
                    update_profile.put("emailAddress", edtEmail.getText().toString());
                    update_profile.put("DOB", edtDOB.getText().toString());
                    update_profile.put("gender", spinner.getText().toString());


                    FirebaseDatabase.getInstance()
                            .getReference("User")
                            .child(Common.currentUser.getPhone())
                            .updateChildren(update_profile)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        Toast.makeText(Profile.this, "Welcome to the TasteHub !!!", Toast.LENGTH_SHORT).show();

                                }
                            });
                    Intent homeIntent = new Intent(Profile.this, Home.class);
                    startActivity(homeIntent);
                    finish();

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
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "You Can't Go Back, Please Enter All Details First", Toast.LENGTH_SHORT).show();
    }

    public final static boolean isValidEmail(CharSequence target)
    {
        if (TextUtils.isEmpty(target))
        {
            return false;
        } else {
            return Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }


}
