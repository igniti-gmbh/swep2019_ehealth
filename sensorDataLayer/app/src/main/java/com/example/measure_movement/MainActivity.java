package com.example.measure_movement;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {


    Switch aSwitch=null;
    Switch stepsSwitch=null;
    Switch sigMotionSwitch=null;
    Button testButton=null;

    //LocationLoggerService logger=new LocationLoggerService();


    LocationLoggerListener locList=new LocationLoggerListener();

    StepsLoggerListener stepList=new StepsLoggerListener();

    SigMotionListener sigList=new SigMotionListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println("starting here");

        //logger.setAlarm(this);


        aSwitch=(Switch)findViewById(R.id.aSwitch);

        stepsSwitch=(Switch)findViewById(R.id.stepsSwitch);

        testButton=(Button)findViewById(R.id.testButton);

        sigMotionSwitch=(Switch)findViewById(R.id.sigMotionSwitch);
        //aSwitch.setChecked(logger.checkAlarm(this));
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    SwitchTurnedOn();
                }else{
                    SwitchTurnedOff();
                }
            }
        });

        stepsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    LogStepsTurnedOn();
                }else{
                    LogStepsTurnedOff();
                }
            }
        });

        sigMotionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sigList.startListening(MainActivity.this);
                }else{
                    sigList.stopListening(MainActivity.this);
                }
            }
        });
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });
    }



    void SwitchTurnedOn(){
        System.out.println("location tracking on");
        //send.setAlarm(this);
        //logger.setAlarm(this);

        locList.startListeningLocations(this);

    }

    void SwitchTurnedOff(){
        System.out.println("location tracking off");
        //send.cancelAlarm(this);
        //logger.cancelAlarm(this);

        locList.stopListeningLocation(this);

    }

    void LogStepsTurnedOn(){
        System.out.println("steps tracking on");
        stepList.startListening(this);
    }

    void LogStepsTurnedOff(){
        System.out.println("steps tracking off");
        stepList.stopListening(this);
    }

    void test(){
        if(locList!=null) {
            locList.printData();
        }
        if(stepList!=null){
            stepList.printData();
        };

    }
}
