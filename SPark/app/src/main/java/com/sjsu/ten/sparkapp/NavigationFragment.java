package com.sjsu.ten.sparkapp;

import android.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Ben on 9/24/2016.
 */
public class NavigationFragment extends Fragment implements LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private String garagechoice = Data.getInstance().getGarage();
    private View view;
    private TextView ParkingSpot;
    private String AvailableSpot; //change to request
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private Query query;
    private int refresh = 0;
    protected LocationManager locationManager;
    TextToSpeech tts;
    TextView txtLat;
    private static final int GPS_TIME_INTERVAL = 10000;             //Check every minute
    private double[] GarageLat;
    private double[] GarageLong;
    private double[] Distance;
    private int locationGranted;
    private String ClosestGarage = "4th";
    public boolean hasEventListener = false;
    public boolean isVisible = false;
    private double threshold = 80;

    private TextView ParkingFloor;
    private int AvailableFloor;

    public AssetManager am;
    public Typeface custom_font;

    SharedPreferences sharedPreferences;


    public NavigationFragment() {
        GarageLat = new double[] {37.3363601, 37.3330122, 37.3392174};                //{4th,7th,10th}
        GarageLong = new double[] {-121.8860603, -121.8808466, -121.8806396};         //{4th,7th,10th}
        Distance = new double[] {0,0,0};
    }

    private com.google.firebase.database.ValueEventListener queryEventListener() {

        return new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                ParkingSpot = (TextView) view.findViewById(R.id.ParkedSpot);
                ParkingFloor = (TextView) view.findViewById(R.id.floortext);
                boolean WeHaveFreeSpots = false;
                int temp=0;
                List<String> ListSpaces = new ArrayList<String>();
                List<String> ListFloors = new ArrayList<String>();
                Random rnjesus = new Random();
                for (DataSnapshot snap : snapshotIterator) {
                    ParkingSpot object = snap.getValue(ParkingSpot.class);
                    if (object.getStatus().contains("empty")) {
                        ListSpaces.add(object.getId());
                        ListFloors.add(""+object.getFloor());
                        WeHaveFreeSpots = true;
                        temp=temp+1;
                    }
                }

                Log.d("Log","Space?");
                if (WeHaveFreeSpots) {
                    Log.d("Log","Yes");
                    //ParkingSpot.setText(AvailableSpot);
                    //ParkingFloor.setText(""+AvailableFloor);
                    if(temp!=0){
                        int ran = rnjesus.nextInt(temp);
                        ParkingSpot.setText(ListSpaces.get(ran));
                        AvailableSpot=ListSpaces.get(ran);

                        ParkingFloor.setText("on Floor number "+ListFloors.get(ran));
                        AvailableFloor=Integer.parseInt(ListFloors.get(ran));
                    }
                    if (getVoiceGuidance()) {TTS(1);}
                }
                else {
                    ParkingSpot.setText("--");
                    ParkingFloor.setText("on Floor number --");
                    if (getVoiceGuidance()) {TTS(0);}
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }




        };
    }

    public void TTS(final int value){
        if (refresh == 1) {
            tts = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (value == 1)
                        speak(AvailableSpot + "on floor" + AvailableFloor + "is empty.");
                    if (value == 0)
                        speak("No spots are empty.");
                }
            });
        }
    }

    public void speak(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);

        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
        removeQueryListener();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (isVisible && Data.getInstance().getGarage().equals("auto"))
            locationCheck();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d("Log", "HOLA LOG " + key);
            if (key.equals("locationDebug")) {
                Boolean value = sharedPreferences.getBoolean("locationDebug", false);
                txtLat = (TextView) view.findViewById(R.id.LocationText);
                if (!value)
                    txtLat.setText("");
                else
                    txtLat.setText("Longitude:\nLatitude:");
            }
        }
    };

    public void locationCheck(){

        try {
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 2);
            }
            else {
                locationSetup();
                locationGranted = 1;
            }
        }
        catch (Exception e) {e.printStackTrace();locationGranted = 0;}
    }

    public void locationSetup(){
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPS_TIME_INTERVAL, 0, this);      //can comment out
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_TIME_INTERVAL, 0, this);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 2) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                locationGranted = 1;
                locationSetup();
            }
            else{
                Data.getInstance().setGarage("4th");
                garagechoice = "4th";
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("GarageChoice", "4th");
                editor.commit();
                dbresume();
                Toast.makeText(getActivity(), "Location Service was denied.\nPriority set to 4th Street Garage.", Toast.LENGTH_LONG).show();
                locationGranted = 0;
            }
        }

    }

    @Nullable
    @Override                                                                                       //CHECK IF VIEW ALREADY EXISTS
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_navigator, container, false);
        else
            ((ViewGroup) view.getParent()).removeView(view);

        am = getActivity().getApplicationContext().getAssets();
        custom_font = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "Stark.ttf"));

        TextView nps = (TextView) view.findViewById(R.id.textView);
        nps.setTextSize(30);
        nps.setTypeface(custom_font);
        TextView fl = (TextView) view.findViewById(R.id.floortext);
        fl.setTextSize(20);
        fl.setTypeface(custom_font);

        if (!Data.getInstance().getGarage().equals("auto")){
            txtLat = (TextView) view.findViewById(R.id.LocationText);
            txtLat.setText("Longitude:\nLatitude:");
        }
        return view;
    }

    @Override
    public void onPause() {
        //if (locationGranted == 1)
        //    locationManager.removeUpdates(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Data.getInstance().getGarage().equals("auto") && isVisible){
            Log.d("Log","Displayed message");
            this.locationCheck();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isVisible = isVisibleToUser;
        if (isVisibleToUser && refresh == 0) {
            Log.d("Log", "Navigation is now visible");
            refresh = 1;
            Log.d("Log", "GarageChoiceOriginal: " + Data.getInstance().getGarage());
            if (Data.getInstance().getGarage().equals("4th") || Data.getInstance().getGarage().equals("7th") || Data.getInstance().getGarage().equals("10th")) {
                garagechoice = Data.getInstance().getGarage();
                Log.d("Log", "GarageChoiceFinal: " + garagechoice);
                dbresume();
            }
            else if (Data.getInstance().getGarage().equals("auto")) {
                this.locationCheck();
                //garagechoice = ClosestGarage;
                Log.d("Log", "GarageChoiceFinal2: " + garagechoice);
            }
        }
        else {
            Log.d("Log", "Navigation is now invisible");
            if(locationManager != null){
                locationManager.removeUpdates(this);
            }
            removeQueryListener();
            refresh = 0;
        }
    }

    public void dbresume() {
        database = FirebaseDatabase.getInstance();
        ref = database.getReferenceFromUrl(FirebaseConn.FIREBASE_URL).child("Garage").child(garagechoice);
        query = ref.limitToFirst(5);
        if (!hasEventListener) {
            query.addValueEventListener(queryEventListener());
            hasEventListener = true;
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d("Log", "HOLA MUCHACHOS!!!!!");
        if (locationGranted == 1){
            String OriginalChoice = garagechoice;
            double myLatitude = location.getLatitude();
            double myLongitude = location.getLongitude();
            Log.d("Log", "Latitude: " + myLatitude + ", Longitude: " + myLongitude);
            txtLat = (TextView) view.findViewById(R.id.LocationText);
            String coordinates = getLocationDebug() ? "Latitude: " + myLatitude + ", \nLongitude: " + myLongitude : "";
            {txtLat.setText(coordinates);}

            double closest = Double.POSITIVE_INFINITY;
            String closestName = "4th";
            for (int i = 0; i <3; i++){
                Distance[i] = distanceBetween(myLatitude, myLongitude, GarageLat[i], GarageLong[i]);
                Log.d("Log", "Distance "+ i +": " + Distance[i]);
                if (Distance[i] < closest && Data.getInstance().getPercent(getIndex(i)) < threshold){
                    closest = Distance[i];
                    closestName = getIndex(i);
                }
            }
            Log.d("Log","EVALUATED CLOSEST DISTANCE AS " + closest);
            Log.d("Log","EVALUATED CLOSEST AS " + closestName);

            Log.d("Log","ORIGINAL AS " + OriginalChoice);

            ClosestGarage = closestName;
            if (!OriginalChoice.equals(ClosestGarage)){
                //Data.getInstance().setGarage(ClosestGarage);
                garagechoice = ClosestGarage;
                removeQueryListener();
                ref = database.getReferenceFromUrl(FirebaseConn.FIREBASE_URL).child("Garage").child(ClosestGarage);
                query = ref.limitToFirst(5);
                Log.d("Log","111111111");
                if (!hasEventListener) {
                    Log.d("Log","1111111112");
                    query.addValueEventListener(queryEventListener());
                    hasEventListener = true;
                }
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Log", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Log", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Log", "status");
    }

    private double distanceBetween(double lat1, double long1, double lat2, double long2) {
        double theta = long1 - long2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private void removeQueryListener(){
        if (hasEventListener) {
            ref.removeEventListener(queryEventListener());
            hasEventListener = false;
        }
    }

    public Boolean getVoiceGuidance(){
        try {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Log.d("Log", "" + sharedPref.getBoolean("voiceGuidance",true));
        return sharedPref.getBoolean("voiceGuidance",true);
        }
        catch (Exception e){
            return false;
        }

    }


    public Boolean getLocationDebug(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Log.d("Log", "" + sharedPref.getBoolean("locationDebug",false));
        return sharedPref.getBoolean("locationDebug",false);
    }

    @Override
    public void onDetach(){
        if (locationManager != null)
            locationManager.removeUpdates(this);
        removeQueryListener();
        super.onDetach();
    }

    public String getIndex(int i){
        String closestName;
        switch (i){
            case 0: closestName = "4th"; break;
            case 1: closestName = "7th"; break;
            case 2: closestName = "10th"; break;
            default: closestName = "4th"; break;
        }
        return closestName;
    }
}