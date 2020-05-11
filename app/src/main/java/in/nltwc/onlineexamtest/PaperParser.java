package in.nltwc.onlineexamtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PaperParser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_parser);
        TextView title=findViewById(R.id.paper_title);
        TextView subtitle=findViewById(R.id.paper_subtitle);
        TextView price=findViewById(R.id.price);

        title.setText(getIntent().getStringExtra("papertitle"));
        subtitle.setText(getIntent().getStringExtra("papersubtitle"));
        price.setText(getIntent().getStringExtra("paperprice"));
    }

    public void startExam(View view) {
        EditText minet=findViewById(R.id.min);
        EditText secet=findViewById(R.id.sec);
        int min=Integer.parseInt(minet.getText().toString());
        int sec=Integer.parseInt(secet.getText().toString());
        Intent i=new Intent(this,PaperWebView.class);
        i.putExtra("time",(60*min)+sec);
        i.putExtra("title",getIntent().getStringExtra("papertitle"));
        i.putExtra("subtitle",getIntent().getStringExtra("papersubtitle"));
        i.putExtra("id",getIntent().getStringExtra("paperid"));
        i.putExtra("link",getIntent().getStringExtra("paperlink"));
        i.putExtra("price",getIntent().getStringExtra("paperprice"));
        startActivity(i);
    }
}
