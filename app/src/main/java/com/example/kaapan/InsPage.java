package com.example.kaapan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.kaapan.databinding.ActivityInsPageBinding;

import java.util.Locale;

public class InsPage extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityInsPageBinding binding;
    double latitude,longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInsPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView call = findViewById(R.id.ambulance);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = "tel:108";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(phone));
                startActivity(intent);
            }
        });

        TextView hos = findViewById(R.id.findhos);
        hos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri = "https://www.google.com/maps/search/?api=1&query=hospital+clinic";
                Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent1);
            }
        });

        TextView callpolice = findViewById(R.id.police);
        callpolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = "tel:100";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(phone));
                startActivity(intent);
            }
        });
        TextView pol = findViewById(R.id.findpol);
        pol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri = "https://www.google.com/maps/dir/?api=1&origin=&destination=police+station&travelmode=walking";
                Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent1);
            }
        });
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                latitude = getIntent().getDoubleExtra("latitude",0);
                longitude = getIntent().getDoubleExtra("longitude",0);
                Intent intent = new Intent(getApplicationContext(), map.class);
                intent.putExtra("long",latitude);
                intent.putExtra("lat",longitude);
                startActivity(intent);

            }
        });
    }

}