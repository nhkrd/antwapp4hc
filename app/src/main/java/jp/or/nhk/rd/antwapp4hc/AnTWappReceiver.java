package jp.or.nhk.rd.antwapp4hc ;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class AnTWappReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("AntWappRecv", intent.getAction());

        if( intent.getAction().equals("jp.or.nhk.rd.antwapp4hc.intent.action.LAUNCH_WS") ) {
            Log.i("AnTWapp", "Receive LAUNCH_WS intent");

            Bundle extras = intent.getExtras();
            Log.i("AntWapp", "setClassName[WebViewActivity]");
            intent.setClassName("jp.or.nhk.rd.antwapp4hc", "jp.or.nhk.rd.antwapp4hc.WebViewActivity");

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.startActivity(intent);
        }
        else if( Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ) {
            Intent activityIntent = new Intent(context, WebViewActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
    }
}
