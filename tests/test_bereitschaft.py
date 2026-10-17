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
