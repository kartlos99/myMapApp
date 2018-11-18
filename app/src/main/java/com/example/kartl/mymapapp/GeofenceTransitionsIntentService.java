package com.example.kartl.mymapapp;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {
    // ...
    String TAG = "IntentServiseGeoFence";
    Notification.Builder notifBuilder;
    public static final int NOTIF_ID = 2345;
    NotificationManager notificationManager;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    public GeofenceTransitionsIntentService() {
        super("isthisdef");

        Log.d(TAG, "IntentServise amoqmedda!");

    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
//            String errorMessage = GeofenceErrorMessages.getErrorString(this,
//                    geofencingEvent.getErrorCode());
            Log.e(TAG, " shecdoma geofence intentis migebaze");
            return;
        }
        Context context = getApplicationContext();

        notifBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_gps)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentText("An iqit An aqet!")
                .setContentTitle("Transition Detected");

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // OREO an unro axali
            String chanelID = "1";
            String chanelName = "chanel_1";

            NotificationChannel channel = new NotificationChannel(chanelID, chanelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setShowBadge(true);
            channel.enableVibration(true);

            notifBuilder.setChannelId(chanelID);

            if(notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }

        }else {
            // Nuga an ufro dabali
        }

        Intent nIntent = new Intent(context, MapActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntent(nIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notifBuilder.setContentIntent(pendingIntent);

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

//            Toast.makeText(getApplicationContext(), "Enter fance", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "shevida!");
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
//            String geofenceTransitionDetails = getGeofenceTransitionDetails(
//                    this,
//                    geofenceTransition,
//                    triggeringGeofences
//            );

            // Send notification and log the transition details.
//            sendNotification(geofenceTransitionDetails);
//            Log.i(TAG, geofenceTransitionDetails);
            notifBuilder.setContentText("Shesvla dafiqsirda");
            notificationManager.notify(NOTIF_ID, notifBuilder.build());
        } else {
            // Log the error.
//            Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
//                    geofenceTransition));

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
//                Toast.makeText(getApplicationContext(), "Exit fance", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "gamovida!");
                notifBuilder.setContentText("Gasvla dafiqsirda");
                notificationManager.notify(NOTIF_ID, notifBuilder.build());
            }
        }
    }
}