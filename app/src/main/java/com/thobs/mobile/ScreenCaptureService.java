package com.thobs.mobile;

import android.app.*;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.content.pm.ServiceInfo;

import com.pedro.common.ConnectChecker;
import com.pedro.library.rtmp.RtmpDisplay;

public class ScreenCaptureService extends Service implements ConnectChecker {
    private static final String CHANNEL = "thobs_capture";
    private static final String TAG = "THOBS";
    private RtmpDisplay rtmpDisplay;

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
        Notification notification = new Notification.Builder(this, CHANNEL)
            .setContentTitle("TH OBS Studio Mobile")
            .setContentText("Ekran yayını hazırlanıyor")
            .setSmallIcon(android.R.drawable.presence_video_online)
            .setOngoing(true)
            .build();
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION | ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        } else {
            startForeground(1, notification);
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL, "TH OBS canlı yayın", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        int resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED);
        Intent projectionData = intent.getParcelableExtra("data");
        String endpoint = intent.getStringExtra("endpoint");
        int quality = intent.getIntExtra("quality", 0);

        try {
            if (projectionData == null || endpoint == null) {
                throw new IllegalArgumentException("Ekran izni veya RTMP adresi eksik.");
            }
            rtmpDisplay = new RtmpDisplay(this, true, this);
            rtmpDisplay.setIntentResult(resultCode, projectionData);
            rtmpDisplay.getGlInterface().setForceRender(true);

            // Baldi oyunları genellikle yatay oynandığı için 16:9 çözünürlük kullan.
            int width = quality == 1 ? 854 : 1280;
            int height = quality == 1 ? 480 : 720;
            int videoBitrate = quality == 1 ? 1200 * 1000 : 2500 * 1000;

            // RootEncoder 2.8.1 sırası: width, height, bitrate, fps.
            boolean videoReady = rtmpDisplay.prepareVideo(width, height, videoBitrate, 30);
            boolean audioReady = rtmpDisplay.prepareAudio(96 * 1000, 44100, false);
            if (!videoReady || !audioReady) {
                throw new IllegalStateException("Telefonun kodlayıcısı video veya sesi hazırlayamadı.");
            }
            rtmpDisplay.startStream(endpoint);
            Log.i(TAG, "RTMP stream start requested");
        } catch (Exception e) {
            Log.e(TAG, "Stream start failed", e);
            stopStream();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void stopStream() {
        if (rtmpDisplay != null) {
            try {
                if (rtmpDisplay.isStreaming()) rtmpDisplay.stopStream();
                rtmpDisplay.release();
            } catch (Exception e) {
                Log.w(TAG, "Error while releasing stream", e);
            }
            rtmpDisplay = null;
        }
        stopForeground(true);
    }

    @Override public void onConnectionStarted(String url) {
        Log.i(TAG, "Connecting to stream server");
    }
    @Override public void onConnectionSuccess() {
        Log.i(TAG, "RTMP connected");
    }
    @Override public void onNewBitrate(long bitrate) {
        Log.i(TAG, "Upload bitrate: " + bitrate);
    }
    @Override public void onConnectionFailed(String reason) {
        Log.e(TAG, "RTMP connection failed: " + reason);
        stopStream();
        stopSelf();
    }
    @Override public void onDisconnect() {
        Log.i(TAG, "RTMP disconnected");
    }
    @Override public void onAuthError() {
        Log.e(TAG, "RTMP authentication error");
        stopStream();
        stopSelf();
    }
    @Override public void onAuthSuccess() {
        Log.i(TAG, "RTMP authentication accepted");
    }

    @Override public void onDestroy() {
        stopStream();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
