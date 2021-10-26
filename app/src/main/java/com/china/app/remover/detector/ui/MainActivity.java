package com.china.app.remover.detector.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.china.app.remover.detector.R;
import com.china.app.remover.detector.adapter.ItemlistAdapter;
import com.china.app.remover.detector.databinding.ActivityMainBinding;
import com.china.app.remover.detector.listner.OnItemClickListener;
import com.china.app.remover.detector.model.AppInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnItemClickListener {
    private ActivityMainBinding binding;
    private final List<String> myAppsList = new ArrayList<>();
    private final List<AppInfo> appInfos = new ArrayList();
    private ItemlistAdapter itemlistAdapter;
    private InterstitialAd mInterstitialAd;


    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setClickListner();
        loadAds();

        itemlistAdapter = new ItemlistAdapter(this, appInfos, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(itemlistAdapter);
        getListItems();
    }


    @Override
    public void onPause() {
        pauseAdview();
        super.onPause();
    }
    
    void pauseAdview(){
        if (adView != null) {
            adView.pause();
        }
    }

    @Override
    public void onDestroy() {
       destroyAdview();
        super.onDestroy();
    }

    void destroyAdview(){
          if (adView != null) {
            adView.destroy();
        }
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
                isDeleteClick = true;
                uninstallIntent(selectedPos);
                // Code to be executed when the interstitial ad is closed.
            }
        });

    }

    private void uninstallIntent(int pos) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= 14) {
            intent = new Intent("android.intent.action.UNINSTALL_PACKAGE");
        } else {
            intent = new Intent("android.intent.action.DELETE");
        }
        intent.setData(Uri.fromParts("package", appInfos.get(pos).packageName, (String) null));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void setClickListner() {
        binding.ivMenu.setOnClickListener(this);
        binding.btnShare.setOnClickListener(this);
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

    private void rateus() {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + getPackageName()));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException unused) {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    private void getListItems() {
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        DocumentReference questionsRef1 = rootRef.collection("appList").document("apps");
        questionsRef1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isComplete()) {
                    myAppsList.addAll((Collection<? extends String>) task.getResult().getData().get("chinaAppsList"));
                    getApps();
                }
            }
        });
    }

    private AdView adView;

    private void loadBanner() {
        adView = new AdView(this);
        adView.setAdUnitId(getResources().getString(R.string.banner_id));
        binding.adViewContainer.addView(adView);


        AdRequest adRequest =
                new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                        .build();

        AdSize adSize = getAdSize();
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);

        // Step 5 - Start loading the ad in the background.
        adView.loadAd(adRequest);
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

    private boolean isDeleteClick = false;

    public void onResume() {
        super.onResume();
        if (isDeleteClick) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    getApps();
                }
            }, 1500);
        }
        if (adView != null) {
            adView.resume();
        }

        isDeleteClick = false;
    }

    private int selectedPos;

    public void onItemClick(int position) {
        selectedPos = position;
        isDeleteClick = true;

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            uninstallIntent(selectedPos);
        }
    }

    private boolean isSystemPackage(PackageInfo packageInfo) {
        return (packageInfo.applicationInfo.flags & 1) != 0;
    }

    private ArrayList getInstalledApps() {
        ArrayList arrayList = new ArrayList();

Log.e("myappslistsize",""+myAppsList.size());
        List<PackageInfo> installedPackages = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < installedPackages.size(); i++) {
            PackageInfo packageInfo = installedPackages.get(i);
            if (!isSystemPackage(packageInfo)) {
                if (myAppsList.contains(packageInfo.packageName)) {
                    AppInfo appInfo = new AppInfo();
                    appInfo.appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                    appInfo.packageName = packageInfo.packageName;
                    appInfo.versionName = packageInfo.versionName;
                    appInfo.versionCode = packageInfo.versionCode;
                    appInfo.icon = packageInfo.applicationInfo.loadIcon(getPackageManager());
                    appInfo.size = (new File(packageInfo.applicationInfo.publicSourceDir).length() / 1048576) + " MB";
                    arrayList.add(appInfo);
                }
            }
        }
        return arrayList;
    }


    class GetAppsAsync extends AsyncTask<Void, Void, List<AppInfo>> {
        GetAppsAsync() {
        }

        public void onPreExecute() {
            super.onPreExecute();
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        public List<AppInfo> doInBackground(Void... voidArr) {
            return getInstalledApps();
        }

        public void onPostExecute(List<AppInfo> list) {
            super.onPostExecute(list);
            appInfos.clear();
            appInfos.addAll(list);
            binding.progressBar.setVisibility(View.GONE);
            if (list.size() > 0) {
                binding.appFoundCount.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.conNoApps.setVisibility(View.GONE);
                binding.appFoundCount.setText(Html.fromHtml(getResources().getString(R.string.app_found_count, new Object[]{Integer.valueOf(list.size())})));
            } else {
                binding.appFoundCount.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.GONE);
                binding.conNoApps.setVisibility(View.VISIBLE);
            }
            itemlistAdapter.notifyDataSetChanged();
            //if (list.size() == 0) {
            Collections.sort(list, new Comparator<AppInfo>() {
                public int compare(AppInfo appInfo, AppInfo appInfo2) {
                    return appInfo.appName.compareToIgnoreCase(appInfo2.appName);
                }
            });
        }
    }

    private void getApps() {
        new GetAppsAsync().execute();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivMenu:
                showPopup(view);
                break;
            case R.id.btnShare:
                shareApp();
                break;
        }
    }
}
