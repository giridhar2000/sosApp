package com.example.kaapan;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static java.lang.Thread.sleep;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Home extends AppCompatActivity {
    String latitude, longitude;
    double lat,lon;
    List<Address> addresses;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home1);
        BottomSheetBehavior mBottomSheetBehavior;
        ImageView con = findViewById(R.id.contacts);
        TextView top = findViewById(R.id.textView2);
        TextView locat = findViewById(R.id.location);
        Button sos = findViewById(R.id.SoS);
        Switch sil;
        sil = findViewById(R.id.silent);
        Vibrator vibrator;
        final MediaPlayer mediaPlayer = MediaPlayer.create(this,R.raw.sos);
        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        View bottomSheet = findViewById( R.id.sheet);
        populateListView();


        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        mBottomSheetBehavior.setPeekHeight(650);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBottomSheetBehavior.setPeekHeight(300);
                    top.setVisibility(View.VISIBLE);
                    sos.setVisibility(View.VISIBLE);
                    sil.setVisibility(View.VISIBLE);
                    mBottomSheetBehavior.setPeekHeight(650);
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
                top.setVisibility(View.GONE);
                sos.setVisibility(View.GONE);
                sil.setVisibility(View.GONE);
            }
        });



        sos.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                if(!sil.isChecked()) {
                    vibrator.vibrate(1000);
                    mediaPlayer.start();
                    {
                        try {
                            sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
                if(sil.isChecked()){
                    mediaPlayer.pause();
                    vibrator.vibrate(1000);
                }
                //after sound
                DBHelper db = new DBHelper(Home.this);
                Cursor data = db.getPhn();
                ArrayList<String> phn = new ArrayList<>();
                String message = "Emergency! need your help, my current address: " + longitude + " My Co-ordinates: " + latitude;
                while (data.moveToNext()){
                    phn.add(data.getString(0));
                }
                for(String no:phn) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(no, null, message, null, null);
                    Toast.makeText(Home.this, "Message sent", Toast.LENGTH_LONG).show();
                }
                Intent intent = new Intent(getApplicationContext(), InsPage.class);
                intent.putExtra("longitude",lat);
                intent.putExtra("latitude",lon);
                startActivity(intent);

            }
        });

        con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                pickContact.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(pickContact, 1);
            }

        });


        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getLocation();
        }
        else{
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Permissions");
            alertDialog.setMessage("Go to settings and allow permissions for contacts, location, and messages");
            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                    startActivity(i);
                }
            });

            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialog.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44 && grantResults.length > 0 && (grantResults[0] + grantResults[1])
                == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(locationManager.isProviderEnabled(GPS_PROVIDER)
                || locationManager.isProviderEnabled(NETWORK_PROVIDER)){
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    TextView locat = findViewById(R.id.location);
                    lat = location.getLatitude();
                    getLat();
                    lon = location.getLongitude();
                    if(location!=null){
                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        try {
                             addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            latitude = location.getLatitude() +", "+ location.getLongitude();
                            if(addresses.size() > 0){

                                latitude = location.getLatitude() +", "+ location.getLongitude();
                                longitude = addresses.get(0).getAddressLine(0);
                                locat.setText("CurrentLocation: " + longitude);
                            }
                            else{
                                locat.setText(latitude);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        long t = 1000;
                        float d = 100;
                        LocationListener locationListener = null;
                        locationManager.requestLocationUpdates(NETWORK_PROVIDER,t,d, (android.location.LocationListener) locationListener);
                    }
                }
            });
        }
        else{
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

    }

    public double getLat(){
        double latitude = lat;
        return latitude;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri contactData = data.getData();
        Cursor c = getContentResolver().query(contactData, null, null, null, null);
        if (c.moveToFirst()) {
            int phoneIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            String cname = c.getString(nameIndex);
            String num = c.getString(phoneIndex);
            AddData(cname, num);
            populateListView();
        }
    }

    public void AddData(String data,String data1){
        DBHelper db = new DBHelper(this);
        boolean insertData = db.addData(data,data1);
        if(insertData){
            Toast.makeText(Home.this, "Contact added", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(Home.this, "Failed to add contact", Toast.LENGTH_LONG).show();
        }
    }

    private void populateListView() {
        ListView listView = findViewById(R.id.listView);
        DBHelper db = new DBHelper(this);
        Cursor data = db.getData();
        ArrayList<String> listdata = new ArrayList<>();
        while (data.moveToNext()){
            listdata.add(data.getString(0)+". "+ data.getString(1)+" : "+data.getString(2));
        }
        ListAdapter adapter = new ArrayAdapter<>(this, R.layout.list_layout, listdata);
        listView.setAdapter(adapter);
    }


}