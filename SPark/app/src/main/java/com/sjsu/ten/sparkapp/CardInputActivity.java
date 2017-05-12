package com.sjsu.ten.sparkapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.exception.AuthenticationException;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class CardInputActivity extends Activity {
    public static final String PREF_CURRENT = "MyPrefsFile";
    private CardInputWidget mCardInputWidget;
    private Button mPayButton;
    private static final String TAG = "CardInputActivity";
    private String email;
    private String currency;
    private String garageWanted;
    private String spotWanted;
    private int timeWanted;
    private int amountInCents;
    private EditText nameField;
    private EditText addr1Field;
    private EditText addr2Field;
    private EditText addrCityField;
    private EditText addrStateField;
    private EditText addrZipCodeField;
    private CheckBox saveCardDetails;
    private Card cardToSave;
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase database;
    private String tokenTemp = "";
    private String outputPost = "error";
    public AssetManager am;
    public Typeface custom_font;

    // You will need to use your live API key even while testing (for androidpay)
    public static final String PUBLISHABLE_KEY_LIVE = "pk_live_SpQlbSrkoNEzW8mcqRAkbTmg";
    public static final String PUBLISHABLE_KEY_TEST = "pk_test_Ti0rWDveCKHPrgsxxZJiQeKL";

//    private Context mContext;
    private Stripe stripe;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_input);

        am = getApplicationContext().getAssets();
        custom_font = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "Stark.ttf"));
        TextView title = (TextView) findViewById(R.id.title);
        title.setTypeface(custom_font);

        Intent intent= getIntent();
        timeWanted = intent.getIntExtra("timeWanted",0);
        garageWanted = intent.getStringExtra("garage");
        spotWanted = intent.getStringExtra("spot");
        email = intent.getStringExtra("email");
        amountInCents = intent.getIntExtra("amount",0);
        currency = intent.getStringExtra("currency");

        mCardInputWidget = (CardInputWidget) findViewById(R.id.card_input_widget);
        mPayButton = (Button) findViewById(R.id.paySpotButton);
        nameField = (EditText) findViewById(R.id.name);
        addr1Field = (EditText) findViewById(R.id.address_line1);
        addr2Field = (EditText) findViewById(R.id.address_line2);
        addrCityField = (EditText) findViewById(R.id.address_city);
        addrStateField = (EditText) findViewById(R.id.address_state);
        addrZipCodeField = (EditText) findViewById(R.id.address_zip);
        saveCardDetails = (CheckBox) findViewById(R.id.saveCardCheck);
        database=FirebaseDatabase.getInstance();

        nameField.requestFocus();
//        mContext = getBaseContext();
        mPayButton.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View v){
               try {
                   cardToSave = mCardInputWidget.getCard();
                   cardToSave.setCurrency("usd");
                   cardToSave.setName(nameField.getText().toString());
                   cardToSave.setAddressLine1(addr1Field.getText().toString());
                   cardToSave.setAddressLine2(addr2Field.getText().toString());
                   cardToSave.setAddressCity(addrCityField.getText().toString());
                   cardToSave.setAddressState(addrStateField.getText().toString());
                   cardToSave.setAddressZip(addrZipCodeField.getText().toString());
                   if (cardToSave == null) {                        //not valid card
                       Log.d(TAG, "Invalid Card Data");
                   } else {                                        //valid card

                       try {
                           stripe = new Stripe(getBaseContext(), PUBLISHABLE_KEY_TEST);
                       } catch (AuthenticationException e) {
                           e.printStackTrace();
                       }
                       stripe.createToken(
                               cardToSave,
                               new TokenCallback() {
                                   public void onSuccess(Token token) {
                                       tokenTemp = token.getId();
                                       Log.d(TAG, "" + token.getId());
                                       new StripeCharge(token.getId()).execute();

                                   }

                                   public void onError(Exception error) {
                                       // Show localized error message
                                       Toast.makeText(getBaseContext(),
                                               error.getLocalizedMessage(),
                                               Toast.LENGTH_LONG
                                       ).show();
                                   }
                               }
                       );
                       try{
                           finish();
//                           saveCardDetails = (CheckBox) findViewById(R.id.saveCardCheck);
//                           if(saveCardDetails.isChecked()){
//                               //TODO: save user card or customer id
//                           }
                       }    catch (NullPointerException e) {
                           e.printStackTrace();
                       }
                   }
               }
               catch (Exception e){
                   showInputAlert();
               }
           }
        });
    }

    private void showInputAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CardInputActivity.this);
        builder.setTitle(getString(R.string.invalidForm_title));
        builder.setMessage(getString(R.string.invalidForm_text));

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // positive button logic
                    }
                });

        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // negative button logic
                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

    public class StripeCharge extends AsyncTask<String, Void, String> {
        String token;
        public StripeCharge(String token){
            this.token=token;
        }
        @Override
        protected String doInBackground(String... params){

            new Thread(){
                @Override
                public void run(){
                    outputPost = postData(cardToSave,token, amountInCents, email, garageWanted, spotWanted, timeWanted);
                }
            }.start();
            return "Done";
        }
        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            Log.e("Result",s);
        }
    }

    public String postData(Card card, final String token, int amount, String email, String garage, String spot, int time) {   //post to a php server
        // Create a new HttpClient and Post Header
        try {
//            URL url = new URL("http://192.168.43.59/insert.php");
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setReadTimeout(10000);
//            conn.setConnectTimeout(15000);
//            conn.setRequestMethod("POST");
//            conn.setDoInput(true);
//            conn.setDoOutput(true);

            HttpPost httpPost = new HttpPost("http://gaganjit.club/insert.php"); ////replace later with actual site

            List<org.apache.http.NameValuePair> params = new ArrayList<org.apache.http.NameValuePair>();
            params.add(new BasicNameValuePair("method", "charge"));
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("stripeToken", token));
            params.add(new BasicNameValuePair("amount", ""+amount));
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("garage", garage));
            params.add(new BasicNameValuePair("spot", spot));
            params.add(new BasicNameValuePair("time", ""+time));
            params.add(new BasicNameValuePair("name", card.getName()));
            params.add(new BasicNameValuePair("lastFour", card.getLast4()));
            params.add(new BasicNameValuePair("addrLine1", card.getAddressLine1()));
            params.add(new BasicNameValuePair("addrLine2", card.getAddressLine2()));
            params.add(new BasicNameValuePair("addrCity", card.getAddressCity()));
            params.add(new BasicNameValuePair("addrState", card.getAddressState()));
            params.add(new BasicNameValuePair("addrZip", card.getAddressZip()));

//            HashMap<String, Object> params = new HashMap<String,Object>();
//            params.put("method", "charge");
//            params.put("account_id", description);
//            params.put("stripeToken", token);
//            params.put("amount", amount);

//            OutputStream os = null;
//
//            os = conn.getOutputStream();
//            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//            writer.write(getPostDataString(params));
//            writer.flush();
//            writer.close();
//            os.close();
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode()== HttpStatus.SC_OK){
                Log.d(TAG,"gdsgsd");
            }
            BufferedReader rd =  new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line="";
            String result = "no error";
            while ((line=rd.readLine()) != null){
                result = result+ line;
            }
            if (result.contains("Successful")){
                final String customer = result.substring(33);
                Log.d(TAG,customer);
                Query query;
                mDatabaseReference = database.getReferenceFromUrl(FirebaseConn.FIREBASE_URL).child("Garage").child(garageWanted).child(spotWanted);

                query = mDatabaseReference.limitToFirst(5);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                            ParkingSpot object = dataSnapshot.getValue(ParkingSpot.class);
                            int tempTime = timeWanted;
                            tempTime = tempTime + object.getPaid();
                            mDatabaseReference.child("paid").setValue(tempTime);
                            Log.d("DataChanged:",""+customer);
                            mDatabaseReference.child("current").setValue(customer);
                            SharedPreferences currentSettings = getSharedPreferences(PREF_CURRENT,0);
                            SharedPreferences.Editor editor = currentSettings.edit();
                            editor.putString("currentCustomer",customer);
                            editor.commit();
                            Log.d("Setting local cus_id",""+customer);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                return customer;
            }
            Log.d(TAG,result);
            //conn.disconnect();
            Log.d(TAG,"post");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Done";
    }

    private String getPostDataString(HashMap<String,Object> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String,Object> entry : params.entrySet()){
            if (first) first=false;
            else result.append("&");

            result.append(URLEncoder.encode(entry.getKey(),"UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(""+entry.getValue(),"UTF-8"));
        }

        return result.toString();
    }

}

