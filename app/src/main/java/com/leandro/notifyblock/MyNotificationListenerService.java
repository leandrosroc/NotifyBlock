package com.leandro.notifyblock;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class MyNotificationListenerService extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!MainActivity.isServiceRunning) {
            return;
        }
        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;
        CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);

        String packageName = sbn.getPackageName();
        String appName = getAppNameFromPackage(packageName);

        String keywords = loadKeyword();
        if (keywords != null && !keywords.isEmpty()) {
            String[] keywordArray = keywords.split(";");
            boolean foundKeyword = false;
            if (text != null) {
                for (String keyword : keywordArray) {
                    String trimmedKeyword = keyword.trim();
                    if (!trimmedKeyword.isEmpty() && text.toString().contains(trimmedKeyword)) {
                        foundKeyword = true;
                        break;
                    }
                }
            }
            if (!foundKeyword && title != null) {
                for (String keyword : keywordArray) {
                    String trimmedKeyword = keyword.trim();
                    if (!trimmedKeyword.isEmpty() && title.toString().contains(trimmedKeyword)) {
                        foundKeyword = true;
                        break;
                    }
                }
            }
            if (foundKeyword) {
                cancelNotification(sbn.getKey());
                addNotificationToHistory(appName + " - " + title + " - " + text.toString());
                Intent intent = new Intent("UPDATE_NOTIFICATION_HISTORY");
                sendBroadcast(intent);
            }
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
        return sharedPreferences.getString(MainActivity.KEYWORDS_KEY, "");
    }
}