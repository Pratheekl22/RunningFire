package com.example.runningfire;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;


import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private static final String TAG = "EmailPassword";
    private FirebaseUser user;
    SensorManager sensorManager;
    TextView tv_steps;
    int prevTotalSteps;
    int totalSteps;

    boolean running;

    Button login;
    Button register;
    Button run;
    Button fin;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        TextView username = (TextView) findViewById(R.id.username);
        TextView password = (TextView) findViewById(R.id.password);

        login = (Button) findViewById(R.id.loginbtn);
        register = (Button) findViewById(R.id.registerbtn);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        running = false;

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(username.getText().toString(), password.getText().toString());
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount(username.getText().toString(), password.getText().toString());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        }
    }

    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            MainActivity.this.user = user;
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if(user != null) {
            setContentView(R.layout.activity_main);
            String introduction = "Welcome to the Running App\n" + user.getEmail() + "!";
            TextView intro = findViewById(R.id.welcomeText);
            intro.setText(introduction);

            mainActivityHandler();
        }
    }

    private void reload() { }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            tv_steps = (TextView) findViewById(R.id.stepcount);
            if(tv_steps != null) {
                System.out.println("Hello there" + running);
                if (running) {
                    totalSteps = (int) sensorEvent.values[0];
                    int currentSteps = totalSteps - prevTotalSteps;
                    System.out.println(totalSteps);
                    String cnt = "" + currentSteps;
                    tv_steps.setText(cnt);
                }
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    public void onResume() {
        super.onResume();
        running = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(countSensor != null) {
            sensorManager.registerListener(sensorEventListener, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
            System.out.println("Step counter found!");
        } else {
            Toast.makeText(this, "Sensor not found!", Toast.LENGTH_SHORT).show();
        }

        runActivityHandler();
    }

    @Override
    public void onPause() {
        super.onPause();
        running = false;
    }

    public void mainActivityHandler() {
        //Get run button and add handler
        run = (Button) findViewById(R.id.runbutton);
        run.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.activity_run);
                tv_steps = (TextView) findViewById(R.id.stepstxt);
                prevTotalSteps = totalSteps;
                onResume();
            }
        }));

    }

    public void runActivityHandler() {
        //Store how many steps taken into firebase
        //mDatabase.child("users").child(user.getUid()).setValue()

        //Add listener for finish run button
        fin = (Button) findViewById(R.id.stopbtn);
        if(fin != null) {
            fin.setOnClickListener((new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setContentView(R.layout.activity_main);
                    mainActivityHandler();
                }
            }));
        }
    }
}
