package jp.or.nhk.rd.antwapp4hc ;

import android.content.Context;
import android.content.Intent;

import android.content.res.AssetManager;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.view.animation.AnimationUtils;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;

import android.net.http.SslError;
import android.os.Build;
import android.widget.ViewSwitcher;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.Runnable;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import javax.net.ssl.HttpsURLConnection;
//import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONjava.JSONObject;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class WebViewActivity extends FragmentActivity  {
    static String console_url    = Const.console_url;
    static String hybridcast_url = Const.hybridcast_url;
    static String hcsub_url      = Const.hcsub_url;
    static String tune_url       = Const.tune_url;
    static String config_path    = Const.config_path;

    private static Context context;
    private static FragmentActivity ownActivity = null;
    private static AnTWappConfigManager configMan = null;

    private static String curr_url = console_url;
    private static WebView webview = null;
    private static WebView webview_console = null;
    private static WebView webview_hc = null;
    private static WebView webview_hcsub = null;

    private static ViewSwitcher viewSwitcher;
    private static Boolean view_toggle = false ;

    private static Boolean isOnPageFinished = false;
    private static String antwapp_status = DIALRestHandlerInterface.DialApplicationState.Stopped;
    //private static JSONObject configJSON = null;

    private static Boolean dispatchKeyEventEnable = true ;
    private static List<Message> logdisp_buffer = new ArrayList<Message>();

    private Bundle newExtra = null;
    Boolean showAccessDialog = false;
    String showAccessDialogAppName = "";


    /**
     * MessageType
     */
    public enum MessageType {
        URL(0),
        JS(1);

        private final int id;
        private MessageType(final int id) {
            this.id = id;
        }
        public int getId() {
            return id;
        }
    };

    final static Handler webview_msghandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == MessageType.URL.getId()) { // messageTypeがURLの場合
                String[] urlinfo = (String[])msg.obj;
                Log.i("Message URL: ", String.format("%s, %s", urlinfo[0], urlinfo[1]) );
                String hcViewMode = (String)configMan.get( Const.Config.hcViewMode.Name );

                webview_console.evaluateJavascript( urlinfo[0], new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                    }
                });
                if( hcViewMode.equals( Const.Config.hcViewMode.Value.Full ) || hcViewMode.equals( Const.Config.hcViewMode.Value.Both ) ) {
                    WebViewActivity.switch_view("hc");
                    webview_hcsub.loadUrl(hcsub_url);
                    String req_url = urlinfo[1];
                    webview_hc.loadUrl(req_url);
                    curr_url = req_url;
                }
                else {
                    webview_hcsub.loadUrl("");
                    webview_hc.loadUrl("");
                }
            }
            else if (msg.arg1 == MessageType.JS.getId()) {
                String jsstr = (String)msg.obj;
                Log.i("wvconsole_handler: ", jsstr );

                webview_console.evaluateJavascript( jsstr, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                    }
                });
            }
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i("handleMessage: ", (String)(msg.obj));
            JSONObject keyobj = new JSONObject();
            keyobj.put("msg", ((String)(msg.obj)).toString() );

            String script = "javascript:dispatchKeyEvent(" + keyobj.toString() + ");";
            webview.loadUrl(script);
/*
            switch (msg.what) {
                case 1:
                    // msg.objはObject型なのでキャストする必要がある
                    Log.d("Handler", (String) msg.obj);
                    break;
                default:
                    break;
            }
*/
        }
    };


//    public void sendwebview(String msg) {
//        String script = "javascript:dispatchKeyEvent(" + msg + ");";
//        webview.loadUrl(script);
//    }

    public static Context getContext() {
        return WebViewActivity.context; // TODO: contextCompatを返すのが正しい？
    }
    public static WebViewActivity getActivity() {
        return (WebViewActivity)WebViewActivity.ownActivity;
    }
    public static String getAppStatus() {
        return WebViewActivity.antwapp_status;
    }
//    public static Map<String, Object> getConfig() {
//        return WebViewActivity.config;
//    }
    public static AnTWappConfigManager configMan(){
        return WebViewActivity.configMan;
    }


    public static void setDispatchKeyEventEnable(Boolean b) {
        WebViewActivity.dispatchKeyEventEnable = b;
    }

    /**
     * ローカルファイル読み込み
     * @param path
     * @return
     * @throws IOException
     */
    public static byte[] readAssetTextFile(String path) throws FileNotFoundException, IOException {
        String readBuf = "";
        int offset = 0;
        int bytesRead = 0;
        byte[] data = null;
        byte[] b = new byte[1024];

        AssetManager as = WebViewActivity.getContext().getResources().getAssets();
        try {
            int readlen;
            InputStream iStream = as.open(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ( (readlen = iStream.read(b, 0, 1024)) > 0 ) {
//                Log.i("readAssetTextFile:", String.format("readlen: %d", readlen));
                baos.write(b, 0, readlen );
            }
            baos.close();
            iStream.close();
            data = baos.toByteArray();
        } catch (FileNotFoundException e) {
            //TODO:forOSS
            Log.i("readAssetTextFile:", String.format("FileNotFoundException: %s, %s", path, e.toString()));
        }
        catch (IOException e) {
            //TODO:forOSS
            Log.i("readAssetTextFile:", String.format("Error: %s, %s", path, e.toString()));
        }

        return data;
    }

    /**
     * Get methodによるHTTPアクセス
     * @param strGetUrl URL
     * @return
     */
    public static Map<String, Object> getHTTP(String strGetUrl, Integer timeout) {
        HttpURLConnection con = null;
        StringBuffer result = new StringBuffer();
        int status = Const.HTTP.InternalServerError.code();
        String retStr = "";
        Map<String, Object> ret = new HashMap<String, Object>();

        try {
            URL url = new URL(strGetUrl);
            if(url.getProtocol().equals("https")) {
                con = (HttpsURLConnection) url.openConnection();
            }else if(url.getProtocol().equals("http")) {
                con = (HttpURLConnection) url.openConnection();
            }
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept-Language", "jp");
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);
            con.connect();

            // HTTPレスポンスコード
            status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                // 通信に成功した
                // テキストを取得する
                final InputStream in = con.getInputStream();
                String encoding = con.getContentEncoding();
                if (null == encoding) {
                    encoding = "UTF-8";
                }
                final InputStreamReader inReader = new InputStreamReader(in, encoding);
                final BufferedReader bufReader = new BufferedReader(inReader);
                String line = null;
                // 1行ずつテキストを読み込む
                while ((line = bufReader.readLine()) != null) {
                    if(0 < result.length()) {
                        result.append("\n");
                    }
                    result.append(line);
                }
                bufReader.close();
                inReader.close();
                in.close();

                retStr = result.toString();
            } else {
                Log.i("getHTTP:", String.format("Error: %d", status));
            }

        } catch (EOFException e1) {
            Log.i("getHTTP:", String.format("Error: EOFException"));
        } catch (FileNotFoundException e1) {
            Log.i("getHTTP:", String.format("Error: FileNotFoundException"));
        } catch (SocketTimeoutException e){
            Log.i("getHTTP:", String.format("Error: SocketTimeoutException"));
        } catch (Exception e1) {
            Log.i("getHTTP:", String.format("Error: Exception"));
            e1.printStackTrace();
        } finally {
            if (con != null) {
                // コネクションを切断
                con.disconnect();
            }
        }

        ret.put(Const.HTTP.Status, status);
        ret.put(Const.HTTP.Response, retStr);
        return ret;
    }

    /**
     * Post methodによるHTTPアクセス
     * @param strPostUrl
     * @param JSON
     * @return
     */
    public static Map<String, Object> postHTTP(String strPostUrl, String JSON, Integer timeout) {
        HttpURLConnection con = null;
        StringBuffer result = new StringBuffer();
        int status = Const.HTTP.InternalServerError.code();
        String retStr = "";
        Map<String, Object> ret = new HashMap<String, Object>();

        Log.i("post-http: ", strPostUrl);
        try {
            URL url = new URL(strPostUrl);
            if(url.getProtocol().equals("https")) {
                con = (HttpsURLConnection) url.openConnection();
            }else if(url.getProtocol().equals("http")) {
                con = (HttpURLConnection) url.openConnection();
            }
            // HTTPリクエストコード
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);
            con.setRequestProperty("Accept-Language", "jp");
            // データがJSONであること、エンコードを指定する
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            // POSTデータの長さを設定
            con.setRequestProperty("Content-Length", String.valueOf(JSON.length()));
            // リクエストのbodyにJSON文字列を書き込む
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
            out.write(JSON);
            out.flush();
            con.connect();

            // HTTPレスポンスコード
            status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                // 通信に成功した
                // テキストを取得する
                final InputStream in = con.getInputStream();
                String encoding = con.getContentEncoding();
                if (null == encoding) {
                    encoding = "UTF-8";
                }
                final InputStreamReader inReader = new InputStreamReader(in, encoding);
                final BufferedReader bufReader = new BufferedReader(inReader);
                String line = null;
                // 1行ずつテキストを読み込む
                while ((line = bufReader.readLine()) != null) {
                    result.append(line);
                }
                bufReader.close();
                inReader.close();
                in.close();
                con.disconnect();
                con = null;

                retStr = result.toString();
            } else {
                // 通信が失敗した場合のレスポンスコードを表示
                Log.i("postHTTP:", String.format("Error: %d", status));
            }
        } catch (SocketTimeoutException e){
        } catch (IOException e){
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (con != null) {
                // コネクションを切断
                con.disconnect();
            }
        }

        ret.put(Const.HTTP.Status, status);
        ret.put(Const.HTTP.Response, retStr);
        return ret;
    }



    /**
     * WebViewへのメッセージ送信
     * @param msg
     */
    public static void sendmsg(Message msg) {
        Log.i("antwapp: ", "sendmsg()");

        if( antwapp_status.equals( DIALRestHandlerInterface.DialApplicationState.Running )) {
            webview_msghandler.sendMessage(msg);
        }
        else {
            if (msg.arg1 == MessageType.URL.getId()) {
                String[] urlinfo = (String[])msg.obj;

                Intent activityIntent = new Intent(context, WebViewActivity.class);
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activityIntent.putExtra("command", Const.Command.tune);
                activityIntent.putExtra("debugmsg", urlinfo[0]);
                activityIntent.putExtra("hcurl", urlinfo[1]);
                context.startActivity(activityIntent);
            }
            else {
                //TODO:forOSS
                //logdisp_buffer.add(msg);
            }
        }
    }
    private void activity_sendmsg(WebViewActivity.MessageType type, Object obj) {
        Message msg = new Message();
        msg.arg1 = type.getId();
        msg.obj = obj;
        sendmsg(msg);
    }

    /**
     * Viewの切替
     * @param mode
     */
    public static void switch_view( String mode ) {
        Boolean doSwicth = false;
        String hcViewMode = (String)configMan.get( Const.Config.hcViewMode.Name );
        if( hcViewMode.equals( Const.Config.hcViewMode.Value.Full ) || hcViewMode.equals( Const.Config.hcViewMode.Value.Both ) ) {
            if( mode.equals("toggle")) {
                doSwicth = true;
            }
            else if( mode.equals("console")) {
                if( webview != webview_console) {
                    doSwicth = true;
                }
            }
            else if( mode.equals("hc")) {
                if( webview != webview_hc) {
                    doSwicth = true;
                }
            }

            if( doSwicth ) {
                if (WebViewActivity.view_toggle) {
                    webview = webview_console;
                    WebViewActivity.viewSwitcher.setInAnimation(AnimationUtils.loadAnimation(WebViewActivity.context, R.anim.slide_in_from_left));
                    WebViewActivity.viewSwitcher.setOutAnimation(AnimationUtils.loadAnimation(WebViewActivity.context, R.anim.slide_out_to_right));
                    WebViewActivity.viewSwitcher.showNext();

                    WebViewActivity.view_toggle = false;
                } else {
                    webview = webview_hc;
                    WebViewActivity.viewSwitcher.setInAnimation(AnimationUtils.loadAnimation(WebViewActivity.context, R.anim.slide_in_from_right));
                    WebViewActivity.viewSwitcher.setOutAnimation(AnimationUtils.loadAnimation(WebViewActivity.context, R.anim.slide_out_to_left));
                    WebViewActivity.viewSwitcher.showPrevious();

                    WebViewActivity.view_toggle = true;
                }
            }
        }
    }

    /**
     * show Access Dialog
     */
    public void showAccessDialog(String appName, String infoStr) {
        Log.i("antwappwebview: ", "showAccessDialog()");

        if( !showAccessDialogAppName.equals(appName) ) {
            Intent i = new Intent(WebViewActivity.getContext(), AccessDialogActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("appName", appName);
            i.putExtra("infoStr", infoStr);
            WebViewActivity.getContext().startActivity(i);

            showAccessDialog = true;
            showAccessDialogAppName = appName;
        }
    }

    /**
     * dismiss AccessDialog
     */
    public void dismissAccessDialog(Boolean needIntent) {
        Log.i("antwappwebview: ", "dismissAccessDialog()");
        showAccessDialog = false;

        //Dialog.dismiss();
        if( needIntent ) {
            Intent i = new Intent(WebViewActivity.getContext(), AccessDialogActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("appName", "finish");
            WebViewActivity.getContext().startActivity(i);
        }
    }
    /**
     * dismiss Dialog
     */
    public void showInfoDialogOK(Boolean needIntent) {
        Log.i("antwappwebview: ", "showAccessDialogOK()");

        if( needIntent ) {
            Intent i = new Intent(WebViewActivity.getContext(), AccessDialogActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("appName", "infook");
            WebViewActivity.getContext().startActivity(i);
        }
    }
    /**
     * isShowInfoDialog
     */
    public Boolean isShowAccessDialog() {
        return showAccessDialog;
    }


    /**
     * dispatchKeyEvent
     * @param event
     * @return
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Boolean retc = true;
        Boolean seturl = true;

        if( dispatchKeyEventEnable ) {
            JSONObject keyobj = new JSONObject();
            //try {
            int keyCode = event.getKeyCode();
            int action = event.getAction();
            String ch = event.getCharacters();

            //Log.i("dispatchKeyEvent: ", String.valueOf(keyCode));

            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                keyCode = 13;
            } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                keyCode = 4;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                keyCode = 37;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                keyCode = 38;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                keyCode = 39;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                keyCode = 40;
            } else if (keyCode == KeyEvent.KEYCODE_0) {
                keyCode = 48;
            } else if (keyCode == KeyEvent.KEYCODE_1) {
                keyCode = 49;
            } else if (keyCode == KeyEvent.KEYCODE_2) {
                keyCode = 50;
            } else if (keyCode == KeyEvent.KEYCODE_3) {
                keyCode = 51;
            } else if (keyCode == KeyEvent.KEYCODE_4) {
                keyCode = 52;
            } else if (keyCode == KeyEvent.KEYCODE_5) {
                keyCode = 53;
            } else if (keyCode == KeyEvent.KEYCODE_6) {
                keyCode = 54;
            } else if (keyCode == KeyEvent.KEYCODE_7) {
                keyCode = 55;
            } else if (keyCode == KeyEvent.KEYCODE_8) {
                keyCode = 56;
            } else if (keyCode == KeyEvent.KEYCODE_9) {
                keyCode = 57;
            } else if (keyCode == KeyEvent.KEYCODE_11) {
                keyCode = 1024;
            } else if (keyCode == KeyEvent.KEYCODE_12) {
                keyCode = 1025;
            } else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
                keyCode = 1048;
            } else if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
                keyCode = 1049;
            } else if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
                keyCode = 1050;
            } else if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
                keyCode = 1051;
            } else if (keyCode == KeyEvent.KEYCODE_TV_DATA_SERVICE) {
                keyCode = 1052;
                if (action == KeyEvent.ACTION_DOWN) {
                    switch_view( "toggle" );
                }
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                keyCode = 1060;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
                keyCode = 1061;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
                keyCode = 1062;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                keyCode = 1063;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                keyCode = 1064;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                keyCode = 1065;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                keyCode = 1066;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_RECORD) {
                keyCode = 1067;
            } else if (keyCode == 82) {
                keyCode = 1052;
                if (action == KeyEvent.ACTION_DOWN) {
                    switch_view( "toggle" );
                }
            }

            keyobj.put("keyCode", keyCode);

            if (action == KeyEvent.ACTION_DOWN) {
                keyobj.put("type", "keydown");
            } else if (action == KeyEvent.ACTION_UP) {
                keyobj.put("type", "keyup");
            }

            if (ch != null) {
                keyobj.put("key", ch.toString());
            }


            //ESC key: Going to HOME Screen
            if ((action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ESCAPE)) {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
            }

            if ( (keyCode == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_BACK) ) {
                retc = false;
            }
        }

        if (retc) {
            return super.dispatchKeyEvent(event);
        } else {
            return false;
        }
    }


    /**
     * onCreate
     *
     * @param savedInsanceState
     */
    @Override
    public void onCreate(Bundle savedInsanceState) {
        Log.i("antwapp: ", "onCreate()");

//        WebViewActivity.context = getApplicationContext();
        this.context = getApplicationContext();
        this.ownActivity = this;
        super.onCreate(savedInsanceState);

        //
        //Start HTTP & WS Server
        //
        ////////////////////////////////////////////////////////////////////
        new Thread(new Runnable() {
            public void run() {
                HTTPServer wsserver = new HTTPServer();
                try {
                    wsserver.start();
                } catch (Exception e) {
                }
            }
        }).start();
        ////////////////////////////////////////////////////////////////////

        //create instance
        configMan = new AnTWappConfigManager( config_path );

        //
        //Load config
        //
        configMan.loadConfig(context);
        setContentView(R.layout.activity_webview);
        WebViewActivity.viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher1);

        //Set Console View
        webview_console = (WebView) findViewById(R.id.web_view_console);
        webview_console.clearCache(true);
        webview_console.setInitialScale(1);
        webview_console.getSettings().setDomStorageEnabled(true);
        webview_console.getSettings().setUseWideViewPort(true);
        webview_console.getSettings().setLoadWithOverviewMode(true);
        webview_console.getSettings().setBuiltInZoomControls(true);
        webview_console.getSettings().setSupportZoom(true);
        webview_console.getSettings().setMediaPlaybackRequiresUserGesture(false);

        webview_console.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                isOnPageFinished = true;
                antwapp_status = DIALRestHandlerInterface.DialApplicationState.Running;

                if (0 < logdisp_buffer.size()) {
                    for (Message msg : logdisp_buffer) {
                        if (msg.arg1 == MessageType.JS.getId()) {
                            webview_msghandler.sendMessage(msg);
                        }
                    }
                    logdisp_buffer.clear();
                }
            }

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onLoadResource(WebView view, String url) {
//                if (url.contains("https://www.example.com/")) {
//                    webview_main.stopLoading();
//                    final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    mActivity.startActivity(i);
//                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//                if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
//                    return OptionsAllowResponse.build();
//                }
                return null;
            }
        });
        // JavaScript側へオブジェクトを追加する
        webview_console.getSettings().setUserAgentString((String) configMan.get( Const.Config.userAgent.Name ));
        webview_console.getSettings().setJavaScriptEnabled(true);
        webview_console.addJavascriptInterface(new AnTWappWebView(), "antwapp");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webview_console.setWebContentsDebuggingEnabled(true);
            webview_console.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        webview_console.loadUrl(console_url);
        webview = webview_console;


        //Set Hybridcast View
        webview_hc = (WebView) findViewById(R.id.web_view_hc);
        webview_hc.clearCache(true);
        webview_hc.setInitialScale(1);
        webview_hc.getSettings().setDomStorageEnabled(true);
        webview_hc.getSettings().setUseWideViewPort(true);
        webview_hc.getSettings().setLoadWithOverviewMode(true);
        webview_hc.getSettings().setBuiltInZoomControls(true);
        webview_hc.getSettings().setSupportZoom(true);
        webview_hc.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webview_hc.setWebViewClient(new WebViewClient() {
            public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                           SslError error) {
                handler.proceed();
            }

            @Override
            public void onLoadResource(WebView view, String url) {
//                if (url.contains("https://www.example.com/")) {
//                    webview.stopLoading();
//                    final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    mActivity.startActivity(i);
//                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//                if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
//                    return OptionsAllowResponse.build();
//                }
                return null;
            }
        });
        // JavaScript側へオブジェクトを追加する
        webview_hc.getSettings().setUserAgentString((String) configMan.get( Const.Config.userAgent.Name ));
        webview_hc.getSettings().setJavaScriptEnabled(true);
        webview_hc.addJavascriptInterface(new AnTWappWebView(), "antwapp");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webview_hc.setWebContentsDebuggingEnabled(true);
            webview_hc.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        webview_hcsub = (WebView) findViewById(R.id.web_view_hcsub);
        webview_hcsub.clearCache(true);
        webview_hcsub.setInitialScale(1);
//            webview_hcsub.getSettings().setDomStorageEnabled(true);
        webview_hcsub.getSettings().setUseWideViewPort(true);
        webview_hcsub.getSettings().setLoadWithOverviewMode(true);
        webview_hcsub.getSettings().setBuiltInZoomControls(true);
        webview_hcsub.getSettings().setSupportZoom(true);
        webview_hcsub.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webview_hcsub.setBackgroundColor(0x00000000);

        webview_hcsub.setWebViewClient(new WebViewClient() {
            //@Override
            //public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //    //Reset zoom
            //    view.setInitialScale(0);
            //    return false;
            //}
        });
        // JavaScript側へオブジェクトを追加する
        webview_hcsub.getSettings().setUserAgentString((String)configMan.get( Const.Config.userAgent.Name ));
        webview_hcsub.getSettings().setJavaScriptEnabled(true);
//            webview_hcsub.addJavascriptInterface(new AnTWappWebView(), "antwapp");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webview_hcsub.setWebContentsDebuggingEnabled(true);
            webview_hcsub.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }


        ////////////////////////////////////////////////////////////////////
//      mDNS
        this.mDNSControl();
        ////////////////////////////////////////////////////////////////////

    }

    /**
     *
     */
    @Override
    public void onStart() {
        super.onStart();
        Log.i("antwappwebview: ", "onStart()");
    }

    /**
     *
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.i("antwappwebview: ", "onResume()");
//        setContentView(R.layout.activity_webview_sub);

        if( isOnPageFinished == true ) {
            antwapp_status = DIALRestHandlerInterface.DialApplicationState.Running;

            if (0 < logdisp_buffer.size()) {
                for (Message msg : logdisp_buffer) {
                    if (msg.arg1 == MessageType.JS.getId()) {
                        webview_msghandler.sendMessage(msg);
                    }
                }
                logdisp_buffer.clear();
            }

            if (newExtra != null) {
                String command = newExtra.getString("command");

                //Disp AccessDialog
                if (command.equals(Const.Command.accessDialog)) {
                    String appName = newExtra.getString("appName");
                    String infoStr = newExtra.getString("infoStr");

                    Log.i("antwappwebview: ", String.format("onResume(): %s, %s, %s", command, appName, infoStr));
                    showAccessDialog(appName, infoStr);
                }

                //Tune
                else if (command.equals(Const.Command.tune)) {
                    String debugmsg = newExtra.getString("debugmsg");
                    String hcurl = newExtra.getString("hcurl");
                    String[] urlinfo = {debugmsg, hcurl};
                    activity_sendmsg(MessageType.URL, urlinfo);
                }
            }

            newExtra = null;
        }
    }

    /**
     *
     */
    @Override
    public void onPause() {
        super.onPause();
        Log.i("antwappwebview: ", "onPause()");
        antwapp_status = DIALRestHandlerInterface.DialApplicationState.Stopped;
    }

    /**
     *
     */
    @Override
    public void onStop() {
        super.onStop();
        Log.i("antwappwebview: ", "onStop()");
    }

    /**
     * onNewIntent
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.i("antwappwebview: ", "onNewIntent()");
//        intent.getStringExtra();   // セットした値が取り出せる
//        Intent intent = getIntent();
        newExtra = intent.getExtras();
    }

    /**
     *
     */
    @Override
    public void onRestart() {
        super.onRestart();
        Log.i("antwappwebview: ", "onRestart()");
    }

    /**
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean retc = false;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            retc = false;
        }
        else {
            retc = super.onKeyDown(keyCode, event);
        }
        return retc;
    }

    /**
     * mDNS-SD Implementation
     */
    NsdManager nsdManager = null;
    Boolean mDNS_active = false;
    NsdManager.RegistrationListener registrationListener = new NsdManager.RegistrationListener() {
        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            // Save the service name. Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
            String serviceName = NsdServiceInfo.getServiceName();
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            // Registration failed! Put debugging code here to determine why.
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo arg0) {
            // Service has been unregistered. This only happens when you call
            // NsdManager.unregisterService() and pass in this listener.
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            // Unregistration failed. Put debugging code here to determine why.
        }
    };


    /**
     * registerService
     */
    public void registerService() {
        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo = new NsdServiceInfo();

        serviceInfo.setServiceName(WotHandlerInterface.mDNS.serviceName);
        serviceInfo.setServiceType(WotHandlerInterface.mDNS.serviceType);
        serviceInfo.setPort(WotHandlerInterface.mDNS.port);
        for( String key : WotHandlerInterface.mDNS.txt.keySet()) {
            serviceInfo.setAttribute(key, WotHandlerInterface.mDNS.txt.get(key));
        }

        if(nsdManager == null) {
            nsdManager = (NsdManager) WebViewActivity.getContext().getSystemService(Context.NSD_SERVICE);
        }
        nsdManager.registerService( serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener );
        mDNS_active = true;
    }

    /**
     * registerService
     */
    public void unregisterService() {
        nsdManager.unregisterService(registrationListener);
        mDNS_active = false;
    }

    // Device dependent settings
    public void mDNSControl() {
        if( (mDNS_active == false) && (Boolean)configMan.get( Const.Config.mDNS.Name )) {
            Log.i("mDNS", "registerService()");
            this.registerService();
            mDNS_active = true;
        }
        else if( (mDNS_active == true) && !(Boolean)configMan.get( Const.Config.mDNS.Name )) {
            Log.i("mDNS", "unregisterService()");
            this.unregisterService();
            mDNS_active = false;
        }
    }
}
