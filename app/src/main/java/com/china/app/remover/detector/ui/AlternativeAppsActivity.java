package com.china.app.remover.detector.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.china.app.remover.detector.adapter.AlterNativeAdapter;
import com.china.app.remover.detector.adapter.ItemlistAdapter;
import com.china.app.remover.detector.model.ModelAlternative;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.china.app.remover.detector.R;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AlternativeAppsActivity extends AppCompatActivity {
    List<ModelAlternative> alternativeList = new ArrayList<>();
    AlterNativeAdapter alterNativeAdapter;
    ProgressBar pb;
    FrameLayout adViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alternative_apps);
        setTitle();
        initData();l̥
    }
    
    void setTitle(){
        getSupportActionBar().setTitle("Alternative Apps");
    
    }
    
    void initData(){
        adViewContainer = findViewById(R.id.ad_view_container);
        loadAds();
        pb = findViewById(R.id.progressBar);
        loadRecyclerViewData();
        pb.setVisibility(View.VISIBLE);
        getListItems();
    
    }

     void loadRecyclerViewData(){
           RecyclerView recyclerView = findViewById(R.id.recycler_view);
     
        alterNativeAdapter = new AlterNativeAdapter(this, alternativeList);
     
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(alterNativeAdapter);
     
     }
    AdView adView;

    private void loadBanner() {
        adView = new AdView(this);
        adView.setAdUnitId(getResources().getString(R.string.banner_id));
        adViewContainer.addView(adView);


        AdRequest adRequest =
                new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                        .build();

        AdSize adSize = getAdSize();
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);

        // Step 5 - Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    /**
     * Called when leaving the activity
     */
    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    /**
     * Called when returning to the activity
     */
    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    /**
     * Called before the activity is destroyed
     */
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

    
    //this function is use for load admob ads
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
    }



    //this function receive all china app list and alternative of that app lists.
    private void getListItems() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        rootRef.collection("appList").document("alternativeAppsList").collection("allAlternativeApps").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {


                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                    Log.d("alldata", document.getId() + " => " + document.getData().get("chinaAppName"));
                    ModelAlternative m = new ModelAlternative();
                    m.setTitle(true);
                    m.setName(document.getData().get("chinaAppName").toString());
                    alternativeList.add(m);
                    List<Map<String, String>> l = (List<Map<String, String>>) document.getData().get("alternativeApps");
                    for (int i = 0; i < l.size(); i++) {
                        ModelAlternative model = new ModelAlternative();
                        model.setName(l.get(i).get("name"));
                        model.setUrl(l.get(i).get("url"));
                        model.setTitle(false);
                        alternativeList.add(model);
                    }
                }
                pb.setVisibility(View.GONE);
                alterNativeAdapter.notifyDataSetChanged();
            }
        });

    }

}
