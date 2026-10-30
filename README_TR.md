# TH OBS Studio Mobile — 0.3.0 (Android Studio kaynak projesi)

Bu bir **kaynak proje ZIP dosyasıdır; APK değildir**. RootEncoder 2.8.1 ile Android ekranını RTMP/RTMPS sunucusuna göndermeyi hedefleyen başlangıç uygulamasıdır.

## 0.3.0 düzeltmeleri
- RootEncoder 2.8.1 `prepareVideo(width, height, bitrate, fps)` parametre sırası düzeltildi.
- Baldi gibi yatay oyunlar için 16:9 çözünürlük profilleri: 1280x720 / 854x480.
- Mikrofon izni çalışma anında istenir.
- Android 8 ve üzeri için foreground service başlatma yolu ayrıldı.
- Mikrofon ve ekran yakalama foreground service türleri bildirildi.

## Özellikler / sınırlar
- RTMP/RTMPS yayın adresi alanı (sunucu adresi + yayın anahtarı tek URL olarak girilir).
- 720p/30 FPS/2.5 Mbps ve 480p/30 FPS/1.2 Mbps profilleri.
- Android MediaProjection ekran yakalama izni.
- Mikrofon sesi. Android sürümü/cihaz desteğine göre oyun içi dahili ses yakalama ayrıca doğrulanmalıdır; bu sürümde garanti edilmez.
- Çoklu sahne, kamera katmanı, gelişmiş ses mikseri ve OBS masaüstü özellikleri yoktur.

## APK oluşturma
1. ZIP'i bilgisayarda çıkar ve klasörü Android Studio ile aç.
2. Gradle senkronizasyonunun tamamlanmasını bekle (internet gerekir; Android Gradle Plugin ve RootEncoder indirilir).
3. Build > Build Bundle(s) / APK(s) > Build APK(s) seç.
4. Android Studio'nun bildirdiği `app-debug.apk` dosyasını Android 11 telefona aktar.
5. Önce 480p profiliyle, kısa ve herkese açık olmayan bir test yayını yap.

## Önemli doğrulama notu
Bu ortamda Android SDK, Gradle ve bağlı Android cihaz bulunmadığından APK derlemesi ve gerçek cihaz testi yapılamadı. 0.3.0'da kaynak kodundaki tespit edilen video parametre sırası hatası düzeltildi; buna rağmen projeyi derlenmiş/çalışan APK olarak tanımlamıyorum. Android Studio derlemesi ve Reeder S19 Max Pro üzerinde test gereklidir.

## Kütüphane
RootEncoder 2.8.1: https://github.com/pedroSG94/RootEncoder


## Bilgisayarsız bulut derlemesi (GitHub Actions)

Projede `.github/workflows/android.yml` dosyası bulunur. Projeyi kendi GitHub hesabında yeni bir depoya yükledikten sonra:
1. Depoda **Actions** sekmesini aç.
2. **Build Android APK** iş akışını seç.
3. **Run workflow** düğmesine bas.
4. İşlem başarılı olursa çalıştırmanın **Artifacts** bölümünden `TH-OBS-Studio-Mobile-debug` dosyasını indir.
5. ZIP içindeki `app-debug.apk` dosyasını telefona aktar.

Bu bulut iş akışı APK derlemesini otomatikleştirir; derlemenin başarılı olacağını veya APK'nın cihazında sorunsuz yayın yapacağını önceden garanti etmez. Gerçek cihaz ve YouTube testinin ayrıca yapılması gerekir.
