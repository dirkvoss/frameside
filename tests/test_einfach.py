"""Einfacher Zugang: ein Link, eine ganz einfache Seite (Fotos hinzufuegen, eigene Shows) im EIGENEN Immich-Konto der Person, nur fuer die eingetragenen Rahmen."""
import json

import conftest

H = {"X-Rahmen": "1"}
JPEG = b"\xff\xd8\xff\xe0" + b"\x00" * 64
ANNA = "aaaaaaaa-0000-4000-8000-00000000000a"
LAN = {"RAHMEN_WEB_LAN": "192.168.1.0/24", "RAHMEN_WEB_RAHMEN_ZIELE": "oma=Omas Rahmen,flur=Flur", "RAHMEN_WEB_RAHMEN_SEK": "5"}


def aufbau(app_laden, monkeypatch, tmp_path):
    w, client, schrein = app_laden(RAHMEN_WEB_BENUTZER_FILE=str(tmp_path / "benutzer.json"), RAHMEN_WEB_EINFACH_FILE=str(tmp_path / "einfach.json"), **LAN)
    anm = conftest.SchreinAnmeldung()
    gesendet = []
    orig = anm.__call__

    def immich(methode, pfad, daten=None, token=None, schluessel=None, basis=None):
        if pfad == "/albums":
            return [] if methode == "GET" else {"id": "album-x"}
        if pfad.startswith("/albums/") and methode == "PUT":
            return []
        return orig(methode, pfad, daten, token, schluessel, basis)

    def senden(pfad, body, kopf):
        gesendet.append(kopf["x-api-key"])
        return 201, json.dumps({"id": "11111111-1111-4111-8111-%012d" % len(gesendet), "status": "created"}).encode()

    monkeypatch.setattr(w, "immich_aufruf", immich)
    monkeypatch.setattr(w, "upload_senden", senden)
    monkeypatch.setattr(w, "immich_roh", lambda *a, **k: (b"x", "image/jpeg"))
    monkeypatch.setattr(w.time, "sleep", lambda s: None)
    return w, client, schrein, anm, gesendet


def anlegen(admin, **extra):
    r = admin.post("/api/verwaltung/einfach", json={"name": "Oma", "email": "anna@example.org", "passwort": "anna-passwort", "ziele": ["oma"], **extra}, headers=H)
    assert r.status_code == 200, r.text
    return r.json()["token"]


def oeffnen(client, token, host="203.0.113.9"):
    oma = client(host)
    r = oma.get(f"/einfach/{token}", follow_redirects=False)
    assert r.status_code == 200 and "Fotos hinzufügen" in r.text and "rw_einfach" in r.headers["set-cookie"] and "HttpOnly" in r.headers["set-cookie"]
    return oma


def test_zugang_anlegen_link_oeffnen_und_widerrufen(app_laden, monkeypatch, tmp_path):
    w, client, schrein, anm, _ = aufbau(app_laden, monkeypatch, tmp_path)
    admin = client()
    token = anlegen(admin)
    assert json.loads((tmp_path / "benutzer.json").read_text())[ANNA]["key"] == anm.schluessel[ANNA]          # nur der Schluessel, nie das Passwort
    assert "anna-passwort" not in (tmp_path / "benutzer.json").read_text() + (tmp_path / "einfach.json").read_text()
    assert client("203.0.113.9").get("/api/einfach/ich").status_code == 401                                   # ohne Link nichts
    oma = oeffnen(client, token)
    ich = oma.get("/api/einfach/ich").json()
    assert ich["name"] == "Oma" and ich["hochladen"] is True and [z["id"] for z in ich["ziele"]] == ["oma"]
    lst = admin.get("/api/verwaltung/einfach").json()["zugaenge"]
    assert lst[0]["token"] == token and lst[0]["ziele"] == ["oma"]
    assert admin.delete(f"/api/verwaltung/einfach/{lst[0]['id']}", headers=H).status_code == 200
    assert oma.get("/api/einfach/ich").status_code == 401                                                     # sofort widerrufen


def test_falscher_link_und_sperre(app_laden, monkeypatch, tmp_path):
    w, client, *_ = aufbau(app_laden, monkeypatch, tmp_path)
    anlegen(client())
    fremd = client("203.0.113.77")
    for _ in range(20):
        assert fremd.get("/einfach/falsch", follow_redirects=False).status_code == 404
    assert fremd.get("/einfach/falsch", follow_redirects=False).status_code == 429


def test_nur_die_einfachen_aufrufe_sind_erlaubt(app_laden, monkeypatch, tmp_path):
    w, client, *_ = aufbau(app_laden, monkeypatch, tmp_path)
    oma = oeffnen(client, anlegen(client()))
    for pfad in ("/api/neueste", "/api/shows", "/api/verwaltung", "/api/verwaltung/einfach", "/api/suche?text=x", "/api/geraete", "/api/me"):
        r = oma.get(pfad)
        assert r.status_code == 401 or r.json().get("angemeldet") is False, pfad
    assert oma.post("/api/anzeigen", json={"name": "x", "ids": ["00000001-0000-4000-8000-000000000001"]}, headers=H).status_code == 401
    assert oma.put("/api/verwaltung/geraete/oma", json={"sek": 3}, headers=H).status_code == 401
    assert oma.delete("/api/verwaltung/einfach/irgendeiner", headers=H).status_code == 401


def test_fotos_und_vorschau_kommen_aus_ihrem_konto(app_laden, monkeypatch, tmp_path):
    w, client, schrein, anm, _ = aufbau(app_laden, monkeypatch, tmp_path)
    oma = oeffnen(client, anlegen(client()))
    d = oma.get("/api/einfach/fotos").json()
    assert d["fotos"] and all("id" in f for f in d["fotos"])
    assert schrein.schluessel[-1] == anm.schluessel[ANNA]                                                       # die Suche lief mit IHREM Schluessel
    assert oma.get("/api/einfach/vorschau/" + d["fotos"][0]["id"]).status_code == 200


def test_hochladen_in_ihr_konto(app_laden, monkeypatch, tmp_path):
    w, client, schrein, anm, gesendet = aufbau(app_laden, monkeypatch, tmp_path)
    oma = oeffnen(client, anlegen(client()))
    r = oma.put("/api/einfach/hochladen", content=JPEG, headers={**H, "X-Dateiname": "urlaub.jpg", "X-Datum": "1700000000000", "Content-Type": "image/jpeg"})
    assert r.status_code == 200 and r.json()["ok"]
    assert gesendet == [anm.schluessel[ANNA]]
    assert client("203.0.113.9").put("/api/einfach/hochladen", content=JPEG, headers=H).status_code == 401      # ohne Link nicht


def test_show_zeigen_speichern_starten_loeschen_nur_auf_ihrem_rahmen(app_laden, monkeypatch, tmp_path):
    w, client, schrein, anm, _ = aufbau(app_laden, monkeypatch, tmp_path)
    admin = client()
    oma = oeffnen(client, anlegen(admin))
    ids = ["00000001-0000-4000-8000-000000000001", "00000002-0000-4000-8000-000000000002"]
    r = oma.post("/api/einfach/zeigen", json={"ids": ids, "name": "Urlaub", "speichern": True, "ziele": ["flur"]}, headers=H)       # "ziele" wird ignoriert
    assert r.status_code == 200, r.text
    assert w.TV["oma"]["aktiv"]["ids"] and set(w.TV["oma"]["aktiv"]["ids"]) == set(ids)
    assert w.TV["flur"]["aktiv"] is None                                                                        # der andere Rahmen bleibt unberuehrt
    assert w.geraete_schluessel(ids[0]) == anm.schluessel[ANNA]                                                 # der Rahmen holt die Fotos mit IHREM Schluessel
    shows = oma.get("/api/einfach/shows").json()["shows"]
    assert [s["name"] for s in shows] == ["Urlaub"] and shows[0]["anzahl"] == 2
    assert admin.get("/api/shows").json()["shows"] == []                                                        # eigene Shows, nicht die des Administrators
    w.TV["oma"]["aktiv"] = None
    assert oma.post(f"/api/einfach/shows/{shows[0]['id']}/starten", headers=H).status_code == 200
    assert set(w.TV["oma"]["aktiv"]["ids"]) == set(ids)
    assert oma.put(f"/api/einfach/shows/{shows[0]['id']}", json={"name": "Urlaub 2026"}, headers=H).status_code == 200
    assert oma.get("/api/einfach/shows").json()["shows"][0]["name"] == "Urlaub 2026"
    assert oma.delete(f"/api/einfach/shows/{shows[0]['id']}", headers=H).status_code == 200
    assert oma.get("/api/einfach/shows").json()["shows"] == []


def test_ungespeicherte_auswahl_erscheint_nicht_in_meine_shows(app_laden, monkeypatch, tmp_path):
    w, client, *_ = aufbau(app_laden, monkeypatch, tmp_path)
    oma = oeffnen(client, anlegen(client()))
    assert oma.post("/api/einfach/zeigen", json={"ids": ["00000001-0000-4000-8000-000000000001"]}, headers=H).status_code == 200
    assert oma.get("/api/einfach/shows").json()["shows"] == []
    assert oma.post("/api/einfach/zeigen", json={"ids": ["kaputt"]}, headers=H).status_code == 400
    assert oma.post("/api/einfach/zeigen", json={"ids": [], "name": "x", "speichern": True}, headers=H).status_code == 400


def test_normales_programm(app_laden, monkeypatch, tmp_path):
    w, client, *_ = aufbau(app_laden, monkeypatch, tmp_path)
    oma = oeffnen(client, anlegen(client()))
    oma.post("/api/einfach/zeigen", json={"ids": ["00000001-0000-4000-8000-000000000001"]}, headers=H)
    assert oma.post("/api/einfach/normal", headers=H).status_code == 200
    assert not w.TV["oma"]["aktiv"]


def test_anlegen_prueft_eingaben(app_laden, monkeypatch, tmp_path):
    w, client, *_ = aufbau(app_laden, monkeypatch, tmp_path)
    admin = client()
    ok = {"name": "Oma", "email": "anna@example.org", "passwort": "anna-passwort", "ziele": ["oma"]}
    assert admin.post("/api/verwaltung/einfach", json={**ok, "passwort": "falsch"}, headers=H).status_code == 400
    assert admin.post("/api/verwaltung/einfach", json={**ok, "ziele": []}, headers=H).status_code == 400
    assert admin.post("/api/verwaltung/einfach", json={**ok, "ziele": ["gibtsnicht"]}, headers=H).status_code == 400
    assert admin.post("/api/verwaltung/einfach", json={**ok, "name": ""}, headers=H).status_code == 400
    assert admin.post("/api/verwaltung/einfach", json={**ok, "email": ""}, headers=H).status_code == 400
    assert admin.get("/api/verwaltung/einfach").json()["zugaenge"] == []
    assert client("203.0.113.9").post("/api/verwaltung/einfach", json=ok, headers=H).status_code == 401      # nur mit Anmeldung in der App


def test_seite_wird_ausgeliefert(app_laden, monkeypatch, tmp_path):
    w, client, *_ = aufbau(app_laden, monkeypatch, tmp_path)
    r = client("203.0.113.9").get("/einfach/")
    assert r.status_code == 200 and "Fotos hinzufügen" in r.text and "Eine neue Show machen" in r.text


def test_adresse_von_aussen_wird_als_vorschlag_gemerkt(app_laden, monkeypatch):
    w, client, _ = app_laden(RAHMEN_WEB_LAN="192.168.1.0/24", RAHMEN_WEB_PROXIES="192.168.1.200", RAHMEN_WEB_RAHMEN_ZIELE="rahmen=Bilderrahmen")
    c = client()
    def ursprung():
        return {g["id"]: g for g in c.get("/api/verwaltung", headers={"X-Rahmen": "1"}).json()["geraete"]}["rahmen"]["ursprung"]
    c.get("/api/tv/abfrage?ziel=rahmen&seq=-1&o=https%3A%2F%2Frahmen.example.org")
    assert ursprung() == ""                                                   # aus dem Heimnetz: kein Vorschlag (die Adresse waere intern)

    aussen = client("203.0.113.5")                                            # Tablet bei den Eltern: fremde Adresse, Geraete-Zugang per Cookie
    aussen.cookies.set(w.GERAET_COOKIE, w.zugang_ausstellen("rahmen"))

    def von_aussen(o):
        assert aussen.get("/api/tv/abfrage?ziel=rahmen&seq=-1&o=" + o).status_code == 200
    von_aussen("https%3A%2F%2Frahmen.example.org")
    assert ursprung() == "https://rahmen.example.org"
    von_aussen("javascript%3Aalert(1)")                                       # ungueltige Werte werden ignoriert
    assert ursprung() == ""
