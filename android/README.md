# Frameside TV (Android TV / Google TV / Fire TV)

A thin native shell around the Frameside TV page: full screen, finds your server on the home network by itself, and works as a **screensaver** (daydream). The photos, settings and pairing are the same as in the browser; the device shows up in the Frameside app under *Devices → ＋ Add device*.

## Install
1. Download the APK: **https://github.com/dirkvoss/frameside/releases/latest/download/Frameside-TV.apk** (always the newest app; the releases page also lists the versioned file).
2. Put it on the TV: with the free *Downloader* app (type the link above; allow *Install unknown apps* once), a USB stick, or `adb install Frameside-TV.apk`.
3. Open **Frameside** from the app list. It searches the home network (ports 8090 and 80). Pick your server with OK, or choose *Adresse eintippen* (type the address).
4. Pair it: in the Frameside app tap *Devices → ＋ Add device*; the TV appears by itself.
5. Screensaver (optional): *Settings → Device preferences → Screen saver → Frameside*.

Remote: **Menu** (or a long press on **Back**) opens a small menu: reload, change server, type an address, quit.

## Build
Needs JDK 17 and the Android SDK (platform 34, build-tools 34.0.0).
```bash
cd android
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
./gradlew assembleDebug          # app/build/outputs/apk/debug/app-debug.apk
```
A release build is signed with a key kept outside the repository (`FRAMESIDE_KEYSTORE`, `FRAMESIDE_KEYSTORE_PASSWORD`, optional `FRAMESIDE_KEY_ALIAS`/`FRAMESIDE_KEY_PASSWORD`), then `./gradlew assembleRelease`.

## Provision from the command line
`adb shell am start -n com.dirkvoss.frameside.tv/.MainActivity --es adresse 192.168.1.20:8090`
