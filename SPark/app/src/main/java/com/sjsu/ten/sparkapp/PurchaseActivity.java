package com.sjsu.ten.sparkapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentMethodTokenizationType;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.stripe.android.model.Token;
import com.stripe.android.net.TokenParser;

import org.json.JSONException;

public class PurchaseActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    public static final int mEnvironment = WalletConstants.ENVIRONMENT_TEST;
    private static final String TAG = "PurchaseActivityLog";
    private GoogleApiClient mGoogleApiClient;

    // You will need to use your live API key even while testing
    public static final String PUBLISHABLE_KEY = "pk_live_SpQlbSrkoNEzW8mcqRAkbTmg";

    // Unique identifiers for asynchronous requests:
    private static final int LOAD_MASKED_WALLET_REQUEST_CODE = 1000;
    private static final int LOAD_FULL_WALLET_REQUEST_CODE = 1001;

    private SupportWalletFragment walletFragment;

    private int timeWanted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wallet.API, new Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                        .setTheme(WalletConstants.THEME_LIGHT)
                        .build())
                .build();
        Wallet.Payments.isReadyToPay(mGoogleApiClient).setResultCallback(
                new ResultCallback<BooleanResult>() {
                    @Override
                    public void onResult(@NonNull BooleanResult booleanResult) {
                        if (booleanResult.getStatus().isSuccess()) {
                            if (booleanResult.getValue()) {
                                showAndroidPay();
                            } else {
                                // Hide Android Pay buttons, show a message that Android Pay
                                // cannot be used yet, and display a traditional checkout button
                            }
                        } else {
                            // Error making isReadyToPay call
                            Log.e(TAG, "isReadyToPay:" + booleanResult.getStatus());
                        }
                    }
                }
        );

        //...
        Intent intent = getIntent();
        timeWanted = intent.getIntExtra("EXTRA_SLIDER_PROGRESS",0);
//        checkAndroidPayAvailable();
    }

    private void checkAndroidPayAvailable() {
        Wallet.Payments.isReadyToPay(mGoogleApiClient).setResultCallback(new ResultCallback<BooleanResult>() {
            @Override
            public void onResult(@NonNull BooleanResult result) {
                if (walletFragment != null) {
                    boolean enabled = result.getStatus().isSuccess() && result.getValue();
                    walletFragment.setEnabled(enabled);
                    Log.d("Log", "Android pay is available/wallet fragment is not null.");
                }
            }
        });
    }

    private void showAndroidPay() {
//        setContentView(R.layout.activity_purchase);

        walletFragment =
                (SupportWalletFragment) getSupportFragmentManager().findFragmentById(R.id.wallet_fragment);

        MaskedWalletRequest maskedWalletRequest = MaskedWalletRequest.newBuilder()

                // Request credit card tokenization with Stripe by specifying tokenization parameters:
                .setPaymentMethodTokenizationParameters(PaymentMethodTokenizationParameters.newBuilder()
                        .setPaymentMethodTokenizationType(PaymentMethodTokenizationType.PAYMENT_GATEWAY)
                        .addParameter("gateway", "stripe")
                        .addParameter("stripe:publishableKey", PUBLISHABLE_KEY)
                        .addParameter("stripe:version", com.stripe.android.BuildConfig.VERSION_NAME)
                        .build())

                // You want the shipping address:
                .setShippingAddressRequired(true)

                // Price set as a decimal:
                .setEstimatedTotalPrice("2.00")
                .setCurrencyCode("USD")
                .build();

        // Set the parameters:
        WalletFragmentInitParams initParams = WalletFragmentInitParams.newBuilder()
                .setMaskedWalletRequest(maskedWalletRequest)
                .setMaskedWalletRequestCode(LOAD_MASKED_WALLET_REQUEST_CODE)
                .build();

        // Initialize the fragment:
        walletFragment.initialize(initParams);
        Log.d("Log", "initialize");
//    ...
    }

    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
//    ...
    }

    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
//    ...
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"Got to activity result!");
        Log.d(TAG,"Request code: "+ requestCode);
        Log.d(TAG,"Result code: "+ resultCode);
        Log.d(TAG,"data: "+ data);
//        if (requestCode == LOAD_MASKED_WALLET_REQUEST_CODE) { // Unique, identifying constant
//            if (resultCode == Activity.RESULT_OK && data != null) {
//                Log.d(TAG,"Result masked wallet was ok");
//                MaskedWallet maskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
//                Log.d(TAG,"Result masked wallet was ok2");/*
//                FullWalletRequest fullWalletRequest = FullWalletRequest.newBuilder()
//                        .setCart(Cart.newBuilder()
//                                .setCurrencyCode("USD")
//                                .setTotalPrice("2.00")
//                                .addLineItem(LineItem.newBuilder() // Identify item being purchased
//                                        .setCurrencyCode("USD")
//                                        .setQuantity("1")
//                                        .setDescription("Parking spot A02")
//                                        .setTotalPrice("2.00")
//                                        .setUnitPrice("2.00")
//                                        .build())
//                                .build())
//                        .setGoogleTransactionId(maskedWallet.getGoogleTransactionId())
//                        .build();*/
//                Log.d(TAG,"Result masked wallet was ok3");
//                Wallet.Payments.loadFullWallet(mGoogleApiClient, getFullWalletRequest(maskedWallet) , LOAD_FULL_WALLET_REQUEST_CODE);
//                Log.d(TAG,"Result masked wallet was ok4");
//            }
//        }
//
//        else if (requestCode == LOAD_FULL_WALLET_REQUEST_CODE) {
//            if (resultCode == Activity.RESULT_OK) {
//                Log.d(TAG,"Result full wallet was ok");
//                FullWallet fullWallet = data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);
//                String tokenJSON = fullWallet.getPaymentMethodToken().getToken();
//                Log.d(TAG,"Token: "+tokenJSON);
//                //A token will only be returned in production mode,
//                //i.e. WalletConstants.ENVIRONMENT_PRODUCTION
//                if (mEnvironment == WalletConstants.ENVIRONMENT_PRODUCTION) {
//                    try {
//                        Token token = TokenParser.parseToken(tokenJSON);
//                        // TODO: send token to your server
//                    } catch (JSONException jsonException) {
//                        // Log the error and notify Stripe helpß
//                    }
//                }
//            }
//        } else {
//            Log.d(TAG,"Neither masked wallet or full wallet");
//            super.onActivityResult(requestCode, resultCode, data);
//        }
        if (requestCode == LOAD_MASKED_WALLET_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG,"Result masked wallet was ok");
                MaskedWallet mMaskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                Log.d(TAG,"Result masked wallet was ok2");
//                ((FullWalletConfirmationButtonFragment)getResultTargetFragment()).updateMaskedWallet(mMaskedWallet);      /////Create a new fragment class for full wallet?
                FullWalletRequest fullWalletRequest = FullWalletRequest.newBuilder()
                        .setCart(Cart.newBuilder()
                                .setCurrencyCode("USD")
                                .setTotalPrice(""+timeWanted*.25)
                                .addLineItem(LineItem.newBuilder() // Identify item being purchased
                                        .setCurrencyCode("USD")
                                        .setQuantity("1")
                                        .setDescription("Parking Spot: "+"A02")
                                        .setTotalPrice(""+timeWanted*.25)
                                        .setUnitPrice(""+timeWanted*.25)
                                        .build())
                                .build())
                        .setGoogleTransactionId(mMaskedWallet.getGoogleTransactionId())
                        .build();
                Log.d(TAG,"Result masked wallet was ok3");
                Wallet.Payments.loadFullWallet(mGoogleApiClient, fullWalletRequest, LOAD_FULL_WALLET_REQUEST_CODE);
                Log.d(TAG,"Result masked wallet was ok4");
            }
        } else if (requestCode == LOAD_FULL_WALLET_REQUEST_CODE) {
            Log.d(TAG,"Result full wallet");
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG,"Result full wallet was ok2");
                FullWallet fullWallet = data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);

                String tokenJSON = fullWallet.getPaymentMethodToken().getToken();
                Log.d("Log Pay", tokenJSON);
                //A token will only be returned in production mode,
                //i.e. WalletConstants.ENVIRONMENT_PRODUCTION
                if (mEnvironment == WalletConstants.ENVIRONMENT_PRODUCTION) {
                    try {
                        Token token = TokenParser.parseToken(tokenJSON);
                        // TODO: send token to your server
                    } catch (JSONException jsonException) {
                        // Log the error and notify Stripe helpß
                    }
                }
            }else if(resultCode==Activity.RESULT_CANCELED){

            } else{
                Log.d(TAG,"RESULT_FIRST_USER/RESULT_ERROR");
                int errorCode = -1;
                if(data != null) {
                    errorCode =  data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE,-1);
                    handleError(errorCode);
                }
            }
        } else {
            Log.d(TAG,"Neither masked wallet or full wallet");
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private FullWalletRequest getFullWalletRequest(MaskedWallet maskedWallet) {

        return FullWalletRequest.newBuilder()
                .setGoogleTransactionId(maskedWallet.getGoogleTransactionId())
                .setCart(Cart.newBuilder()
                        .setCurrencyCode("USD")
                        .setTotalPrice("10.10")
                        .addLineItem(LineItem.newBuilder()
                                .setCurrencyCode("USD")
                                .setDescription("Google I/O Sticker")
                                .setQuantity("1")
                                .setUnitPrice("10.00")
                                .setTotalPrice("10.00")
                                .build())
                        .addLineItem(LineItem.newBuilder()
                                .setCurrencyCode("USD")
                                .setDescription("Tax")
                                .setRole(LineItem.Role.TAX)
                                .setTotalPrice(".10")
                                .build())
                        .build())
                .build();
    }
    void handleError(int errorCode) {
        switch (errorCode) {
            case WalletConstants.ERROR_CODE_INVALID_PARAMETERS:
                Log.d(TAG,"RESULT_FIRST_USER/RESULT_ERROR1");
            case WalletConstants.ERROR_CODE_AUTHENTICATION_FAILURE:
                Log.d(TAG,"RESULT_FIRST_USER/RESULT_ERROR2");
            case WalletConstants.ERROR_CODE_BUYER_ACCOUNT_ERROR:
                Log.d(TAG,"RESULT_FIRST_USER/RESULT_ERROR3");
            case WalletConstants.ERROR_CODE_MERCHANT_ACCOUNT_ERROR:
                Log.d(TAG,"RESULT_FIRST_USER/RESULT_ERROR4");
            case WalletConstants.ERROR_CODE_SERVICE_UNAVAILABLE:
                Log.d(TAG,"RESULT_FIRST_USER/RESULT_ERROR5");
            case WalletConstants.ERROR_CODE_UNSUPPORTED_API_VERSION:
                Log.d(TAG,"RESULT_FIRST_USER/RESULT_ERROR5");
            case WalletConstants.ERROR_CODE_UNKNOWN:
                Log.d(TAG,"RESULT_FIRST_USER/RESULT_ERROR6");
            default:
                // unrecoverable error
//                mGoogleWalletDisabled = true;
//                displayGoogleWalletErrorToast(errorCode);
                break;
        }
    }

}


