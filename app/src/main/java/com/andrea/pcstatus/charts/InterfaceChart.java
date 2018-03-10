package com.andrea.pcstatus.charts;

import android.view.View;

/**
 * Created by andre on 30/11/2017.
 */

public interface InterfaceChart {
    void addEntry();
    View getView();
    void animate();

    //todo eliminare le selezioni nei charts
}
