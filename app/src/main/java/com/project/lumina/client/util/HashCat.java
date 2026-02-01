package com.project.lumina.client.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import java.security.MessageDigest;
import java.util.Locale;

public class HashCat {
    private static final String TAG = "McDeHasher";
    private static HashCat instance;

    
    public native String getSignaturesSha1(Context context);
    public native boolean checkSha1(Context context);
    public native String getToken(Context context, String userId);

    
    static {
        System.loadLibrary("native-lib");
    }

    
    public static synchronized HashCat getInstance() {
        if (instance == null) {
            instance = new HashCat();
        }
        return instance;
    }

    private HashCat() {
        
    }


    public boolean LintHashInit(Context context) {
        boolean isValid = checkSha1(context);

        if (!isValid) {
            String sha1 = getSha1Value(context);
            String errorMessage = "";
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                });

                
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, errorMessage);
                
                if (context instanceof Activity) {
                    ((Activity) context).finishAffinity();
                }
                Process.killProcess(Process.myPid());
                System.exit(0);
            }
            return false;
        }

        return true;
    }


    public String getSha1Value(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuilder hexString = new StringBuilder();

            for (byte b : publicKey) {
                String appendString = Integer.toHexString(0xFF & b).toUpperCase(Locale.US);
                if (appendString.length() == 1) {
                    hexString.append("0");
                }
                hexString.append(appendString);
            }

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public String getTokenForUser(Context context, String userId) {
        return getToken(context, userId);
    }
}