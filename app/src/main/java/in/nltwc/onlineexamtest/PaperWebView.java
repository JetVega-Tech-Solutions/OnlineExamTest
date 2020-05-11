package in.nltwc.onlineexamtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
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

public class PaperWebView extends AppCompatActivity {
    int time;
    WebView wv;
    boolean timerstarted;
    FirebaseAuth firebaseAuth;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_web_view);

        database= FirebaseDatabase.getInstance().getReference();
        firebaseAuth=FirebaseAuth.getInstance();
        timerstarted = false;
        time = getIntent().getIntExtra("time",120);

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
                    loadinglayout.setVisibility(View.VISIBLE);
                    loadingtextview.setText("Fetching Results");
                }
                else{
                    loadinglayout.setVisibility(View.VISIBLE);
                    loadingtextview.setText("Fetching Paper");
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(url.endsWith("formResponse")){
                    wv.loadUrl("javascript:var scorelink=document.getElementsByClassName(\"appsMaterialWizButtonNestedLink exportButtonNestedLink\")[0].href;\n" +
                            "Android.openResult(scorelink);");
                    Toast.makeText(PaperWebView.this,"get score",Toast.LENGTH_SHORT).show();
                }if(!timerstarted){
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

        wv.addJavascriptInterface(new WebAppInterface(this),"Android");

        wv.setWebChromeClient(new WebChromeClient());

        wv.loadUrl(getIntent().getStringExtra("link"));
    }

    private void startTimer(int currenttime) {
        try {
            Thread.sleep(1000);
            currenttime++;
            final int min=(time-currenttime)/60;
            final int sec=(time-currenttime)%60;
            final int finalCurrenttime = currenttime;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProgressBar pb=findViewById(R.id.timerpb);
                    TextView tv=findViewById(R.id.timertv);
                    tv.setText(min+":"+sec);
                    pb.setProgress((finalCurrenttime *100)/time);
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
            database.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("papers").child(getIntent().getStringExtra("id")).child("result").setValue(url);
                Intent i=new Intent(mContext,PaperResult.class);
                i.putExtra("id",getIntent().getStringExtra("id"));
                i.putExtra("title",getIntent().getStringExtra("title"));
                i.putExtra("subtitle",getIntent().getStringExtra("subtitle"));
                i.putExtra("link",getIntent().getStringExtra("link"));
                i.putExtra("price",getIntent().getStringExtra("price"));
                mContext.startActivity(i);
                mContext.finish();
        }
    }
}
