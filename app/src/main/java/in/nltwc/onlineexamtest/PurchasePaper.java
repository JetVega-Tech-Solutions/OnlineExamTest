package in.nltwc.onlineexamtest;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class PurchasePaper extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_paper);
    }

    public void sendMessage(View view) {
        String msg=getIntent().getStringExtra("userid")+"\n" +
                getIntent().getStringExtra("username")+"\n" +
                getIntent().getStringExtra("useremail")+"\n" +
                getIntent().getStringExtra("paperid")+"\n" +
                getIntent().getStringExtra("papertitle")+"\n" +
                getIntent().getStringExtra("papersubtitle")+"\n" +
                getIntent().getStringExtra("paperprice");
        ClipboardManager clipboardManager= (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip=ClipData.newPlainText("info",msg);
        clipboardManager.setPrimaryClip(clip);
        try{
            Uri uri=Uri.parse("smsto:7020828342");
            Intent i=new Intent(Intent.ACTION_SENDTO,uri);
            i.putExtra(Intent.EXTRA_SUBJECT,"Subject");
            i.putExtra(Intent.EXTRA_TEXT,"body");
            i.setPackage("com.whatsapp");
            startActivity(i);
        }catch (ActivityNotFoundException e){
            Toast.makeText(this, "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
        }
    }
}
