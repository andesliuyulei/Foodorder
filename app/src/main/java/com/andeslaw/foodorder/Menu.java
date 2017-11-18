package com.andeslaw.foodorder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class Menu extends AppCompatActivity {
    //public static String[] menuList = null;

    //private Spinner selectFood = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //selectFood = (Spinner) findViewById(R.id.selectFood);
        //selectFood.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menuList));
    }

    public void addToOrder(View view) {
        Intent intent = new Intent(this, SingleItemOrder.class);
        startActivity(intent);
    }
}
