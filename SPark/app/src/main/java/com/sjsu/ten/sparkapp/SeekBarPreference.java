/*
package com.sjsu.ten.sparkapp;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {


    public static int maximum    = 100;
    public static int interval   = 1;

    private float oldValue = 80;
    private TextView monitorBox;


    public SeekBarPreference(Context context) {
        super(context);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    protected View onCreateView(ViewGroup parent){
        super.onCreateView(parent);
        LinearLayout layout = new LinearLayout(getContext());


        layout.setPadding(15, 5, 10, 5);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        TextView view = new TextView(getContext());
        view.setText(getTitle());
        view.setTextSize(18);
        view.setGravity(Gravity.LEFT);



        SeekBar bar = new SeekBar(getContext());
        bar.setMax(maximum);
        bar.setProgress((int)this.oldValue);

        bar.setOnSeekBarChangeListener(this);

        this.monitorBox = new TextView(getContext());

        this.monitorBox.setPadding(2, 5, 0, 0);
        this.monitorBox.setText(bar.getProgress()+"");


        layout.addView(view);
        layout.addView(bar);
        layout.addView(this.monitorBox);
        layout.setId(android.R.id.widget_frame);


        return layout;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {

        progress = Math.round(((float)progress)/interval)*interval;

        if(!callChangeListener(progress)){
            seekBar.setProgress((int)this.oldValue);
            return;
        }

        seekBar.setProgress(progress);
        this.oldValue = progress;
        this.monitorBox.setText(progress+"");
        updatePreference(progress);

        notifyChanged();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }


    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index){

        int dValue = (int)ta.getInt(index,50);

        return validateValue(dValue);
    }


    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        int temp = restoreValue ? getPersistedInt(50) : (Integer)defaultValue;

        if(!restoreValue)
            persistInt(temp);

        this.oldValue = temp;
    }


    private int validateValue(int value){

        if(value > maximum)
            value = maximum;
        else if(value < 0)
            value = 0;
        else if(value % interval != 0)
            value = Math.round(((float)value)/interval)*interval;


        return value;
    }


    private void updatePreference(int newValue){

        SharedPreferences.Editor editor =  getEditor();
        editor.putInt(getKey(), newValue);
        editor.commit();
    }

}
*/