package org.muffin.imgui.muffin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

public class Muffin extends Activity {

    static {
        System.loadLibrary("pixie");
    }

    public Muffin(Context context) {
        Start(context);
    }

    public static void Start(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            Toast.makeText(context.getApplicationContext(), "Overlay permission is required in order to show mod menu. Restart the game after you allow permission", Toast.LENGTH_LONG).show();
            context.startActivity(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + context.getPackageName())));
            final Handler handler = new Handler();
            handler.postDelayed(() -> System.exit(1), 5000);
        } else {
            final Handler handler = new Handler();
            handler.postDelayed(() -> context.startService(new Intent(context, MuffinService.class)), 5000);
        }
    }

}
