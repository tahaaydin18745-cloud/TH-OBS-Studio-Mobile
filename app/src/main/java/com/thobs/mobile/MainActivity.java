package com.thobs.mobile;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import android.graphics.Color;
import android.view.Gravity;

public class MainActivity extends Activity {
    private static final int REQUEST_CAPTURE = 7001;
    private static final int REQUEST_MICROPHONE = 7002;
    private TextView status;
    private EditText streamUrl;
    private Spinner quality;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(22, 28, 22, 22);
        root.setBackgroundColor(Color.rgb(16,20,24));

        TextView title = new TextView(this);
        title.setText("TH OBS  /  MOBILE");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(0, 0, 0, 18);
        root.addView(title);

        TextView preview = new TextView(this);
        preview.setText("CANLI YAYIN ÖNİZLEMESİ\n\nEkran yakalama izni verildiğinde yayın motoru başlatılır.");
        preview.setTextColor(Color.LTGRAY);
        preview.setTextSize(15);
        preview.setGravity(Gravity.CENTER);
        preview.setBackgroundColor(Color.rgb(35,43,50));
        root.addView(preview, new LinearLayout.LayoutParams(-1, 170));

        TextView label = new TextView(this);
        label.setText("YAYIN ADRESİ");
        label.setTextColor(Color.WHITE);
        label.setPadding(0, 18, 0, 5);
        root.addView(label);

        streamUrl = new EditText(this);
        streamUrl.setSingleLine(true);
        streamUrl.setTextColor(Color.WHITE);
        streamUrl.setHintTextColor(Color.GRAY);
        streamUrl.setHint("rtmps://.../live2/YAYIN_ANAHTARI");
        streamUrl.setText("rtmps://a.rtmps.youtube.com/live2/");
        root.addView(streamUrl);

        TextView qualityLabel = new TextView(this);
        qualityLabel.setText("YAYIN KALİTESİ");
        qualityLabel.setTextColor(Color.WHITE);
        qualityLabel.setPadding(0, 12, 0, 5);
        root.addView(qualityLabel);

        quality = new Spinner(this);
        String[] qualities = {"720p • 30 FPS • 2.5 Mbps", "480p • 30 FPS • 1.2 Mbps"};
        quality.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, qualities));
        root.addView(quality);

        Button start = new Button(this);
        start.setText("EKRAN YAYININI BAŞLAT");
        root.addView(start);
        start.setOnClickListener(v -> requestCapture());

        Button stop = new Button(this);
        stop.setText("YAYINI DURDUR");
        root.addView(stop);
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, ScreenCaptureService.class));
            setStatus("Yayın durduruldu.");
        });

        status = new TextView(this);
        status.setTextColor(Color.rgb(54,201,143));
        status.setText("Hazır • Yayın henüz başlamadı");
        status.setPadding(0, 18, 0, 0);
        root.addView(status);

        TextView note = new TextView(this);
        note.setTextColor(Color.GRAY);
        note.setTextSize(12);
        note.setPadding(0, 16, 0, 0);
        note.setText("0.3.0 • RTMP ekran yayını prototipi. Mikrofon izni istenir. Yayın anahtarını gizli tut; uygulama anahtarı kaydetmez.");
        root.addView(note);

        setContentView(root);
    }

    private void requestCapture() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
            return;
        }
        beginCaptureRequest();
    }

    private void beginCaptureRequest() {
        String endpoint = streamUrl.getText().toString().trim();
        if (!(endpoint.startsWith("rtmp://") || endpoint.startsWith("rtmps://")) ||
                endpoint.endsWith("/live2/") || endpoint.length() < 25) {
            setStatus("Geçerli YouTube RTMP adresini ve yayın anahtarını gir.");
            return;
        }
        MediaProjectionManager manager =
            (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(manager.createScreenCaptureIntent(), REQUEST_CAPTURE);
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MICROPHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                beginCaptureRequest();
            } else {
                setStatus("Canlı yayın için mikrofon izni gerekli.");
            }
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAPTURE) {
            if (resultCode == RESULT_OK && data != null) {
                Intent service = new Intent(this, ScreenCaptureService.class);
                service.putExtra("resultCode", resultCode);
                service.putExtra("data", data);
                service.putExtra("endpoint", streamUrl.getText().toString().trim());
                service.putExtra("quality", quality.getSelectedItemPosition());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(service);
                else startService(service);
                setStatus("Ekran izni alındı • RTMP bağlantısı başlatılıyor…");
            } else {
                setStatus("Ekran yakalama izni verilmedi.");
            }
        }
    }

    private void setStatus(String value) { status.setText(value); }
}
