package com.franklinwireless.android.jexkids.locker.permission;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.franklinwireless.android.jexkids.R;
import com.franklinwireless.android.jexkids.home.Homepage;
import com.franklinwireless.android.jexkids.home.ParentControl;
import com.franklinwireless.android.jexkids.locker.base.AppConstants;
import com.franklinwireless.android.jexkids.locker.password.DefinePasswordActivity;
import com.franklinwireless.android.jexkids.locker.services.BackgroundManager;
import com.franklinwireless.android.jexkids.locker.services.LockService;
import com.franklinwireless.android.jexkids.locker.setting.SettingsActivity;
import com.franklinwireless.android.jexkids.locker.utils.LockUtil;
import com.franklinwireless.android.jexkids.locker.utils.MainUtil;
import com.franklinwireless.android.jexkids.locker.utils.ToastUtil;
import com.franklinwireless.android.jexkids.locker.widget.PermissionDialog;

import java.util.List;
import java.util.Locale;

public class AppPermission extends AppCompatActivity implements View.OnClickListener
        , CompoundButton.OnCheckedChangeListener {

    private CheckBox read_phone;
    private CheckBox permit_usage;
    private ImageView btn_back;
    private Button proceed_btn;
    private static final int REQUEST_READ_PHONE_STATE = 1;
    private static final int REQUEST_PACKAGE_USAGE_STATS = 2;
    private static final int REQUEST_QUERY_ALL_PACKAGES = 3;
    private static final int RESULT_ACTION_USAGE_ACCESS_SETTINGS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_permission);
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = sharedPreferences.getString("My_Lang", null);
        if (language != null) {
            if (language.equals("en")) {
                setLocale("en");
            } else if (language.equals("es")) {
                setLocale("es");
            } else if (language.equals("ko")) {
                setLocale("ko");
            }
        }

        read_phone = findViewById(R.id.read_phone);
        permit_usage = findViewById(R.id.permit_usage);
        btn_back = findViewById(R.id.btn_back);
        proceed_btn = findViewById(R.id.proceed_btn);
        proceed_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AppPermission.this, Homepage.class));
                //startActivity(new Intent(AppPermission.this, ParentControl.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });

        read_phone.setOnCheckedChangeListener(this);
        permit_usage.setOnCheckedChangeListener(this);
        btn_back.setOnClickListener(this);
        check_permissions();
        check_overallPermissions();
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = this.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    public void check_overallPermissions() {
        boolean isFirstLock =
                MainUtil.getInstance().getBoolean(AppConstants.LOCK_IS_FIRST_LOCK, true);
        int i = 0;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            proceed_btn.setVisibility(View.GONE);
            i = i + 1;
        }
        if (isFirstLock) {
            i = i + 1;
            proceed_btn.setVisibility(View.GONE);
        }
        if (i == 0) {
            proceed_btn.setVisibility(View.VISIBLE);
        }
    }

    public void check_permissions() {
        if (ContextCompat.checkSelfPermission(AppPermission.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            read_phone.setChecked(false);
        } else {
            read_phone.setChecked(true);
            read_phone.setEnabled(false);
        }
        boolean isFirstLock =
                MainUtil.getInstance().getBoolean(AppConstants.LOCK_IS_FIRST_LOCK, true);
        if (isFirstLock) {
            permit_usage.setChecked(false);
        } else {
            permit_usage.setChecked(true);
            permit_usage.setEnabled(false);
        }
    }

    @Override
    public void onClick(@NonNull View view) {
        switch (view.getId()) {

            case R.id.btn_back:
                onBackPressed();
                break;
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean b) {
        switch (buttonView.getId()) {
            case R.id.read_phone:
                int j = 0;
                boolean isFirstLock2 =
                        MainUtil.getInstance().getBoolean(AppConstants.LOCK_IS_FIRST_LOCK, true);

                if (ContextCompat.checkSelfPermission(AppPermission.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
                    proceed_btn.setVisibility(View.GONE);
                    //j=j+1;
                }
                if (isFirstLock2) {
                    proceed_btn.setVisibility(View.GONE);
                    j = j + 1;
                }

                checkPermission(Manifest.permission.READ_PHONE_STATE, REQUEST_READ_PHONE_STATE);
                if (j == 0) {
                    proceed_btn.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.permit_usage:
                boolean isFirstLock =
                        MainUtil.getInstance().getBoolean(AppConstants.LOCK_IS_FIRST_LOCK, true);
                int k = 0;
                if (isFirstLock) {
                    showDialog();
                } else {
                }
                if (ContextCompat.checkSelfPermission(AppPermission.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
                    proceed_btn.setVisibility(View.GONE);
                    k = k + 1;
                }
                if (isFirstLock) {
                    proceed_btn.setVisibility(View.GONE);
                }

                if (k == 0) {
                    proceed_btn.setVisibility(View.VISIBLE);
                }
                break;
        }
    }


    private void showDialog() {
        if (!LockUtil.isStatAccessPermissionSet(AppPermission.this) && LockUtil.isNoOption(AppPermission.this)) {
            PermissionDialog dialog = new PermissionDialog(AppPermission.this);
            dialog.show();
            dialog.setOnClickListener(new PermissionDialog.onClickListener() {
                @Override
                public void onClick() {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        Intent intent = null;
                        intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        startActivityForResult(intent, RESULT_ACTION_USAGE_ACCESS_SETTINGS);
                        //permit_usage.setEnabled(false);
                    }
                }
            });
        } else {
            gotoCreatePwdActivity();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_ACTION_USAGE_ACCESS_SETTINGS) {
            if (LockUtil.isStatAccessPermissionSet(AppPermission.this)) {
                gotoCreatePwdActivity();
            } else {
                ToastUtil.showToast("Permission denied");
                finish();
            }
        }
    }

    private void gotoCreatePwdActivity() {
        SharedPreferences sharedPreference = getSharedPreferences("login", Context.MODE_PRIVATE);
        boolean loginstatus = sharedPreference.getBoolean("loginstatus", false);
        MainUtil.getInstance().putBoolean(AppConstants.LOCK_STATE, true);
//        BackgroundManager.getInstance().init(this).startService(LockService.class);
        MainUtil.getInstance().putBoolean(AppConstants.LOCK_IS_FIRST_LOCK, false);
        /*Intent intent2 = new Intent(AppPermission.this, DefinePasswordActivity.class);
        startActivity(intent2);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);*/
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(AppPermission.this, permission) == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(AppPermission.this, new String[]{permission}, requestCode);
        } else {
            //Toast.makeText(AppPermission.this, "Permission already granted", Toast.LENGTH_SHORT).show();
            boolean isFirstLock =
                    MainUtil.getInstance().getBoolean(AppConstants.LOCK_IS_FIRST_LOCK, true);
            if (isFirstLock) {
                proceed_btn.setVisibility(View.GONE);
            } else {
                proceed_btn.setVisibility(View.VISIBLE);
            }
            read_phone.setEnabled(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //Toast.makeText(AppPermission.this, "Permission Granted", Toast.LENGTH_SHORT) .show();
                    read_phone.setEnabled(false);
                    boolean isFirstLock =
                            MainUtil.getInstance().getBoolean(AppConstants.LOCK_IS_FIRST_LOCK, true);
                    if (isFirstLock) {
                        proceed_btn.setVisibility(View.GONE);
                    } else {
                        proceed_btn.setVisibility(View.VISIBLE);
                    }
                } else {
                    //Toast.makeText(AppPermission.this, "Permission Denied", Toast.LENGTH_SHORT) .show();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}