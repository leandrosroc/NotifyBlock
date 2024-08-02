package com.leandro.notifyblock;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.content.Intent;

public class MyNotificationListenerService extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if(!MainActivity.isServiceRunning) {
            return;
        }
        Notification notification = sbn.getNotification();
        CharSequence notificationText = notification.extras.getCharSequence(Notification.EXTRA_TEXT);

        String packageName = sbn.getPackageName();
        String appName = getAppNameFromPackage(packageName);

        String keyword = loadKeyword();
        if (notificationText != null && notificationText.toString().contains(keyword)) {
            cancelNotification(sbn.getKey());
            addNotificationToHistory(appName + " - " + notificationText.toString());

            Intent intent = new Intent("UPDATE_NOTIFICATION_HISTORY");
            sendBroadcast(intent);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        //TODO: manipula quando uma notificação é removida
    }

    private void addNotificationToHistory(String content) {
        NotificationHistoryDatabaseHelper dbHelper = new NotificationHistoryDatabaseHelper(getApplicationContext());
        dbHelper.addNotification(content);
    }

    private String getAppNameFromPackage(String packageName) {
        try {
            return getPackageManager().getApplicationLabel(
                    getPackageManager().getApplicationInfo(packageName, 0)).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return packageName;
        }
    }

    private String loadKeyword() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(MainActivity.KEYWORD_KEY, "");
    }
}