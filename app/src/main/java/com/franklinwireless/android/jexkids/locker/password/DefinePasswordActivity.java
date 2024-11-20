package com.franklinwireless.android.jexkids.locker.password;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.widget.TextView;

import com.franklinwireless.android.jexkids.R;
import com.franklinwireless.android.jexkids.auth.Onboarding;
import com.franklinwireless.android.jexkids.home.Homepage;
import com.franklinwireless.android.jexkids.locker.base.AppConstants;
import com.franklinwireless.android.jexkids.locker.base.BaseActivity;
import com.franklinwireless.android.jexkids.locker.model.LockStage;
import com.franklinwireless.android.jexkids.locker.gesture.contract.GestureCreateContract;
import com.franklinwireless.android.jexkids.locker.gesture.presenter.GestureCreatePresenter;
import com.franklinwireless.android.jexkids.locker.permission.AppPermission;
import com.franklinwireless.android.jexkids.locker.services.BackgroundManager;
import com.franklinwireless.android.jexkids.locker.services.LockService;
import com.franklinwireless.android.jexkids.locker.utils.AppUtils;
import com.franklinwireless.android.jexkids.locker.utils.LockPatternUtils;
import com.franklinwireless.android.jexkids.locker.utils.MainUtil;
import com.franklinwireless.android.jexkids.locker.widget.LockPatternView;
import com.franklinwireless.android.jexkids.locker.widget.LockPatternViewPattern;
import com.franklinwireless.android.jexkids.locker.widget.PermissionDialogMI;
import com.franklinwireless.android.jexkids.locker.widget.PermissionDialogVivo;

import java.util.List;

public class DefinePasswordActivity extends BaseActivity implements View.OnClickListener,
        GestureCreateContract.View {

    @Nullable
    private List<LockPatternView.Cell> mChosenPattern = null;

    private TextView mLockTip;
    private LockPatternView mLockPatternView;
    @NonNull
    private final Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };
    private TextView mBtnReset;
    private LockStage mUiStage = LockStage.Introduction;
    private LockPatternUtils mLockPatternUtils;
    private LockPatternViewPattern mPatternViewPattern;
    private GestureCreatePresenter mGestureCreatePresenter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_define_password;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mLockPatternView = findViewById(R.id.lock_pattern_view);
        mLockTip = findViewById(R.id.lock_tip);
        mBtnReset = findViewById(R.id.btn_reset);


        if (Build.MANUFACTURER.equalsIgnoreCase("xiaomi") || Build.MANUFACTURER.equalsIgnoreCase("redmi")) {

            PermissionDialogMI dialog = new PermissionDialogMI(DefinePasswordActivity.this);
            dialog.show();
            dialog.setOnClickListener(new PermissionDialogMI.onClickListener() {
                @Override
                public void onClick() {
                    AppUtils.autoStartMi(DefinePasswordActivity.this);
                }
            });
        }else {
            if (Build.MANUFACTURER.equalsIgnoreCase("vivo")){
                PermissionDialogVivo dialog = new PermissionDialogVivo(DefinePasswordActivity.this);
                dialog.show();
                dialog.setOnClickListener(new PermissionDialogVivo.onClickListener() {
                    @Override
                    public void onClick() {
                    }
                });
            }
            AppUtils.autoStart(DefinePasswordActivity.this);
        }
    }

    @Override
    protected void initData() {
        mGestureCreatePresenter = new GestureCreatePresenter(this, this);
        initLockPatternView();
    }

    private void initLockPatternView() {
        mLockPatternUtils = new LockPatternUtils(this);
        mPatternViewPattern = new LockPatternViewPattern(mLockPatternView);
        mPatternViewPattern.setPatternListener(new LockPatternViewPattern.onPatternListener() {
            @Override
            public void onPatternDetected(@NonNull List<LockPatternView.Cell> pattern) {
                mGestureCreatePresenter.onPatternDetected(pattern, mChosenPattern, mUiStage);
            }
        });
        mLockPatternView.setOnPatternListener(mPatternViewPattern);
        mLockPatternView.setTactileFeedbackEnabled(true);
    }

    @Override
    protected void initAction() {
        mBtnReset.setOnClickListener(this);
    }

    @Override
    public void onClick(@NonNull View view) {
        switch (view.getId()) {
            case R.id.btn_reset:
                setStepOne();
                break;
        }
    }

    private void setStepOne() {
        mGestureCreatePresenter.updateStage(LockStage.Introduction);
        mLockTip.setText(getString(R.string.lock_recording_intro_header));
    }

    private void gotoLockMainActivity() {
        SharedPreferences sharedPreference =  getSharedPreferences("login", Context.MODE_PRIVATE);
        boolean loginstatus = sharedPreference.getBoolean("loginstatus",false);
        MainUtil.getInstance().putBoolean(AppConstants.LOCK_STATE, true);
//        BackgroundManager.getInstance().init(this).startService(LockService.class);
        MainUtil.getInstance().putBoolean(AppConstants.LOCK_IS_FIRST_LOCK, false);
        if (loginstatus) {
            startActivity(new Intent(this, AppPermission.class));

        } else {
            startActivity(new Intent(this, AppPermission.class));
        }
        finish();
    }

    @Override
    public void updateUiStage(LockStage stage) {
        mUiStage = stage;
    }

    @Override
    public void updateChosenPattern(List<LockPatternView.Cell> mChosenPattern) {
        this.mChosenPattern = mChosenPattern;
    }

    @Override
    public void updateLockTip(String text, boolean isToast) {
        mLockTip.setText(text);
    }

    @Override
    public void setHeaderMessage(int headerMessage) {
        mLockTip.setText(headerMessage);
    }

    @Override
    public void lockPatternViewConfiguration(boolean patternEnabled, LockPatternView.DisplayMode displayMode) {
        if (patternEnabled) {
            mLockPatternView.enableInput();
        } else {
            mLockPatternView.disableInput();
        }
        mLockPatternView.setDisplayMode(displayMode);
    }

    @Override
    public void Introduction() {
        clearPattern();
    }

    @Override
    public void HelpScreen() {

    }

    @Override
    public void ChoiceTooShort() {
        mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
        mLockPatternView.removeCallbacks(mClearPatternRunnable);
        mLockPatternView.postDelayed(mClearPatternRunnable, 500);
    }

    @Override
    public void moveToStatusTwo() {

    }


    @Override
    public void clearPattern() {
        mLockPatternView.clearPattern();
    }


    @Override
    public void ConfirmWrong() {
        mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
        mLockPatternView.removeCallbacks(mClearPatternRunnable);
        mLockPatternView.postDelayed(mClearPatternRunnable, 500);
    }


    @Override
    public void ChoiceConfirmed() {
        mLockPatternUtils.saveLockPattern(mChosenPattern);
        clearPattern();
        gotoLockMainActivity();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGestureCreatePresenter.onDestroy();
    }
}
