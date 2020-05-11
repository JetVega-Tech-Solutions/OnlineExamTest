package in.nltwc.onlineexamtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class ContestWebView extends AppCompatActivity {

    long time;
    long startedtime,endedtime;
    WebView wv;
    boolean timerstarted;
    FirebaseAuth firebaseAuth;
    DatabaseReference database;
    Bundle bundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contest_web_view);

        bundle = getIntent().getBundleExtra("dataBundle");
        database= FirebaseDatabase.getInstance().getReference();
        firebaseAuth=FirebaseAuth.getInstance();
        timerstarted = false;
        Date sdate=new Date(bundle.getLong("stime"));
        final Date edate=new Date(bundle.getLong("etime"));

        final RelativeLayout loadinglayout=findViewById(R.id.loadingpaperlayout);
        final TextView loadingtextview=loadinglayout.findViewById(R.id.loadingTV);

        wv = findViewById(R.id.paper_webview);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (url.endsWith("formResponse"))
                {
                    endedtime=new Date().getTime();
                    loadinglayout.setVisibility(View.VISIBLE);
                    loadingtextview.setText("Fetching Results");
                }
                else{
                    loadinglayout.setVisibility(View.VISIBLE);
                    loadingtextview.setText("Fetching Contest");
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(url.endsWith("formResponse")){
                    wv.loadUrl("javascript:var scorelink=document.getElementsByClassName(\"appsMaterialWizButtonNestedLink exportButtonNestedLink\")[0].href;\n" +
                            "Android.openResult(scorelink);");
                    Toast.makeText(ContestWebView.this,"get score",Toast.LENGTH_SHORT).show();
                }if(!timerstarted){
                    Date cdate=new Date();
                    time=(edate.getTime()-cdate.getTime())/1000;
                    startedtime=cdate.getTime();
                    timerstarted=true;
                    loadinglayout.setVisibility(View.INVISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            startTimer(0);
                        }
                    }).start();
                }
            }
        });

        wv.addJavascriptInterface(new ContestWebView.WebAppInterface(this),"Android");

        wv.setWebChromeClient(new WebChromeClient());

        String link=bundle.getString("c_link");
        int lsteq=link.lastIndexOf("=");
        link=link.substring(0,lsteq+1);
        link=link+firebaseAuth.getCurrentUser().getUid();
        wv.loadUrl(link);
    }

    private void startTimer(int currenttime) {
        try {
            Thread.sleep(1000);
            currenttime++;
            final long min=(time-currenttime)/60;
            final long sec=(time-currenttime)%60;
            final int finalCurrenttime = currenttime;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProgressBar pb=findViewById(R.id.timerpb);
                    TextView tv=findViewById(R.id.timertv);
                    tv.setText(min+":"+sec);
                    pb.setProgress((int) ((finalCurrenttime *100)/time));
                }
            });
            if(currenttime!=time){
                startTimer(currenttime);
            }
            else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wv.loadUrl("javascript:document.getElementsByClassName(\"freebirdFormviewerViewNavigationSubmitButton\")[0].click()");
                    }
                });
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class WebAppInterface {
        Activity mContext;
        public WebAppInterface(Context c) {
            mContext= (Activity) c;
        }

        @JavascriptInterface
        public  void openResult(String url){
            database.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("contests").child(bundle.getString("c_id")).child("result").setValue(url);
            database.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("contests").child(bundle.getString("c_id")).child("time").setValue(endedtime-startedtime);
            Intent i=new Intent(mContext,ContestResultPage.class);
            i.putExtra("link",url);
            mContext.startActivity(i);
            mContext.finish();
        }
    }
}
