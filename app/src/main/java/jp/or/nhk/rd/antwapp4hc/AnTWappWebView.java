package jp.or.nhk.rd.antwapp4hc ;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONjava.JSONObject;

//
// JavaScriptから利用できるオブジェクトのクラス
//
class AnTWappWebView {
    @JavascriptInterface
    public void launchApp( String params ) {
        Log.i("launchApp: ", params);
        //try {
        JSONObject pobj = new JSONObject(params);
        if( pobj.has("action") ) {
            Intent intent = new Intent(Intent.ACTION_MAIN);

            String action =  (String)pobj.get("action") ;
            intent.setAction( action ) ;

            if( pobj.has("packagename") && pobj.has("classname") ) {
                String packagename =  (String)pobj.get("packagename") ;
                String classname =  (String)pobj.get("classname") ;

                intent.setClassName(packagename, classname);
            }

            if( pobj.has("data") ) {
                String data =  (String)pobj.get("data") ;

                intent.setData( Uri.parse( data ) );
            }

            if( pobj.has("flags") ) {
                int flags =  (int)pobj.get("flags") ;

                intent.setFlags(flags);
            }
            WebViewActivity.getContext().startActivity(intent);
        }
        //}
        //catch( e) {
        //}
    }

    @JavascriptInterface
    public void dispatchKeyEventEnable( String param ) {
        if( param.equals("true") ) {
            WebViewActivity.setDispatchKeyEventEnable(true);
        }
        else {
            WebViewActivity.setDispatchKeyEventEnable(false);
        }
    }
}
