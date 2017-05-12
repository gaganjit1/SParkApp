package com.sjsu.ten.sparkapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Locale;


/**
 * Created by Ben on 9/24/2016.
 */


public class GaragePickerFragment extends Fragment{
    
    private Button buttonGarage1;
    private Button buttonGarage3;
    private Button buttonGarage2;
    private Button buttonGarageAuto;
    private static int count1 = 0;
    private static int count2 = 0;
    private static int count3 = 0;
    private View view;
    public boolean isVisible = false;
    public AssetManager am;
    public Typeface custom_font;

    SpannableString slash=  new SpannableString(" | ");
    SpannableString ss4=  new SpannableString("\n ");
    public GaragePickerFragment(){}


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_garage_picker, container, false);
        }
        else {
            ((ViewGroup) view.getParent()).removeView(view);
        }

        am = getActivity().getApplicationContext().getAssets();
        custom_font = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "Stark.ttf"));
        buttonGarage1    = (Button) view.findViewById(R.id.button1);
        buttonGarage2    = (Button) view.findViewById(R.id.button2);
        buttonGarage3    = (Button) view.findViewById(R.id.button3);
        buttonGarageAuto = (Button) view.findViewById(R.id.button4);

        buttonGarage1.setTypeface(custom_font);
        buttonGarage2.setTypeface(custom_font);
        buttonGarage3.setTypeface(custom_font);
        buttonGarageAuto.setTypeface(custom_font);

        buttonGarage1.setAllCaps(false);
        buttonGarage2.setAllCaps(false);
        buttonGarage3.setAllCaps(false);
        buttonGarageAuto.setAllCaps(false);

        ss4.setSpan(new RelativeSizeSpan(0.5f), 0, ss4.length(), 0);
        
        if (Data.getInstance().getGarage().equals("4th"))
            buttonGarage1.setBackgroundResource(R.drawable.grayback_press);
        if (Data.getInstance().getGarage().equals("7th"))
            buttonGarage2.setBackgroundResource(R.drawable.grayback_press);
        if (Data.getInstance().getGarage().equals("10th"))
            buttonGarage3.setBackgroundResource(R.drawable.grayback_press);
        if (Data.getInstance().getGarage().equals("auto"))
            buttonGarageAuto.setBackgroundResource(R.drawable.grayback_press);

        FirebaseSetup();
        return view;
    }

    public void FirebaseSetup(){

        Firebase f1 = new Firebase("https://spark-94584.firebaseio.com/Garage/4th/");
        f1.addValueEventListener(new ValueEventListener()
        {

            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                double temp = dataSnapshot.getChildrenCount();
                count1 = 0;
                for (DataSnapshot snap : snapshotIterator) {
                    ParkingSpot object = snap.getValue(ParkingSpot.class);
                    if (object.getStatus().contains("empty")) {
                        GaragePickerFragment.count1 +=1;
                        Log.d("Log", "EMPTY!!! " + GaragePickerFragment.count1);
                    }
                }

                Data.getInstance().setPercent4th(100*(temp - count1)/temp);

                //buttonGarage1.setText("\n4th Street Garage \n Available Slots: " + count1 + "\n Percent Full: " + (int) Math.ceil(Data.getInstance().getPercent("4th")) + "\n");
                buttonGarage1.setTextSize(40);


                String title= "4th Street Garage ";
                String slots="\n"+count1+" slots";
                String percent= (int) Math.ceil(Data.getInstance().getPercent("4th")) +"%";

                SpannableString ss1=  new SpannableString(title);
                SpannableString ss2=  new SpannableString(slots);
                SpannableString ss3=  new SpannableString(percent);
                ss1.setSpan(new RelativeSizeSpan(0.6f), 0, ss1.length(), 0);
                ss2.setSpan(new RelativeSizeSpan(0.5f), ss2.length()-5, ss2.length(), 0);
                ss3.setSpan(new RelativeSizeSpan(0.5f), ss3.length()-1, ss3.length(), 0);

                if (Math.ceil(Data.getInstance().getPercent("4th")) >80) {
                    ss2.setSpan(new ForegroundColorSpan(Color.RED), 0, ss2.length(), 0);
                    ss3.setSpan(new ForegroundColorSpan(Color.RED), 0, ss3.length(), 0);
                }
                else {
                    ss2.setSpan(new ForegroundColorSpan(Color.WHITE), 0, ss2.length(), 0);
                    ss3.setSpan(new ForegroundColorSpan(Color.WHITE), 0, ss3.length(), 0);}


                buttonGarage1.setText(TextUtils.concat(ss1,ss2,slash,ss3,ss4));

                buttonGarage1.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        TabLayout tab = (TabLayout) getActivity().findViewById(R.id.tab_layout);
                        Data.getInstance().setGarage("4th");
                        SaveChoice("4th");
                        buttonGarage1.setBackgroundResource(R.drawable.grayback_press);
                        buttonGarage2.setBackgroundResource(R.drawable.grayback);
                        buttonGarage3.setBackgroundResource(R.drawable.grayback);
                        buttonGarageAuto.setBackgroundResource(R.drawable.grayback);
                        tab.getTabAt(0).select();
                        Toast.makeText(getActivity(), "Priority set to 4th Street Garage.", Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onCancelled(FirebaseError databaseError) {

            }

        });


        Firebase f2 = new Firebase("https://spark-94584.firebaseio.com/Garage/7th/");
        f2.addValueEventListener(new ValueEventListener()
        {
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                double temp = dataSnapshot.getChildrenCount();
                count2 = 0;
                for (DataSnapshot snap : snapshotIterator) {
                    ParkingSpot object = snap.getValue(ParkingSpot.class);
                    if (object.getStatus().contains("empty")) {
                        GaragePickerFragment.count2 +=1;
                        Log.d("Log", "EMPTY!!! " + GaragePickerFragment.count2);
                    }
                }
                
                Data.getInstance().setPercent7th(100*(temp - count2)/temp);

                //buttonGarage2.setText("\n7th Street Garage \n Available Slots: " + count2 + "\n Percent Full: " + (int) Math.ceil(Data.getInstance().getPercent("7th")) + "\n");

                buttonGarage2.setTextSize(40);


                String title= "7th Street Garage ";
                String slots="\n"+count2+" slots";
                String percent= (int) Math.ceil(Data.getInstance().getPercent("7th")) +"%";

                SpannableString ss1=  new SpannableString(title);
                SpannableString ss2=  new SpannableString(slots);
                SpannableString ss3=  new SpannableString(percent);
                
                ss1.setSpan(new RelativeSizeSpan(0.6f), 0, ss1.length(), 0);
                ss2.setSpan(new RelativeSizeSpan(0.5f), ss2.length()-5, ss2.length(), 0);
                ss3.setSpan(new RelativeSizeSpan(0.5f), ss3.length()-1, ss3.length(), 0);

                if (Math.ceil(Data.getInstance().getPercent("7th")) >80) {
                    ss2.setSpan(new ForegroundColorSpan(Color.RED), 0, ss2.length(), 0);
                    ss3.setSpan(new ForegroundColorSpan(Color.RED), 0, ss3.length(), 0);
                }
                else {
                    ss2.setSpan(new ForegroundColorSpan(Color.WHITE), 0, ss2.length(), 0);
                    ss3.setSpan(new ForegroundColorSpan(Color.WHITE), 0, ss3.length(), 0);}

                buttonGarage2.setText(TextUtils.concat(ss1,ss2,slash,ss3,ss4));

                buttonGarage2.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        TabLayout tab = (TabLayout) getActivity().findViewById(R.id.tab_layout);
                        Data.getInstance().setGarage("7th");
                        SaveChoice("7th");
                        buttonGarage1.setBackgroundResource(R.drawable.grayback);
                        buttonGarage2.setBackgroundResource(R.drawable.grayback_press);
                        buttonGarage3.setBackgroundResource(R.drawable.grayback);
                        buttonGarageAuto.setBackgroundResource(R.drawable.grayback);
                        tab.getTabAt(0).select();
                        Toast.makeText(getActivity(), "Priority set to 7th Street Garage.", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError databaseError) {

            }

        });


        Firebase f3 = new Firebase("https://spark-94584.firebaseio.com/Garage/10th/");
        f3.addValueEventListener(new ValueEventListener()
        {

            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                double temp = dataSnapshot.getChildrenCount();
                count3 = 0;
                for (DataSnapshot snap : snapshotIterator) {
                    ParkingSpot object = snap.getValue(ParkingSpot.class);
                    if (object.getStatus().contains("empty")) {
                        GaragePickerFragment.count3 +=1;
                        Log.d("Log", "EMPTY!!! " + GaragePickerFragment.count2);
                    }
                }

                Data.getInstance().setPercent10th(100*(temp - count3)/temp);


                //buttonGarage3.setText("\n10th Street Garage \n Available Slots: " + count3 + "\n Percent Full: " + (int) Math.ceil(Data.getInstance().getPercent("10th")) + "\n");

                buttonGarage3.setTextSize(40);


                String title= "10th Street Garage ";
                String slots="\n"+count3+" slots";
                String percent= (int) Math.ceil(Data.getInstance().getPercent("10th")) +"%";

                SpannableString ss1=  new SpannableString(title);
                SpannableString ss2=  new SpannableString(slots);
                SpannableString ss3=  new SpannableString(percent);
                
                ss1.setSpan(new RelativeSizeSpan(0.6f), 0, ss1.length(), 0);
                ss2.setSpan(new RelativeSizeSpan(0.5f), ss2.length()-5, ss2.length(), 0);
                ss3.setSpan(new RelativeSizeSpan(0.5f), ss3.length()-1, ss3.length(), 0);


                if (Math.ceil(Data.getInstance().getPercent("10th")) >80) {
                    ss2.setSpan(new ForegroundColorSpan(Color.RED), 0, ss2.length(), 0);
                    ss3.setSpan(new ForegroundColorSpan(Color.RED), 0, ss3.length(), 0);
                }
                else {
                    ss2.setSpan(new ForegroundColorSpan(Color.WHITE), 0, ss2.length(), 0);
                    ss3.setSpan(new ForegroundColorSpan(Color.WHITE), 0, ss3.length(), 0);}

                buttonGarage3.setText(TextUtils.concat(ss1,ss2,slash,ss3,ss4));

                buttonGarage3.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        TabLayout tab = (TabLayout) getActivity().findViewById(R.id.tab_layout);
                        Data.getInstance().setGarage("10th");
                        SaveChoice("10th");
                        buttonGarage1.setBackgroundResource(R.drawable.grayback);
                        buttonGarage2.setBackgroundResource(R.drawable.grayback);
                        buttonGarage3.setBackgroundResource(R.drawable.grayback_press);
                        buttonGarageAuto.setBackgroundResource(R.drawable.grayback);
                        tab.getTabAt(0).select();
                        //NavigationFragment.setGaragechoice("10th"));
                        Toast.makeText(getActivity(), "Priority set to 10th Street Garage.", Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onCancelled(FirebaseError databaseError) {

            }

        });


        buttonGarageAuto.setText("\nAutomatic\n");
        buttonGarageAuto.setTextSize(30);
        buttonGarageAuto.setTextColor(Color.parseColor("#FFFFFFFF"));
        buttonGarageAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabLayout tab = (TabLayout) getActivity().findViewById(R.id.tab_layout);
                Data.getInstance().setGarage("auto");
                SaveChoice("auto");
                buttonGarage1.setBackgroundResource(R.drawable.grayback);
                buttonGarage2.setBackgroundResource(R.drawable.grayback);
                buttonGarage3.setBackgroundResource(R.drawable.grayback);
                buttonGarageAuto.setBackgroundResource(R.drawable.grayback_press);
                tab.getTabAt(0).select();
                Toast.makeText(getActivity(), "Priority set to automatic.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isVisible = isVisibleToUser;
        if (view != null){                                                      //SLOPPY BUG FIX
            buttonGarage1    = (Button) view.findViewById(R.id.button1);
            buttonGarage2    = (Button) view.findViewById(R.id.button2);
            buttonGarage3    = (Button) view.findViewById(R.id.button3);
            buttonGarageAuto = (Button) view.findViewById(R.id.button4);

            buttonGarage1.setBackgroundResource(R.drawable.grayback);
            buttonGarage2.setBackgroundResource(R.drawable.grayback);
            buttonGarage3.setBackgroundResource(R.drawable.grayback);
            buttonGarageAuto.setBackgroundResource(R.drawable.grayback);

            if (Data.getInstance().getGarage().equals("4th"))
                buttonGarage1.setBackgroundResource(R.drawable.grayback_press);
            if (Data.getInstance().getGarage().equals("7th"))
                buttonGarage2.setBackgroundResource(R.drawable.grayback_press);
            if (Data.getInstance().getGarage().equals("10th"))
                buttonGarage3.setBackgroundResource(R.drawable.grayback_press);
            if (Data.getInstance().getGarage().equals("auto"))
                buttonGarageAuto.setBackgroundResource(R.drawable.grayback_press);
        }
    }

    public void SaveChoice(String choice){
        Log.d("Log", "HIIIII");
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("GarageChoice", choice);
        editor.commit();
    }
}
