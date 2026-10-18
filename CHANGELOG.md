# Änderungsprotokoll
Format: [Keep a Changelog](https://keepachangelog.com/de/1.1.0/), Versionen nach [SemVer](https://semver.org/lang/de/).

## [2.36.1]
### Geändert
- **Standard und Profi unterscheiden sich im Gerätedialog jetzt deutlich** (im Standard etwa 57 sichtbare Bedienelemente statt 133 in der Profi-Ansicht). Im Standard bleibt, was eine normale Person einstellt: Name, Hintergrund, Sekunden und Zoom; bei der Anzeige je Element nur Haken und Lage; das Dauerprogramm, Erinnerungen und die Nachtruhe-Zeiten; der Tablet-Check; Notiz, Gruß und Einfacher Zugang. In die Profi-Ansicht gewandert sind: Hochkant-Paare, bei jedem Anzeige-Element Größe, Farbe, Schrift und Zusatzoptionen, „Anordnung übernehmen“, die Vorschau-Tage, der Kalender-Link, Zeitplan, Favoriten, Geburtstagsfotos, Nachtruhe nach Sonnenstand, „Fest mit Gästefotos“ und „Wartung“.

## [2.36.0]
### Geändert
- **Der Dialog „Gerät einstellen“ ist aufgeräumt: fünf Reiter statt eines langen Formulars.** **Allgemein** (Name, Hintergrund, Sekunden pro Foto, Zoom, Hochkant-Paare), **Anzeige** (Anordnung der Einblendungen, Wetter-Ort, Termine von Handys, Vorschau-Tage, der Kalender-Link in einem zugeklappten Block), **Programm** (Dauerprogramm, Zeitplan, besondere Fotos, Nachtruhe), **Tablet** (Tablet-Check, Neustart-Test, Ladeverlauf, Zugang widerrufen) und **Mehr** (Notiz, Fest mit Gästefotos, Gruß mit Foto, Einfacher Zugang, Wartung). Kein Feld ist weggefallen, jedes liegt in genau einem Reiter.
- **Technik nur in der Profi-Ansicht:** „Art ändern“, „Kennung (Home Assistant)“, „Bildschärfe“, der ADB-Weg und „Verbindungs-Link und QR-Code“ erscheinen nur noch bei Einstellungen → Ansicht → Profi. Im Reiter „Tablet“ steht im Standard nur noch der Tablet-Check, der Hinweis zu Fully gehört zur Profi-Ansicht.
### Behoben
- **Android-App 1.4.2: Ein einzelner Entwicklungs-Server wird nicht mehr von selbst gewählt.** Die App sucht nur im eigenen Netz. Stand dort nur ein Entwicklungs-Server (Version „dev“), verband sie sich damit, obwohl der eigentliche Server in einem anderen Netz stand. Jetzt fragt sie in diesem Fall („Entwicklungs-Server gefunden“) und weist auf die Möglichkeit hin, die Adresse einzutippen. Bei genau einem normalen Server verbindet sie sich weiter von selbst, bei mehreren fragt sie wie bisher.

## [2.35.2]
### Behoben
- **Der Knopf „App jetzt aktualisieren“ im Tablet-Check erschien nie** (Fehler seit 2.35.0). Die Zeilen des Tablet-Checks kannten ihre Kennung nicht, deshalb wusste die Seite nicht, bei welcher Zeile der Knopf stehen soll. Jetzt trägt jede Zeile Kennung, Status und Wert mit; ein Test hält das fest.

## [2.35.1]
### Behoben
- **Eine neue Version der Seite lädt sich von selbst neu.** Bisher erschien nur ein Banner „Neue Version verfügbar – tippen zum Aktualisieren“, und eine lange offene Seite blieb auf dem alten Stand. Jetzt lädt sich die Seite selbst neu, sobald kein Fenster offen ist und nichts getippt wird; sonst liegt das Banner über allen Fenstern und die Seite lädt neu, sobald alles zu ist. Geprüft wird alle 30 Sekunden und beim Zurückkehren zur Seite.

## [2.35.0]
### Neu
- **Die Android-App aktualisiert sich selbst (App 1.4.0 und 1.4.1).** Gibt es eine neuere App, zeigt das Menü der App (langes Drücken aufs Bild) „Update auf … installieren“, und der Tablet-Check in Frameside bietet bei einer veralteten App den Knopf **„App jetzt aktualisieren“**. Das Tablet lädt die neue Datei von GitHub und startet die Installation; Android nimmt nur eine Datei an, die mit demselben Schlüssel signiert ist, und fragt einmal „Aktualisieren?“. Beim ersten Mal erlaubt man der App einmalig „Apps installieren“. Nach dem Update startet die App von selbst wieder. Ein Hinweisfenster mitten in der Diashow gibt es nicht. Die Version 1.3.x kann das noch nicht und muss einmal von Hand aktualisiert werden.
- **Die Konfiguration des Servers nennt die aktuelle App-Version** (`app_aktuell`), danach fragt die App.
### Behoben
- **Android-App 1.4.1: Schrift und Symbole auf Tablets waren noch zu klein.** Die Rahmen-Seite verkleinerte sich für die Fernseher-App auf 1920 Pixel Breite, und diese Regel traf auch Tablets. Die App meldet jetzt, ob sie auf einem Tablet läuft; nur am Fernseher wird verkleinert.

## [2.34.1]
### Behoben
- **Android-App 1.3.1: Schrift und Symbole auf Tablets waren zu klein.** Die App stellte die Seite im „Übersichtsmodus“ dar und verkleinerte sie damit auf einem Tablet; jetzt gilt der Übersichtsmodus nur noch am Fernseher, am Tablet ist die Darstellung wie in Fully.
- **Android-App 1.3.1: Der Dialog „Ladegrenze“ zeigte nur den Text und „Schließen“.** Die Auswahl (Shelly-Steckdose suchen, Web-Adressen eintippen, Grenzen ändern, ausschalten) wurde von Android nicht angezeigt, wenn der Dialog auch einen Text hatte. Text und Auswahl stehen jetzt in einer Liste.
- **Die App-Datei am GitHub-Release ist jetzt wirklich die neue:** Bisher hing dort die alte Version 1.0.0 vom 10. Oktober (die CI übernimmt nur die APK des vorigen Releases, sie baut keine); die Funktionen der Versionen 1.2 und 1.3 (Tablet-Player, Fully-Brücke, Ladegrenze) fehlten in der Download-Datei.

## [2.34.0]
### Neu
- **Profi-Ansicht (serverweit):** In den Einstellungen wählst du die **Ansicht**: **Standard** (Voreinstellung) oder **Profi**. Im Standard zeigt Frameside nur den einfachen Weg für Tablets: die **Frameside-App** und den **Tablet-Check**. Die Technik-Einstellungen (Fully mit Adresse, Passwort und „Einrichten“, „Gerät von Hand hinzufügen“, ADB, Link und Adresse für den Browser) erscheinen erst in der Profi-Ansicht. Der Schalter gilt für alle Browser und Geräte.
- **Tablet-Check zeigt, ob die Frameside-App aktuell ist.** Die App meldet ihre Version; ist eine neuere da, steht in der Ampel „Es gibt eine neuere Frameside-App“ mit dem Weg, sie zu aktualisieren.
- **Geführter Neustart-Test** (Geräte → ⚙ → „Neustart-Test“): Du startest das Tablet neu und fasst es nicht mehr an; Frameside wartet, bis es ausgeht und von selbst wiederkommt, und meldet „✓ von selbst wieder da (nach … s)“ oder nach 15 Minuten, was zu prüfen ist.
- **Tablet-Check erkennt das Gerät** (Fire-Tablet, Android-Tablet …) und zeigt „Erkanntes Gerät“; für Nicht-Fire-Geräte entfällt der Fire-Hinweis zum Sperrbildschirm. Außerdem prüft er, ob die **Diashow läuft** (das Bild wechselt) und meldet eine hängende Anzeige.
- **Reparieren mit einem Klick:** Zeigt der Tablet-Check ein Warnzeichen, erscheinen direkt darunter „Seite neu laden“ und „Anzeige neu starten“.
- **„Problem melden“:** kopiert Version, Zeit und die Zeilen des Tablet-Checks in die Zwischenablage (ohne Passwörter), zum Einfügen in eine Nachricht.
- **„Zugang des Tablets widerrufen“** steht jetzt direkt im Tablet-Check („Tablet verloren oder verkauft?“), nicht mehr nur unter „Wartung“.
- **Tablet-Art beim Anlegen:** Im Assistenten „Gerät hinzufügen“ wählst du bei einem Bilderrahmen „Wofür ist das Tablet?“: **Wohnzimmer** (Standard), **Küche** (Uhrzeit und Datum im Bild, 10 Sekunden je Foto) oder **für eine ältere Person** (Datum und Uhrzeit, 15 Sekunden je Foto, Termine für 3 Tage). Alles lässt sich danach am Gerät ändern.
- **Ladeverlauf:** Unter Geräte → ⚙ → „Ladeverlauf“ siehst du, wann das Tablet zuletzt Strom bekommen oder verloren hat, mit Akkustand. Frameside merkt sich die letzten 60 Wechsel je Rahmen.
- **Wochenbericht aufs Handy:** Sonntags ab 18 Uhr eine Pushover-Meldung „Wochenbericht: alle Rahmen in Ordnung“ oder welcher Rahmen Aufmerksamkeit braucht (offline, Bild hängt, Akku niedrig ohne Strom, App veraltet). In den Einstellungen unter den Meldungen abschaltbar.
- **Offline-Reserve im Tablet-Check:** Der Check zeigt, ob das Tablet bei WLAN-Ausfall schon gesehene Fotos weiterzeigt (bis zu 150, nur mit HTTPS) oder nicht.
- **Die App-Schritte nennen die Server-Adresse**, die man in der App eintippt, wenn das Tablet außerhalb des Heimnetzes steht.
### Geändert
- **„Gerät hinzufügen“ zeigt zuerst den App-Weg** (aufgeklappt): App laden, Frameside öffnen, auf „Verbinden“ tippen.
### Sicherheit
- **Kopplungscodes sind einmalig:** Nachdem das Tablet seinen Zugang abgeholt hat, antwortet der Code nur noch zwei Minuten (falls die Antwort verloren ging), danach gilt er als verbraucht. Zuvor blieb er bis zum Ablauf der 15 Minuten gültig.

## [2.33.1]
### Geändert
- **Einfacher Zugang: die Adresse für den Link wird vorgeschlagen.** Läuft der Rahmen von außen (Tablet bei den Eltern), meldet die Rahmen-Seite die Adresse, unter der sie läuft, und das Feld „Adresse, unter der die Person den Server erreicht“ ist damit schon ausgefüllt. Das Feld ist jetzt als Internetadresse gekennzeichnet, damit Browser dort keine Postadresse mehr einsetzen. Der Knopf „Zugang anlegen“ ist jetzt hervorgehoben und wird nicht mehr übersehen.

## [2.33.0]
### Neu
- **Einfacher Zugang für eine Person (z. B. Oma):** Am Rahmen unter ⚙ → „Einfacher Zugang für eine Person“ legst du einen Zugang an (Name, Immich-E-Mail und -Passwort des Kontos der Person; gespeichert wird nur ein eigener, widerrufbarer Schlüssel ohne Löschrechte). Du bekommst einen **Link und QR-Code** auf eine ganz einfache Seite mit großen Knöpfen: **Fotos hinzufügen** (landen im eigenen Immich-Konto), **eine neue Show machen** (Fotos antippen, jetzt zeigen oder mit Namen speichern), **Meine Shows** (zeigen, löschen) und **wieder das normale Programm**. Die Person sieht und ändert sonst nichts in der App und kann nur auf den eingetragenen Rahmen zeigen; der Link lässt sich jederzeit widerrufen. Läuft in allen fünf Sprachen. Für eine Außen-Adresse mit eingeschränkten Pfaden müssen `/einfach/` und `/api/einfach/` freigegeben und für `/api/einfach/hochladen` mehr als 1 MB erlaubt werden (siehe README, „Ein Rahmen an einem anderen Ort“).
- **Vorschau der Termine je Rahmen einstellbar:** Unter Geräte → ⚙ → Kalender legst du fest, ob der Rahmen weiter vorausschaut, wenn heute nichts mehr ansteht, und wie viele Tage: **0** = nur heute, **1** = auch morgen (Standard, wie bisher), bis **14** Tage. Der Rahmen zeigt zuerst, was heute noch kommt, sonst die Termine des nächsten Tages mit Terminen innerhalb dieser Tage, mit Wochentag und Datum („Mi 21.10. 09:00 Pilates“). Gilt für Termine aus dem Kalender-Link; die Handy-Termine kennen weiter nur heute und morgen.

## [2.32.0]
### Neu
- **„Kalender testen“ am Rahmen** (Geräte → ⚙ → Kalender): prüft Link, Benutzer und Passwort (auch vor dem Speichern) und nennt bei einem Fehler den Grund („Benutzer oder Passwort stimmen nicht“, „Unter diesem Link gibt es keinen Kalender“ …). Bei Erfolg steht dort der **lesbare Name des Kalenders** (z. B. „Family“ statt der langen Radicale-Adresse), die Zahl der Einträge und wie viele heute und morgen anstehen. Der Name erscheint auch beim Öffnen des Dialogs.
### Behoben
- **Ein einzelner kaputter Eintrag machte den ganzen Kalender unlesbar:** Ein alter Termin mit unlesbarem `SEQUENCE` ließ den vollen Termin-Leser abstürzen, danach las nur noch der einfache Leser (ohne monatliche Wiederholungen und Ausnahmen). Jetzt werden kaputte Nebenangaben ignoriert und nur Einträge mit unlesbarem Datum oder unlesbarer Wiederholung ausgelassen.

## [2.31.0]
### Neu
- **Kalender-Link mit Anmeldung, je Rahmen:** Unter Geräte → ⚙ → Kalender gibt es zum Kalender-Link jetzt **Benutzer und Passwort** (für geschützte Kalender wie Radicale oder Nextcloud). Jeder Rahmen kann seinen eigenen Kalender mit eigener Anmeldung zeigen; die Anmeldung gilt nur für den eigenen Link dieses Rahmens, wird nur auf dem Server gespeichert und nie wieder angezeigt. `webcal://`-Links werden angenommen.
### Geändert
- **Kalender-Link: vollständiger Termin-Leser** (Bibliotheken `icalendar` und `recurring-ical-events`): versteht alle Wiederholungen (auch **monatlich**, jährlich mit Regeln, `COUNT`), **ausgenommene Tage** (`EXDATE`), **verschobene Einzeltermine** und **mehrtägige** ganztägige Termine; abgesagte Termine werden nicht mehr gezeigt. Bei einer Datei, die die Bibliothek nicht lesen kann, liest weiter der bisherige einfache Leser.

## [2.30.0]
### Neu
- **Ladegrenze für Tablets (Android-App 1.3.0):** Menü → „Ladegrenze (Steckdose)“. Die App hält den Akku in einem Bereich (z. B. 20–80 %), indem sie eine Shelly-Steckdose im selben WLAN (Gen1 bis Gen4, automatisch gefunden) bei der Obergrenze aus- und bei der Untergrenze einschaltet; andere Steckdosen gehen über eine Ein- und eine Aus-Web-Adresse. Alles lokal, ohne Konto, Cloud, Skript oder Firewall-Regel. In der Steckdose wird eine **Sicherung** gesetzt („nach Stromausfall an“, „nach 8 Stunden aus von selbst wieder an“), damit das Tablet nie leerläuft. Ändert sich die Adresse der Steckdose, sucht die App sie von selbst wieder. Per ADB einrichtbar: `--es ladegrenze shelly,<adresse>,20,80`.
- **Tablet-Check:** Unter Geräte → ⚙ → „Tablet einrichten und steuern“ → „Tablet prüfen“ zeigt Frameside mit Haken und Kreuzen, ob das Tablet für den Dauerbetrieb bereit ist (nur lesend): Fernsteuerung, Start nach Neustart, Bildschirm an, Start-Adresse, Mikrofon, Fully Plus, Sperrbildschirm, Akku, Ladegrenze, Verbindungsweg (Heimnetz oder außen) und Hinweise zu Dingen, die der Server nicht sehen kann („Über anderen Apps anzeigen“, Neustart-Test).
- **Ausführliche Anleitung „Tablet als Bilderrahmen einrichten“** (Deutsch und Englisch, `docs/tablet-einrichten.md`, `docs/tablet-setup.md`): Geräteempfehlung, Neustart-Test, Fire-Tablet-Sperrbildschirm per ADB, Ladegrenze, Tablet bei den Eltern (ohne DynDNS und Portfreigabe), Test zu Hause und die DNS-Falle, Checkliste zur Übergabe.
### Geändert
- **Tablet einrichten schaltet das Mikrofon in Fully aus** („Mikrofon-Zugriff“): Der Rahmen braucht keins, und auf Fire-Tablets fragte Fully sonst nach Neustarts wieder danach und die Anzeige wartete auf den Dialog. Nur bei Fully-Versionen, die die Einstellung kennen.
- Der Herzschlag der Rahmen-Seite meldet jetzt den Verbindungsweg (Heimnetz/außen) und die Ladegrenze der Android-App an den Server.

## [2.29.0]
### Neu
- **Android-TV-App einfach auf den Fernseher bekommen:** Der Server leitet seine kurze Adresse `<Server>/app` (z. B. `192.168.1.20:8090/app`) auf die neueste App-Datei weiter – in der kostenlosen App „Downloader“ am Fernseher muss man nicht mehr den langen GitHub-Link tippen. Im Dialog „Gerät hinzufügen“ gibt es dazu den neuen Block „Android TV, Google TV, Fire TV oder Shield: Frameside-App installieren“ (drei Schritte, die Adresse wird gleich angezeigt, Hinweis zum Bildschirmschoner). Übersetzt in Englisch, Französisch, Niederländisch und Spanisch.
### Behoben
- **Bildschirmschoner der Android-App (Version 1.2.1) heilt sich selbst:** Fällt der Server oder das Netz aus, bleibt der Bildschirm schwarz (statt einer Browser-Fehlerseite) und lädt alle 20 Sekunden bzw. sofort bei Rückkehr des Netzes neu; stürzt die Anzeige ab, wird sie neu aufgebaut. Vorher blieb der Schoner nach einem Ausfall hängen.

## [2.28.0]
### Neu
- **Android-App „Frameside“ als vollständiger Rahmen für Tablets (Version 1.2.0):** Sie stellt der Rahmen-Seite die fünf Funktionen der Fully-Schnittstelle bereit (Akku, Laden, Helligkeit, Bildschirm an, Neustart) – Nachtruhe, Akku-Anzeige und Fernbefehle laufen damit ohne Fully und ohne Einstellungen. Dazu Selbstheilung (sofortiges Neuladen, wenn das Netz zurückkommt; Neuaufbau der Anzeige nach einem Absturz) und ein einmaliger Hinweis zum Start nach dem Einschalten. Im Dialog „Gerät hinzufügen“ stehen jetzt die drei Schritte mit QR-Code zum Laden der APK; Fire-Tablets werden als „Fire-Tablet“ erkannt. Anleitung in `android/README.md` und im README.
### Geändert
- **Tablet-Einrichtung: verständliche Meldung, wenn der Server das Tablet nicht erreicht:** „Verbindung testen“, „Einrichten“ und „Prüfen“ erklären jetzt, dass Server und Tablet meist in verschiedenen Netzen stehen (z. B. Server im Gast-/DMZ-Netz, Tablet im Heimnetz) und die Firewall die Verbindung blockiert, und weisen auf den Ausweg „Einstellungsdatei laden“ hin. Eine Voraussetzungs-Zeile steht vorab im Dialog.

## [2.27.1]
### Behoben
- **Tablet-Einrichtung bei Tablets, die sich selbst melden:** Der Assistent „Tablet einrichten und steuern“ ist jetzt immer sichtbar (vorher hinter „Adresse und Passwort trotzdem einrichten“ versteckt). Adresse und Passwort merkt er sich getrennt von der Nachtsteuerung, „Speichern“ im Dialog löscht sie nicht mehr, und „Prüfen“ braucht keine erneute Eingabe. Die erkannte Adresse des Tablets wird vorgeschlagen.
- **Auswahlfenster auf dem Rechner:** Die Knöpfe „Diashow“, „Rückgängig“ und „Leeren“ ragten über den rechten Rand und waren abgeschnitten; sie brechen jetzt um.

## [2.27.0]
### Neu
- **Tablet mit Fully Kiosk per Klick als Bilderrahmen einrichten:** Unter Geräte → ⚙ → „Tablet einrichten und steuern“ beantwortest du nur vier einfache Fragen (Ausrichtung, Bildschirm immer an, Start nach Neustart/Stromausfall, Neuladen bei Netzausfall; optional Helligkeit) und gibst die Adresse des Servers an. Frameside setzt dann über die Fernsteuerung von Fully rund 25 Einstellungen, **einschließlich der Start-Adresse** (`/tv/?ziel=<Rahmen>`) – kein Tippen im Kiosk-Browser mehr. **Einrichten** ist beliebig oft wiederholbar (ändert nur, was abweicht), **Prüfen** zeigt Abweichungen, wenn jemand am Tablet etwas verstellt hat, und die Wahl bleibt gespeichert und änderbar. Für Tablets, die der Server nicht erreicht (z. B. bei den Eltern): **Einstellungsdatei laden** zum Importieren in Fully.
- **Android-App „Frameside“ auch für Tablets:** Start nach dem Einschalten (Menü → „Start nach dem Einschalten erlauben“, einmalig „Über anderen Apps einblenden“ freigeben); auf dem Tablet öffnet langes Drücken auf das Bild das Menü. Version 1.1.0.
### Behoben
- **Tablet sperrte sich beim Koppeln selbst aus („?“ statt Code):** Lädt ein Gerät seine Seite immer wieder neu (z. B. Fully mit „Alle 60 Sekunden neu laden“), holte es jedes Mal einen neuen Kopplungs-Code und war nach fünf offenen Codes gesperrt. Jetzt behält es seinen Code, solange er noch nicht vergeben ist; bei einer Sperre zeigt die Seite „…“ statt „?“ und versucht es weiter.

## [2.26.2]
### Behoben
- **Rahmen mit Fully Kiosk wachte morgens nicht mehr auf:** Die Nachtruhe schaltete den Bildschirm ganz aus, dabei friert Fully die Seite ein, und sie konnte sich um das Ende der Nachtruhe nicht mehr selbst einschalten (auch Antippen half nicht). Jetzt stellt die Seite nachts die **Helligkeit auf 0** und zeigt schwarz, läuft aber weiter und stellt die Helligkeit am Ende der Nachtruhe von selbst wieder her. Wer den Bildschirm wirklich ausschalten möchte, nutzt dafür eine eigene Automatik (siehe README).

## [2.26.1]
### Behoben
- **QR-Scan in der App („QR-Code am Gerät scannen“):** Er öffnet jetzt gleich „Gerät gefunden“ mit Namensfeld (statt den Code nur zu übernehmen und ein vorhandenes Gerät zu verlangen). Gehört der Code zu einem anderen Server oder ist er abgelaufen, steht ein klarer Hinweis da.

## [2.26.0]
### Neu
- **Frameside TV: App für Android TV, Google TV und Fire TV** (Ordner `android/`, APK im Release). Sie öffnet die Fernseher-Seite im Vollbild, **findet den Server im Heimnetz selbst**, hält den Bildschirm an und arbeitet als **Bildschirmschoner** (Einstellungen → Geräteeinstellungen → Bildschirmschoner → Frameside). Die Kopplung ist dieselbe wie im Browser. Getestet auf einer NVIDIA Shield (Android 11, 4K). Menü per Menü-Taste oder langem Druck auf Zurück.
### Behoben
- **QR-Code am Fernseher:** Scannt man ihn mit der App, öffnet sich jetzt gleich „Gerät gefunden“ mit Namensfeld (statt des alten Kopplungsdialogs ohne Namen). Ist der Code abgelaufen, steht ein Hinweis da.
- **Fernseher-Seite in der Android-TV-App:** Die Seite passt sich der Bildschirmgröße der App an (der WebView meldet dort nur 960 × 540 Pixel).

## [2.25.0]
### Neu
- **Schärfere Fotos auf 4K-Fernsehern:** Große Bildschirme bekommen das Foto jetzt in voller Auflösung (Immich „fullsize“) statt der Vorschau von etwa 1440 Pixeln. Zu große oder nicht darstellbare Dateien (über 12 MB, Formate wie HEIC) fallen automatisch auf die Vorschau zurück. Je Gerät einstellbar unter ⚙ → „Bildschärfe“ (Automatisch, Standard, Hoch).
- **Zuschnitt und Zoom mit Gesichtern:** Im Modus „Zuschnitt“ und beim sanften Zoom richtet sich der Bildausschnitt nach den Gesichtern im Foto, damit keine Köpfe abgeschnitten werden. Dafür braucht der Immich-Schlüssel das Leserecht `face.read`; neue Einrichtungen legen es an, bei älteren Schlüsseln bleibt es bei der Bildmitte. Es wird nichts geändert oder gelöscht.
- **Abwechslungsreicher Zoom:** Statt nur hinein und heraus gibt es jetzt sechs sanfte Bewegungen (Zoom und leichte Schwenks), passend zur Anzeigedauer.
- **Einbrennschutz:** Die Einblendungen (Uhr, Wetter, Termine …) wandern alle vier Minuten unmerklich um wenige Pixel. Das schont OLED-Bildschirme.
- **Rahmen läuft bei Netzausfall weiter:** Mit HTTPS merkt sich der Rahmen zuletzt gesehene Fotos und zeigt sie bei kurzem Netzausfall weiter (bis zu 150 Fotos auf dem Gerät).

## [2.24.0]
### Neu
- **PIN ändern in der Oberfläche und in der App:** Einstellungen (Konto-Menü → ⚙) → „Zugang (PIN)“. Von außen braucht es die bisherige PIN, im vertrauten Heimnetz nicht (zum Beispiel wenn man sie vergessen hat). Falsche Versuche werden wie bei der Anmeldung gezählt und gesperrt, sehr leichte PINs (123456, lauter gleiche Ziffern) werden abgelehnt. Das eigene Gerät bleibt angemeldet, alle anderen geben die neue PIN einmal ein. Bisher ging das nur per Befehl auf dem Server.
### Geändert
- Das Protokoll ist aufgeräumt: keine „on_event is deprecated“-Warnungen mehr (Startfunktionen laufen über den neuen Lebenszyklus von FastAPI).

## [2.23.1]
### Geändert
- Texte nach dem neuen Ablauf: Einrichtungsassistent, Installationsskript und „Alles in Ordnung?“ erklären jetzt, dass Fernseher und Tablets von selbst in der App erscheinen (kein Code mehr nötig). Die Zeile zur Version heißt in „Alles in Ordnung?“ jetzt „Frameside“.
- Anleitungen mit neuen Bildern (Name Frameside, neuer Kopplungs-Ablauf).
- Freiwillige Spenden über Ko-fi: Link im README und GitHub-Sponsor-Knopf (`.github/FUNDING.yml`). Frameside bleibt kostenlos.

## [2.23.0]
### Neu
- **Neuer Name: Frameside** (vorher „Immich Showcase“). Der Name „Immich“ gehört dem Immich-Projekt und soll nicht im Namen einer eigenständigen App stehen. Frameside ist ein eigenständiges Programm **für Immich**. Die technischen Namen bleiben unverändert, damit nichts bricht: GitHub-Repository `dirkvoss/immich-showcase`, Docker-Image `ghcr.io/dirkvoss/immich-showcase`, Umgebungsvariablen `RAHMEN_WEB_*`, Bonjour-Diensttyp `_showcase._tcp`, Album „Showcase-Uploads“. Neue Installationen zeigen den neuen Namen in der Oberfläche, im Fernseher-Titel, im Einrichtungsassistenten, in den Docs und in der iPhone-App. Wer in seiner `.env` `RAHMEN_WEB_NAME` gesetzt hat, behält seinen eigenen Namen.
- **Geräte finden sich selbst:** „Gerät hinzufügen“ zeigt nur noch die kurze Adresse. Öffnest du sie am Fernseher, Tablet oder Raspberry Pi, erscheint das Gerät in der App von selbst („📺 LG-Fernseher gefunden“), du gibst einen Namen ein und tippst auf „Verbinden“. Kein Code, keine Vorab-Auswahl der Geräteart (sie wird am Browser erkannt und lässt sich ändern). Das Eintippen eines Codes bleibt als Ausweg.
- **Fernseher haben jetzt dieselben Anzeige-Einstellungen wie Bilderrahmen:** Anordnung der Einblendungen (Uhr, Wetter, Termine, Geburtstage …), Wetter-Ort, Termine von Handys, Kalender-Link, Nachtruhe, sanfter Zoom, Hochkant-Paare sowie Notiz, Gruß mit Foto und Gäste-Upload. Nur das Dauerprogramm (Zufallsfotos im Leerlauf, Zeitplan, Erinnerungen, Favoriten) bleibt den Rahmen vorbehalten.
- **Einfache Diashow:** Beim Senden an einen Fernseher schaltet „Einfache Diashow“ alle Einblendungen für diese Show aus – nur die Fotos, zum Beispiel für Besuch.
### Geändert
- **Geräteart verständlicher und nachträglich änderbar:** Beim Anlegen heißt die Auswahl jetzt „Fernseher (Smart-TV oder TV-Stick mit Browser)“ oder „Bilderrahmen (Tablet, Raspberry Pi oder Bildschirm mit Dauerprogramm)“, Fernseher steht oben. Wurde ein Gerät mit der falschen Art angelegt (ein Fernseher als Bilderrahmen hat viele Einstellungen, ein Fernseher wenige), lässt sich die Art unter ⚙ ändern; Einstellungen, die nur für Rahmen gelten, fallen dann weg.
- **Gelöschtes Gerät:** Läuft ein Fernseher oder Tablet noch mit dem Namen eines gelöschten Geräts, vergisst die Seite ihn nach wenigen Sekunden selbst und zeigt den Code zum Neu-Koppeln (statt „keine Verbindung zum Server“).
- **Fernseher/Tablet neu koppeln:** Ein Gerät, das schon einmal gekoppelt wurde, zeigt keinen Code mehr. Mit `?neu=1` hinter der Adresse (zum Beispiel `tv.example.com/tv/?neu=1`) vergisst es die alte Kopplung und zeigt wieder den Code.
### Behoben
- **Gäste-QR-Code mit Adresse ohne `https://`:** Steht als „Adresse für Tablets und Fernseher“ nur `tv.beispiel.de`, setzt der QR-Code und der Link für Gäste jetzt automatisch `https://` davor (bei IP-Adressen, `.local` und Namen ohne Punkt `http://`). Vorher konnten manche Handys den QR-Code nicht als Link öffnen.

## [2.22.0]
### Neu
- **Raspberry Pi als Server in einem Befehl:** `examples/raspberry-pi/install-pi.sh` installiert auf einem frisch aufgespielten Raspberry Pi OS (Lite, 64-Bit) bzw. jedem Debian-Rechner Docker und Immich Showcase, meldet den Server im Netz an (Bonjour: die iPhone-App findet ihn selbst; mit `--hostname showcase` auch unter `showcase.local`) und zeigt den Einrichtungs-Link. Optionen: `--hostname`, `--port`, `--ohne-bonjour`, `--version`. Auf einem Debian-12-System getestet; auf echter Raspberry-Pi-Hardware (arm64) noch nicht.
### Geändert
- Der Update-Hinweis in „Alles in Ordnung?“ nennt jetzt den genauen Befehl für die Standard-Installation (`cd ~/immich-showcase && docker compose pull && docker compose up -d`) und weist auf Watchtower für automatische Updates hin.

## [2.21.0]
### Neu
- **Gäste-Upload per QR-Code („Fest“):** Unter Geräte → ⚙ → „Fest: Fotos von Gästen“ startest du für 4 Stunden bis 3 Tage einen Gäste-Upload. Am Rahmen erscheint ein QR-Code (neues Layout-Element „Gäste-QR-Code“), Gäste scannen ihn und laden auf einer einfachen Seite (ohne Anmeldung, deutsch/englisch) Fotos hoch; sie kommen gleich als Nächstes auf den Rahmen. Die Fotos liegen in Immich im Upload-Album (Namen mit „Gast-“). Schutz: zufällige Kennung, Zeitfenster, höchstens 300 Fotos je Fest, 40 Fotos je 10 Minuten und IP, nur Fotos (keine Videos). Schnittstellen: `POST/GET/DELETE /api/gaeste/{rahmen}`, öffentlich `GET /gast/{kennung}` und `PUT /api/gast/{kennung}/hochladen`.
- **Grüße mit Foto:** Unter Geräte → ⚙ → „Gruß mit Foto an diesen Rahmen“ schickst du ein Foto mit einem Satz („💌 Anna: Hallo Oma!“). Das Foto kommt als Nächstes und danach alle paar Fotos wieder, solange der Gruß gilt (2 Stunden). **Ein Tipp auf den Rahmen sendet ein ❤️** an dich (Pushover). Neues Layout-Element „Gruß mit Foto“.
- **Favoriten öfter zeigen** (aus, selten, öfter, oft): Fotos mit Stern in Immich werden im Dauerprogramm bevorzugt.

## [2.20.0]
### Neu
- **Sanfter Zoom** bei jedem Foto (Ken-Burns-Effekt), je Rahmen ein-/ausschaltbar.
- **Zwei Hochkant-Fotos nebeneinander:** Statt schwarzer Ränder setzt der Rahmen zwei Hochkant-Fotos zu einem Bild zusammen (je Rahmen schaltbar).
- **Notizen an den Rahmen:** Ein kurzer Text („Heute Abend Pizza 🍕“) erscheint für 1 Stunde bis 1 Woche am Rahmen; eigenes Layout-Element „Notiz“ (Lage, Größe, Farbe, Schrift). Eingabe unter Geräte → ⚙ → „Notiz an diesen Rahmen“. Schnittstelle: `PUT/GET /api/notizen/{rahmen}`.
- **Erinnerungen („An diesem Tag“):** Je Rahmen einstellbar (aus, selten, öfter, oft) mischt der Rahmen Fotos von heute in früheren Jahren in das Dauerprogramm. Das neue Layout-Element „Erinnerung“ zeigt dazu „Vor 5 Jahren“ (nur bei Fotos von heute ±3 Tage).
- **Geburtstags-Fotos:** Hat eine Person aus Immich heute Geburtstag (aus dem Geburtstags-Kalender eines freigegebenen Handys, z. B. „Omas Geburtstag“ → Person „Oma“, auch über Spitznamen), zeigt das Dauerprogramm mehr Fotos dieser Person. Je Rahmen abschaltbar.
- **Nachtruhe nach Sonnenstand:** Statt fester Uhrzeiten von Sonnenuntergang bis Sonnenaufgang am Wetter-Ort (täglich neu berechnet).
- **Mehr Meldungen (Pushover), je Art abschaltbar** (Einstellungen): Immich seit 10 Minuten nicht erreichbar (und wieder da), Speicherplatz unter 10 %, Handy-Kalender seit 3 Tagen nicht gesendet.
- **Monatsbrief:** Am 1. jedes Monats ein kurzer Rückblick per Pushover (neue Fotos in Immich, Erreichbarkeit und Ausfälle je Rahmen); „jetzt senden“ zum Ausprobieren in den Einstellungen.
### Geändert
- **Die Nachtruhe gilt am Rahmen jetzt immer**, auch während einer Show (bisher nur im Dauerprogramm; ein Rahmen mit dauerhafter Show wurde nie dunkel).

## [2.19.3]
### Neu
- **Datumsformat beim Foto-Aufnahmedatum:** Auswahl zwischen **ausgeschrieben** („4. Dezember 2025“) und **kurz** („04.12.2025“), je Rahmen im Editor („Datumsformat“ beim Element „Foto: Aufnahmedatum“). Standard bleibt ausgeschrieben.

## [2.19.2]
### Geändert
- Beim Element „Foto: Aufnahmedatum“ steht der Schalter **„Uhrzeit der Aufnahme mitanzeigen“** jetzt als eigene, gut sichtbare Zeile direkt unter dem Titel (vorher klein und grau darunter). Die Vorschau zeigt sofort „… · 14:30“.

## [2.19.1]
### Geändert
- **Dialoge haben eine feste Knopfleiste:** „Speichern“, „Schließen“, „Abbrechen“ usw. bleiben am unteren Rand des Dialogs sichtbar, auch wenn der Inhalt lang ist (am Handy musste man bisher bis ganz nach unten scrollen). Die Höhe berücksichtigt die Adressleiste von Safari.
- Auf der Vorschau der Anordnung lässt sich am Handy normal scrollen; nur die einzelnen Elemente lassen sich ziehen.

## [2.19.0]
### Geändert
- **Die Anzeige gilt jetzt pro Rahmen statt für alle:** Anordnung der Einblendungen (Uhrzeit, Datum, Foto-Datum und -Ort, Wetter, Termine, Geburtstage, Show-Titel), **Wetter-Ort**, **Kalender-Link** und die Auswahl der **Handys** stellst du für jeden Rahmen einzeln unter Geräte → ⚙ ein (Abschnitte „Anzeige auf diesem Rahmen“ und „Wetter und Termine auf diesem Rahmen“). Aus den allgemeinen Einstellungen ist die Anordnung entfernt; dort bleiben Wetter-Ort und Kalender-Link als Standard für Rahmen ohne eigene Angabe.
- Ein **neuer Rahmen startet mit den Standardwerten** (keine Vorlage). „Anordnung übernehmen von …“ kopiert die eines anderen Rahmens.
- **Neue Handys erscheinen zunächst auf keinem Rahmen.** Welche Handy-Termine ein Rahmen zeigt, wird am Rahmen angehakt. Beim Update bekommen bestehende Rahmen einmalig alle bis dahin bekannten Handys, damit sich nichts ändert; wer das nicht möchte (z. B. bei einem Rahmen an einem anderen Ort), nimmt den Haken unter ⚙ weg. Die Kürzel der Personen erscheinen, wenn auf diesem Rahmen mehrere Handys teilen.
- Wetter und Termine werden je Rahmen abgefragt und getrennt zwischengespeichert (`GET /api/rahmen/zusatz?ziel=`). Neue Felder am Gerät: `wetter`, `termin_quellen`, `kalender_url`.
- Die Kennung für „Anordnung/Termine neu laden“ am laufenden Rahmen berücksichtigt den Inhalt der Handy-Termine (auch zwei Änderungen in derselben Sekunde).

## [2.18.1]
### Behoben
- **Uhrzeit, Datum, Wetter, Termine und Geburtstage erscheinen jetzt auch während einer Show** (bisher nur im Dauerprogramm; ein Rahmen, der dauerhaft eine Show spielt, zeigte sie nie). Was in „Anzeige auf dem Rahmen“ eingeschaltet ist, wird gezeigt.
- Ein laufender Rahmen **lädt sich bei einer neuen Version immer neu** (bisher nur außerhalb von Shows, sodass er die neue Seite nie bekam). Eine laufende Show wird danach fortgesetzt.
- Ein im Editor gespeichertes Layout gilt jetzt vor der alten Geräte-Einstellung „Bildunterschrift“ (Foto-Ort und Foto-Datum ließen sich an einem solchen Rahmen nicht ausschalten).
- Der Show-Titel liegt standardmäßig etwas über der Uhr, damit beide sich nicht überdecken.

## [2.18.0]
### Neu
- **Einblendungen am Rahmen frei anordnen:** Uhrzeit, heutiges Datum, Datum und Ort des Fotos, Wetter, Termine, Geburtstage und der Show-Titel sind einzeln ein- und ausschaltbar, in Farbe und Schrift (Serif, Sans, Monospace) einstellbar, per Ziehen (oder Lage-Auswahl und Pfeiltasten) beliebig positionierbar und in der Größe (40–300 %) einstellbar. Eine Vorschau zeigt das Ergebnis. Eine allgemeine Anordnung (⚙ Einstellungen → „Anzeige auf dem Rahmen“) und je Rahmen eine eigene (⚙ am Gerät). Änderungen erscheinen am laufenden Rahmen sofort, ohne Neuladen. Die frühere Einstellung „Bildunterschrift: Datum/Uhrzeit/Ort“ gilt als Ausgangspunkt weiter.
- **Termine aus den Kalendern der Handys (iPhone-App):** Unter App-Einstellungen → „Meine Termine auf dem Rahmen zeigen“ gibt jede Person ihre Kalender frei (alles, was im iPhone-Kalender steht: iCloud, Google, Exchange, CalDAV). Es werden keine Links oder Passwörter gebraucht; wahlweise nur Uhrzeiten ohne Titel. Die App sendet beim Öffnen, bei Kalender-Änderungen und nach Möglichkeit im Hintergrund. In den Server-Einstellungen („Kalender von Handys“) sieht man die Handys und kann sie entfernen. Neue Schnittstellen: `PUT/GET /api/termine/telefon`, `DELETE /api/termine/telefon/{kennung}`.
- **Geburtstage:** Aus dem Geburtstags-Kalender des iPhones zeigt der Rahmen „🎂 Omas Geburtstag“ als eigene Zeile (eigenes Element), nicht in der Terminliste.
- **Termine nach Person:** Teilen mehrere Handys ihre Termine, steht vor jedem Termin ein farbiges Kürzel der Person (in der App unter „Kürzel am Rahmen“ und „Farbe am Rahmen“ einstellbar; am Rahmen unter „Termine“ abschaltbar). Bei nur einem Handy erscheint kein Kürzel.
- **Show-Titel** (der Name der Show, der beim Start kurz erscheint) ist jetzt ein verschiebbares, abschaltbares Element.
### Geändert
- Texte der Nutzer am Rahmen (Termine, Show-Namen) werden nicht mehr von der Oberflächen-Übersetzung verändert (`translate="no"`).
- Die Terminanzeige zeigt die **nächsten Termine des heutigen Tages** (bereits beendete verschwinden); ist heute nichts mehr, die von morgen. Termine aus dem Kalender-Link und von den Handys werden zusammengeführt. Die Zusatzanzeige wird alle 5 Minuten aufgefrischt.

## [2.17.0]
### Neu – Einrichtung so einfach wie möglich
- **Ein Befehl zum Installieren:** `curl -fsSL https://raw.githubusercontent.com/dirkvoss/immich-showcase/main/install.sh | bash` lädt alles selbst, erkennt ein auf demselben Rechner laufendes Immich (Docker-Netz, Machine-Learning-Dienst) und startet Immich Showcase. Am Ende steht ein fertiger Einrichtungs-Link mit QR-Code. Optionen: `--with-immich`, `--port`, `--bonjour`, `--no-open`, `--version`.
- **Einrichtungsassistent in 3 Schritten** (`/setup/`): Immich wird automatisch gefunden (sonst Adresse eintragen), Anmeldung mit dem Immich-Konto (legt einen eigenen Schlüssel ohne Lösch-Rechte an, das Passwort wird nicht gespeichert), dann die Frage „Wer soll Immich Showcase benutzen dürfen?“ in klarer Sprache (gemeinsame PIN, optional ohne PIN im Heimnetz, oder Immich-Konten). Am Ende: PIN, QR-Code für die iPhone-App und die Adresse für Tablets und Fernseher.
- **„Erste Schritte“** auf der Startseite hakt ab, was erledigt ist (Immich verbinden, Handy verbinden, Rahmen koppeln, erste Show senden).
- **Rahmen und Fernseher per Code oder QR-Code koppeln** (Code am Gerät, in der App eingeben oder mit der Kamera scannen); der Assistent „Gerät hinzufügen“ führt durch alle Schritte.
- **„Alles in Ordnung?“** prüft die Verbindung und meldet in Klartext, was zu tun ist; ein Hinweis auf neue Versionen ist enthalten.
- **Eigene Fotos senden pro Person:** Jede Person lädt mit ihrem eigenen Konto hoch (Upload-Schlüssel je Person).
- **Server im Netz ankündigen (Bonjour):** `docker-compose.bonjour.yml` bzw. `install.sh --bonjour`; die iPhone-App findet den Server im WLAN von selbst.
- **Vorlagen für NAS und Portainer:** `examples/unraid`, `synology`, `truenas`, `portainer`, `watchtower` (automatische Updates).
- **Illustrierte Anleitungen** mit Bildern und Text in einem Dokument (HTML/PDF): `docs/anleitung.md` (Erste Schritte) und `docs/anleitung-externer-rechner.md` (Immich Showcase auf einem anderen Rechner als Immich, z. B. Raspberry Pi).
- **iPhone-App:** Server im WLAN per Bonjour finden, Verbindung prüfen mit klaren Fehlermeldungen, QR-Kopplung von Tablets und Fernsehern, PIN mit Face ID, englische Oberfläche (Texte als String-Kataloge).
### Geändert
- Es gibt keine Beispielgeräte „LG TV“ und „Shield“ mehr in einer frischen Installation (`.env.example`, `docker-compose.yml`); Geräte legt man in der App an. Bestehende Installationen behalten ihre Geräte.
### Behoben
- „Alles in Ordnung?“ meldete „Motivsuche nicht erreichbar“, obwohl ohne Datenbankzugang (z. B. wenn Immich Showcase auf einem anderen Rechner läuft) die Motivsuche über Immich selbst läuft und der Machine-Learning-Dienst nicht gebraucht wird. Die Prüfung läuft jetzt nur noch mit Datenbankzugang.

## [2.16.0]
### Neu
- **Rahmen an einem anderen Ort (Eltern, Ferienhaus):** Ein Tablet außerhalb des Heimnetzes bekommt beim Koppeln (Code am Rahmen, in der App „Gerät koppeln“) einen **eigenen Zugang**, der nur die Rahmen-Seiten öffnet (nicht die App, nicht die Fotoübersicht, nicht die Home-Assistant-Schnittstellen) und nur für dieses Gerät gilt. Er wird bei jeder Nutzung erneuert und läuft praktisch nie ab; je Gerät widerrufbar (⚙ → *Wartung und Zugang von außen* → *Zugang widerrufen*), ohne die PIN zu ändern. Anleitung: README, Abschnitt „Rahmen an einem anderen Ort“.
- **Fernsteuerung je Rahmen** in der App: *Seite neu laden* und *Anzeige neu starten* (mit Fully). Dazu eine eigene **Meldezeit** (Pushover) je Rahmen, wenn er nicht erreichbar ist.
- **„Alles in Ordnung?“** (Konto-Menü, in der iPhone-App unter App-Einstellungen): prüft Immich, Schlüssel, Bildsuche, Upload, jeden Rahmen und Fernseher (online, Akku, hängendes Bild), Speicherplatz und Version und sagt, was zu tun ist. Ein farbiger Punkt am Konto-Symbol zeigt Warnungen an.
- **Willkommens-Rundgang** beim ersten Start (einmal je Gerät; unter *Hilfe* wiederholbar), auch in der Demo.
- **iPhone-App:** Demo-Modus, Suche nach Servern im WLAN (auch in einem anderen Netz und über bekannte Namen), „Zuletzt verbunden“, Rückweg von der Anmeldeseite, Anleitung „So kommst du an den QR-Code“, „Demo ansehen“ in den Einstellungen, QR-Code am Ende der Server-Einrichtung.
### Behoben
- Die Rahmen-Seite lädt sich bei fehlendem Netz nicht mehr neu (das zeigte eine Fehlerseite); das Dauerprogramm läuft mit den geladenen Fotos weiter und verbindet sich von selbst wieder.

## [2.15.0]
### Neu
- **iPhone-App – Demo-Modus:** „Demo ausprobieren (ohne Server)“ zeigt die echte Oberfläche mit erfundenen, gemalten Beispielfotos; es wird nichts gesendet. Ein Banner führt jederzeit zum Verbinden des eigenen Servers. Auch für die Prüfung im App Store gedacht.
- **iPhone-App – Server finden:** Die App sucht Showcase-Server im eigenen WLAN (/24, Ports 8090 und 80) und probiert bekannte Namen (`showcase`, `immich-showcase`, `bilderrahmen`, auch `.local`). Treffer erscheinen sofort zum Antippen. Für ein anderes Netz gibt es ein Eingabefeld (nur private Adressbereiche).
- **Einrichtungsassistent:** Am Ende zeigt `/setup/` einen QR-Code „iPhone-App verbinden“.

## [2.14.3]
### Geändert
- **Ein normaler Browser genügt:** Die Anleitung stellt den einfachen Weg (Chrome bzw. Safari, „Zum Startbildschirm hinzufügen“) an die erste Stelle; Fully Kiosk wird als optionales Extra beschrieben (Bildschirm nachts wirklich aus, Akkustand). Ein Hinweis im Geräte-Dialog sagt das auch in der Oberfläche.
- Die Rahmen- und Fernseher-Seite bittet den Browser jetzt, den Bildschirm wach zu halten (Wake Lock), wo er das unterstützt.

## [2.14.2]
### Neu
- **Hilfe und Urheberangabe:** Der Hilfe-Dialog hat einen Abschnitt „Eigene Fotos senden“ und „Über Immich Showcase“ (Entwickelt von Dirk Voß, GitHub, Hinweis auf das unabhängige Projekt); die Versionszeile nennt den Urheber. In der iPhone-App gibt es unter *App-Einstellungen* „Hilfe anzeigen“, einen Link zur Anleitung und „Entwickelt von“.
- **Hinweis beim Senden eigener Fotos:** Beim ersten Mal erklärt ein Fenster, dass die Fotos in Immich gespeichert werden und mit der Kamera aufgenommene Fotos danach nur dort (nicht in der Fotos-Mediathek) liegen; mit „Nicht mehr zeigen“.
### Behoben
- „📡 Geräte“, „❓ Hilfe“ und „📤 Meine Fotos“ waren auf schmalen Bildschirmen (Handy) unsichtbar; dort erscheinen jetzt die Symbole.

## [2.14.1]
### Behoben
- **Upload-Album:** Der Server legte beim Hochladen nach jedem Neustart ein neues Album „Showcase-Uploads“ an, weil er das vorhandene nicht fand (Immich nennt in der Albumliste keinen Besitzer). Es wird jetzt wiedergefunden; bei mehreren gleichnamigen gilt das mit den meisten Fotos.

## [2.14.0]
### Neu
- **iPhone-App – Siri und Kurzbefehle:** „Fotos auf den Rahmen zeigen“ (Suchtext wie in der Web-App), „Show beenden“, „Zur vorherigen Show zurück“.
- **iPhone-App – Teilen aus der Fotos-App:** Fotos wählen, Teilen, „Showcase Immich“: Die Fotos werden hochgeladen und laufen auf dem Rahmen (bei mehreren Rahmen mit Auswahl). Benötigt `RAHMEN_IMMICH_UPLOAD_KEY` am Server.
- **iPhone-App – Widget „Bilderrahmen“:** zeigt, was auf dem Rahmen läuft, mit den Knöpfen „Beenden“ und „Zurück“.
### Geändert
- Die Oberfläche hält den Zustand des Rahmens aktuell (alle 10 Sekunden und beim Zurückkehren auf die Seite bzw. in die App), auch wenn er von Siri, dem Widget oder einem anderen Gerät geändert wurde.

## [2.13.2]
### Behoben
- **Hochladen aus der iPhone-Mediathek:** Fotos in Originalgröße (HEIC) wurden von Immich schon beim Senden abgelehnt und der Fehler als „nicht erreichbar“ gemeldet. Die Dateiauswahl bietet jetzt JPEG, PNG und WebP an, dadurch wandelt iOS Mediatheksfotos selbst in JPEG um. Lehnt Immich ein Foto ab, liest der Server jetzt auch dann seine Antwort, wenn die Verbindung beim Senden abbricht, und meldet den echten Grund.

## [2.13.1]
### Geändert
- Beim Hochladen eigener Fotos protokolliert der Server jetzt genau, was Immich antwortet bzw. woran die Verbindung scheitert (ohne Geheimnisse), und die Fehlermeldung nennt die Fehlerart. Das erleichtert die Suche, wenn ein Upload fehlschlägt.

## [2.13.0]
### Neu
- **Eigene Fotos vom Handy senden** („📤 Meine Fotos“ oben, auch in der iPhone-App): Fotos aus der Mediathek werden hochgeladen und zur Auswahl gelegt – so lassen sie sich mit Fotos aus Immich mischen und gemeinsam auf den Rahmen schicken. Der Nutzer braucht dafür kein Immich-Konto. Die Fotos landen in Immich im Album „Showcase-Uploads“ (`RAHMEN_WEB_UPLOAD_ALBUM`). Nur mit einem eigenen Immich-Schlüssel (`RAHMEN_IMMICH_UPLOAD_KEY`, Rechte `asset.upload` und Albumverwaltung, keine Löschrechte); ohne ihn bleibt die Funktion aus. Nur Fotos (JPEG, PNG, HEIC, WebP), höchstens 40 MB je Foto (`RAHMEN_WEB_UPLOAD_MAX_MB`) und 300 pro Stunde.

## [2.12.1]
### Geändert
- **iPhone-App:** Die Server-Adresse darf ohne `http://`/`https://` eingegeben werden; die App probiert beide Varianten durch und nennt bei einem Fehler die genaue Ursache.
- **iPhone-App:** Der Startbildschirm weist jetzt deutlich darauf hin, dass die App einen eigenen Immich-Showcase-Server braucht und sich nicht direkt mit Immich verbindet.
- iPhone-App trägt jetzt die Version 1.0 (Versions- und Build-Nummer kommen aus den Projekteinstellungen).

## [2.12.0]
### Neu
- **iPhone-App „Showcase Immich“** (Ordner `ios/`, SwiftUI + Web-Ansicht): Einrichtung per Adresse oder QR-Code, Face-ID-Sperre, Fehlerseite ohne Verbindung; TestFlight-Skripte. In der Web-App gibt es dafür unter *Einstellungen → iPhone-App verbinden* einen QR-Code und in der App *Konto → App-Einstellungen*.
- **Neues App-Symbol** (goldener Bilderrahmen mit Berglandschaft) für die iPhone-App und die Web-App (Startbildschirm-Symbol).

## [2.11.2]
### Behoben
- Nach dem Start einer Show auf dem **Fernseher** bleibt die Auswahl nicht mehr stehen: die Leiste „n Fotos ausgewählt“ geht zu wie beim Rahmen (mit „↶ Rückgängig“ zurückholbar).

## [2.11.1]
### Geändert
- Der Assistent „Gerät hinzufügen“ ist bei **Fernsehern** jetzt auf den einfachen Weg ausgerichtet: kurze Adresse am Fernseher eingeben, den angezeigten Code gleich im Assistenten eintragen („Koppeln“). Link und QR-Code (für Tablets mit Kamera) sind eingeklappt; bei Tablets und Rahmen bleibt der QR-Code oben und das Codefeld darunter.

## [2.11.0]
### Neu
- **Fernseher ohne lange Adresse:** Fernseher-Browser (LG, Samsung, Android TV, Fire TV, Chromecast …) werden von der Startadresse des Servers automatisch auf die Fernseher-Seite geleitet (`/?ui=1` zeigt die App). Am Fernseher genügt die kurze Adresse, z. B. `192.168.1.20:8090`; die Rolle wird per Code vom Handy zugewiesen.
- `./install.sh --port 80`: Immich Showcase auf Port 80 – dann genügt am Fernseher die reine IP. `install.sh` trägt die erkannte Adresse (`RAHMEN_WEB_TV_URL`) selbst in die `.env` ein.
- Der Assistent „Gerät hinzufügen“ zeigt die **Adresse des Servers als bearbeitbares Feld** (mit „Als Standard speichern“ und Hinweis bei `localhost`) und die kurze Fernseher-Adresse.
- **Für Fortgeschrittene – Seite per ADB direkt am Fernseher öffnen (nur Android TV, Shield, Fire TV; die Entwickler-Funktion ist bei den meisten Geräten aus):** Der Server öffnet die Fernseher-Seite per ADB im Browser des Fernsehers – ohne Tippen und ohne Home Assistant. Voraussetzungen: „Netzwerk-Debugging“ am Fernseher und Netzverbindung vom Server zum Fernseher; beim ersten Mal bestätigt man am Fernseher einmal „Immer erlauben“. Das Docker-Image enthält dafür `adb`.

## [2.10.3]
### Geändert
- Meldet sich ein Tablet selbst über die Fully-Schnittstelle, werden **Adresse und Passwort der Fully-Fernbedienung automatisch verworfen** (beim Speichern, mit Hinweis) und nicht mehr abgefragt – kein Dauer-Versuch mehr, ein Tablet zu erreichen, das der Server (z. B. aus der DMZ) gar nicht erreichen darf. Der Knopf „Bildschirm aus/an“ per Fernsteuerung entfällt dann in der Geräteliste.

## [2.10.2]
### Behoben
- Das Dauerprogramm lieferte keine Fotos, wenn **mehrere Alben** eingestellt waren (`RAHMEN_WEB_RAHMEN_ALBEN=a,b`): Immich verknüpft mehrere `albumIds` mit UND (nur Fotos, die in allen Alben liegen). Jetzt wird je Album gefragt und gemischt – auch im Marker-Modus (`#nurrahmen` an mehreren Alben, dort jetzt je Album mit eigener Position).

## [2.10.1]
### Behoben
- Im Banner gibt es wieder einen Knopf **„■ Show beenden“** (zurück ins Dauerprogramm), solange eine Show läuft – er war beim neuen Aussehen versehentlich versteckt, sodass sich eine laufende Show nur mit „Zurück zu …“ beenden ließ.
- „Verbindung testen“ (Fully): Die Meldung lag hinter dem Dialog und war unsichtbar; sie steht jetzt auch direkt unter dem Knopf. Falsches Passwort und nicht erreichbares Tablet werden klar unterschieden (vorher hätte ein falsches Passwort „Verbunden“ gezeigt).

## [2.10.0]
### Neu
- **Assistent „Gerät hinzufügen“:** Name und Art eingeben, dann zeigt die App Adresse und QR-Code zum Verbinden und wartet live, bis sich das Tablet/der Fernseher meldet („✓ Verbunden“). Den Verbindungs-Link gibt es auch später im ⚙ des Geräts. Der Kopplungs-Code bleibt als Alternative.
- **Einstellungen in der App** (Konto-Menü → ⚙ Einstellungen) statt in der `.env`: Vorgaben für alle Rahmen (Sekunden, Hintergrund, Bildunterschrift, Offline-Alarm), Wetter-Ort (Ortssuche) und Kalender-Link mit Test, Pushover-Zugang mit Test-Nachricht, Spitznamen für Personen, Adresse der Fernseher-Seite. Die Datei `rahmen_web_einstellungen.json` hat Vorrang vor den Umgebungsvariablen; Geheimnisse (Kalender-Link, Pushover) werden nie ausgeliefert. Anmeldeart, vertrautes Netz, Proxys und Immich-Schlüssel bleiben bewusst in der Umgebung.
- **Tablet-Steuerung ohne Adresse und Passwort:** Läuft die Rahmen-Seite in Fully Kiosk mit aktiver JavaScript-Schnittstelle, schaltet das Tablet den Bildschirm zur Nachtruhe selbst aus und meldet Akku und Ladezustand (inkl. Warnung bei niedrigem Akku). Wer das nicht nutzen kann, trägt wie bisher Adresse und Passwort der Fully-Fernbedienung ein – die App schlägt die erkannte Adresse des Tablets vor. Der ganze Bereich ist „optional“ und eingeklappt; ohne ihn läuft alles wie gehabt.
- **Bildunterschrift mit Uhrzeit** (Datum · Uhrzeit · Ort).
- Der Banner nennt bei genau einem Rahmen dessen Namen („Läuft gerade auf Bilderrahmen“); die Hilfe erklärt die Geräte-Verwaltung.

## [2.9.0]
### Neu
- **Zeitplan mit Eingabemaske:** Zeitfenster mit Wochentagen, Uhrzeit von–bis und eigenen Quellen (statt Textfeld); bestehende Zeitpläne werden gelesen und wieder geschrieben.
- **Shows auf die Rahmen verteilen:** Unter „Meine Shows“ (ab zwei Rahmen) wählst du in einem Dialog für jeden Rahmen eine eigene Show, „Normales Programm“ oder „Nicht ändern“.
- Im Geräte-Dialog steht die **Kennung** des Geräts (für Home Assistant); das HA-Beispiel erklärt, wie weitere Geräte ergänzt werden.

## [2.8.0]
### Neu
- **Geräte-Verwaltung in der App:** Unter „Geräte“ (⚙ je Gerät, „＋ Gerät hinzufügen“) legst du Bilderrahmen und Fernseher an, stellst sie ein und löschst sie – ohne Docker-Konfiguration und ohne Neustart. Je Rahmen: Name, Sekunden pro Foto, Hintergrund, Bildunterschrift, Nachtruhe, Dauerprogramm (Quellen mit Gewicht), Zeitplan und Fully Kiosk (mit Verbindungstest). Fernseher: Name und Hintergrund.
- Die Geräteliste liegt in `rahmen_web_geraete.json` im Datenordner (`RAHMEN_WEB_GERAETE_FILE`). Solange es die Datei nicht gibt, gelten wie bisher die Umgebungsvariablen (`RAHMEN_WEB_RAHMEN_ZIELE`, `RAHMEN_WEB_TV_ZIELE`, `RAHMEN_WEB_FULLY_<ID>`); die erste Änderung in der App legt die Datei an, danach hat sie Vorrang. Leere Einstellungen eines Geräts bedeuten: allgemeiner Wert der Installation.
- Die Rahmen-Seite (`/tv/`) holt ihre Einstellungen je Gerät (`/api/config?ziel=…`).
- Schnittstellen: `GET /api/verwaltung`, `POST /api/verwaltung/geraete`, `PUT/DELETE /api/verwaltung/geraete/<id>`, `POST /api/verwaltung/fully-test`. Das Fully-Passwort wird nie ausgeliefert.
### Geändert
- Die Überwachungen (Rahmen offline, Fully-Nachtruhe/Akku) laufen immer und berücksichtigen neu angelegte Geräte sofort.

## [2.7.0]
### Neu
- **Mehrere Bilderrahmen:** Jeder Rahmen (`RAHMEN_WEB_RAHMEN_ZIELE`) hat jetzt seinen eigenen Zustand – laufende Show, „Zurück“, Reihenfolge. Beim Senden wählst du in der Auswahlleiste per Häkchen einen oder mehrere Rahmen (dieselbe Show an mehrere, oder nacheinander verschiedene Shows an verschiedene Rahmen). Im Banner oben wählst du, welchen Rahmen „Normal“, „Zurück“ und die Reihenfolge betreffen.
- Gespeicherte Shows („Meine Shows“) laufen auf den gewählten Rahmen.
- Schnittstellen: `/api/anzeigen` und `/api/shows/<id>/anzeigen` nehmen `ziele` (Liste), `/api/status`, `/api/zurueck`, `/api/normal` und `/api/reihenfolge` nehmen `ziel`. Ohne Angabe gilt der erste Rahmen – bestehende Einrichtungen und Automationen ändern sich nicht.

## [2.6.2]
### Behoben
- Am Rechner wurde die Auswahl-Seitenleiste rechts abgeschnitten (z. B. „merken“); Inhalt bleibt jetzt in der Leistenbreite, die Zeile „Name der Show“ darf umbrechen.
- Die Vorlagen für „Problem melden“ und „Wunsch“ auf GitHub waren kein gültiges YAML und wurden nicht geladen; jetzt zweisprachig und gültig.
### Intern
- README mit animiertem Ablauf-GIF.
- `tools/check.sh` prüft vor dem Push, was die CI prüft (Quelltext-Warnungen, Shell-Skripte, YAML, Compose, Tests).
- CI robuster: Docker-Hub-Spiegel gegen die Download-Begrenzung, Container-Test wartet auf das Protokoll, Warnungen im Quelltext lassen den Bau fehlschlagen.

## [2.6.1]
### Behoben
- `install.sh` fand den Einrichtungs-Code im Protokoll nicht (falsches Suchmuster).
- Kalender-Termine: ungültiges Escape-Zeichen im Quelltext (Python-Warnung) korrigiert.
- Der Container-Test in der CI wartet jetzt kurz auf das Protokoll (Version 2.6.0 wurde deshalb nicht veröffentlicht, der Inhalt ist in 2.6.1 enthalten).

## [2.6.0]
### Neu
- **Neues Aussehen („Galerie“):** warmes Papier (hell) bzw. Nachtgalerie (dunkel) mit Messing-Akzent, Serifen-Schrift für Datums-Überschriften, Show-Namen und Fenster-Titel, einheitliche Linien-Symbole statt Emojis (`static/ikonen.js`).
- **Fotos zuerst am Handy:** kompakter Kopf (Geräte, Hilfe, Konto-Menü als Symbole), schmale Rahmen-Statusleiste, Filter als scrollbare Chip-Zeile, größere Foto-Kacheln mit Messing-Rahmen für die Auswahl, neu geordnete Auswahlleiste.
- **Am Rechner:** Fotos links, Auswahl als feste Seitenleiste rechts; Tastenkürzel „/“ (Suche) und „Esc“ (Fenster schließen).
### Behoben
- Der „Zurück zu …“-Knopf im Rahmen-Status erscheint nur noch, solange eine Show läuft, und ist übersetzt (er wurde am Handy abgeschnitten).

## [2.5.1]
### Behoben
- **Eine laufende Show wird nach einem Neustart oder Update des Servers nicht mehr vergessen.** Bisher lief sie am Gerät weiter, aber die App zeigte „Normales Programm“, und das Stoppen bzw. Neu-Laden der Seite funktionierte nicht richtig. Der Zustand wird jetzt gespeichert (`rahmen_web_aktiv.json` neben den Shows).

## [2.5.0]
### Neu
- **Home Assistant:** `GET /api/ha/status`, `POST /api/ha/steuer|show|bildschirm` und eine fertige Vorlage `examples/home-assistant/immich_showcase.yaml` (Sensoren, Online-Sensor, Befehle).
- **Fully Kiosk fernsteuern:** Bildschirm zur Nachtruhe wirklich aus/an (`RAHMEN_WEB_FULLY_<KENNUNG>`), per App an/aus, Akkustand in der Geräte-Ansicht und Pushover-Meldung bei leerem Akku.
- **Programm nach Person:** neue Quelle `person=<Name oder Spitzname>` für das Dauerprogramm.

## [2.4.0]
### Neu
- **Geräte per Code koppeln:** `/tv/` ohne Kennung zeigt einen 6-stelligen Code und einen QR-Code; in der App unter „📡 Geräte“ eingeben (oder scannen) und zuweisen – das Gerät merkt sich seine Rolle. Hinweis: `/tv/` ohne `?ziel=` zeigt jetzt die Kopplung statt automatisch den Fernseher „lg“.
- **Geräte-Ansicht** in der App: online/offline, was läuft, Alter des Bildes, zuletzt gesehen.
- **Mehrere Rahmen mit eigenem Programm** (`RAHMEN_WEB_RAHMEN_QUELLEN_<KENNUNG>`) und **Zeitplan** (`RAHMEN_WEB_RAHMEN_ZEITPLAN`, auch je Rahmen; Wochentage, Uhrzeiten, über Mitternacht).
- **„Heute vor Jahren“:** neue Quelle `heute` / `heuteN` für das Dauerprogramm.
- **Zusatzanzeige am Rahmen:** Wetter (open-meteo.com) und Termine (iCalendar-Link), nur wenn eingerichtet.
- **Schnellinstallation:** `install.sh` und `examples/immich-stack/` (Immich und Immich Showcase zusammen).
### Geändert
- Der Integrationstest in der CI lädt Images mit Wiederholung (Registry-Begrenzung).

## [2.3.0]
### Neu
- **Version immer sichtbar:** klein links in der Kopfzeile der Hauptseite (z. B. „v2.3.0“).
- **Problem melden / Wunsch äußern** im Hilfe-Fenster (öffnet GitHub mit eingetragener Version); Anleitung dazu im README; Link zu den Diskussionen für Fragen.

## [2.2.1]
### Neu
- Die **Version** steht jetzt sichtbar in der App: klein unter der Anmeldeseite und am Ende der Hauptseite (zusätzlich wie bisher im Hilfe-Fenster).

## [2.2.0]
### Neu
- **Englische Dokumentation:** `README.md` ist jetzt eine ausführliche Installations- und Bedienungsanleitung auf Englisch, `README.de.md` die deutsche Fassung; Screenshots mit dem neuen Namen.
- Marker-Alben erkennen auch englische Marker (`#frame`, `#onlyframe`).
### Behoben
- Der längere Name „Immich Showcase“ wird im Kopf der App nicht mehr abgeschnitten (bricht um).

## [2.1.0]
### Neu
- **Marker-Alben für den Bilderrahmen** (`RAHMEN_WEB_RAHMEN_MARKER=1`): Alben mit `#…rahmen…` in der Beschreibung kommen von selbst zum Dauerprogramm, `#…nurrahmen…` zeigt nur diese Alben (in der gewählten Reihenfolge, laufend weiter). Damit kann der Eigenplayer den bisherigen Rahmen-Kiosk ersetzen.

## [2.0.0]
### Geändert (Umbenennung – bitte lesen)
- Das Projekt heißt jetzt **Immich Showcase** (früher „Guckloch“), mit neuem Symbol.
- **Image:** `ghcr.io/dirkvoss/immich-showcase` (statt `…/guckloch`).
- **Umgebungsvariablen:** Präfix `SHOWCASE_` statt `GUCKLOCH_` (`SHOWCASE_VERSION`, `SHOWCASE_PIN`, …). `deploy/deploy.sh` liest `GUCKLOCH_*` aus `deploy.conf` und `.env` weiterhin; die Compose-Datei muss angepasst werden (siehe README/Skript).
- CI schneller: Image-Bau parallel zum Integrationstest, Versions-Tags wiederholen den Integrationstest nicht.

## [2.27.0]
### Neu
- **Geräte per Code koppeln:** `/tv/` ohne Kennung zeigt einen 6-stelligen Code und einen QR-Code; in der App unter „📡 Geräte“ eingeben (oder scannen) und zuweisen – das Gerät merkt sich seine Rolle. Hinweis: `/tv/` ohne `?ziel=` zeigt jetzt die Kopplung statt automatisch den Fernseher „lg“.
- **Geräte-Ansicht** in der App: online/offline, was läuft, Alter des Bildes, zuletzt gesehen.
- **Mehrere Rahmen mit eigenem Programm** (`RAHMEN_WEB_RAHMEN_QUELLEN_<KENNUNG>`) und **Zeitplan** (`RAHMEN_WEB_RAHMEN_ZEITPLAN`, auch je Rahmen; Wochentage, Uhrzeiten, über Mitternacht).
- **„Heute vor Jahren“:** neue Quelle `heute` / `heuteN` für das Dauerprogramm.
- **Zusatzanzeige am Rahmen:** Wetter (open-meteo.com) und Termine (iCalendar-Link), nur wenn eingerichtet.
- **Schnellinstallation:** `install.sh` und `examples/immich-stack/` (Immich und Immich Showcase zusammen).
### Geändert
- Der Integrationstest in der CI lädt Images mit Wiederholung (Registry-Begrenzung).

### Neu (älter)
- **Einrichtungsassistent** unter `/setup/` (Code aus dem Protokoll): verbindet mit Immich, legt den Schlüssel selbst an, setzt die PIN, startet neu.
- **Anmeldung mit eigenem Immich-API-Schlüssel** (für Konten ohne Passwort, z. B. SSO).
- **Rahmen-Anzeigeoptionen:** unscharfer Hintergrund oder Zuschnitt statt schwarzer Balken, Datum/Ort als Bildunterschrift, Nachtruhe (`RAHMEN_WEB_RAHMEN_FUELLUNG`, `_ANZEIGE`, `_NACHT`).
- Die Motivsuche fällt ohne Immich Machine Learning auf eine Suche ohne Motiv zurück.
- **Sprachen:** Oberfläche und Suche jetzt auch auf Spanisch, Französisch und Niederländisch (Umschalter im Hilfe-Fenster); Englisch für die Suche.
- **Hilfe-Fenster** mit Kurzanleitung und **Support-Paket** (Diagnose ohne Geheimnisse) zum Herunterladen für Fehlerberichte.
- Test gegen echte Immich-Versionen in GitHub Actions; Container-Image für amd64 **und arm64** (Raspberry Pi, Synology).
### Geändert
- Der Immich-Schlüssel braucht zusätzlich `user.read`.
- Kopfzeile bei Immich-Anmeldung: Name und „Abmelden“ in eigener Zeile.

## [1.3.0]
- Keine Musik mehr im Lieferumfang: eigene Ordner (mp3/ogg/m4a), Titel aus Dateimarken, optional Hochladen/Entfernen in der App (`RAHMEN_WEB_MUSIK_UPLOAD`).
- **Prüfungen nach dem Deploy** (`postdeploy.py`) mit automatischem Rückweg in `deploy.sh`.

## [1.2.0]
- Dauerprogramm des Rahmens mit gewichteten Quellen, ohne Wiederholer, Überwachung (Selbstheilung, Pushover).
- Komplette Docker-Compose-Installation, alle Einstellungen über `.env`, Rauchtest in der CI, `:latest`-Tag.

## [1.1.0]
- Anmeldung mit dem Immich-Konto (`RAHMEN_WEB_AUTH`), je Person eigener Schlüssel, Shows und Zähler.

## [1.0.0]
- Erste Veröffentlichung: Suche in ganzen Sätzen, Auswahl, Shows, Fernseher, Bilderrahmen, Browser-Diashow, Deutsch/Englisch.
