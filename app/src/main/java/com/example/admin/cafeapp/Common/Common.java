package com.example.admin.cafeapp.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.text.format.DateFormat;

import com.example.admin.cafeapp.Model.Request;
import com.example.admin.cafeapp.Model.ShippingInformation;
import com.example.admin.cafeapp.Model.User;
import com.example.admin.cafeapp.Remote.APIService;
import com.example.admin.cafeapp.Remote.IGoogleService;
import com.example.admin.cafeapp.Remote.RetrofitClient;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class Common {

    public static String topicName="News";

    public static User currentUser;
    public static final int PICK_IMAGE_REQUEST = 71;

    public static String currentKey;

    public static Request currentRequest;

    public static String PHONE_TEXT="userPhone";

    public static final String INTENT_FOOD_ID="FoodId";

    private static final String BASE_URL="https://fcm.googleapis.com/";
    private static final String GOOGLE_API_URL="https://maps.googleapis.com/";

    public static final String DELETE="Delete";

    public static final String USER_KEY="User";
    public static final String PWD_KEY="Password";

    public static final int REQUEST_CODE=1000;

    public static APIService getFCMService()
    {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static IGoogleService getGoogleMapAPI()
    {
        return RetrofitClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    public static String convertCodeToStatus(String code)
    {
        if (code.equals("0"))
            return "Placed";
        else if (code.equals("1"))
            return "On Process";
        else if (code.equals("2"))
            return "Shipping";
        else
            return "Shipped";

    }

    public static boolean isConnectedToInternet(Context context)
    {
        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager!=null)
        {
            NetworkInfo[] info=connectivityManager.getAllNetworkInfo();
            if (info!=null)
            {
                for (int i=0;i<info.length;i++)
                {
                    if (info[i].getState()==NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    public static BigDecimal formatCurrency(String amount, Locale locale) throws ParseException, java.text.ParseException {
        NumberFormat format=NumberFormat.getCurrencyInstance(locale);
        if (format instanceof DecimalFormat)
            ((DecimalFormat)format).setParseBigDecimal(true);
        return (BigDecimal)format.parse(amount.replace("[^\\d.,]",""));
    }

    public static String getDate(long time)
    {
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date=new StringBuilder(
                DateFormat.format("dd-MM-yyyy HH:mm"
                ,calendar)
                .toString());
        return date.toString();
    }
}
