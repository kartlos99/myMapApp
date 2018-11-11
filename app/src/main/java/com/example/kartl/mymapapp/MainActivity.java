package com.example.kartl.mymapapp;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isServicesOK()){
            init();
        }

        final TextView textView = findViewById(R.id.tview);

        EditText editText = findViewById(R.id.etext);
        editText.setKeyListener(new KeyListener() {
            @Override
            public int getInputType() {
                return 0;
            }

            @Override
            public boolean onKeyDown(View view, Editable editable, int i, KeyEvent keyEvent) {
                textView.append(keyEvent.toString());
                Toast.makeText(getApplicationContext(), "kay_ID : "+i, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onKeyUp(View view, Editable editable, int i, KeyEvent keyEvent) {
                return false;
            }

            @Override
            public boolean onKeyOther(View view, Editable editable, KeyEvent keyEvent) {
                Toast.makeText(getApplicationContext(), "kay_Other : ", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public void clearMetaKeyState(View view, Editable editable, int i) {

            }
        });
    }

    private void init(){
        Button btnmap = findViewById(R.id.btnMap);

        btnmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

    }

    public boolean isServicesOK(){
        Log.d(TAG, "check google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (available == ConnectionResult.SUCCESS){
            Log.d(TAG, "Google play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(TAG, "isServiseOK: error, but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "you can't make map requests", Toast.LENGTH_SHORT).show();
        }

        return false;
    }
}
