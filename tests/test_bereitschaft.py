"""Tablet-Check: was fuer den Dauerbetrieb als Rahmen in Ordnung ist, Ladegrenze der Android-App und Verbindungsweg im Herzschlag."""
import json
import urllib.parse

H = {"X-Rahmen": "1"}
LAN = {"RAHMEN_WEB_LAN": "192.168.1.0/24", "RAHMEN_WEB_PROXIES": "192.168.1.200", "RAHMEN_WEB_RAHMEN_ZIELE": "rahmen=Bilderrahmen", "RAHMEN_WEB_RAHMEN_SEK": "8"}
GUT = {"launchOnBoot": True, "keepScreenOn": True, "startURL": "http://192.168.1.20:8090/tv/?ziel=rahmen", "microphoneAccess": False}
INFO = {"batteryLevel": 55, "isPlugged": True, "isLicensed": True, "keyguardLocked": False, "screenOn": True}


class FakeTablet:
    def __init__(self, pw="geheim", ist=None, info=None):
        self.pw, self.ist, self.info = pw, dict(GUT if ist is None else ist), dict(INFO if info is None else info)

    def __call__(self, url, timeout=6):
        q = {k: v[0] for k, v in urllib.parse.parse_qs(urllib.parse.urlparse(url).query).items()}
        if q.get("password") != self.pw:
            return json.dumps({"status": "Error", "statustext": "Please login"})
        return json.dumps(self.info if q["cmd"] == "deviceInfo" else self.ist)


def pruefen(c, tablet, monkeypatch, w, **extra):
    monkeypatch.setattr(w, "fully_get", tablet)
    r = c.post("/api/verwaltung/bereitschaft", json={"id": "rahmen", "fully_host": "192.168.1.55", "fully_pw": "geheim", **extra}, headers=H)
    assert r.status_code == 200, r.text
    d = r.json()
    return d, {x["id"]: x for x in d["punkte"]}


def test_alles_in_ordnung(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    c = client()
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1")
    d, p = pruefen(c, FakeTablet(), monkeypatch, w)
    assert d["ok"] is True
    for k in ("online", "fully", "autostart", "bildschirm_an", "adresse", "mikrofon", "plus"):
        assert p[k]["status"] == "ok", k
    assert p["akku"]["wert"] == "55:1" and p["akku"]["status"] == "ok"
    assert p["sperrbildschirm"]["status"] == "info" and p["ueberlagerung"]["status"] == "info" and p["neustarttest"]["status"] == "info"


def test_abweichungen_werden_gemeldet(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    c = client()
    schlecht = dict(GUT, launchOnBoot=False, keepScreenOn=False, microphoneAccess=True, startURL="https://example.org")
    d, p = pruefen(c, FakeTablet(ist=schlecht, info=dict(INFO, isPlugged=False, batteryLevel=15, isLicensed=False)), monkeypatch, w)
    assert d["ok"] is False
    assert p["autostart"]["status"] == "fehler" and p["bildschirm_an"]["status"] == "fehler"
    assert p["adresse"]["status"] == "warnung" and p["mikrofon"]["status"] == "warnung" and p["plus"]["status"] == "warnung"
    assert p["akku"]["status"] == "warnung" and p["akku"]["wert"] == "15:0"
    assert p["online"]["status"] == "fehler"                                  # das Geraet hat sich noch nie gemeldet


def test_mikrofon_zaehlt_nicht_wenn_die_fully_version_die_einstellung_nicht_kennt(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    ohne = {k: v for k, v in GUT.items() if k != "microphoneAccess"}
    _, p = pruefen(client(), FakeTablet(ist=ohne), monkeypatch, w)
    assert p["mikrofon"]["status"] == "ok"


def test_sperrbildschirm_wird_gemeldet_wenn_der_bildschirm_an_ist(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    _, p = pruefen(client(), FakeTablet(info=dict(INFO, keyguardLocked=True)), monkeypatch, w)
    assert p["sperrbildschirm"]["status"] == "warnung"


def test_tablet_nicht_erreichbar_oder_falsches_passwort(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    c = client()

    def weg(url, timeout=6):
        raise OSError("Zeitüberschreitung")
    monkeypatch.setattr(w, "fully_get", weg)
    d = c.post("/api/verwaltung/bereitschaft", json={"id": "rahmen", "fully_host": "192.168.1.55", "fully_pw": "geheim"}, headers=H).json()
    assert {x["id"]: x["status"] for x in d["punkte"]}["fully"] == "fehler" and d["ok"] is False
    _, p = pruefen(c, FakeTablet(pw="anders"), monkeypatch, w)
    assert p["fully"]["status"] == "fehler"


def test_ohne_adresse_nur_der_herzschlag(app_laden):
    w, client, _ = app_laden(**LAN)
    c = client()
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&fj=1&ak=61&pl=1")
    d = c.post("/api/verwaltung/bereitschaft", json={"id": "rahmen"}, headers=H).json()
    p = {x["id"]: x for x in d["punkte"]}
    assert p["fully"]["status"] == "info" and p["online"]["status"] == "ok" and p["akku"]["wert"] == "61:1"
    assert "autostart" not in p                                               # ohne Zugriff auf Fully keine Einstellungen raten


def test_ladegrenze_der_app_und_verbindungsweg(app_laden):
    w, client, _ = app_laden(**LAN)
    app = client()
    app.get("/api/tv/abfrage?ziel=rahmen&seq=-1&lg=an:20:80")
    p = {x["id"]: x for x in app.post("/api/verwaltung/bereitschaft", json={"id": "rahmen"}, headers=H).json()["punkte"]}
    assert p["ladegrenze"] == {"id": "ladegrenze", "status": "ok", "wert": "an:20:80"}
    assert p["weg"]["wert"] == "heimnetz"
    app.get("/api/tv/abfrage?ziel=rahmen&seq=-1&lg=fehler:20:80")
    assert {x["id"]: x for x in app.post("/api/verwaltung/bereitschaft", json={"id": "rahmen"}, headers=H).json()["punkte"]}["ladegrenze"]["status"] == "fehler"
    app.get("/api/tv/abfrage?ziel=rahmen&seq=-1&lg=quatsch")                  # ungueltige Werte werden ignoriert
    assert {x["id"]: x for x in app.post("/api/verwaltung/bereitschaft", json={"id": "rahmen"}, headers=H).json()["punkte"]}["ladegrenze"]["status"] == "info"


def test_verbindungsweg_aussen(app_laden):
    w, client, _ = app_laden(**LAN)
    tablet = client("203.0.113.9")                                            # von aussen, ohne Zugang: 401, aber der Herzschlag zaehlt nur mit Zugang
    assert tablet.get("/api/tv/abfrage?ziel=rahmen&seq=-1").status_code == 401
    w.TV["rahmen"]["extern"] = True
    w.TV["rahmen"]["hb"] = __import__("time").time()
    p = {x["id"]: x for x in client().post("/api/verwaltung/bereitschaft", json={"id": "rahmen"}, headers=H).json()["punkte"]}
    assert p["weg"]["wert"] == "aussen"


def test_unbekanntes_geraet_und_ohne_anmeldung(app_laden):
    w, client, _ = app_laden(**LAN)
    assert client().post("/api/verwaltung/bereitschaft", json={"id": "gibtsnicht"}, headers=H).status_code == 404
    assert client("203.0.113.9").post("/api/verwaltung/bereitschaft", json={"id": "rahmen"}, headers=H).status_code == 401


def test_app_version_und_update_hinweis(app_laden):
    w, client, _ = app_laden(**LAN)
    c = client()
    gelesen = lambda: {x["id"]: x for x in c.post("/api/verwaltung/bereitschaft", json={"id": "rahmen"}, headers=H).json()["punkte"]}
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1")
    assert "app" not in gelesen()                                             # Browser oder Fully: keine App-Version
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1", headers={"User-Agent": "Mozilla/5.0 FramesideTV/1.2.1"})
    assert gelesen()["app"] == {"id": "app", "status": "warnung", "wert": "1.2.1:" + w.APP_AKTUELL}
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1", headers={"User-Agent": "Mozilla/5.0 FramesideTV/" + w.APP_AKTUELL})
    assert gelesen()["app"]["status"] == "ok"
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1", headers={"User-Agent": "Mozilla/5.0 FramesideTV/dream"})
    assert "app" not in gelesen()


def test_app_aktuell_passt_zur_android_app():
    import os, re
    import conftest  # noqa: F401
    raiz = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    gradle = open(os.path.join(raiz, "android", "app", "build.gradle")).read()
    quelle = open(os.path.join(raiz, "rahmen_web.py"), encoding="utf-8").read()
    assert re.search(r'versionName "([\d.]+)"', gradle).group(1) == re.search(r'APP_AKTUELL = "([\d.]+)"', quelle).group(1)


def test_neustarttest_wartet_auf_das_tablet(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    c = client()
    status = lambda: c.get("/api/verwaltung/neustarttest/rahmen").json()
    assert status()["zustand"] == "keiner"
    jetzt = [1000.0]
    monkeypatch.setattr(w.time, "time", lambda: jetzt[0])
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1")
    assert c.post("/api/verwaltung/neustarttest", json={"id": "rahmen"}, headers=H).json()["ok"] is True
    jetzt[0] += 2
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1")
    assert status()["zustand"] == "wartet"
    jetzt[0] += 60                                                            # Tablet meldet sich nicht mehr
    assert status()["zustand"] == "aus"
    jetzt[0] += 40                                                            # ... und kommt von selbst zurueck
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1")
    r = status()
    assert r["zustand"] == "fertig" and r["aus_sek"] >= 60
    assert c.get("/api/verwaltung/neustarttest/gibtsnicht").status_code == 404


def test_neustarttest_gilt_nicht_als_bestanden_bei_kurzer_pause_und_laeuft_ab(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    c = client()
    jetzt = [5000.0]
    monkeypatch.setattr(w.time, "time", lambda: jetzt[0])
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1")
    c.post("/api/verwaltung/neustarttest", json={"id": "rahmen"}, headers=H)
    jetzt[0] += 10                                                            # nur ein kurzer Aussetzer
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1")
    assert c.get("/api/verwaltung/neustarttest/rahmen").json()["zustand"] == "wartet"
    jetzt[0] += w.NEUSTARTTEST_MAX + 5
    assert c.get("/api/verwaltung/neustarttest/rahmen").json()["zustand"] == "zeit"


def test_geraetename_und_haengende_anzeige(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    c = client()
    fire = {"User-Agent": "Mozilla/5.0 (Linux; Android 11; KFTUWI) AppleWebKit/537.36 FramesideTV/1.3.0"}
    gelesen = lambda: {x["id"]: x for x in c.post("/api/verwaltung/bereitschaft", json={"id": "rahmen"}, headers=H).json()["punkte"]}
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&a=1&ra=3", headers=fire)
    p = gelesen()
    assert p["geraet"]["wert"] == "Fire-Tablet" and p["anzeige"]["status"] == "ok"
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&a=1&ra=" + str(w.ANZEIGE_HAENGT_SEK + 5), headers=fire)
    assert gelesen()["anzeige"] == {"id": "anzeige", "status": "warnung", "wert": str(w.ANZEIGE_HAENGT_SEK + 5)}
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&a=0&ra=5000", headers=fire)      # kein Dauerprogramm: kein Urteil ueber das Bild
    assert "anzeige" not in gelesen()
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1", headers={"User-Agent": "Mozilla/5.0 (Linux; Android 13; SM-T505) Chrome/120"})
    assert gelesen()["geraet"]["wert"] == "Android-Tablet"


def test_ladeverlauf_haelt_nur_wechsel_fest(app_laden):
    w, client, _ = app_laden(**LAN)
    c = client()
    ab = lambda ak, pl: c.get(f"/api/tv/abfrage?ziel=rahmen&seq=-1&fj=1&ak={ak}&pl={pl}")
    ab(50, 1); ab(49, 1)                                                       # kein Wechsel: nichts aufzeichnen
    assert c.get("/api/verwaltung/geraete/rahmen/ladeverlauf").json()["verlauf"] == []
    ab(80, 0); ab(79, 0); ab(20, 1)
    r = c.get("/api/verwaltung/geraete/rahmen/ladeverlauf").json()
    assert [(e["laedt"], e["akku"]) for e in r["verlauf"]] == [(True, 20), (False, 80)]       # neueste zuerst
    assert r["akku"] == 20 and r["laedt"] is True
    assert c.get("/api/verwaltung/geraete/gibtsnicht/ladeverlauf").status_code == 404


def test_wochenbericht_nennt_was_aufmerksamkeit_braucht(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    c = client()
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&fj=1&ak=60&pl=1&a=1&ra=3")
    titel, text = w.wochenbericht_text()
    assert titel == "Wochenbericht: alle Rahmen in Ordnung" and "Bilderrahmen: in Ordnung (Akku 60 %)" in text
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&fj=1&ak=12&pl=0&a=1&ra=3", headers={"User-Agent": "FramesideTV/1.0.0"})
    titel, text = w.wochenbericht_text()
    assert "1 von 1" in titel and "Akku 12 % ohne Strom" in text and "App veraltet (1.0.0)" in text
    titel, text = w.wochenbericht_text(jetzt=w.time.time() + 4 * 3600)                    # Tablet seit Stunden stumm
    assert "offline seit 4 Std." in text


def test_wochenbericht_geht_sonntags_einmal_raus(app_laden, monkeypatch):
    import datetime
    w, client, _ = app_laden(**LAN)
    gesendet = []
    monkeypatch.setattr(w, "pushover", lambda titel, text, prio=0: gesendet.append(titel))
    monkeypatch.setattr(w, "immich_aufruf", lambda *a, **k: {})
    sonntag = datetime.datetime(2026, 10, 18, 18, 30)                                    # ein Sonntag
    w.weitere_wache_pruefen(sonntag)
    w.weitere_wache_pruefen(sonntag + datetime.timedelta(minutes=5))                      # nicht noch einmal in derselben Woche
    assert sum(t.startswith("Wochenbericht") for t in gesendet) == 1
    w.weitere_wache_pruefen(datetime.datetime(2026, 10, 19, 18, 30))                      # Montag: nichts
    assert sum(t.startswith("Wochenbericht") for t in gesendet) == 1


def test_offline_reserve_gibt_es_nur_mit_https(app_laden):
    w, client, _ = app_laden(**LAN)
    c = client()
    gelesen = lambda: {x["id"]: x for x in c.post("/api/verwaltung/bereitschaft", json={"id": "rahmen"}, headers=H).json()["punkte"]}
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&a=1&ra=3&o=http%3A%2F%2F192.168.1.20%3A8090")
    assert gelesen()["reserve"]["status"] == "info"
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&a=1&ra=3&o=https%3A%2F%2Frahmen.example.org")
    assert gelesen()["reserve"]["status"] == "ok"
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&a=1&ra=3")                         # kein Wert gemeldet (aeltere Seite): kein Urteil
    assert "reserve" not in gelesen()


def test_app_kann_sich_selbst_aktualisieren_befehl_und_version(app_laden):
    import os
    w, client, _ = app_laden(**LAN)
    c = client()
    assert c.get("/api/config").json()["app_aktuell"] == w.APP_AKTUELL                     # die App fragt hier nach der aktuellen Version
    r = c.post("/api/geraete/rahmen/befehl", json={"aktion": "appupdate"}, headers=H)
    assert r.status_code == 200 and w.TV["rahmen"]["befehl"] == "appupdate"
    assert c.post("/api/geraete/rahmen/befehl", json={"aktion": "quatsch"}, headers=H).status_code == 400
    raiz = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    seite = open(os.path.join(raiz, "static", "tv", "index.html"), encoding="utf-8").read()
    assert "fully.updateApp" in seite                                                       # die Rahmen-Seite reicht den Befehl an die App weiter
    kotlin = open(os.path.join(raiz, "android", "app", "src", "main", "java", "com", "dirkvoss", "frameside", "tv", "FullyBruecke.java"), encoding="utf-8").read()
    assert "public void updateApp()" in kotlin and "@JavascriptInterface" in kotlin


def test_tablet_app_wird_nicht_auf_1920_pixel_verkleinert():
    import os
    raiz = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    seite = open(os.path.join(raiz, "static", "tv", "index.html"), encoding="utf-8").read()
    assert "/FramesideTV/.test(navigator.userAgent) && !/FramesideTablet/.test(navigator.userAgent)" in seite       # nur der Fernseher bekommt die 1920er Breite
    app = open(os.path.join(raiz, "android", "app", "src", "main", "java", "com", "dirkvoss", "frameside", "tv", "MainActivity.java"), encoding="utf-8").read()
    assert '" FramesideTablet"' in app and "istFernseher()" in app


def test_app_waehlt_einen_einzelnen_entwicklungs_server_nicht_von_selbst():
    import os
    raiz = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    app = open(os.path.join(raiz, "android", "app", "src", "main", "java", "com", "dirkvoss", "frameside", "tv", "MainActivity.java"), encoding="utf-8").read()
    i = app.index('"dev".equals(treffer.get(0).version)')
    j = app.index("Server.merken(MainActivity.this, treffer.get(0).adresse)")
    assert i < j                                                                              # der Dev-Fall steht VOR der automatischen Wahl
    assert "zeigeSeite" not in app[i:j]                                                      # und verbindet nicht selbst


def test_geraeteliste_zeigt_hinweise_ohne_netzzugriff(app_laden):
    w, client, _ = app_laden(RAHMEN_WEB_LAN="192.168.1.0/24", RAHMEN_WEB_RAHMEN_ZIELE="rahmen=Bilderrahmen", RAHMEN_WEB_TV_ZIELE="lg=LG TV")
    c = client()
    liste = lambda: {g["id"]: g for g in c.get("/api/geraete").json()["geraete"]}
    assert liste()["rahmen"]["check"] == {"status": "ok", "hinweise": []}                       # offline: die Liste zeigt es selbst
    assert "check" not in liste()["lg"]                                                         # Fernseher haben keine Tablet-Hinweise
    ua = {"User-Agent": "FramesideTV/1.0.0"}
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&a=1&ra=3&fj=1&ak=60&pl=1", headers=ua)
    assert liste()["rahmen"]["check"] == {"status": "warnung", "hinweise": ["app"]}               # App veraltet
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&a=1&ra=3&fj=1&ak=30&pl=0", headers={"User-Agent": "FramesideTV/" + w.APP_AKTUELL})
    assert liste()["rahmen"]["check"]["status"] == "ok"                                         # 30 % ohne Strom ist mit Ladegrenze normal
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&a=1&ra=3&fj=1&ak=10&pl=0&lg=fehler:20:80", headers={"User-Agent": "FramesideTV/" + w.APP_AKTUELL})
    assert liste()["rahmen"]["check"] == {"status": "fehler", "hinweise": ["akku", "ladegrenze"]}
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&a=1&ra=" + str(w.ANZEIGE_HAENGT_SEK + 1) + "&fj=1&ak=80&pl=1", headers={"User-Agent": "FramesideTV/" + w.APP_AKTUELL})
    assert liste()["rahmen"]["check"] == {"status": "warnung", "hinweise": ["anzeige"]}


def test_alter_verlauf_wird_vergessen_gemerktes_bleibt(app_laden, monkeypatch, tmp_path):
    import datetime
    w, client, _ = app_laden(RAHMEN_WEB_LAN="192.168.1.0/24")
    monkeypatch.setattr(w, "shows_datei", lambda: str(tmp_path / "shows.json"))
    jetzt = datetime.datetime.now()
    vor = lambda tage: (jetzt - datetime.timedelta(days=tage)).strftime("%Y-%m-%dT%H:%M")
    d = {"shows": [{"id": "neu", "name": "Neu", "ids": [], "zeit": vor(1)}, {"id": "alt", "name": "Alt", "ids": [], "zeit": vor(w.VERLAUF_TAGE + 5)},
                   {"id": "gemerkt", "name": "Gemerkt", "ids": [], "zeit": vor(400), "gemerkt": True}, {"id": "ohnezeit", "name": "?", "ids": []}]}
    w.daten_schreiben(d)
    assert [s["id"] for s in d["shows"]] == ["neu", "gemerkt", "ohnezeit"]


def test_mit_der_app_ist_ein_alter_fully_zugang_kein_fehler(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    c = client()
    aufrufe = []
    monkeypatch.setattr(w, "fully_get", lambda url, timeout=6: aufrufe.append(url) or (_ for _ in ()).throw(OSError("keine Verbindung")))
    c.put("/api/verwaltung/geraete/rahmen", json={"fully_host": "192.168.1.55", "fully_pw": "geheim"}, headers=H)
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&fj=1&ak=60&pl=1", headers={"User-Agent": "Mozilla/5.0 Fully"})                 # Fully (kein App-Kennzeichen): Fehler ist echt
    p = {x["id"]: x for x in c.post("/api/verwaltung/bereitschaft", json={"id": "rahmen"}, headers=H).json()["punkte"]}
    assert p["fully"]["status"] == "fehler" and aufrufe
    aufrufe.clear()
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&fj=1&ak=60&pl=1", headers={"User-Agent": "Mozilla/5.0 FramesideTV/1.4.2 FramesideTablet"})   # die App laeuft
    p = {x["id"]: x for x in c.post("/api/verwaltung/bereitschaft", json={"id": "rahmen"}, headers=H).json()["punkte"]}
    assert p["fully"]["status"] == "info" and not aufrufe                                                               # kein Zugriff auf Fully, kein Fehler
