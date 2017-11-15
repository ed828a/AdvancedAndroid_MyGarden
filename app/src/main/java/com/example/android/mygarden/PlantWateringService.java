package com.example.android.mygarden;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

import static com.example.android.mygarden.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.mygarden.provider.PlantContract.INVALID_PLANT_ID;
import static com.example.android.mygarden.provider.PlantContract.PATH_PLANTS;
import static com.example.android.mygarden.ui.PlantDetailActivity.EXTRA_PLANT_ID;
import static com.example.android.mygarden.utils.PlantUtils.MIN_AGE_BETWEEN_WATER;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PlantWateringService extends IntentService {
    private static final String LOG_TAG = PlantWateringService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_WATER_PLANTS =
            "com.example.android.mygarden.action.water_plants";
    public static final String ACTION_WATER_PLANT =
            "com.example.android.mygarden.action.water_plant";
    public static final String ACTION_UPDATE_PLANT_WIDGETS =
            "com.example.android.mygarden.action.update_plant_widgets";

    public PlantWateringService() {
        super("PlantWateringService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionWaterPlants(Context context, long plantId) {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANT);
        intent.putExtra(EXTRA_PLANT_ID, plantId);
        context.startService(intent);
    }

    public static void startActionUpdatePlantWidgets(Context context) {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_UPDATE_PLANT_WIDGETS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.i(LOG_TAG, "onHandleIntent called, action: " + action);

            if (ACTION_WATER_PLANT.equals(action)) {
                long plantId = intent.getLongExtra(EXTRA_PLANT_ID, INVALID_PLANT_ID);
                handleActionWaterPlant(plantId);
            } else if (ACTION_UPDATE_PLANT_WIDGETS.equals(action)){
                handleActionUpdatePlantWidgets();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionWaterPlant(long plantId) {
//        Uri PLANTS_URI = BASE_CONTENT_URI.buildUpon()
//                .appendPath(PATH_PLANTS).build();
        Uri SINGLE_PLANT_URI = ContentUris.withAppendedId(
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build(), plantId);
        ContentValues contentValues = new ContentValues();
        long timeNow = System.currentTimeMillis();
        long latestWateringTime = timeNow - PlantUtils.MAX_AGE_WITHOUT_WATER;
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, timeNow);
        // update only plants that still alive
        getContentResolver().update(
                SINGLE_PLANT_URI,
                contentValues,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME + ">?",
                new String[]{String.valueOf(latestWateringTime)});

    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdatePlantWidgets() {
        Uri PLANTS_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        Cursor cursor = getContentResolver().query(
                PLANTS_URI,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME
        );

        // extract the plant details
        int imgRes = R.drawable.grass; // default image in case our garden is empty
        long plantId = INVALID_PLANT_ID;
        boolean isAllowWatering = true;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int createTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
            int waterTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
            int plantTypeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);
            int plantIdIndex = cursor.getColumnIndex(PlantContract.PlantEntry._ID);
            long timeNow = System.currentTimeMillis();
            long wateredAt = cursor.getLong(waterTimeIndex);
            long createdAt = cursor.getLong(createTimeIndex);
            int plantType = cursor.getInt(plantTypeIndex);
            plantId = cursor.getLong(plantIdIndex);
            if (timeNow-wateredAt >= MIN_AGE_BETWEEN_WATER){
                isAllowWatering = true;
            } else {
                isAllowWatering = false;
            }
            cursor.close();
            imgRes = PlantUtils.getPlantImageRes(this,
                    timeNow - createdAt, timeNow - wateredAt, plantType);
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(this, PlantWidgetProvider.class));
        // now update all widgets
        PlantWidgetProvider.updatePlantWidgets(this,
                appWidgetManager, imgRes, appWidgetIds, plantId, isAllowWatering);

    }

}
