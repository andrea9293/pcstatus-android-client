package com.andrea.pcstatus.firebaseClasses;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.LinearLayout;

import com.andrea.pcstatus.MainActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Created by andre on 12/03/2018.
 */

// this class is not uses anymore
public class AdsClass /*implements RewardedVideoAdListener*/ {

    private InterstitialAd mInterstitialAd;
    private CountDownTimer countDownTimer;
    private String TAG = "AdsClassTAG";
    private MainActivity mainActivity;

    public AdsClass(MainActivity mainActivity, LinearLayout mainLayout) {
        this.mainActivity = mainActivity;
        initAds(mainActivity, mainLayout);
    }

    private void initAds(Context context, LinearLayout mainLayout) {
        //MobileAds.initialize(context, mobileAdsId);
        AdView mAdView = new AdView(context);
        mAdView.setAdSize(AdSize.BANNER);
        mAdView.setAdUnitId("ca-app-pub-3940256099942544/6300978111"); // test ad
        mainLayout.addView(mAdView);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                mAdView.loadAd(new AdRequest.Builder().build());
            }
        });

        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712"); // test ad
        mInterstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                createTimer(1);
            }

            @Override
            public void onAdFailedToLoad(int i) {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mAdView.loadAd(new AdRequest.Builder().build());
    }

    /**
     * create a timer to show ad in fullscreen
     *
     * @param time minutes for timer
     */
    private void createTimer(long time) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(time * 60000, 50) {
            @Override
            public void onTick(long millisUnitFinished) {
                //Log.d(TAG, "rimane " + millisUnitFinished);
            }

            @Override
            public void onFinish() {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    createTimer(1);
                    Log.d(TAG, "The interstitial wasn't loaded yet.");
                }
            }
        };
        countDownTimer.start();
    }
}
