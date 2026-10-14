"""Fully Kiosk: Tablet per Klick als Bilderrahmen einrichten, pruefen, als Datei laden; Kopplungs-Code bleibt beim Neuladen gleich."""
import json
import urllib.parse

H = {"X-Rahmen": "1"}
LAN = {"RAHMEN_WEB_LAN": "192.168.1.0/24", "RAHMEN_WEB_RAHMEN_ZIELE": "rahmen=Bilderrahmen", "RAHMEN_WEB_TV_ZIELE": "wz=Wohnzimmer", "RAHMEN_WEB_RAHMEN_SEK": "8"}
FALSCH = {"startURL": "https://example.org", "reloadEachSeconds": "60", "reloadOnScreenOn": True, "timeToScreensaverV2": "15", "keepScreenOn": False, "launchOnBoot": False,
          "forceScreenOrientation": "0", "screenOffTimer": "0"}


class FakeTablet:
    """Verhaelt sich wie die Fernsteuerung von Fully (listSettings, setBooleanSetting, setStringSetting, loadStartURL)."""
    def __init__(self, pw="geheim", ist=None):
        self.pw, self.ist, self.befehle = pw, dict(FALSCH if ist is None else ist), []

    def __call__(self, url, timeout=6):
        q = {k: v[0] for k, v in urllib.parse.parse_qs(urllib.parse.urlparse(url).query).items()}
        if q.get("password") != self.pw:
            return json.dumps({"status": "Error", "statustext": "Please login"})
        self.befehle.append(q["cmd"])
        if q["cmd"] == "listSettings":
            return json.dumps(self.ist)
        if q["cmd"] == "setBooleanSetting":
            self.ist[q["key"]] = q["value"] == "true"
        elif q["cmd"] == "setStringSetting":
            self.ist[q["key"]] = q["value"]
        return json.dumps({"status": "OK"})


def einrichten(c, tablet, monkeypatch, w, **extra):
    monkeypatch.setattr(w, "fully_get", tablet)
    return c.post("/api/verwaltung/fully-einrichten", json={"id": "rahmen", "fully_host": "192.168.1.55", "fully_pw": "geheim", "basis": "http://192.168.1.20:8090", **extra}, headers=H)


def test_einrichten_setzt_alles_und_laedt_die_seite(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    c, t = client(), FakeTablet()
    r = einrichten(c, t, monkeypatch, w, ausrichtung="quer").json()
    assert r["ok"] and r["geaendert"] > 5 and r["offen"] == []
    assert t.ist["startURL"] == "http://192.168.1.20:8090/tv/?ziel=rahmen"
    assert t.ist["reloadEachSeconds"] == "0" and t.ist["reloadOnScreenOn"] is False and t.ist["timeToScreensaverV2"] == "0"
    assert t.ist["keepScreenOn"] is True and t.ist["launchOnBoot"] is True and t.ist["forceScreenOrientation"] == "2"
    assert "loadStartURL" in t.befehle
    g = [x for x in c.get("/api/verwaltung").json()["geraete"] if x["id"] == "rahmen"][0]            # die Wahl bleibt gespeichert und ist spaeter aenderbar
    assert g["fully_ad_host"] == "192.168.1.55:2323" and g["fully_host"] == "192.168.1.55:2323" and g["fully_ausrichtung"] == "quer" and g["fully_pw_gesetzt"] and g["fully_ad_pw_gesetzt"]
    assert "fully_pw" not in g


def test_zweiter_lauf_aendert_nichts_und_pruefen_findet_abweichungen(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    c, t = client(), FakeTablet()
    einrichten(c, t, monkeypatch, w)
    t.befehle.clear()
    assert einrichten(c, t, monkeypatch, w).json()["geaendert"] == 0 and not any(b.startswith("set") for b in t.befehle)
    t.ist["reloadEachSeconds"] = "60"                                                         # der Kunde verstellt etwas
    p = einrichten(c, t, monkeypatch, w, nur_pruefen=True).json()
    assert not p["ok"] and [a["key"] for a in p["abweichungen"]] == ["reloadEachSeconds"] and t.ist["reloadEachSeconds"] == "60"
    assert einrichten(c, t, monkeypatch, w).json()["geaendert"] == 1 and t.ist["reloadEachSeconds"] == "0"


def test_optionen_abwaehlbar(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    c, t = client(), FakeTablet()
    einrichten(c, t, monkeypatch, w, immer_an=False, selbststart=False, helligkeit="120")
    assert t.ist["keepScreenOn"] is False and t.ist["launchOnBoot"] is False and t.ist["screenBrightness"] == "120" and t.ist["timeToScreensaverV2"] == "15"


def test_falsches_passwort_und_fehlende_angaben(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    c = client()
    monkeypatch.setattr(w, "fully_get", FakeTablet(pw="richtig"))
    r = c.post("/api/verwaltung/fully-einrichten", json={"id": "rahmen", "fully_host": "tablet.lan", "fully_pw": "falsch", "basis": "http://192.168.1.20:8090"}, headers=H)
    assert r.status_code == 401 and "Passwort stimmt nicht" in r.json()["nachricht"]
    assert c.post("/api/verwaltung/fully-einrichten", json={"id": "rahmen", "fully_pw": "x", "basis": "http://a"}, headers=H).status_code == 400            # keine Tablet-Adresse
    assert c.post("/api/verwaltung/fully-einrichten", json={"id": "rahmen", "fully_host": "tablet.lan", "basis": ""}, headers=H).status_code == 400          # keine Server-Adresse
    assert c.post("/api/verwaltung/fully-einrichten", json={"id": "gibtsnicht", "fully_host": "t", "basis": "http://a"}, headers=H).status_code == 404
    assert c.post("/api/verwaltung/fully-einrichten", json={"id": "rahmen", "fully_host": "t", "basis": "http://a", "ausrichtung": "schief"}, headers=H).status_code == 400


def test_einstellungsdatei_fuer_unerreichbare_tablets(app_laden):
    w, client, _ = app_laden(**LAN)
    r = client().post("/api/verwaltung/fully-datei", json={"id": "rahmen", "basis": "https://tv.example.org", "ausrichtung": "hoch"}, headers=H)
    d = r.json()
    assert r.status_code == 200 and "attachment" in r.headers["content-disposition"]
    assert d["startURL"] == "https://tv.example.org/tv/?ziel=rahmen" and d["forceScreenOrientation"] == "1" and d["reloadEachSeconds"] == "0"


def test_kopplungs_code_bleibt_beim_neuladen_gleich(app_laden):
    w, client, _ = app_laden(**LAN)
    tablet = client()
    code = tablet.post("/api/koppeln/neu", headers=H).json()["code"]
    for _ in range(12):                                                                       # Fully laedt die Seite immer wieder neu: kein neuer Code, keine Sperre
        r = tablet.post(f"/api/koppeln/neu?alt={code}", headers=H)
        assert r.status_code == 200 and r.json()["code"] == code
    fremd = tablet.post("/api/koppeln/neu?alt=XXXXXX", headers=H)                             # unbekannter alter Code: ein neuer wird vergeben
    assert fremd.status_code == 200 and fremd.json()["code"] != "XXXXXX"


def test_tablet_das_sich_selbst_steuert_behaelt_die_zugangsdaten_des_assistenten(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    c, t = client(), FakeTablet()
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&fj=1")                                          # das Tablet meldet sich ueber die Fully-JavaScript-Schnittstelle
    einrichten(c, t, monkeypatch, w)
    g = [x for x in c.get("/api/verwaltung").json()["geraete"] if x["id"] == "rahmen"][0]
    assert g["selbst"] and g["fully_ad_host"] == "192.168.1.55:2323" and g["fully_ad_pw_gesetzt"] and not g["fully_host"]     # Nachtsteuerung bleibt aus, der Assistent merkt sich Adresse und Passwort
    c.put("/api/verwaltung/geraete/rahmen", json={"sek": 10}, headers=H)                      # Speichern im Dialog loescht sie nicht
    monkeypatch.setattr(w, "fully_get", t)
    t.befehle.clear()
    r = c.post("/api/verwaltung/fully-einrichten", json={"id": "rahmen", "basis": "http://192.168.1.20:8090", "nur_pruefen": True}, headers=H)       # Pruefen ohne erneute Eingabe
    assert r.status_code == 200 and r.json()["ok"] is True
