package com.example.android.mygarden;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.mygarden.ui.MainActivity;
import com.example.android.mygarden.ui.PlantDetailActivity;

import static com.example.android.mygarden.provider.PlantContract.INVALID_PLANT_ID;
import static com.example.android.mygarden.ui.PlantDetailActivity.EXTRA_PLANT_ID;

/**
 * Implementation of App Widget functionality.
 */
public class PlantWidgetProvider extends AppWidgetProvider {
    public static final int PLANT_REQUEST_CODE = 0;
    public static final int WATER_REQUEST_CODE = 1;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int imgRes, int appWidgetId, long plantId, boolean allowWatering) {


        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget);
        views.setImageViewResource(R.id.widget_plant_image, imgRes); // update image
        views.setTextViewText(R.id.plant_Id_text, Long.toString(plantId));

        Intent intent = null;
        if (plantId == INVALID_PLANT_ID) {
            // Create an Intent to launch MainActivity when clicked.
            intent = new Intent(context, MainActivity.class);
        } else {
            intent = new Intent(context, PlantDetailActivity.class);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, PLANT_REQUEST_CODE, intent, 0);

        // Widgets allow click handlers to only launch pending intents
        views.setOnClickPendingIntent(R.id.widget_plant_image, pendingIntent);

        if (allowWatering) {
            views.setViewVisibility(R.id.widget_water_button, View.VISIBLE);
            Intent startWateringIntent = new Intent(context, PlantWateringService.class);
            startWateringIntent.setAction(PlantWateringService.ACTION_WATER_PLANT);
            startWateringIntent.putExtra(EXTRA_PLANT_ID, plantId);
            PendingIntent wateringPendingIntent = PendingIntent.getService(context,
                    WATER_REQUEST_CODE, startWateringIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.widget_water_button, wateringPendingIntent);
        } else {
            views.setViewVisibility(R.id.widget_water_button, View.INVISIBLE);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget);
//        String plantIdString = views.get
        // start the intent service update widget action, the service takes care of updating the widgets UI
        PlantWateringService.startActionUpdatePlantWidgets(context);
//        for (int appWidgetId : appWidgetIds){
//            updateAppWidget(context,appWidgetManager, R.drawable.grass, appWidgetId);
//        }
    }

    public static void updatePlantWidgets(Context context, AppWidgetManager appWidgetManager,
                                          int imgRes, int[] appWidgetIds,
                                          long plantId, boolean isAllowWatering){
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds){
            updateAppWidget(context,appWidgetManager, imgRes, appWidgetId,
                    plantId, isAllowWatering);
        }
    }
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Perform any action when one or more Appwidget instances have been deleted
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

