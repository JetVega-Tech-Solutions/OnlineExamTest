package in.nltwc.onlineexamtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Date;

public class ContestPage extends AppCompatActivity {

    TextView mCTitle,mCDescription;
    Bundle bundle;
    DatabaseReference database;
    FirebaseAuth firebaseAuth;
    LinearLayout leaderboard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contest_page);

        bundle = getIntent().getBundleExtra("dataBundle");
        database= FirebaseDatabase.getInstance().getReference();
        firebaseAuth=FirebaseAuth.getInstance();

        mCTitle=findViewById(R.id.c_title);
        mCDescription=findViewById(R.id.c_desc);
        leaderboard=findViewById(R.id.leader_board);
        TextView user_display_name;
        user_display_name = findViewById(R.id.user_display_name);
        user_display_name.setText(firebaseAuth.getCurrentUser().getDisplayName());

        mCTitle.setText(bundle.getString("c_title"));
        mCDescription.setText(bundle.getString("c_desc"));


        if(getIntent().hasExtra("winners")){
                Bundle winners=getIntent().getBundleExtra("winners");
                leaderboard.removeAllViews();
                LayoutInflater inflater=getLayoutInflater();

                for(int i=0;i<3;i++){
                    RelativeLayout scorerl= (RelativeLayout) inflater.inflate(R.layout.c_score_card,leaderboard,false);
                    TextView name,score,time;
                    name=scorerl.findViewById(R.id.display_name);
                    time=scorerl.findViewById(R.id.time);
                    score=scorerl.findViewById(R.id.score);

                    String rank;
                    if(i==0) {
                        rank="first";
                        ImageView first=scorerl.findViewById(R.id.first_icon);
                        first.setVisibility(View.VISIBLE);
                    } else if(i==1) {
                        rank="second";
                        ImageView second=scorerl.findViewById(R.id.second_icon);
                        second.setVisibility(View.VISIBLE);
                    } else {
                        rank="third";
                        ImageView third=scorerl.findViewById(R.id.third_icon);
                        third.setVisibility(View.VISIBLE);
                    }

                    name.setText(winners.getBundle(rank).getString("name"));
                    score.setText("Marks "+winners.getBundle(rank).getString("score"));
                    time.setText("Time "+winners.getBundle(rank).getString("time"));

                    leaderboard.addView(scorerl);
                }
        }

    }

    public void startContestExam(View view) {
        if(getIntent().getBooleanExtra("isOngoing",false)){
            database.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("contests").child(bundle.getString("c_id")).child("result").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()){
                        Intent i=new Intent(ContestPage.this,ContestWebView.class);
                        i.putExtra("dataBundle",bundle);
                        startActivity(i);
                    }
                    else{
                        Toast.makeText(ContestPage.this, "You have already finished the contest", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        else{
            Toast.makeText(this, "Contest is finished", Toast.LENGTH_SHORT).show();
        }
    }

    public void viewYourResult(View view) {
        database.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("contests").child(bundle.getString("c_id")).child("result").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Intent i=new Intent(ContestPage.this,ContestResultPage.class);
                    i.putExtra("link",dataSnapshot.getValue(String.class));
                    startActivity(i);
                }
                else{
                    Toast.makeText(ContestPage.this, "You have not participated in contest", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
