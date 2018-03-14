package com.andrea.pcstatus.firebaseClasses;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import com.andrea.pcstatus.MainActivity;
import com.andrea.pcstatus.SingletonModel;
import com.andrea.pcstatus.firebaseClasses.util.IabHelper;
import com.android.vending.billing.IInAppBillingService;

import java.util.ArrayList;

import static com.andrea.pcstatus.firebaseClasses.util.IabHelper.BILLING_RESPONSE_RESULT_OK;

/**
 * Created by andre on 12/03/2018.
 *
 */

public class InAppBillingClass {
    private IInAppBillingService mService;
    private MainActivity mainActivity;
    private ServiceConnection mServiceConn;
    private String TAG = "InAppBillingClassTAG";
    private static String SKU = "premium_upgrade";

    public static final int REQUEST_PREMIUM = 1001;
    private IabHelper mHelper;

    public InAppBillingClass(MainActivity mainActivity){
        this.mainActivity = mainActivity;

        mHelper = new IabHelper(mainActivity, getBase64EncodedPublicKey());
        mHelper.startSetup(result -> {
            if (!result.isSuccess()) {
                // Oh no, there was a problem.
                Log.d(TAG, "Problem setting up In-app Billing: " + result);
            }else {
                Log.d(TAG, "tutto ok In-app Billing: " + result);

            }
        });

        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "non connesso billing");
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "connected billing");
                mService = IInAppBillingService.Stub.asInterface(service);
            }
        };

        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        mainActivity.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        ArrayList<String> skuList = new ArrayList<>();
        skuList.add(SKU);
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
    }

    public void unbindInAppBillingService(){
        if (mService != null) {
            mainActivity.unbindService(mServiceConn);
        }

        if (mHelper != null) try {
            mHelper.dispose();
            mHelper = null;
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    public void purchaseItem(){
        String sku = SKU;
        String deviceId = Settings.Secure.getString(mainActivity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        //final String[] deviceId = new String[1];
        //CountDownLatch latch = new CountDownLatch(1);
        try {
            Bundle buyIntentBundle = mService.getBuyIntent(3, mainActivity.getPackageName(),
                    sku, "inapp", deviceId);

            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

            if (pendingIntent != null) {
                mainActivity.startIntentSenderForResult(pendingIntent.getIntentSender(),
                        REQUEST_PREMIUM, new Intent(), 0, 0,
                        0);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    public void restorePurchase(){
        if (mService != null) {
            try {
                Bundle ownedItems = mService.getPurchases(3, mainActivity.getPackageName(), "inapp", null);
                Log.d(TAG, ownedItems.toString());
                int response = ownedItems.getInt("RESPONSE_CODE");
                if (response == BILLING_RESPONSE_RESULT_OK) {
                    ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    //ArrayList<String>  purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                    //ArrayList<String>  signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                    //String continuationToken = ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                    if (ownedSkus != null) {
                        for (int i = 0; i < ownedSkus.size(); ++i) {
                            //String purchaseData = purchaseDataList.get(i);
                            //String signature = signatureList.get(i);
                            String sku = ownedSkus.get(i);

                            Log.d(TAG, "stampo sku" + sku);
                            if (sku.equals(SKU)) {
                                SingletonModel.getInstance().setIsPremium(true);
                                restartApplication();
                            }

                            // do something with this purchase information
                            // e.g. display the updated list of products owned by user
                        }
                    }

                    // if continuationToken != null, call getPurchases again
                    // and pass in the token to retrieve more items
                }else {
                    Log.d(TAG, "stampo " + response);
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void restartApplication(){
        Intent i = mainActivity.getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( mainActivity.getBaseContext().getPackageName() );
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mainActivity.startActivity(i);
        }
    }
}
