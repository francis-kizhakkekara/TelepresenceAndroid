package com.example.telepresence_v1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    TextView pitchTextView, yawTextView;

    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private SensorEventListener gyroEventListener;

    DatabaseReference dbRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference dbPitchRef = dbRootRef.child("thePitch");
    DatabaseReference dbYawRef = dbRootRef.child("theYaw");

    private WebView LeftView, RightView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pitchTextView = findViewById(R.id.pitchView);
        yawTextView = findViewById(R.id.yawView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        gyroEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // Oneplus 3
                // If phone is in landscape orientation with usb port to the right
                // then Forward is going into the screen, Backward is coming out
                // Up for us is right of the phone, down for left side of the phone
                // Left for us, top of the phone, right for us is bottom of the phone
                // In this case, the Yaw, the left/right motion of head is event.values[2]
                // and Pitch, the up/down motion of head is event.values[1])
                // -180 to 180 deg
                int pitchDeg, rollDeg, yawDeg;
                rollDeg = (int)Math.round(180 * event.values[0]);
                pitchDeg  = (int)Math.round(180 * event.values[1]);     // This is PITCH
                yawDeg = (int)Math.round(180 * event.values[2]);        // This is YAW
                //pitchView.setText(Integer.toString(pitchDeg));
                //rollView.setText(Integer.toString(rollDeg));
                //yawView.setText(Integer.toString(yawDeg));

                dbPitchRef.setValue(pitchDeg);
                dbYawRef.setValue(yawDeg);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        String htmlText = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "\n" +
                "<head>\n" +
                "    <style>\n" +
                "        img {\n" +
                "            width: 100%;\n" +
                "            height: auto;\n" +
                "        }\n" +
                "\n" +
                "        .rotateimg180 {\n" +
                "            -webkit-transform: rotate(90deg);\n" +
                "            -moz-transform: rotate(90deg);\n" +
                "            -ms-transform: rotate(90deg);\n" +
                "            -o-transform: rotate(90deg);\n" +
                "            transform: rotate(90deg);\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "\n" +
                "<body style=\"background-color:#000000;\">\n" +
                "    <img src=\"http://192.168.1.165:8080/?action=stream\" width=\"640\" height=\"480\" class=\"rotateimg180\">\n" +
                "</body>\n" +
                "\n" +
                "</html>";
        String encodedHTML = Base64.encodeToString(htmlText.getBytes(), Base64.NO_PADDING);

        LeftView = findViewById(R.id.webViewLeft);
        //LeftView.setWebViewClient(new WebViewClient());       // Webview not chrome based
        LeftView.setWebChromeClient(new WebChromeClient());
        //LeftView.loadUrl("http://192.168.1.165:8080/?action=stream");
        LeftView.loadData(encodedHTML, "text/html", "base64");
        WebSettings webSettingsLeft = LeftView.getSettings();
        webSettingsLeft.setJavaScriptEnabled(true);

        RightView = findViewById(R.id.webViewRight);
        //RightView.setWebViewClient(new WebViewClient());  // Webview not chrome based
        RightView.setWebChromeClient(new WebChromeClient());
        //RightView.loadUrl("http://192.168.1.165:8080/?action=stream");
        RightView.loadData(encodedHTML, "text/html", "base64");
        WebSettings webSettingsRight = LeftView.getSettings();
        webSettingsRight.setJavaScriptEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(gyroEventListener, gyroSensor, sensorManager.SENSOR_DELAY_FASTEST);

        dbPitchRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer text = dataSnapshot.getValue(Integer.class);
                pitchTextView.setText(text.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        dbYawRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer text = dataSnapshot.getValue(Integer.class);
                yawTextView.setText(text.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(gyroEventListener);
    }
}
