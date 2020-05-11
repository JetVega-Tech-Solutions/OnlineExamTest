package in.nltwc.onlineexamtest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class Home extends AppCompatActivity {

    static RelativeLayout home_tab;
    static RelativeLayout playandearn_tab;
    static ScrollView papers_tab;
    LinearLayout contests_container;
    LinearLayout papers_container;
    FirebaseAuth firebaseAuth;
    DatabaseReference database;
    StorageReference storage;
    String addedMsgs[];


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));


        //my own code starts here
        database=FirebaseDatabase.getInstance().getReference();
        firebaseAuth=FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

        final LayoutInflater inflater=getLayoutInflater();

        home_tab = (RelativeLayout) inflater.inflate(R.layout.home,null);
        final LinearLayout messages_container=home_tab.findViewById(R.id.messages_container);
        papers_tab = (ScrollView) inflater.inflate(R.layout.papers,null);
        papers_container = papers_tab.findViewById(R.id.papers_container);
        playandearn_tab = (RelativeLayout) inflater.inflate(R.layout.playandearn,null);
        contests_container = playandearn_tab.findViewById(R.id.contests_container);

        //get pinned msg
        database.child("pin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final RelativeLayout pinnedmsgrl=messages_container.findViewById(R.id.pinned_msg);

                if(dataSnapshot.exists()){
                    database.child("messages").child("global").child(dataSnapshot.getValue(String.class)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            LayoutInflater inflater = getLayoutInflater();
                            RelativeLayout msg_card=pinnedmsgrl;
                            DataSnapshot msg = dataSnapshot;


                            if(msg.hasChild("msg")){
                                TextView msgtv=msg_card.findViewById(R.id.msg_tv);
                                msgtv.setVisibility(View.VISIBLE);
                                msgtv.setText(msg.child("msg").getValue(String.class));
                            }
                            else if(msg.hasChild("file")){
                                final String filename = msg.child("file").getValue(String.class);
                                if(filename.endsWith(".jpeg") || filename.endsWith(".jpg") || filename.endsWith(".png")){
                                    final ImageView msgiv=msg_card.findViewById(R.id.msg_iv);
                                    msgiv.setVisibility(View.VISIBLE);
                                    storage.child("chatfiles").child(filename).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(final Uri uri) {
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        URL u=new URL(uri.toString());
                                                        final Bitmap bitmap = BitmapFactory.decodeStream(u.openStream());
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                msgiv.setImageBitmap(bitmap);

                                                                msgiv.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        HelperClass.bitmap=bitmap;
                                                                        startActivity(new Intent(Home.this,ImageViewer.class));
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).start();
                                        }
                                    });
                                }
                                else{
                                    final Button msg_file=msg_card.findViewById(R.id.msg_file);
                                    msg_file.setVisibility(View.VISIBLE);
                                    msg_file.setText(filename);

                                    storage.child("chatfiles").child(filename).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(final Uri uri) {
                                            msg_file.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent i=new Intent(Intent.ACTION_VIEW);
                                                    i.setData(uri);
                                                    startActivity(i);
                                                }
                                            });
                                        }
                                    });
                                }
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else{
                    messages_container.removeView(pinnedmsgrl);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //gets papers
        database.child("users").child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final DataSnapshot userdata = dataSnapshot;

                database.child("papers").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //removes all elements from papaers container
                        papers_container.removeAllViews();

                        //unsolved papers only
                        for(int i=0;i<dataSnapshot.getChildrenCount();i++){
                            final DataSnapshot paper = dataSnapshot.child(String.valueOf(i));
                            if (!userdata.child("papers").child(paper.child("id").getValue(String.class)).child("result").exists()){
                                RelativeLayout paper_card= (RelativeLayout) inflater.inflate(R.layout.paper_card,papers_container,false);
                                TextView title=paper_card.findViewById(R.id.paper_title);
                                TextView subtitle=paper_card.findViewById(R.id.paper_subtitle);
                                TextView price=paper_card.findViewById(R.id.price);
                                title.setText(paper.child("title").getValue(String.class));
                                subtitle.setText(paper.child("subtitle").getValue(String.class));
                                price.setText(paper.child("price").getValue(String.class));
                                paper_card.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i=new Intent(Home.this,PurchasePaper.class);
                                        i.putExtra("userid",firebaseAuth.getCurrentUser().getUid());
                                        i.putExtra("username",firebaseAuth.getCurrentUser().getDisplayName());
                                        i.putExtra("useremail",firebaseAuth.getCurrentUser().getEmail());
                                        i.putExtra("paperid",paper.child("id").getValue(String.class));
                                        i.putExtra("papertitle",paper.child("title").getValue(String.class));
                                        i.putExtra("papersubtitle",paper.child("subtitle").getValue(String.class));
                                        i.putExtra("paperprice",paper.child("price").getValue(String.class));
                                        startActivity(i);
                                    }
                                });
                                try{
                                    if(userdata.child("papers").child(paper.child("id").getValue(String.class)).child("unlocked").getValue(Boolean.class) || paper.child("isfree").getValue(Boolean.class)) {
                                        price.setText("Un Solved");
                                        paper_card.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent i=new Intent(Home.this,PaperParser.class);
                                                i.putExtra("paperid",paper.child("id").getValue(String.class));
                                                i.putExtra("papertitle",paper.child("title").getValue(String.class));
                                                i.putExtra("papersubtitle",paper.child("subtitle").getValue(String.class));
                                                i.putExtra("paperprice",paper.child("price").getValue(String.class));
                                                i.putExtra("paperlink",paper.child("link").getValue(String.class));
                                                startActivity(i);
                                            }
                                        });
                                    }
                                }catch (NullPointerException e){
                                    if(paper.child("isfree").getValue(Boolean.class)) {
                                        price.setText("Un Solved");
                                        paper_card.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent i=new Intent(Home.this,PaperParser.class);
                                                i.putExtra("paperid",paper.child("id").getValue(String.class));
                                                i.putExtra("papertitle",paper.child("title").getValue(String.class));
                                                i.putExtra("papersubtitle",paper.child("subtitle").getValue(String.class));
                                                i.putExtra("paperprice",paper.child("price").getValue(String.class));
                                                i.putExtra("paperlink",paper.child("link").getValue(String.class));
                                                startActivity(i);
                                            }
                                        });
                                    }
                                }
                                papers_container.addView(paper_card);
                            }
                        }

                        //solved papers only
                        for(int i=0;i<dataSnapshot.getChildrenCount();i++){
                            final DataSnapshot paper = dataSnapshot.child(String.valueOf(i));
                            if (userdata.child("papers").child(paper.child("id").getValue(String.class)).child("result").exists()){
                                RelativeLayout paper_card= (RelativeLayout) inflater.inflate(R.layout.paper_card,papers_container,false);
                                TextView title=paper_card.findViewById(R.id.paper_title);
                                TextView subtitle=paper_card.findViewById(R.id.paper_subtitle);
                                TextView price=paper_card.findViewById(R.id.price);
                                title.setText(paper.child("title").getValue(String.class));
                                subtitle.setText(paper.child("subtitle").getValue(String.class));
                                price.setText("Solved");
                                paper_card.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i=new Intent(Home.this,PaperResult.class);
                                        i.putExtra("id",paper.child("id").getValue(String.class));
                                        i.putExtra("title",paper.child("title").getValue(String.class));
                                        i.putExtra("subtitle",paper.child("subtitle").getValue(String.class));
                                        i.putExtra("price",paper.child("price").getValue(String.class));
                                        i.putExtra("link",paper.child("link").getValue(String.class));
                                        startActivity(i);
                                    }
                                });
                                papers_container.addView(paper_card);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //gets all messges from user session and admin session
        database.child("messages").child(firebaseAuth.getCurrentUser().getUid()).orderByKey().limitToLast(50).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final DataSnapshot userDataSnapshot = dataSnapshot;

                //get only new messages from user session
                database.child("messages").child(firebaseAuth.getCurrentUser().getUid()).orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
                    boolean isnew=false;
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(isnew || userDataSnapshot.getChildrenCount()==0){
                            ScrollView parent= (ScrollView) messages_container.getParent();
                            addNewMsg(dataSnapshot,messages_container);
                            parent.fullScroll(View.FOCUS_DOWN);
                        }
                        else{
                            isnew=true;
                        }
                    }



                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                final Iterator<DataSnapshot> userMsgsSnapshots = dataSnapshot.getChildren().iterator();
                database.child("messages").child("global").orderByKey().limitToLast(50).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final DataSnapshot adminDataSnapshot=dataSnapshot;
                        //get only new messages from admin session
                        database.child("messages").child("global").orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
                            boolean isnew=false;
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                if(isnew || adminDataSnapshot.getChildrenCount()==0){
                                    ScrollView parent= (ScrollView) messages_container.getParent();
                                    addNewMsg(dataSnapshot,messages_container);
                                    parent.fullScroll(View.FOCUS_DOWN);
                                }
                                else{
                                    isnew=true;
                                }
                            }



                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        Iterator<DataSnapshot> globalMsgsSnapshots = dataSnapshot.getChildren().iterator();
                        DataSnapshot usermsg = null;
                        DataSnapshot globalmsg = null;

                        if(userMsgsSnapshots.hasNext())
                            usermsg = userMsgsSnapshots.next();
                        if(globalMsgsSnapshots.hasNext())
                            globalmsg = globalMsgsSnapshots.next();


                        while (usermsg!=null || globalmsg!=null){
                            if (usermsg!=null && globalmsg!=null){
                                if(usermsg.child("timestamp").getValue(Long.class)>globalmsg.child("timestamp").getValue(Long.class)){
                                    addNewMsg(globalmsg,messages_container);
                                    globalmsg=null;
                                    if(globalMsgsSnapshots.hasNext())
                                        globalmsg=globalMsgsSnapshots.next();
                                }
                                else{
                                    addNewMsg(usermsg,messages_container);
                                    usermsg=null;
                                    if(userMsgsSnapshots.hasNext())
                                        usermsg=userMsgsSnapshots.next();
                                }
                            }

                            if(usermsg!=null && globalmsg==null){
                                addNewMsg(usermsg,messages_container);
                                usermsg=null;
                                if(userMsgsSnapshots.hasNext())
                                    usermsg=userMsgsSnapshots.next();
                            }

                            if(globalmsg!=null && usermsg==null){
                                addNewMsg(globalmsg,messages_container);
                                globalmsg=null;
                                if(globalMsgsSnapshots.hasNext())
                                    globalmsg=globalMsgsSnapshots.next();
                            }
                        }

                       final ScrollView parent= (ScrollView) messages_container.getParent();
                        parent.post(new Runnable() {
                            @Override
                            public void run() {
                                parent.fullScroll(View.FOCUS_DOWN);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(Home.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Home.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        database.child("contests").orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                RelativeLayout contest_card= (RelativeLayout) inflater.inflate(R.layout.contest_card,contests_container,false);
                final TextView title,time,timer;
                title=contest_card.findViewById(R.id.c_title);
                time=contest_card.findViewById(R.id.c_time);
                timer=contest_card.findViewById(R.id.c_timer);

                final int sy,sm,sd,sh,smin;
                final int ey,em,ed,eh,emin;

                String stime=dataSnapshot.child("stime").getValue(String.class);
                String etime=dataSnapshot.child("etime").getValue(String.class);

                sy= Integer.parseInt(stime.split("-")[0]);
                sm= Integer.parseInt(stime.split("-")[1]);
                sd= Integer.parseInt(stime.split("-")[2].split("T")[0]);
                sh= Integer.parseInt(stime.split(":")[0].split("T")[1]);
                smin= Integer.parseInt(stime.split(":")[1]);

                ey= Integer.parseInt(etime.split("-")[0]);
                em= Integer.parseInt(etime.split("-")[1]);
                ed= Integer.parseInt(etime.split("-")[2].split("T")[0]);
                eh= Integer.parseInt(etime.split(":")[0].split("T")[1]);
                emin= Integer.parseInt(etime.split(":")[1]);

                time.setText("Starts "+stime.replace("T"," ")+"\n"+"Ends "+etime.replace("T"," "));
                title.setText(dataSnapshot.child("title").getValue(String.class));
                final Date sdate =new GregorianCalendar(sy,sm-1,sd,sh,smin).getTime();
                final Date edate =new GregorianCalendar(ey,em-1,ed,eh,emin).getTime();
                Date cdate=new Date();

                Intent contestPageIntent=new Intent(Home.this,ContestPage.class);
                Bundle bundle=new Bundle();
                bundle.putString("c_id",dataSnapshot.child("id").getValue(String.class));
                bundle.putString("c_title",dataSnapshot.child("title").getValue(String.class));
                bundle.putString("c_desc",dataSnapshot.child("subtitle").getValue(String.class));
                bundle.putString("c_link",dataSnapshot.child("link").getValue(String.class));
                bundle.putLong("stime",sdate.getTime());
                bundle.putLong("etime",edate.getTime());
                if(dataSnapshot.child("winners").exists()){
                    Bundle first=new Bundle();
                    first.putString("name",dataSnapshot.child("winners").child("first").child("name").getValue(String.class));
                    first.putString("score",dataSnapshot.child("winners").child("first").child("score").getValue(String.class));
                    first.putString("time",dataSnapshot.child("winners").child("first").child("time").getValue(String.class));

                    Bundle second=new Bundle();
                    second.putString("name",dataSnapshot.child("winners").child("second").child("name").getValue(String.class));
                    second.putString("score",dataSnapshot.child("winners").child("second").child("score").getValue(String.class));
                    second.putString("time",dataSnapshot.child("winners").child("second").child("time").getValue(String.class));

                    Bundle third=new Bundle();
                    third.putString("name",dataSnapshot.child("winners").child("third").child("name").getValue(String.class));
                    third.putString("score",dataSnapshot.child("winners").child("third").child("score").getValue(String.class));
                    third.putString("time",dataSnapshot.child("winners").child("third").child("time").getValue(String.class));

                    Bundle winnersBundle=new Bundle();
                    winnersBundle.putBundle("first",first);
                    winnersBundle.putBundle("second",second);
                    winnersBundle.putBundle("third",third);

                    contestPageIntent.putExtra("winners",winnersBundle);
                }

                contestPageIntent.putExtra("dataBundle",bundle);
                startStartingTimer(contest_card,timer,sdate,edate,dataSnapshot,contestPageIntent);

                contests_container.addView(contest_card);
            }

            private void setResultOption(RelativeLayout contest_card, TextView timer, final DataSnapshot dataSnapshot, final Intent contestPageIntent) {
                if(dataSnapshot.hasChild("result")){
                    timer.setText("See Result");
                    timer.setTextColor(Color.parseColor("#43BE31"));
                    contest_card.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(contestPageIntent);
                        }
                    });
                }
                else{
                    timer.setText("Waiting for result");
                    timer.setTextColor(Color.parseColor("#43BE31"));
                    contest_card.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(contestPageIntent);
                        }
                    });
                }
            }

            private void startStartingTimer(final RelativeLayout contest_card, final TextView timer, final Date sdate, final Date edate, final DataSnapshot dataSnapshot, final Intent contestPageIntent) {
                contest_card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(Home.this, "Contest is not started yet", Toast.LENGTH_SHORT).show();
                    }
                });

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true){
                            final Date cdate=new Date();
                            long diff = sdate.getTime() - cdate.getTime();
                            if(diff<0){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        startEndingTimer(contest_card,timer,edate,sdate, dataSnapshot,contestPageIntent);
                                    }
                                });
                                break;
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    long diff = sdate.getTime() - cdate.getTime();
                                    long hours=diff/(1000*60*60);
                                    diff=diff%(1000*60*60);
                                    long minute=diff/(1000*60);
                                    diff=diff%(1000*60);
                                    long second=diff/1000;
                                    timer.setText("Start in "+hours+" : "+minute+" : "+second);
                                }
                            });
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }

            private void startEndingTimer(final RelativeLayout contest_card, final TextView timer, final Date edate, final Date sdate, final DataSnapshot dataSnapshot, final Intent contestPageIntent) {
                contest_card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        contestPageIntent.putExtra("isOngoing",true);
                        startActivity(contestPageIntent);
                    }
                });

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true){
                            final Date cdate=new Date();
                            long diff = edate.getTime() - cdate.getTime();
                            if(diff<0){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setResultOption(contest_card,timer,dataSnapshot,contestPageIntent);
                                    }
                                });
                                break;
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    long diff = edate.getTime() - cdate.getTime();

                                    long hours=diff/(1000*60*60);
                                    diff=diff%(1000*60*60);
                                    long minute=diff/(1000*60);
                                    diff=diff%(1000*60);
                                    long second=diff/1000;

                                    timer.setText("Ends in "+hours+" : "+minute+" : "+second);
                                    timer.setTextColor(Color.parseColor("#fa017d"));
                                }
                            });
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void addNewMsg(DataSnapshot msg,LinearLayout messages_container) {
        LayoutInflater inflater = getLayoutInflater();
        RelativeLayout msg_card;
        if (msg.child("sender").getValue(String.class).equals("admin")){
            msg_card = (RelativeLayout) inflater.inflate(R.layout.received_msg, messages_container, false);
        }
        else{
            msg_card = (RelativeLayout) inflater.inflate(R.layout.sent_msg, messages_container, false);
        }

        if(msg.hasChild("msg")){
            TextView msgtv=msg_card.findViewById(R.id.msg_tv);
            msgtv.setVisibility(View.VISIBLE);
            msgtv.setText(msg.child("msg").getValue(String.class));
        }
        else if(msg.hasChild("file")){
            final String filename = msg.child("file").getValue(String.class);
            if(filename.endsWith(".jpeg") || filename.endsWith(".jpg") || filename.endsWith(".png")){
                final ImageView msgiv=msg_card.findViewById(R.id.msg_iv);
                msgiv.setVisibility(View.VISIBLE);
                storage.child("chatfiles").child(filename).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    URL u=new URL(uri.toString());
                                    final Bitmap bitmap = BitmapFactory.decodeStream(u.openStream());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            msgiv.setImageBitmap(bitmap);

                                            msgiv.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    HelperClass.bitmap=bitmap;
                                                    startActivity(new Intent(Home.this,ImageViewer.class));
                                                }
                                            });
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                });
            }
            else{
                final Button msg_file=msg_card.findViewById(R.id.msg_file);
                msg_file.setVisibility(View.VISIBLE);
                msg_file.setText(filename);

                storage.child("chatfiles").child(filename).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {
                        msg_file.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i=new Intent(Intent.ACTION_VIEW);
                                i.setData(uri);
                                startActivity(i);
                            }
                        });
                    }
                });
            }
        }

        RelativeLayout pinnedmsg=messages_container.findViewById(R.id.pinned_msg);
        if(pinnedmsg==null){
        messages_container.addView(msg_card);}
        else{
            messages_container.addView(msg_card,messages_container.getChildCount()-1);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ConstraintLayout rootView = (ConstraintLayout) inflater.inflate(R.layout.fragment_home, container, false);
            if(getArguments().getInt(ARG_SECTION_NUMBER)==1){
                ViewGroup parent= (ViewGroup) home_tab.getParent();
                if(parent!=null)
                    parent.removeView(home_tab);
                rootView.addView(home_tab);
            }
            else if(getArguments().getInt(ARG_SECTION_NUMBER)==2){
                ViewGroup parent= (ViewGroup) papers_tab.getParent();
                if(parent!=null)
                    parent.removeView(papers_tab);
                rootView.addView(papers_tab);
            }
            else{
                ViewGroup parent= (ViewGroup) playandearn_tab.getParent();
                if(parent!=null)
                    parent.removeView(playandearn_tab);
                rootView.addView(playandearn_tab);
            }
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }
}
