package com.shahardror.wetrip;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class SafeToast {

    public SafeToast() {}

    public void show(Context context, String message) {
        if (context != null) Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        else Log.i("safeToast", "Saved you! you tried toasting '"+message+"'");
    }
}
