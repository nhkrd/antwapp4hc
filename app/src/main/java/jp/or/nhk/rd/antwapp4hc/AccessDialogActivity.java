package jp.or.nhk.rd.antwapp4hc ;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

public class AccessDialogActivity extends Activity {
    DialogInterface.OnClickListener onClickNo;
    DialogInterface.OnClickListener onClickYes;

    static AlertDialog alert ;
    String appName = null;
    String infoStr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("InfoDialog: ", "onCreate()");

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        appName = extras.getString("appName");
        infoStr = extras.getString("infoStr");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i("InfoDialog: ", "onNewIntent()");

        Bundle extras = intent.getExtras();
        appName = extras.getString("appName");
        infoStr = extras.getString("infoStr");
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.i("InfoDialog: ", "onResume() : " + appName);

//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this , R.style.MyAlertDialogStyle);
        builder.setTitle("以下の端末からアクセスされています。");
        builder.setMessage("\n端末情報： " + infoStr);

        final TextView t_view = new TextView(this);
        t_view.setText(appName);
        t_view.setTextSize(40);
        t_view.setGravity(Gravity.CENTER);
        builder.setView(t_view);

        builder.setPositiveButton("確認", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                WebViewActivity.getActivity().dismissAccessDialog(false);
                dialog.cancel();
                finish();
            }
        });
        alert = builder.create();
        alert.show();
    }

    @Override
    public void onPause(){
        super.onPause();

        Log.i("infoDiglog: ", "onPause()");
        if(alert != null) {
            alert.dismiss();
            alert = null;
        }
    }
}
