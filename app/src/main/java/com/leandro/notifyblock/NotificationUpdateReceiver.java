package com.leandro.notifyblock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.TextView;

public class NotificationUpdateReceiver extends BroadcastReceiver {

    private final TextView notificationHistoryTextView;
    private final NotificationHistoryDatabaseHelper dbHelper;

    public NotificationUpdateReceiver(TextView notificationHistoryTextView, NotificationHistoryDatabaseHelper dbHelper) {
        this.notificationHistoryTextView = notificationHistoryTextView;
        this.dbHelper = dbHelper;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        updateNotificationHistory();
    }

    private void updateNotificationHistory() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT content FROM notifications", null);
        StringBuilder history = new StringBuilder();

        while (cursor.moveToNext()) {
            String content = cursor.getString(0);
            history.append(content).append("\n");
        }

        cursor.close();
        notificationHistoryTextView.setText(history.toString());
    }
}