package com.franklinwireless.android.jexkids.home;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.widget.Toast;

import com.franklinwireless.android.jexkids.R;
import com.franklinwireless.android.jexkids.locker.gesture.contract.LockMainContract;
import com.franklinwireless.android.jexkids.locker.gesture.presenter.LockMainPresenter;
import com.franklinwireless.android.jexkids.locker.model.CommLockInfo;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class Mainpage extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, LockMainContract.View{
    private BottomNavigationView bottomNav;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int RESULT_ACTION_USAGE_ACCESS_SETTINGS = 1;
    private PackageManager packageManager;
    private LockMainPresenter mLockMainPresenter;
    String imei;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainpage);
        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnNavigationItemSelectedListener(this);
        bottomNav.setSelectedItemId(R.id.home);
        if(checkPermission()){

            //TelephonyManager telephonyManager = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            //imei = telephonyManager.getImei();
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                imei = Settings.Secure.getString(
                        this.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            } else {
                final TelephonyManager mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (mTelephony.getDeviceId() != null) {
                    imei = mTelephony.getDeviceId();
                } else {
                    imei = Settings.Secure.getString(
                            this.getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                }
            }
        }else{

            requestPermission();
        }

        mLockMainPresenter = new LockMainPresenter(this, this);
        mLockMainPresenter.loadAppInfo(this);

    }
    @Override
    public void loadAppInfoSuccess(@NonNull List<CommLockInfo> list) {
        for (CommLockInfo info : list) {
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {


                }
                break;

        }
    }
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {

            Toast.makeText(getApplicationContext(), "Give permission to check whether internet is of or on.", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
        }
    }
    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE);

        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.home:
                //getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, firstFragment).commit();
                loadFragment(new home());
                return true;

            case R.id.active:
                loadFragment(new active());
                //getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, secondFragment).commit();
                return true;

            case R.id.pending:
                loadFragment(new Pending());
                //getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, thirdFragment).commit();
                return true;
            case R.id.claim:
                loadFragment(new claim());
                //getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, thirdFragment).commit();
                return true;
            case R.id.history:
                loadFragment(new History());
                //getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, thirdFragment).commit();
                return true;
        }
        return false;
    }
    private void loadFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, fragment).commit();
    }
}