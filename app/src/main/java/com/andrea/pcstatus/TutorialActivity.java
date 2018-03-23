package com.andrea.pcstatus;

import android.*;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Created by andre on 23/03/2018.
 *
 */

public class TutorialActivity extends AppIntro {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            getSupportActionBar().hide();
        } catch (NullPointerException ignored) {
        }

        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.tutorial),
                getResources().getString(R.string.pc_status_about),
                R.drawable.logo_small,
                Color.parseColor("#5599ff")));

        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.first_tab_title),
                getResources().getString(R.string.first_tab_desc),
                R.drawable.download_icon,
                Color.parseColor("#5599ff")));

        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.second_tab_title),
                getResources().getString(R.string.second_tab_desc),
                R.drawable.wifi_icon,
                Color.parseColor("#5599ff")));

        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.third_tab_title),
                getResources().getString(R.string.third_tab_desc),
                R.drawable.bluetooth_icon,
                Color.parseColor("#5599ff")));

        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.forth_tab_title),
                getResources().getString(R.string.forth_tab_desc),
                R.drawable.camera_icon,
                Color.parseColor("#5599ff")));

        askForPermissions(new String[]{android.Manifest.permission.CAMERA}, 5);

        setSwipeLock(false);
        setSeparatorColor(Color.WHITE);
        showStatusBar(false);
        showSkipButton(true);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        finish();
    }
}
