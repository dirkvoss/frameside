"""Profi-Ansicht: ein serverweiter Schalter, der die Technik-Einstellungen (Fully, ADB, manuelle Wege) ein- und ausblendet."""
import os

H = {"X-Rahmen": "1"}
LAN = {"RAHMEN_WEB_LAN": "192.168.1.0/24", "RAHMEN_WEB_PROXIES": "192.168.1.200"}
RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def test_standard_ist_aus_und_der_schalter_wirkt_serverweit(app_laden):
    w, client, _ = app_laden(**LAN)
    c = client()
    assert c.get("/api/config").json()["profi"] is False
    assert c.get("/api/einstellungen").json()["profi"] is False
    assert c.put("/api/einstellungen", json={"profi": True}, headers=H).json()["einstellungen"]["profi"] is True
    assert client("192.168.1.77").get("/api/config").json()["profi"] is True            # jeder Browser sieht dieselbe Ansicht
    assert c.put("/api/einstellungen", json={"sek": 10}, headers=H).json()["einstellungen"]["profi"] is True   # andere Aenderungen lassen ihn stehen
    assert c.put("/api/einstellungen", json={"profi": False}, headers=H).json()["einstellungen"]["profi"] is False
    assert c.get("/api/config").json()["profi"] is False
    c.put("/api/einstellungen", json={"profi": "ja"}, headers=H)                            # nur echtes true schaltet ein
    assert c.get("/api/config").json()["profi"] is False


def test_technik_abschnitte_haengen_am_profi_schalter():
    html = open(os.path.join(RAIZ, "static", "index.html"), encoding="utf-8").read()
    for kennung in ('id="gfFullyFelder"', 'id="gfTabletWege"', 'id="gfFullyAnzeigen"', 'id="assiHandarbeit"', 'id="assiDirekt"', 'id="assiLinkTeil"'):
        zeile = next(z for z in html.splitlines() if kennung in z)
        assert "nur-profi" in zeile, kennung
    assert "body:not(.profi) .nur-profi" in html
    assert 'id="assiApp" open' in html                                                      # der App-Weg ist der aufgeklappte Standard
