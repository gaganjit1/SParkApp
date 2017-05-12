package com.sjsu.ten.sparkapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.stripe.android.model.Token;
import com.stripe.android.net.TokenParser;

import org.json.JSONException;

public class HomeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final int mEnvironment = WalletConstants.ENVIRONMENT_TEST;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth firebaseAuth;
    // You will need to use your live API key even while testing
    public static final String PUBLISHABLE_KEY = "pk_live_SpQlbSrkoNEzW8mcqRAkbTmg";

    // Unique identifiers for asynchronous requests:
    private static final int LOAD_MASKED_WALLET_REQUEST_CODE = 1000;
    private static final int LOAD_FULL_WALLET_REQUEST_CODE = 1001;

    private SupportWalletFragment walletFragment;

//    PaymentMethodTokenizationParameters parameters =
//            PaymentMethodTokenizationParameters.newBuilder()
//                    .setPaymentMethodTokenizationType(PaymentMethodTokenizationType.PAYMENT_GATEWAY)
//                    .addParameter("gateway", "stripe")
//                    .addParameter("stripe:publishableKey", publishableKey)
//                    .addParameter("stripe:version", version)
//                    .build();

//    MaskedWalletRequest request = MaskedWalletRequest.newBuilder()
//            .setMerchantName(Constants.MERCHANT_NAME)
//            .setPhoneNumberRequired(true)
//            .setShippingAddressRequired(true)
//            .setCurrencyCode(Constants.CURRENCY_CODE_USD)
//            .setEstimatedTotalPrice(cartTotal)
//            // Create a Cart with the current line items. Provide all the information
//            // available up to this point with estimates for shipping and tax included.
//            .setCart(Cart.newBuilder()
//                    .setCurrencyCode(Constants.CURRENCY_CODE_USD)
//                    .setTotalPrice(cartTotal)
//                    .setLineItems(lineItems)
//                    .build())
//            .setPaymentMethodTokenizationParameters(parameters)
//            .build();

    public GoogleApiClient getmGoogleApiClient(){
        return mGoogleApiClient;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this)     // used to set up wallet
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wallet.API, new Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                        .setTheme(WalletConstants.THEME_LIGHT)
                        .build())
                .enableAutoManage(this,this)
                .build();
        Log.d("Log","Google API Wallet Build Successful");

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        Log.d("Log","SET TO..."+sharedPref.getString("GarageChoice","auto"));
        Data.getInstance().setGarage(sharedPref.getString("GarageChoice","auto"));

        setContentView(R.layout.activity_home);
        Firebase.setAndroidContext(this);
        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();

        //if the user is not logged in
        //that means current user will return null
        if (firebaseAuth.getCurrentUser() == null) {
            //closing this activity
            finish();
            //starting login activity
            startActivity(new Intent(this, LoginActivity.class));
        }

        //getting current user
//        FirebaseUser user = firebaseAuth.getCurrentUser();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setLogo(R.drawable.title);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.nav));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.garbl));
        //tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.schedbl));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.paybl));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        final PagerAdapter adapter = new com.sjsu.ten.sparkapp.PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()){
                    case 0: tab.setIcon(R.drawable.nav); break;
                    case 1: tab.setIcon(R.drawable.gar); break;
                    //case 2: tab.setIcon(R.drawable.sched); break;
                    case 2: tab.setIcon(R.drawable.pay); break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0: tab.setIcon(R.drawable.navbl); break;
                    case 1: tab.setIcon(R.drawable.garbl); break;
                    //case 2: tab.setIcon(R.drawable.schedbl); break;
                    case 2: tab.setIcon(R.drawable.paybl); break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0: tab.setIcon(R.drawable.nav); break;
                    case 1: tab.setIcon(R.drawable.gar); break;
                    //case 2: tab.setIcon(R.drawable.sched); break;
                    case 2: tab.setIcon(R.drawable.pay); break;
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);

        MenuItem Logout = menu.findItem(R.id.action_logout);
        Logout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                logOut();
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.d("Log", "0");
            //finish();
            Log.d("Log", "1");
            //starting settings activity
            Intent i = new Intent(HomeActivity.this, SettingsMenu.class);
            Log.d("Log", "2");
            startActivity(i);
            Log.d("Log", "3");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logOut()
    {
        //logging out the user
        firebaseAuth.signOut();
        //closing activity
        finish();
        //starting login activity
        Intent i = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(i);
    }

    @Override
    public void onStart(){
        super.onStart();
        mGoogleApiClient.connect();          // must connect and disconnect
    }

    @Override
    public void onStop(){
        super.onStop();
        mGoogleApiClient.disconnect();           // must connect and disconnect
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == LOAD_MASKED_WALLET_REQUEST_CODE){
            if (requestCode == Activity.RESULT_OK){
                Log.d("Log","Masked Wallet");
                MaskedWallet maskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                Log.d("Log","Full Wallet");
                FullWalletRequest fullWalletRequest = FullWalletRequest.newBuilder()
                        .setCart(Cart.newBuilder()
                                .setCurrencyCode("USD")
                                .setTotalPrice("20.00")
                                .addLineItem(LineItem.newBuilder() // Identify item being purchased
                                        .setCurrencyCode("USD")
                                        .setQuantity("1")
                                        .setDescription("Premium Llama Food")
                                        .setTotalPrice("20.00")
                                        .setUnitPrice("20.00")
                                        .build())
                                .build())
                        .setGoogleTransactionId(maskedWallet.getGoogleTransactionId())
                        .build();
                Wallet.Payments.loadFullWallet(mGoogleApiClient, fullWalletRequest, LOAD_FULL_WALLET_REQUEST_CODE);
            }
        } else if (requestCode == LOAD_FULL_WALLET_REQUEST_CODE) { // Unique, identifying constant

            if (resultCode == Activity.RESULT_OK) {
                FullWallet fullWallet = data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);
                String tokenJSON = fullWallet.getPaymentMethodToken().getToken();

                //A token will only be returned in production mode,
                //i.e. WalletConstants.ENVIRONMENT_PRODUCTION
                if (mEnvironment == WalletConstants.ENVIRONMENT_PRODUCTION) {
                    try {
                        Token token = TokenParser.parseToken(tokenJSON);
                        // TODO: send token to your server
                    } catch (JSONException jsonException) {
                        Log.d("Log","Stripe Error");
                    }
                        // Log the error and notify Stripe helpß
                    }
                }
        } else{
            super.onActivityResult(requestCode,resultCode,data);
        }
        //...
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("Log","Connection failed");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("Log","Connection succeeded");
    }

    @Override
    public void onConnectionSuspended(int i) {}
    @Override
    public void onBackPressed(){
        if (getFragmentManager().getBackStackEntryCount()>0){
            getFragmentManager().popBackStack();
        }
        else{
            super.onBackPressed();
        }
    }
}

