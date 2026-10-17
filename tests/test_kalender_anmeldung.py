"""Kalender-Link mit Anmeldung (Radicale, Nextcloud ...) je Rahmen und der vollstaendige Termin-Leser (Wiederholungen, Ausnahmen, mehrtaegig)."""
import datetime

H = {"X-Rahmen": "1"}
LAN = {"RAHMEN_WEB_LAN": "192.168.1.0/24", "RAHMEN_WEB_RAHMEN_ZIELE": "wohnzimmer=Wohnzimmer,flur=Flur", "RAHMEN_WEB_RAHMEN_SEK": "8"}

ICS = """BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//test//DE
BEGIN:VEVENT
UID:monat@test
DTSTART;TZID=Europe/Berlin:20260105T190000
DTEND;TZID=Europe/Berlin:20260105T200000
RRULE:FREQ=MONTHLY;BYMONTHDAY=5
SUMMARY:Elternabend
END:VEVENT
BEGIN:VEVENT
UID:sport@test
DTSTART:20260105T070000
RRULE:FREQ=WEEKLY;BYDAY=MO
EXDATE:20261005T070000
SUMMARY:Sport
END:VEVENT
BEGIN:VEVENT
UID:urlaub@test
DTSTART;VALUE=DATE:20261004
DTEND;VALUE=DATE:20261007
SUMMARY:Urlaub
BEGIN:VALARM
ACTION:DISPLAY
DESCRIPTION:Erinnerung
TRIGGER:-PT15M
END:VALARM
END:VEVENT
BEGIN:VEVENT
UID:abgesagt@test
DTSTART:20261005T120000
STATUS:CANCELLED
SUMMARY:Abgesagt
END:VEVENT
BEGIN:VEVENT
UID:woche@test
DTSTART:20261005T080000
RRULE:FREQ=DAILY;COUNT=1
SUMMARY:Nur einmal
END:VEVENT
END:VCALENDAR
"""


def test_voller_leser_kann_monatlich_ausnahmen_mehrtaegig_und_abgesagt(app_laden):
    w, _, _ = app_laden()
    titel = [(e["tag"], e["titel"]) for e in w.ics_termine(ICS, datetime.date(2026, 10, 5), maximum=20)]
    assert ("heute", "Elternabend") in titel                      # monatlich am 5.
    assert ("heute", "Urlaub") in titel and ("morgen", "Urlaub") in titel      # mehrtaegig, ganztaegig (4. bis 6.)
    assert ("heute", "Nur einmal") in titel
    assert not any(t == "Sport" for _, t in titel)                # Montag, aber EXDATE nimmt den Tag aus
    assert not any(t == "Abgesagt" for _, t in titel)             # abgesagte Termine fehlen
    spaeter = [(e["tag"], e["titel"]) for e in w.ics_termine(ICS, datetime.date(2026, 10, 12), maximum=20)]
    assert ("heute", "Sport") in spaeter                          # eine Woche danach wieder normal


def test_einzeltermin_verschoben_ersetzt_die_serie(app_laden):
    w, _, _ = app_laden()
    ics = """BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//t//t
BEGIN:VEVENT
UID:s@t
DTSTART:20261005T090000
RRULE:FREQ=WEEKLY
SUMMARY:Yoga
END:VEVENT
BEGIN:VEVENT
UID:s@t
RECURRENCE-ID:20261012T090000
DTSTART:20261012T110000
SUMMARY:Yoga
END:VEVENT
END:VCALENDAR
"""
    zeiten = [e["zeit"] for e in w.ics_termine(ics, datetime.date(2026, 10, 12))]
    assert zeiten == ["11:00"]                                    # nicht zusaetzlich um 9:00


def test_rueckfall_auf_den_einfachen_leser(app_laden, monkeypatch):
    w, _, _ = app_laden()

    def kaputt(*a, **k):
        raise RuntimeError("Bibliothek fehlt")
    monkeypatch.setattr(w, "_ics_termine_voll", kaputt)
    titel = [e["titel"] for e in w.ics_termine(ICS, datetime.date(2026, 10, 5), maximum=20)]
    assert "Elternabend" not in titel                             # monatlich versteht der einfache Leser nicht, er darf aber nicht abstuerzen
    assert "Sport" in titel                                       # woechentlich versteht er


def test_http_text_sendet_die_anmeldung(app_laden, monkeypatch):
    w, _, _ = app_laden()
    gesehen = {}

    class Antwort:
        def __enter__(self): return self
        def __exit__(self, *a): return False
        def read(self, n): return b"BEGIN:VCALENDAR\nEND:VCALENDAR"

    def falsch(req, timeout=10):
        gesehen["kopf"] = dict(req.header_items())
        return Antwort()
    monkeypatch.setattr(w.urllib.request, "urlopen", falsch)
    w.http_text("https://example.org/c/", auth=("anna", "geheim:mit:doppelpunkt"))
    import base64
    assert gesehen["kopf"]["Authorization"] == "Basic " + base64.b64encode(b"anna:geheim:mit:doppelpunkt").decode()
    gesehen.clear()
    w.http_text("https://example.org/c/")
    assert "Authorization" not in gesehen["kopf"]


def test_anmeldung_gilt_nur_fuer_den_eigenen_link_des_rahmens(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    app = client()
    r = app.put("/api/verwaltung/geraete/wohnzimmer", json={"kalender_url": "webcal://example.org/anna/cal/", "kalender_user": "anna", "kalender_pw": "geheim"}, headers=H)
    assert r.status_code == 200, r.text
    g = r.json()["geraet"]
    assert g["kalender_user"] == "anna" and g["kalender_pw_gesetzt"] is True
    assert "geheim" not in r.text                                      # das Passwort kommt nie zurueck
    assert w.kalender_zugang("wohnzimmer") == ("https://example.org/anna/cal/", ("anna", "geheim"))      # webcal:// wird https://
    assert w.kalender_zugang("flur") == ("", None)                      # der andere Rahmen hat nichts
    aufrufe = []
    monkeypatch.setattr(w, "http_text", lambda url, timeout=10, auth=None: aufrufe.append((url, auth)) or ICS)
    w.kalender_holen("wohnzimmer")
    assert aufrufe == [("https://example.org/anna/cal/", ("anna", "geheim"))]
    # Passwort bleibt, wenn nur etwas anderes gespeichert wird; Benutzer ohne Passwort aendert nichts
    app.put("/api/verwaltung/geraete/wohnzimmer", json={"sek": 12, "kalender_user": "anna"}, headers=H)
    assert w.kalender_zugang("wohnzimmer")[1] == ("anna", "geheim")
    # Link entfernen nimmt auch die Anmeldung mit
    app.put("/api/verwaltung/geraete/wohnzimmer", json={"kalender_url": None}, headers=H)
    assert w.kalender_zugang("wohnzimmer") == ("", None)
    assert "kalender_pw" not in w.GERAETE["wohnzimmer"] and "kalender_user" not in w.GERAETE["wohnzimmer"]


def test_anmeldung_wird_nicht_an_den_allgemeinen_link_gehaengt(app_laden):
    w, client, _ = app_laden(RAHMEN_WEB_KALENDER_URL="https://example.org/allgemein.ics", **LAN)
    client().put("/api/verwaltung/geraete/flur", json={"kalender_user": "x", "kalender_pw": "y"}, headers=H)
    assert w.kalender_zugang("flur") == ("https://example.org/allgemein.ics", None)       # ohne eigenen Link zaehlt die Anmeldung nicht


def test_ein_kaputter_eintrag_macht_nicht_den_ganzen_kalender_unlesbar(app_laden, monkeypatch):
    """Echter Fall: ein alter Termin mit unlesbarem SEQUENCE liess die Bibliothek abstuerzen; dann las nur noch der einfache Leser (ohne monatlich)."""
    w, _, _ = app_laden()
    ics = ICS.replace("END:VCALENDAR", """BEGIN:VEVENT
UID:alt@test
DTSTART:20211104T093000
SEQUENCE:kein-Zahlenwert
SUMMARY:Alter Termin
END:VEVENT
BEGIN:VEVENT
UID:kaputt@test
DTSTART:kein-Datum
SUMMARY:Ohne Datum
END:VEVENT
END:VCALENDAR""")
    monkeypatch.setattr(w, "_ics_termine_einfach", lambda *a, **k: (_ for _ in ()).throw(AssertionError("Rueckfall darf nicht noetig sein")))
    titel = [e["titel"] for e in w.ics_termine(ics, datetime.date(2026, 10, 5), maximum=20)]
    assert "Elternabend" in titel                                 # monatlich: nur der volle Leser kann das
    assert "Ohne Datum" not in titel


def _fehler(w, code):
    import urllib.error

    def f(url, timeout=10, auth=None):
        raise urllib.error.HTTPError(url, code, "x", {}, None)
    return f


ICS_NAME = ICS.replace("PRODID:-//test//DE", "PRODID:-//test//DE\nX-WR-CALNAME:Family")


def test_kalender_testen_zeigt_name_und_zahlen(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    app = client()
    gesehen = {}
    monkeypatch.setattr(w, "http_text", lambda url, timeout=10, auth=None: gesehen.update(url=url, auth=auth) or ICS_NAME)
    r = app.post("/api/verwaltung/geraete/wohnzimmer/kalender-test", json={"kalender_url": "webcal://example.org/c/", "kalender_user": "anna", "kalender_pw": "geheim"}, headers=H)
    assert r.status_code == 200, r.text
    d = r.json()
    assert d["name"] == "Family" and d["eintraege"] == 5 and "Family" in d["nachricht"] and "geheim" not in r.text
    assert gesehen == {"url": "https://example.org/c/", "auth": ("anna", "geheim")}        # es werden die eingegebenen (noch nicht gespeicherten) Werte benutzt
    assert w.KAL_NAMEN["wohnzimmer"] == "Family"


def test_kalender_testen_nutzt_gespeicherte_werte_wenn_nichts_eingegeben(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    app = client()
    app.put("/api/verwaltung/geraete/wohnzimmer", json={"kalender_url": "https://example.org/c/", "kalender_user": "anna", "kalender_pw": "geheim"}, headers=H)
    gesehen = {}
    monkeypatch.setattr(w, "http_text", lambda url, timeout=10, auth=None: gesehen.update(auth=auth) or ICS)
    assert app.post("/api/verwaltung/geraete/wohnzimmer/kalender-test", json={}, headers=H).status_code == 200
    assert gesehen["auth"] == ("anna", "geheim")
    # anderer Benutzer eingegeben, aber kein Passwort: das gespeicherte Passwort gehoert nicht dazu
    app.post("/api/verwaltung/geraete/wohnzimmer/kalender-test", json={"kalender_user": "bernd"}, headers=H)
    assert gesehen["auth"] is None


def test_kalender_testen_nennt_den_grund(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    app = client()
    url = {"kalender_url": "https://example.org/c/"}
    monkeypatch.setattr(w, "http_text", _fehler(w, 401))
    assert "Passwort" in app.post("/api/verwaltung/geraete/wohnzimmer/kalender-test", json={**url, "kalender_user": "anna", "kalender_pw": "x"}, headers=H).json()["nachricht"]
    assert "Anmeldung" in app.post("/api/verwaltung/geraete/wohnzimmer/kalender-test", json=url, headers=H).json()["nachricht"]      # ohne Benutzer: der Kalender verlangt eine Anmeldung
    monkeypatch.setattr(w, "http_text", _fehler(w, 404))
    assert "keinen Kalender" in app.post("/api/verwaltung/geraete/wohnzimmer/kalender-test", json=url, headers=H).json()["nachricht"]
    monkeypatch.setattr(w, "http_text", lambda *a, **k: "<html>Login</html>")
    assert "kein Kalender" in app.post("/api/verwaltung/geraete/wohnzimmer/kalender-test", json=url, headers=H).json()["nachricht"]
    assert app.post("/api/verwaltung/geraete/wohnzimmer/kalender-test", json={}, headers=H).status_code == 400        # ohne Link
    assert app.post("/api/verwaltung/geraete/gibtsnicht/kalender-test", json=url, headers=H).status_code == 404


def test_kalendername_erscheint_am_geraet_nach_dem_abruf(app_laden, monkeypatch):
    w, client, _ = app_laden(**LAN)
    app = client()
    app.put("/api/verwaltung/geraete/wohnzimmer", json={"kalender_url": "https://example.org/c/"}, headers=H)
    monkeypatch.setattr(w, "http_text", lambda url, timeout=10, auth=None: ICS_NAME)
    w.kalender_holen("wohnzimmer")
    g = [e for e in app.get("/api/verwaltung").json()["geraete"] if e["id"] == "wohnzimmer"][0]
    assert g["kalender_name"] == "Family"
