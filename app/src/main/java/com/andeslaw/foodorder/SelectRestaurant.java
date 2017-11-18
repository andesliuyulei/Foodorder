package com.andeslaw.foodorder;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectRestaurant extends AppCompatActivity {
    //private static Spinner selectRestaurant = null;

    //public static String selectedRestaurant = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_restaurant);

        //selectRestaurant = (Spinner) findViewById(R.id.selectRestaurant);
        //selectRestaurant.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, MainActivity.restaurantList));
    }

    public void gotoMenu(View view) {
        //selectedRestaurant = selectRestaurant.getSelectedItem().toString();
        //getMenuListFromApi();
        //startMenu();
    }

    private void startMenu() {
        startActivity(new Intent(this, Menu.class));
    }
}
