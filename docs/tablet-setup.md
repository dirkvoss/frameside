# Setting up a tablet as a picture frame

This guide takes a new tablet to a frame that starts by itself after every restart and power cut, protects its battery, and can be loaded from the app even at your parents' or at a holiday home.

## The simple way (default)
In four steps, without Fully and without scripts:
1. **Get the app:** In Frameside under **Devices → ＋ Add device**, scan the QR code with the tablet (or open `<server>/app`), download the file from GitHub and install it. The first time, allow “install apps from this source” once.
2. **Connect:** Open the app. It finds the server in your home network by itself (if the server is in another network, for example at your parents’: choose **Scan QR code** in the app menu and scan the code from the assistant, or type the address). The tablet appears in Frameside, you give it a name and choose “What is the tablet for?” → **Connect**.
3. **Check:** At the device **⚙ → “Tablet” tab → Check tablet** shows with ticks and crosses what is missing and whether the app is up to date. If there is a warning, “Reload page” and “Restart display” right below help.
4. **Restart test:** Press **Restart test**, restart the tablet and do not touch it. Frameside tells you when it is back by itself. After that, if you like, set up the **charge limit** in the app (menu → “Charge limit (socket)”).

Everything else in this guide (Fully, ADB, your own socket scripts, manual pairing) is for advanced users: switch **Settings → View** to **Expert** and those sections appear in the app.

## 1. Which tablet?
- **Easiest: a regular Android tablet** (for example a Samsung Galaxy Tab A). Its screen lock can be set to *None*.
- **Fire tablets** work too and are cheap, but have a swipe lock screen that only a one-time ADB command removes (section 6). Without it the slideshow only starts after a swipe following a restart.
- The tablet should be able to stay on its charger permanently. That is what the **charge limit** is for (section 7).

## 2. Two ways to show the frame
| | Frameside Android app | Fully Kiosk Browser (Plus, about €8) |
|---|---|---|
| Cost | free | about €8 |
| Microphone dialog after restarts | never (it needs none) | can appear after restarts (section 4) |
| Charge limit with a smart plug | **yes, in the app** | only with your own script on the plug |
| Remote settings from the server | via the app | Fully remote control (address + password) |

## 3. Setup in five steps
1. **Add the device:** in the Frameside app, **Devices → ＋ Add device**; on the tablet open `<server>/tv/` (or start the Android app). The tablet shows a code and appears in the app as “found”. Give it a name, **Connect**.
2. **Screen always on, start after restart:** with Fully, **⚙ on the device → Set up and control the tablet → Set up**. In the Android app: menu (long-press the picture) → **Allow start after switching on** → enable “Display over other apps”.
3. **Lock screen off:** Android: Settings → Security → Screen lock → **None**. Fire tablet: passcode off, then section 6.
4. **Check the tablet:** **⚙ on the device → Set up and control the tablet → Check tablet.** Frameside lists ticks and crosses for what is missing (autostart, screen, start address, microphone, lock screen, battery, charge limit, connection path). Fully’s settings need the tablet’s address and the “Remote Admin” password.
5. **Restart test:** restart the tablet and **touch nothing**. After a minute or two the slideshow must appear without a swipe and without a dialog. Only then are you done.

## 4. Typical pitfalls
| What you see | Cause and fix |
|---|---|
| After a restart the tablet stays on its home screen | Swipe lock screen active, or “Display over other apps” not allowed. Sections 3 and 6. |
| After a restart “Microphone” / “Allow audio recording” appears | Fully asks because “Microphone access” is on. Press **IGNORE** (not OK); “Set up” turns the setting off. The Android app never asks. |
| “Web page not found” | The tablet cannot resolve the address. See section 8 (“Testing at home”). |
| 401, or the pairing code keeps appearing | Not paired, or the access was revoked. Pair again. |
| Tablet says “not plugged in” while on the charger | Loose cable or the plug is off. With a charge limit the plug is off when the battery is above the upper limit; that is normal. |
| The plug is on but draws 0 W | The charger is not plugged into the tablet. |

## 5. Connection path and “outside”
Frameside tells the **home network** from **outside**. At home a device needs no login; from outside it gets its **own long-lived access** at pairing that only opens the frame pages (not the app, not your photo overview). The tablet check shows where the tablet is currently connecting from.

## 6. Fire tablet: switch off the lock screen (one time, with ADB)
You need a computer on the same Wi-Fi with `adb` (Android platform tools). Fire OS shows a swipe lock screen even without a passcode, which stops the slideshow from starting after a restart.

1. On the tablet: Settings → Device options → Fire tablet options → tap the **serial number** 7 times, then in **Developer options** turn on **USB debugging** and **Wireless debugging**.
2. **Wireless debugging → Pair device with pairing code** shows a code and an address with a port. On the computer: `adb pair <address:port> <code>`.
3. The connect address is at the top of the Wireless debugging menu: `adb connect <address:port>`.
4. Switch off the lock screen:
   ```
   adb shell locksettings set-disabled true
   adb shell settings put secure lockscreen.disabled 1
   ```
5. Restart test (section 3, step 5).

**Undo:** `adb shell locksettings set-disabled false` and `adb shell settings put secure lockscreen.disabled 0`.
Wireless debugging switches itself off after a restart; turn it on again in Developer options if you need it.

## 7. Charge limit: keep the battery between 20 and 80 %
Lithium batteries age fastest at a **high state of charge** and at **heat**. A tablet left at 100 % on its charger for months loses capacity quickly. With a smart plug Frameside keeps the battery in a range: at the **upper limit** (for example 80 %) the plug is switched off, at the **lower limit** (for example 20 %) it is switched on again.

**You need:** a Shelly plug (Plug S, Plus Plug S, Gen1 too) on the same Wi-Fi as the tablet. Other plugs work through two web addresses (on/off), for example Tasmota.

**Set up in the Frameside Android app:**
1. Long-press the picture (TV: Menu key) → **Charge limit (plug)**.
2. **Search for a Shelly plug** (found in your own network) or type its address. For a plug in a different network, type the address.
3. Choose lower and upper limit (usual 20 and 80 %). They must be at least 10 % apart.
4. **Check and save.** Frameside tests the plug and sets a **safeguard** in it.

**Safeguard:** the plug gets “after a power cut: ON” and “after 8 hours OFF, switch back ON by itself”. If the app, the tablet or the Wi-Fi fails, the tablet charges again after 8 hours at the latest and never runs flat. At high brightness a tablet usually drops below 20 % in that time, and the app switches on before.

**Notes**
- The charge limit runs while the app is open (the display is on all day anyway).
- With Fully instead of the app it is possible too: a small script on the plug reads the battery through Fully’s remote control. That is more work (firewall, fixed address, a password in the script).
- The tablet check shows whether the charge limit works and whether the plug is currently on or off.
- Set it up over ADB (for example for several tablets): `adb shell am start -n com.dirkvoss.frameside.tv/.MainActivity --es ladegrenze shelly,192.168.1.50,20,80`. Switch off: `--es ladegrenze aus`.
- Manual switching (Shelly app): the charge limit takes priority. Switch on with the battery above the upper limit and it switches off again.

## 8. Tablet at your parents’ or a holiday home
The tablet **fetches everything itself** from the server (it calls your server, not the other way round). So the location needs **no port forwarding and no fixed address (DynDNS)**. All that matters is that your server is reachable under an internet address with HTTPS (see “A frame somewhere else” in the main README) and that this address lets **only the frame pages** through.

**Set up while the tablet is still with you**
1. Point the tablet’s start address at the outside address: `https://frame.example.com/tv/?ziel=<device>` (Fully: “Set up” with that address in “Address under which the tablet reaches this server”).
2. The tablet shows a **pairing code**. In the app: **Devices → Pair device** (or the device appears as “found” → **Connect**).
3. Save the **parents’ Wi-Fi** on the tablet (Settings → Wi-Fi → Add network, save only, do not connect). If a plug with a charge limit comes along, store their Wi-Fi in it as well.
4. At your parents’: plug in the charger and the smart plug, connect the tablet to the Wi-Fi. Done.

**Testing at home**
- The outside address must be **resolvable from your home network**. If a Pi-hole or AdGuard filters names from your domain, it answers the tablet with “does not exist” and the tablet shows “Web page not found”. Add the name there pointing at your server’s **public** address (not the internal one), and the tablet sees at home what it will see at your parents’.
- If the name points at the **internal** address at home, Frameside takes the tablet for a device in the home network and issues **no access of its own**. You would have to pair again at your parents’.
- Easier: pair the tablet briefly on your phone’s **hotspot**.

## 8a. Simple access for the person at the frame
If someone (for example your mother) should **add photos and show their own shows** without knowing the Frameside app:
1. Create an **own account** for the person in Immich.
2. In the app at the frame: **⚙ → Simple access for one person**. Enter name, e-mail and password of their Immich account, the **address** under which they reach the server (for a frame at your parents’, the outside address), **Create access**.
3. **Show link and QR code.** Send her the link or open it on her phone. iPhone: Safari → Share → **Add to Home Screen**. From then on it is an icon like an app.
4. The page has four big buttons: **Add photos**, **Make a new show**, **My shows**, **Back to the normal programme**.
5. If the frame sits behind an outside address with restricted paths, add `/einfach/` and `/api/einfach/` in NPM and allow 40 MB for `/api/einfach/hochladen` (see the README).

The person sees only their own account, cannot delete anything and can only show on the frame you entered. You revoke the link at the frame at any time.

## 9. Hand-over checklist
- [ ] Restart test passed without touching it
- [ ] Tablet check without crosses
- [ ] Outside address set and paired
- [ ] Parents’ Wi-Fi saved
- [ ] Charge limit set up (if wanted), plug with the parents’ Wi-Fi
- [ ] A note for the parents: “Leave the charger plugged in. If nothing is showing, press the power button on the tablet.”
