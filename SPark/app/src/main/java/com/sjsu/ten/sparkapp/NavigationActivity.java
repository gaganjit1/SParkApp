package com.sjsu.ten.sparkapp;

import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.util.Locale;

/**
 * Created by Ben on 9/24/2016.
 */
public class NavigationActivity extends FragmentActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_navigator);
        tts = new TextToSpeech(this, this);

    }

    @Override
    public void onInit(int status){
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
            speak("Hello");

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }


    public void speak(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);

        }else{                                                      //FOR BENJAMIN'S PHONE
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
    }

}
