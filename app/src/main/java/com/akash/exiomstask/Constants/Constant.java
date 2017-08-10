package com.akash.exiomstask.Constants;

/**
 * Created by akash on 8/9/2017.
 */

public class Constant {

    public static final  int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1000;
    public static final int GOOGLE_API_CLIENT_ID = 1001;
    public static final int NOTIFICATION_ID = 1002;
    public static final int SETTINGS_REQUEST = 1003;
    public static final int REQUEST_LOCATION = 1004;


    //location
    public static final int UPDATE_INTERVAL = 10000; // 10 seconds
    public static final int FASTEST_INTERVAL = 20000; // 20 seconds

    //notification
    public static final String NOTIFICATION_TITLE = "ExiomsTask";
    public static final String NOTIFICATION_MESSAGE = "Your destination is just 1 km away!";

    public static final String LOCATION_PERMISSION_DENIED = "App don't have permission to Access Location, please go to settings->Apps->ExiomsTask and provide the permission.";
}
