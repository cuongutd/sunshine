package com.sunshineapp.cuong.sunshine;

/**
 * A callback interface that all activities containing this fragment must
 * implement. This mechanism allows activities to be notified of item
 * selections.
 */

import android.net.Uri;

/**
 * Created by Cuong on 6/22/2015.
 */
public interface Callback {
    /**
     * DetailFragmentCallback for when an item has been selected.
     */
    public void onItemSelected(Uri dateUri);

}
