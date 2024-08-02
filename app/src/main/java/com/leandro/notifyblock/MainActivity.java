package com.leandro.notifyblock;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends ComponentActivity {

    private static final String CHANNEL_ID = "test_channel";
    public static Boolean isServiceRunning = true;
    private TextView permissionInfoTextView;
    private EditText keywordEditText;
    private TextView notificationHistoryTextView;
    private Button openSettingsButton;
    private NotificationHistoryDatabaseHelper dbHistoryHelper;
    private KeywordsSettingsDatabaseHelper dbSettingsHelper;
    private BroadcastReceiver updateReceiver;
    private Button toggleServiceButton;

    private static final int POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        keywordEditText = findViewById(R.id.et_keywords);
        notificationHistoryTextView = findViewById(R.id.tv_notification_history);
        permissionInfoTextView = findViewById(R.id.tv_permission_info);

        dbHistoryHelper = new NotificationHistoryDatabaseHelper(this);
        dbSettingsHelper = new KeywordsSettingsDatabaseHelper(this);

        updateReceiver = new NotificationUpdateReceiver(notificationHistoryTextView, dbHistoryHelper);
        IntentFilter filter = new IntentFilter("UPDATE_NOTIFICATION_HISTORY");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        }

        openSettingsButton = findViewById(R.id.btn_open_settings);

        loadKeyword();
        checkNotificationPermission();
        checkReadNotificationPermission();

        Button saveKeywordButton = findViewById(R.id.btn_save_keyword);
        saveKeywordButton.setOnClickListener(v -> saveKeyword());

        Button createNotificationButton = findViewById(R.id.btn_create_notification);
        createNotificationButton.setOnClickListener(v -> createTestNotification());

        openSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
            }
        });

        Button clearHistoryButton = findViewById(R.id.btn_clear_history);
        clearHistoryButton.setOnClickListener(v -> clearNotificationHistory());

        toggleServiceButton = findViewById(R.id.btn_toggle_service);
        toggleServiceButton.setOnClickListener(v -> toggleNotificationService());

        updateNotificationHistory();
        createPermanentNotification();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(updateReceiver);
    }

    private void toggleNotificationService() {
        if (isServiceRunning) {
            toggleServiceButton.setText("Ativar");
            removePermanentNotification();
        } else {
            toggleServiceButton.setText("Desativar");
            createPermanentNotification();
        }
    }

    private void createPermanentNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Service Notifications", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Serviço Ativo")
                    .setContentText("O serviço está ativo em segundo plano.")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();
        }

        notificationManager.notify(2, notification);
        isServiceRunning = true;
    }

    private void removePermanentNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(2);
        isServiceRunning = false;
    }

    private void saveKeyword() {
        String keyword = keywordEditText.getText().toString();
        if (keyword.isEmpty()) {
            showUnsuccessfulDialog();
            return;
        }

        String packageName = getPackageName();

        dbSettingsHelper.addOrUpdateKeyword(keyword, packageName);
        showSuccessDialog();
    }

    public void loadKeyword() {
        String packageName = getPackageName();

        Cursor cursor = dbSettingsHelper.getKeywordsByPackage(packageName);
        if (cursor != null) {
            try {
                int keywordColumnIndex = cursor.getColumnIndex("keyword");

                if (keywordColumnIndex == -1) {
                    Log.e("MainActivity", "Coluna 'keyword' não encontrada.");
                    return;
                }

                StringBuilder keywords = new StringBuilder();
                boolean isFirst = true;

                while (cursor.moveToNext()) {
                    String keyword = cursor.getString(keywordColumnIndex);
                    if (!isFirst) {
                        keywords.append(";");
                    }
                    keywords.append(keyword);
                    isFirst = false;
                }
                keywordEditText.setText(keywords.toString());
            } finally {
                cursor.close();
            }
        } else {
            Log.e("MainActivity", "Cursor retornado é null.");
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void checkReadNotificationPermission() {
        ContentResolver contentResolver = getContentResolver();
        String enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");

        boolean isServiceEnabled = enabledListeners != null && enabledListeners.contains(MyNotificationListenerService.class.getName());

        if (!isServiceEnabled) {
            showPermissionDialog();
        } else {
            if (permissionInfoTextView != null) {
                permissionInfoTextView.setVisibility(View.GONE);
            }
            openSettingsButton.setVisibility(View.GONE);
        }
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permissão Necessária")
                .setMessage("Para o funcionamento correto do aplicativo, você precisa permitir o acesso às notificações. Deseja ir para as configurações agora?")
                .setPositiveButton("Permitir", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sucesso")
                .setMessage("Palavras-chave salva com sucesso!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showUnsuccessfulDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Ops!")
                .setMessage("Por favor, inserir palavra-chave")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void createTestNotification() {
        String keyword = keywordEditText.getText().toString();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Test Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Notificação de testes")
                    .setContentText("Exemplo de notificação com a palavra: " + (keyword.isEmpty() ? "Palavra-chave" : keyword))
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
        }

        if (notification != null) {
            notificationManager.notify(1, notification);
        }
    }

    private void clearNotificationHistory() {
        dbHistoryHelper.clearAllNotifications();
        updateNotificationHistory();
    }

    private void updateNotificationHistory() {
        Intent intent = new Intent("UPDATE_NOTIFICATION_HISTORY");
        sendBroadcast(intent);
    }
}