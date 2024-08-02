package com.leandro.notifyblock;

import android.app.Notification;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class MyNotificationListenerService extends NotificationListenerService {

    private KeywordsSettingsDatabaseHelper dbSettingsHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        dbSettingsHelper = new KeywordsSettingsDatabaseHelper(this);
    }

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

        String keywords = loadKeyword(null);
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

    private String loadKeyword(String packageName) {
        Cursor cursor;
        if (packageName == null) {
            cursor = dbSettingsHelper.getAllKeywords();
        } else {
            cursor = dbSettingsHelper.getKeywordsByPackage(packageName);
        }

        StringBuilder keywords = new StringBuilder();
        try {
            int keywordColumnIndex = cursor.getColumnIndex("keyword");
            if (keywordColumnIndex == -1) {
                Log.e("MyNotificationListenerService", "Coluna 'keyword' não encontrada.");
                return "";
            }

            boolean isFirst = true;
            while (cursor.moveToNext()) {
                String keyword = cursor.getString(keywordColumnIndex);
                if (!isFirst) {
                    keywords.append(";");
                }
                keywords.append(keyword);
                isFirst = false;
            }
        } catch (Exception e) {
            Log.e("MyNotificationListenerService", "Erro ao carregar palavras-chave", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return keywords.toString();
    }
}