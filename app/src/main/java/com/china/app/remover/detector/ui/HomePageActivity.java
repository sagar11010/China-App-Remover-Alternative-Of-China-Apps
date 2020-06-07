package com.china.app.remover.detector.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.china.app.remover.detector.R;
import com.china.app.remover.detector.databinding.ActivityHomePageBinding;
import com.china.app.remover.detector.util.SharedPrefsUtils;
import com.china.app.remover.detector.util.Utility;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.Arrays;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityHomePageBinding homePageBinding;
    private InterstitialAd mInterstitialAd;
    int type = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homePageBinding = DataBindingUtil.setContentView(this, R.layout.activity_home_page);

        setClickLIstenr();
        loadAds();
    }

    AdView adView;

    private void loadBanner() {
        adView = new AdView(this);
        adView.setAdUnitId(getResources().getString(R.string.banner_id));
        homePageBinding.adViewContainer.addView(adView);


        AdRequest adRequest =
                new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                        .build();

        AdSize adSize = getAdSize();
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);

        // Step 5 - Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private void loadAds() {

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("6B5C82F31A2210A4617D2C10017BCB93"))
                        .build());
        loadBanner();
        loadIntertital();
    }

    private void loadIntertital() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.intrestial_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

            @Override

            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                if (type == 1) {
                    checkRemoverAndGoNext();
                } else {
                    checkUlterNativeAndGoNext();
                }
                // startActivitys(type == 1 ? MainActivity.class : AlternativeAppsActivity.class);
                // Code to be executed when the interstitial ad is closed.
            }
        });

    }

    void setClickLIstenr() {
        homePageBinding.ivMenu.setOnClickListener(this);
        homePageBinding.tvUlternativeApp.setOnClickListener(this);
        homePageBinding.tvUninstallApps.setOnClickListener(this);
    }

    private void rateus() {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + getPackageName()));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException unused) {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    private void shareApp() {
        try {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.putExtra("android.intent.extra.SUBJECT", getResources().getString(R.string.app_name));
            intent.putExtra("android.intent.extra.TEXT", "\nRemove china apps from your device and find alternative solution for this. If you want the same try using the app by clicking\n\n" + "https://play.google.com/store/apps/details?id=" + getPackageName() + "\n\n");
            startActivity(Intent.createChooser(intent, "Choose One"));
        } catch (Exception ignored) {
        }
    }

    private void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.main, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.share_us:
                        shareApp();
                        break;
                    case R.id.rate_us:
                        rateus();
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    public void showInternetAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(HomePageActivity.this).create();
        alertDialog.setTitle(getResources().getString(R.string.app_name));
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setMessage("You don't have internet connection. Kindly check and try again.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
        alertDialog.show();
    }


    void checkUlterNativeAndGoNext() {
        if (!Utility.isNetworkConnected(HomePageActivity.this)) {
            if (SharedPrefsUtils.getBooleanPreference(HomePageActivity.this, SharedPrefsUtils.APP_ALTERNATIVE, false)) {
                startActivitys(AlternativeAppsActivity.class);

            } else {
                showInternetAlert();
            }

        } else {
            SharedPrefsUtils.setBooleanPreference(HomePageActivity.this, SharedPrefsUtils.APP_ALTERNATIVE, true);
            startActivitys(AlternativeAppsActivity.class);

        }

    }

    void checkRemoverAndGoNext() {
        if (!Utility.isNetworkConnected(HomePageActivity.this)) {
            if (SharedPrefsUtils.getBooleanPreference(HomePageActivity.this, SharedPrefsUtils.APP_REMOVER, false)) {
                startActivitys(MainActivity.class);
            } else {
                showInternetAlert();
            }

        } else {
            SharedPrefsUtils.setBooleanPreference(HomePageActivity.this, SharedPrefsUtils.APP_REMOVER, true);
            startActivitys(MainActivity.class);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivMenu:
                showPopup(view);
                break;
            case R.id.tvUlternativeApp:
                if (mInterstitialAd.isLoaded()) {
                    type = 2;
                    mInterstitialAd.show();
                } else {
                    checkUlterNativeAndGoNext();
                }
                break;
            case R.id.tvUninstallApps:
                if (mInterstitialAd.isLoaded()) {
                    type = 1;
                    mInterstitialAd.show();
                } else {
                    checkRemoverAndGoNext();
                }
                break;
        }
    }

    void startActivitys(Class c) {
        Intent i = new Intent(HomePageActivity.this, c);
        startActivity(i);

    }
}
