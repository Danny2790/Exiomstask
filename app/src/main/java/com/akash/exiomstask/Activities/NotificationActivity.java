package com.akash.exiomstask.Activities;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by akash on 8/10/2017.
 */

public class NotificationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Now finish, which will drop the user in to the activity that was at the top
        //  of the task stack
        finish();
    }
}
