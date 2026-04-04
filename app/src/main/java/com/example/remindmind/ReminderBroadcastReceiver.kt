package com.example.remindmind

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderBroadcastReceiver : BroadcastReceiver() {
    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            // Приобретаем WakeLock, чтобы устройство не уснуло во время воспроизведения звука
            try {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "ReminderBroadcast::WakeLock"
                ).apply {
                    acquire(10000) // 10 секунд
                }
            } catch (e: Exception) {
                android.util.Log.e("ReminderBroadcast", "Error acquiring wake lock: ${e.message}")
            }

            val text = intent?.getStringExtra("text") ?: "Reminder Text"
            val id = intent?.getIntExtra("id", 0) ?: 0
            val soundUriString = intent?.getStringExtra("sound_uri")
            val isVibrationEnabled = intent?.getBooleanExtra("vibration_enabled", true) ?: true

            android.util.Log.d("ReminderBroadcast", "Received sound URI: $soundUriString")

            // Воспроизводим звук через MediaPlayer
            try {
                val soundUri: Uri? = if (!soundUriString.isNullOrEmpty()) {
                    try {
                        Uri.parse(soundUriString)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }

                if (soundUri != null && soundUri.toString() != "content://settings/system/notification_sound") {
                    // Воспроизводим выбранный пользователем звук
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(context, soundUri)
                        prepare()
                        start()
                        setOnCompletionListener {
                            release()
                            mediaPlayer = null
                            // Освобождаем WakeLock после воспроизведения
                            wakeLock?.let {
                                if (it.isHeld) it.release()
                                wakeLock = null
                            }
                        }
                    }
                    android.util.Log.d("ReminderBroadcast", "Playing custom sound: $soundUri")
                } else {
                    // Если звук не выбран или стандартный, используем стандартный звук уведомления
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(context, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                        prepare()
                        start()
                        setOnCompletionListener {
                            release()
                            mediaPlayer = null
                            // Освобождаем WakeLock после воспроизведения
                            wakeLock?.let {
                                if (it.isHeld) it.release()
                                wakeLock = null
                            }
                        }
                    }
                    android.util.Log.d("ReminderBroadcast", "Playing default notification sound")
                }
            } catch (e: Exception) {
                android.util.Log.e("ReminderBroadcast", "Error playing sound: ${e.message}")
                // Если не удалось воспроизвести выбранный звук, пробуем стандартный
                try {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(context, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                        prepare()
                        start()
                        setOnCompletionListener {
                            release()
                            mediaPlayer = null
                            wakeLock?.let {
                                if (it.isHeld) it.release()
                                wakeLock = null
                            }
                        }
                    }
                } catch (e2: Exception) {
                    android.util.Log.e("ReminderBroadcast", "Error playing default sound: ${e2.message}")
                    wakeLock?.let {
                        if (it.isHeld) it.release()
                        wakeLock = null
                    }
                }
            }

            // Вибрация
            if (isVibrationEnabled) {
                try {
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        vibratorManager.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ReminderBroadcast", "Error vibrating: ${e.message}")
                }
            }

            // Показываем уведомление (без звука, так как мы уже воспроизвели его)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    android.util.Log.e("ReminderBroadcast", "Notification permission not granted")
                    return
                }
            }

            val notificationManager = NotificationManagerCompat.from(context)
            val builder = NotificationCompat.Builder(context, ReminderApplication.channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.resources.getString(R.string.new_task))
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setSound(null) // Отключаем звук уведомления, так как воспроизводим через MediaPlayer

            notificationManager.notify(id, builder.build())
            android.util.Log.d("ReminderBroadcast", "Notification sent")
        }
    }
}
/*package com.example.remindmind
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(context != null) {
            val text = intent?.getStringExtra("text") ?: "Reminder Text"
            val id = intent?.getIntExtra("id", 0)
            val notificationManager = NotificationManagerCompat.from(context)
            val builder = NotificationCompat.Builder(context, ReminderApplication.channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.resources.getString(R.string.new_task))
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            if(ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            notificationManager.notify(id!!, builder.build())
        }
    }
}*/