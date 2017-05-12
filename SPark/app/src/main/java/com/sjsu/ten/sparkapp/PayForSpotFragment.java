package com.sjsu.ten.sparkapp;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ben on 11/17/2016.
 */

public class PayForSpotFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {
    public static final String PREF_CURRENT = "MyPrefsFile";
    private static final String TAG = "PayForSpotActivity";
    private static final int RATE = 50; // in cents
    private View view;
    private TextView timedSpot;
    private TextView allottedTime;
    private TextView selectedTimeIncrement;
    private int timeAtSpot; //change to request
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private Query query;
    private int refresh = 0;
    private SeekBar slider;
    private int sliderprogress=0;
    private String selectedSpot = "";
    private String selectedGarage = "";
    private String spotsToChoose[]= {""};
    private int spotTimes[]={0};
    private String currentGarageSelection="";

    private Spinner spotSel;

//    public static final int mEnvironment = WalletConstants.ENVIRONMENT_TEST;
//    private GoogleApiClient mGoogleApiClient;

    // You will need to use your live API key even while testing
//    public static final String PUBLISHABLE_KEY = "pk_live_SpQlbSrkoNEzW8mcqRAkbTmg";

    // Unique identifiers for asynchronous requests:
//    private static final int LOAD_MASKED_WALLET_REQUEST_CODE = 1000;
//    private static final int LOAD_FULL_WALLET_REQUEST_CODE = 1001;
//
//    private SupportWalletFragment walletFragment;

    FragmentActivity parent = getActivity();

    private com.google.firebase.database.ValueEventListener newQueryEventListener() {
        return new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                try {
                    int temp = 0;
                    ParkingSpot object = dataSnapshot.getValue(ParkingSpot.class);
                    SharedPreferences currentSettings = getActivity().getSharedPreferences(PREF_CURRENT,0);
                    String current= currentSettings.getString("currentCustomer","");
                    if (current.contains(object.getCurrent())){
                        temp = object.getPaid();
                    }
                    else if ((object.getStatus().contains("occupied")) && (object.getCurrent().contains("---"))) {
                        temp = object.getPaid();
                    }
                    timedSpot = (TextView) view.findViewById(R.id.textView4);
                    timedSpot.setText("" + temp + " minutes");
                }
                catch (Exception e){
                    Log.d("Log","EXCEPTION");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private com.google.firebase.database.ValueEventListener queryEventListener() {
        return new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                List<String> ListSpaces = new ArrayList<String>();
//                List<Integer> ListTimes = new ArrayList<Integer>();
                Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                int temp=0;
                boolean WeHaveOccupiedSpots=false;
                for (DataSnapshot snap : snapshotIterator) {
                    SharedPreferences currentSettings = getActivity().getSharedPreferences(PREF_CURRENT,0);
                    String current= currentSettings.getString("currentCustomer","");
                    ParkingSpot object = snap.getValue(ParkingSpot.class);
                    Log.d(TAG,"Parking object"+object.getId()+","+object.getCurrent()+"end");
                    Log.d(TAG,"our local cust id: "+current);
                    if (current.contains(object.getCurrent())){
                        ListSpaces.add(object.getId());
                        WeHaveOccupiedSpots = true;
                        temp=temp+1;
                    }
                    else if ((object.getStatus().contains("occupied")) && (object.getCurrent().contains("---"))){
                        ListSpaces.add(object.getId());
                        WeHaveOccupiedSpots = true;
                        temp=temp+1;
                    }
                }

                spotsToChoose = new String[temp];
                for(int i=0; i<temp; i++){
                    Log.d(TAG,ListSpaces.get(i));
//                    spotTimes[i]=ListTimes.get(i);
                    spotsToChoose[i]=ListSpaces.get(i);
                }

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, spotsToChoose);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spotSel.setAdapter(spinnerAdapter);
                spinnerAdapter.notifyDataSetChanged();

                if (temp == 0){
                    TextView ParkedSpot = (TextView) view.findViewById(R.id.ParkedSpot);
                    ParkedSpot.setText("---");
                    spotSel.setVisibility(View.INVISIBLE);
                }
                else
                    spotSel.setVisibility(View.VISIBLE);

                //final Spinner spotSel = (Spinner) view.findViewById(R.id.spotSel);
                //ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                //        android.R.layout.simple_spinner_item, garageSelect);
                //dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //spotSel.setAdapter(dataAdapter);



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    public PayForSpotFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_payformyspot, container, false);
        }
        else {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        spotSel = (Spinner) view.findViewById(R.id.spotSel);

        spotSel.setVisibility(View.INVISIBLE);

        createSpinner();
        return view;
    }
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState){
//        super.onViewCreated(view,savedInstanceState);
//        createSpinner();
//    }


    public void createSpinner(){
        final Spinner garSel = (Spinner) view.findViewById(R.id.garSel);
        final Spinner spotSel = (Spinner) view.findViewById(R.id.spotSel);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),R.array.garages, R.layout.spinner_item);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(),R.array.spots, R.layout.spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        garSel.setAdapter(adapter);
        spotSel.setAdapter(adapter2);

        garSel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> av, View v, int pos, long id) {
                selectedGarage= av.getItemAtPosition(pos).toString();
                currentGarageSelection = selectedGarage;
                database = FirebaseDatabase.getInstance();
                ref = database.getReferenceFromUrl(FirebaseConn.FIREBASE_URL).child("Garage").child(selectedGarage);
                query = ref.limitToFirst(5);
                query.addValueEventListener(queryEventListener());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                spotSel.setVisibility(View.INVISIBLE);
            }
        });



        spotSel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> av, View v, int pos, long id) {
                selectedSpot = av.getItemAtPosition(pos).toString();
                TextView ParkedSpot = (TextView) view.findViewById(R.id.ParkedSpot);
                ParkedSpot.setText(selectedSpot);
                database = FirebaseDatabase.getInstance();
                ref = database.getReferenceFromUrl(FirebaseConn.FIREBASE_URL).child("Garage").child(currentGarageSelection).child(selectedSpot);
                query = ref.limitToFirst(5);
                query.addValueEventListener(newQueryEventListener());
//                timedSpot = (TextView) view.findViewById(R.id.textView4);
//                timedSpot.setText(""+spotTimes[pos]+" minutes");
                selectedTimeIncrement = (TextView) getActivity().findViewById(R.id.timeToAdd);
                slider = (SeekBar) getActivity().findViewById(R.id.paymentScroll);

                slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        sliderprogress = slider.getProgress();
                        selectedTimeIncrement.setText("Time: " + (sliderprogress * 15) + " mins    Cost: $" + String.format("%.2f",(double)(sliderprogress * RATE)/100));
                    }

                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        Button button = (Button) view.findViewById(R.id.payformyspot);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent mIntent = new Intent(getContext(),CardInputActivity.class);
                ActivityOptions options= ActivityOptions.makeCustomAnimation(getContext(),R.anim.slide_up,R.anim.slide_down);
                mIntent.putExtra("garage",selectedGarage);
                mIntent.putExtra("spot",selectedSpot);
                mIntent.putExtra("timeWanted",sliderprogress*15);
                mIntent.putExtra("amount",sliderprogress*RATE);
                mIntent.putExtra("currency","usd");
                mIntent.putExtra("email","benji@gmail.com"); //////replace with user email
                getContext().startActivity(mIntent, options.toBundle());
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && refresh == 0) {
            Log.d("Log", "PayForMySpot is now visible");
            refresh = 1;

        }
        else{
            Log.d("Log", "PayForMySpot is now invisible");
            refresh = 0;
        }
    }

    public void showAndroidPay() {
        Log.d("Log", "Enter AndroidPay");
        //setContentView(R.layout.activity_home);

        Log.d("Log", "set content...home");
        Log.d("Log", "initialize");
        //...
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("Log","Connection failed");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Log pay"," got to activity result");
        super.onActivityResult(requestCode,resultCode,data);

    }

    @Override
    public void onStart(){
        super.onStart();
//        mGoogleApiClient.connect();
    }

    @Override
    public void onStop(){
        super.onStop();
//        mGoogleApiClient.disconnect();
    }
    @Override
    public void onDestroy(){
//        mGoogleApiClient=null;
        super.onDestroy();
    }

}
