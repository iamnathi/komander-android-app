package com.efiloe.komander;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.common.api.CommonException;
import com.common.api.temperature.Temperature;
import com.common.api.temperature.TemperatureData;
import com.common.api.temperature.TemperatureNoneCalibrationException;
import com.common.api.temperature.TemperaturePreheatingException;
import com.common.api.temperature.TemperatureUtil;

import java.text.DecimalFormat;

public class TemperatureActivity extends AppCompatActivity {

    private Temperature temperatureScanner;
    private SharedPreferences sharedPreferences;

    Button mScanTemperatureButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        initializeBiometricDevice();
    }

    private void initializeBiometricDevice() {

        try {
            temperatureScanner = new Temperature(this);
            sharedPreferences = getSharedPreferences("mode", MODE_PRIVATE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mScanTemperatureButton = findViewById(R.id.mScanTemperatureButton);
        TextView versionLabel = findViewById(R.id.scannerVersionTextView);

        try {
            versionLabel.setText("Version: " + temperatureScanner.getVersion());

        } catch (TemperatureNoneCalibrationException e) {
            e.printStackTrace();
            versionLabel.setText("Get version failed: Device is not calibrated");
        } catch (TemperaturePreheatingException e) {
            e.printStackTrace();
            versionLabel.setText("Get version failed: Device is preheating now");
        } catch (CommonException e) {
            e.printStackTrace();
            versionLabel.setText("Get version failed");
        }
    }

    private void getTemperature() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mScanTemperatureButton.setEnabled(false);
                        }
                    });

                    TemperatureData temperatureData = temperatureScanner.getTemperatureData();
                    final float distance = temperatureData.getObjectDistance();
                    final float originalTemp = temperatureData.getOriginalTemperature();
                    final float shellTemp = temperatureData.getObjectTemperature();
                    final float sensorTemp = temperatureData.getSensorTemperature();
                    final float emTemp = temperatureData.getAmbientTemperature();
                    final float bodyTemp = TemperatureUtil.convertToBodyTemperature(0, shellTemp, sensorTemp, emTemp, sharedPreferences.getInt("mode",0));
                    final DecimalFormat df = new DecimalFormat("0.00");
                    if (distance < 70) {
                        String summary = "Body temperature: " + df.format(bodyTemp) + "℃"
                                + "\nSurface Temperature: " + df.format(shellTemp) + "℃"
                                + "\nOriginal temperature: " + originalTemp + "℃"
                                + "\nSensor temperature: " + df.format(sensorTemp) + "℃"
                                + "\nNTC temperature: " + df.format(emTemp) + "℃"
                                + "\nDistance: " + distance + "mm";

//                        if(bodyTemp < sp.getFloat("temp_threshold",37.3f)){
//                            textToSpeech.speak("Temperature normal", TextToSpeech.QUEUE_FLUSH,null,"normal");
//                        }else {
//                            textToSpeech.speak("Temperature anomaly",TextToSpeech.QUEUE_FLUSH,null,"high");
//                        }

                    }else {
//                        showResult(false,"Please come closer");
                    }
                } catch (TemperatureNoneCalibrationException e) {
                    e.printStackTrace();
//                    showResult(false,"Get temperature failed" + "\n" + "Device is not calibrated");
                } catch (TemperaturePreheatingException e) {
                    e.printStackTrace();
//                    showResult(false,"Get temperature failed" + "\n" + "Device is preheating now");
                } catch (CommonException e) {
                    e.printStackTrace();
//                    showResult(false,"Get temperature failed");
                }
            }
        }).start();
    }
}