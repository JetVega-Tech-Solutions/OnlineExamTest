package in.nltwc.onlineexamtest;

import android.content.Intent;
import android.support.constraint.solver.widgets.Helper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PaperResult extends AppCompatActivity {

    DatabaseReference database;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_result);

        database= FirebaseDatabase.getInstance().getReference();
        firebaseAuth=FirebaseAuth.getInstance();

        final WebView wv=findViewById(R.id.wv);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebChromeClient(new WebChromeClient());
        wv.setWebViewClient(new WebViewClient());
        database.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("papers").child(getIntent().getStringExtra("id")).child("result").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wv.loadUrl(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void reAttempt(View view) {
        Intent i=new Intent(this,PaperParser.class);
        i.putExtra("paperid",getIntent().getStringExtra("id"));
        i.putExtra("papertitle",getIntent().getStringExtra("title"));
        i.putExtra("papersubtitle",getIntent().getStringExtra("subtitle"));
        i.putExtra("paperprice",getIntent().getStringExtra("price"));
        i.putExtra("paperlink",getIntent().getStringExtra("link"));
        startActivity(i);
        finish();
    }
}
