# Tablet als Bilderrahmen einrichten

Diese Anleitung führt von einem neuen Tablet bis zu einem Bilderrahmen, der nach jedem Neustart und Stromausfall von selbst wieder startet, den Akku schont und auch bei den Eltern oder im Ferienhaus von dir aus der App bespielt werden kann.

## 1. Welches Tablet?
- **Am einfachsten: ein normales Android-Tablet** (zum Beispiel Samsung Galaxy Tab A). Dort lässt sich der Sperrbildschirm in den Einstellungen ganz abschalten.
- **Fire-Tablets** gehen auch und sind günstig. Sie haben aber einen Wisch-Sperrbildschirm, den man nur mit einem einmaligen ADB-Befehl los wird (Abschnitt 6). Ohne das startet die Diashow nach einem Neustart erst nach einem Wisch.
- Das Tablet sollte dauerhaft am Netzteil hängen können. Dafür gibt es die **Ladegrenze** (Abschnitt 7).

## 2. Zwei Wege zur Anzeige
| | Frameside-Android-App | Fully Kiosk Browser (Plus, ca. 8 €) |
|---|---|---|
| Kosten | kostenlos | ca. 8 € |
| Mikrofon-Dialog nach Neustarts | nie (braucht keins) | kann nach Neustarts erscheinen (Abschnitt 4) |
| Ladegrenze mit Steckdose | **ja, in der App** | nur mit eigenem Skript auf der Steckdose |
| Ferneinstellung vom Server aus | über die App | Fully-Fernsteuerung (Adresse + Passwort) |

Beides funktioniert. Die App ist der einfachere Weg.

## 3. Einrichten in fünf Schritten
1. **Gerät anlegen:** In der Frameside-App unter **Geräte → ＋ Gerät hinzufügen** am Tablet die Adresse `<server>/tv/` öffnen (oder die Android-App starten). Das Tablet zeigt einen Code und erscheint in der App als „gefunden“. Namen vergeben, **Verbinden**.
2. **Bildschirm immer an, Start nach Neustart:** Mit Fully geht das per Klick: **⚙ am Gerät → Tablet einrichten und steuern → Einrichten**. In der Android-App: Menü (langes Drücken aufs Bild) → **Start nach dem Einschalten erlauben** → „Über anderen Apps anzeigen“ einschalten.
3. **Sperrbildschirm aus:** Android: Einstellungen → Sicherheit → Bildschirmsperre → **Keine**. Fire-Tablet: Passcode aus und danach Abschnitt 6.
4. **Tablet prüfen:** **⚙ am Gerät → Tablet einrichten und steuern → Tablet prüfen.** Frameside zeigt mit Haken und Kreuzen, was fehlt (Autostart, Bildschirm, Start-Adresse, Mikrofon, Sperrbildschirm, Akku, Ladegrenze, Verbindungsweg). Für die Fully-Einstellungen braucht es die Adresse des Tablets und das „Remote Admin“-Passwort.
5. **Neustart-Test:** Tablet neu starten und **nichts anfassen**. Nach etwa ein bis zwei Minuten muss die Diashow ohne Wischen und ohne Dialog laufen. Erst dann ist es fertig.

## 4. Typische Stolpersteine (aus der Praxis)
| Beobachtung | Ursache und Lösung |
|---|---|
| Nach dem Neustart steht das Tablet auf dem Startbildschirm | Wisch-Sperrbildschirm aktiv oder „Über anderen Apps anzeigen“ nicht erlaubt. Abschnitt 3 und 6. |
| Nach dem Neustart erscheint „Mikrofon“ / „Audioaufnahmen zulassen“ | Fully fragt danach, weil „Mikrofon-Zugriff“ an ist. Im Dialog **IGNORE** (nicht OK), und „Einrichten“ schaltet die Einstellung ab. Die Android-App kennt dieses Problem nicht. |
| „Webseite nicht gefunden“ | Das Tablet findet den Namen der Adresse nicht. Siehe Abschnitt 8 („Zu Hause testen“). |
| 401 oder Kopplungs-Code erscheint immer wieder | Das Tablet ist noch nicht gekoppelt, oder der Zugang wurde widerrufen. Neu koppeln. |
| Das Tablet zeigt „nicht angesteckt“, obwohl es am Netzteil hängt | Kabel locker oder Steckdose aus. Mit Ladegrenze: Die Steckdose ist gerade aus (Akku über der Obergrenze), das ist normal. |
| Steckdose ist an, zieht aber 0 W | Das Netzteil steckt nicht im Tablet. |

## 5. Verbindungsweg und „außen“
Frameside unterscheidet **Heimnetz** und **außen**: Aus dem Heimnetz braucht das Gerät keine Anmeldung, von außen bekommt es beim Koppeln einen **eigenen, langen Zugang**, der nur die Rahmen-Seiten öffnet (nicht die App und nicht deine Fotos). Der Tablet-Check zeigt, von wo das Tablet gerade kommt.

## 6. Fire-Tablet: Sperrbildschirm abschalten (einmalig, mit ADB)
Das braucht einen Computer im selben WLAN mit `adb` (Android-Plattform-Tools). Der Wisch-Sperrbildschirm erscheint auf Fire OS auch ohne Passcode und verhindert den Start der Diashow nach einem Neustart.

1. Am Tablet: Einstellungen → Geräteoptionen → Fire-Tablet-Optionen → 7-mal auf die **Seriennummer** tippen, danach unter **Entwickleroptionen** **USB-Debugging** und **Drahtlos-Debugging** einschalten.
2. In **Drahtlos-Debugging → Gerät mit Kopplungscode koppeln** stehen ein Code und eine Adresse mit Port. Am Computer: `adb pair <adresse:port> <code>`.
3. Die Adresse zum Verbinden steht oben im Menü „Drahtlos-Debugging“: `adb connect <adresse:port>`.
4. Sperrbildschirm abschalten:
   ```
   adb shell locksettings set-disabled true
   adb shell settings put secure lockscreen.disabled 1
   ```
5. Neustart-Test (Abschnitt 3, Schritt 5).

**Rückweg:** `adb shell locksettings set-disabled false` und `adb shell settings put secure lockscreen.disabled 0`.
Nach einem Neustart ist das Drahtlos-Debugging wieder aus. Bei Bedarf in den Entwickleroptionen erneut einschalten.

## 7. Ladegrenze: Akku zwischen 20 und 80 % halten
Lithium-Akkus altern am schnellsten bei **hohem Ladestand** und bei **Wärme**. Ein Tablet, das monatelang auf 100 % am Netzteil hängt, verliert deshalb schnell Kapazität. Mit einer schaltbaren Steckdose hält Frameside den Akku in einem Bereich: Bei der **Obergrenze** (zum Beispiel 80 %) wird die Steckdose ausgeschaltet, bei der **Untergrenze** (zum Beispiel 20 %) wieder eingeschaltet.

**Du brauchst:** eine Shelly-Steckdose (Plug S, Plus Plug S, auch Gen1) im selben WLAN wie das Tablet. Andere Steckdosen gehen über zwei Web-Adressen (Ein/Aus), zum Beispiel Tasmota.

**Einrichten in der Frameside-Android-App:**
1. Langes Drücken auf das Bild (Fernseher: Menü-Taste) → **Ladegrenze (Steckdose)**.
2. **Shelly-Steckdose suchen** (sie wird im eigenen Netz gefunden) oder die Adresse eintippen. Bei einer Steckdose in einem anderen Netz die Adresse eintippen.
3. Unter- und Obergrenze wählen (üblich 20 und 80 %). Zwischen beiden müssen mindestens 10 % liegen.
4. **Prüfen und speichern.** Frameside prüft die Steckdose und stellt in ihr eine **Sicherung** ein.

**Sicherung:** Die Steckdose bekommt „nach Stromausfall: AN“ und „nach 8 Stunden AUS von selbst wieder AN“. Fällt die App, das Tablet oder das WLAN aus, lädt das Tablet also spätestens nach 8 Stunden wieder und läuft nie leer. Bei hoher Helligkeit entlädt sich ein Tablet in dieser Zeit meist unter 20 %, die App schaltet vorher selbst ein.

**Hinweise**
- Die Ladegrenze läuft, solange die App geöffnet ist (die Anzeige läuft ja ohnehin dauernd).
- Mit Fully statt der App geht es auch: ein kleines Skript auf der Steckdose fragt den Akku über die Fully-Fernsteuerung ab. Das ist aufwendiger (Firewall, feste Adresse, Passwort im Skript).
- Im Tablet-Check steht, ob die Ladegrenze arbeitet und ob die Steckdose gerade an oder aus ist.
- Per ADB einrichten (zum Beispiel für mehrere Tablets): `adb shell am start -n com.dirkvoss.frameside.tv/.MainActivity --es ladegrenze shelly,192.168.1.50,20,80`. Ausschalten: `--es ladegrenze aus`.
- Handsteuerung der Steckdose (Shelly-App): Die Ladegrenze hat Vorrang. Schaltest du bei einem Akku über der Obergrenze ein, schaltet sie wieder aus.

## 8. Tablet bei den Eltern oder im Ferienhaus
Das Tablet **holt sich alles selbst** vom Server (es ruft bei deinem Server an, nicht umgekehrt). Dafür braucht der Standort **keine Portfreigabe und keine feste Adresse (DynDNS)**. Wichtig ist nur, dass dein Server unter einer Internet-Adresse mit HTTPS erreichbar ist (siehe Abschnitt „Rahmen an einem anderen Ort“ in der Haupt-Anleitung) und dass diese Adresse **nur die Rahmen-Seiten** durchlässt.

**Einrichten, solange das Tablet noch bei dir ist**
1. Start-Adresse im Tablet auf die Außen-Adresse stellen: `https://rahmen.example.com/tv/?ziel=<gerät>` (Fully: „Einrichten“ mit dieser Adresse im Feld „Adresse, unter der das Tablet diesen Server erreicht“).
2. Das Tablet zeigt einen **Kopplungs-Code**. In der App: **Geräte → Gerät koppeln** (oder das Gerät erscheint als „gefunden“ → **Verbinden**).
3. **WLAN der Eltern** am Tablet speichern (Einstellungen → WLAN → Netzwerk hinzufügen, nur speichern, nicht verbinden). Hängt eine Steckdose mit Ladegrenze dabei, ihr WLAN ebenfalls dort eintragen.
4. Bei den Eltern: Netzteil und Steckdose einstecken, Tablet mit dem WLAN verbinden. Fertig.

**Zu Hause testen, ohne ein Haus weiter zu müssen**
- Der Name der Außen-Adresse muss **dein Heimnetz auflösen können**. Filtert ein Pi-hole oder AdGuard Namen aus deiner Domain, antwortet es dem Tablet mit „gibt es nicht“, und es zeigt „Webseite nicht gefunden“. Trage den Namen dort auf die **öffentliche** Adresse deines Servers ein (nicht auf die interne), dann sieht das Tablet zu Hause dieselbe Antwort wie bei den Eltern.
- Zeigt der Name zu Hause auf die **interne** Adresse, hält Frameside das Tablet für ein Gerät im Heimnetz und stellt **keinen eigenen Zugang** aus. Bei den Eltern müsstest du dann nochmal koppeln.
- Einfacher: das Tablet kurz im **Handy-Hotspot** koppeln.

## 8a. Einfacher Zugang für die Person am Rahmen
Wenn jemand (zum Beispiel deine Mutter) **selbst Fotos hinzufügen und eigene Shows zeigen** soll, ohne die Frameside-App zu kennen:
1. In Immich ein **eigenes Konto** für die Person anlegen.
2. In der App am Rahmen: **⚙ → Einfacher Zugang für eine Person**. Name, E-Mail und Passwort ihres Immich-Kontos eintragen, die **Adresse** angeben, unter der sie den Server erreicht (bei einem Rahmen bei den Eltern die Außen-Adresse), **Zugang anlegen**.
3. **Link und QR-Code anzeigen.** Den Link schickst du ihr oder öffnest ihn auf ihrem Handy. iPhone: Safari → Teilen → **Zum Home-Bildschirm**. Ab dann ist es ein Symbol wie eine App.
4. Die Seite hat vier große Knöpfe: **Fotos hinzufügen**, **Eine neue Show machen**, **Meine Shows**, **Wieder das normale Programm**.
5. Steht der Rahmen hinter einer Außen-Adresse mit eingeschränkten Pfaden, in NPM `/einfach/` und `/api/einfach/` ergänzen und für `/api/einfach/hochladen` 40 MB erlauben (siehe die README).

Die Person sieht nur ihr eigenes Konto, kann nichts löschen und nur auf den eingetragenen Rahmen zeigen. Du widerrufst den Link jederzeit am Rahmen.

## 9. Die Checkliste für die Übergabe
- [ ] Neustart-Test ohne Berührung bestanden
- [ ] Tablet-Check ohne Kreuze
- [ ] Außen-Adresse eingetragen und gekoppelt
- [ ] WLAN der Eltern gespeichert
- [ ] Ladegrenze eingerichtet (falls gewünscht), Steckdose mit WLAN der Eltern
- [ ] Einschalter-Zettel für die Eltern: „Netzteil stecken lassen. Wenn nichts zu sehen ist: Einschalter am Tablet drücken.“
