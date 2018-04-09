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
import com.google.android.gms.ads.MobileAds;

/**
 * Created by andre on 12/03/2018.
 */

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
        MobileAds.initialize(context, mobileAdsId);
        AdView mAdView = new AdView(context);
        mAdView.setAdSize(AdSize.BANNER);
        //mAdView.setAdUnitId("ca-app-pub-3940256099942544/6300978111"); // test ad
        mAdView.setAdUnitId(bannerAdId);
        mainLayout.addView(mAdView);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                mAdView.loadAd(new AdRequest.Builder().build());
            }
        });

        mInterstitialAd = new InterstitialAd(context);
        //mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712"); // test ad
        mInterstitialAd.setAdUnitId(interstitialAdId);
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
        //rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);
        //rewardedVideoAd.setRewardedVideoAdListener(this);

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

    /*private void showVideoReward() {
        if (rewardedVideoAd.isLoaded()) {
            rewardedVideoAd.show();
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        hideDialog();
        showVideoReward();
    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {

    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        if (rewardItem.getType().equals("diskReward")) {
            mainActivity.setDiskReward(true);
        } else if (rewardItem.getType().equals("batteryReward")) {
            mainActivity.setBatteryReward(true);
        } else if (rewardItem.getType().equals("miscellaneousReward")) {
            mainActivity.setMiscellaneousReward(true);
        }
        Toast.makeText(mainActivity, "Thank you :)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        hideDialog();
        Toast.makeText(mainActivity, mainActivity.getString(R.string.error_loading_ad), Toast.LENGTH_SHORT).show();
        Log.e(TAG, "error: " + i);
    }

    private void createDialog() {
        AlertDialogManager.progressBarDialog(mainActivity.getString(R.string.loading_ads));
    }

    private void hideDialog() {
        AlertDialogManager.hideProgressBarDialog();
    }*/
   /*public void loadRewardedVideoAd(AdsRequest adsRequest) {
        if (adsRequest == REQUEST_BATTERY) {
            if (!rewardedVideoAd.isLoaded()) {
                rewardedVideoAd.loadAd(batteryReward, new AdRequest.Builder().build());
            }
        } else if (adsRequest == REQUEST_DISK) {
            if (!rewardedVideoAd.isLoaded()) {
                rewardedVideoAd.loadAd(diskReward, new AdRequest.Builder().build());
            }
        } else if (adsRequest == REQUEST_MISCELLANEOUS) {
            if (!rewardedVideoAd.isLoaded()) {
                rewardedVideoAd.loadAd(miscellaneousReward, new AdRequest.Builder().build());
            }
        }
        createDialog();
    }*/
}
