# Frameside for Android (tablets, Android TV / Google TV / Fire TV / Fire tablets)

A thin native shell around the Frameside TV page: full screen, finds your server on the home network by itself, and works as a **screensaver** (daydream). The photos, settings and pairing are the same as in the browser; the device shows up in the Frameside app under *Devices → ＋ Add device*.

## Install
1. Download the APK: **https://github.com/dirkvoss/frameside/releases/latest/download/Frameside-TV.apk** (always the newest app; the releases page also lists the versioned file).
2. Put it on the TV: with the free *Downloader* app (allow *Install unknown apps* once), a USB stick, or `adb install Frameside-TV.apk`. In Downloader you don't have to type the long link: your Frameside server redirects its short address **`<server-address>/app`** (for example `192.168.1.20:8090/app`) to the newest APK; the same address is shown in the app under *Devices → ＋ Add device → Android TV, Google TV, Fire TV or Shield*.
3. Open **Frameside** from the app list. It searches the home network (ports 8090 and 80). Pick your server with OK, or choose *Adresse eintippen* (type the address).
4. Pair it: in the Frameside app tap *Devices → ＋ Add device*; the TV appears by itself.
5. Screensaver (optional): *Settings → Device preferences → Screen saver → Frameside*. It heals itself like the app: if the server or the network is gone it stays black (no browser error page) and retries every 20 seconds, or right away when the network is back; if the display crashes it is rebuilt. *Fire TV does not let you pick a custom screensaver (as far as we know) – there Frameside runs as a normal app.*

Remote: **Menu** (or a long press on **Back**) opens a small menu: reload, change server, type an address, quit.

## Tablet as a picture frame (Android and Fire tablets)
The same app is the simplest way to turn a tablet into a frame – no browser, no Fully Kiosk, no settings.
1. On the tablet open **https://github.com/dirkvoss/frameside/releases/latest/download/Frameside-TV.apk** (or scan the QR code in the Frameside app under *Devices → ＋ Add device → Android or Fire tablet*). On a **Fire tablet** use the Silk browser.
2. Open the downloaded file and confirm *Install*. The first time Android asks to allow the browser to *install apps from this source* – allow it once.
3. Open **Frameside**. It searches your home network for the server; if the server is in another network, tap *Adresse eintippen* and type e.g. `192.168.1.20:8090`.
4. In the Frameside app tap *Devices → ＋ Add device*: the tablet appears as *Android tablet found* – tap **Connect**.
5. Optional but recommended: allow *Display over other apps* for Frameside when asked (one time). Then the frame starts by itself after a power cut or restart.

What the app does for you: full screen, screen always on, reconnects after a network outage, rebuilds the display if it crashes, reports battery and charging state, dims the screen at night (brightness, as set in the Frameside app), and executes *Reload page / Restart display* from the Frameside app. Long-press the picture (tablet) or press Menu (remote) for the small menu.

### Charge limit (battery care for a tablet on its charger all day)
Menu → **Charge limit (plug)**: pick a smart plug in your Wi-Fi (Shelly Gen1/Gen2/Gen3/Gen4 are found automatically, other plugs work with an on and an off web address) and a range such as 20–80 %. The app switches the plug off at the upper limit and on at the lower limit, and sets a safeguard in the plug (*after a power cut: on*, *after 8 hours off: switch back on by itself*) so the tablet never runs flat if the app or the Wi-Fi fails. Everything stays local: no account, no cloud, no script on the plug. The state is reported to the Frameside server (shown in the tablet check). Full guide: [docs/tablet-setup.md](../docs/tablet-setup.md).

*Technical note:* the app exposes the functions of Fully Kiosk's JavaScript interface (`window.fully`: battery level, plugged, brightness get/set, turn screen on, restart app) plus `getLadegrenze` (charge-limit state), so the frame page behaves exactly as with Fully.

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
Charge limit without the menu: `... --es ladegrenze shelly,192.168.1.50,20,80` (switch off: `--es ladegrenze aus`).
