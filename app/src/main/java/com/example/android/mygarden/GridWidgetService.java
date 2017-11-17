package com.example.android.mygarden;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by Edward on 11/17/2017.
 */

public class GridWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(this.getApplicationContext());
    }
}
