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


def test_neue_version_laedt_die_seite_von_selbst_neu():
    html = open(os.path.join(RAIZ, "static", "index.html"), encoding="utf-8").read()
    i = html.index("async function versionPruefen()")
    funktion = html[i:html.index("$(\"#updateBanner\")", i)]
    assert "location.reload()" in funktion and "tvdialog:not(.versteckt)" in funktion        # automatisch, solange kein Fenster offen ist
    assert "setInterval(versionPruefen, 30000)" in html
    assert "z-index: 100000" in html.split(".updatebanner {", 1)[1].split("}", 1)[0]          # das Banner liegt ueber den Fenstern


def test_tablet_check_zeile_traegt_kennung_und_der_update_knopf_haengt_daran():
    """Fehler in 2.35.0/2.35.1: die Zeilen kannten ihre Kennung nicht, deshalb erschien der Knopf 'App jetzt aktualisieren' nie."""
    html = open(os.path.join(RAIZ, "static", "index.html"), encoding="utf-8").read()
    i = html.index("function checkZeile(")
    zeile = html[i:html.index("\n}\n", i)]
    assert "id: x.id" in zeile and "status: x.status" in zeile and "wert: x.wert" in zeile
    j = html.index("const zeilen = r.punkte.map(p => checkZeile(p, anderes))")
    schleife = html[j:j + 2500]
    assert 'x.id === "app" && x.status === "warnung"' in schleife and "App jetzt aktualisieren" in schleife


GERAETE_DIALOG_KENNUNGEN = ['gfArt', 'gfName', 'gfFuellung', 'gfQualitaet', 'gfSek', 'gfBewegung', 'gfPaare', 'gfErinnerungen', 'gfFavoriten', 'gfGeburtstagsfotos', 'gfLayout', 'gfUebernehmen', 'gfOrtName', 'gfOrtSuche', 'gfOrtTreffer', 'gfHandys', 'gfKal', 'gfKalUser', 'gfKalPw', 'gfVorschau', 'gfKalTest', 'gfKalErg', 'gfKalWeg', 'gfKalWegZeile', 'gfNachtZeiten', 'gfQuellen', 'gfQuelleNeu', 'gfPlan', 'gfPlanNeu', 'gfNotizBlock', 'gfNotiz', 'gfNotizSenden', 'gfGaesteBlock', 'gfGaesteStart', 'gfGrussBlock', 'gfGrussSenden', 'gfWartung', 'gfAlarm', 'gfNeuladen', 'gfNeustart', 'gfZugangWeg', 'gfEinfach', 'gfEinfachAnlegen', 'gfTablet', 'gfCheck', 'gfNeustartTest', 'gfProblem', 'gfLadeVerlauf', 'gfRepLaden', 'gfRepStart', 'gfZugangWeg2', 'gfFullyHost', 'gfFullyPw', 'gfEinr', 'gfPruef', 'gfDatei', 'gfSpeichern', 'gfAbbrechen', 'gfLoeschen', 'gfDirekt', 'gfVerbindenZeile', 'gfKennungZeile', 'gfArtZeile']


def test_geraetedialog_hat_reiter_und_verliert_kein_feld():
    """Der Gerätedialog ist in Reiter geteilt (Allgemein, Anzeige, Programm, Tablet, Mehr); jedes Feld von früher gibt es weiter, jedes liegt in genau einem Reiter."""
    import re
    html = open(os.path.join(RAIZ, "static", "index.html"), encoding="utf-8").read()
    dialog = html[html.index('<div id="geraetDialog"'):html.index('<div id="verteilenDialog"')]
    for tab in ("allg", "anz", "prog", "tab", "mehr"):
        assert f'data-gt="{tab}"' in dialog, tab
        assert dialog.count(f'<div class="gf gt-panel') >= 5
    for kennung in GERAETE_DIALOG_KENNUNGEN:
        assert dialog.count(f'id="{kennung}"') == 1, kennung
    panels = re.split(r'<div class="gf gt-panel[^"]*" data-gt="', dialog)[1:]
    assert len(panels) == 5
    gesehen = {}
    for p in panels:
        tab = p[:p.index('"')]
        for kennung in GERAETE_DIALOG_KENNUNGEN:
            if f'id="{kennung}"' in p:
                assert kennung not in gesehen, (kennung, gesehen.get(kennung), tab)
                gesehen[kennung] = tab
    assert gesehen["gfName"] == "allg" and gesehen["gfLayout"] == "anz" and gesehen["gfQuellen"] == "prog" and gesehen["gfCheck"] == "tab" and gesehen["gfNotizBlock"] == "mehr"
    for kennung in ("gfArtZeile", "gfKennungZeile", "gfDirekt", "gfVerbindenZeile", "gfPaare", "gfUebernehmenZeile", "gfFavoriten", "gfGeburtstagsfotos",
                    "gfVorschau", "gfKalDetails", "gfGaesteBlock", "gfWartung", "gfNachtSonne"):                    # Technik und Feinheiten gehoeren in die Profi-Ansicht
        zeile = next(z for z in dialog.splitlines() if f'id="{kennung}"' in z)
        assert "nur-profi" in zeile, kennung
    assert "body:not(.profi) .nur-profi" in html


def test_allgemeine_einstellungen_haben_reiter_und_verlieren_kein_feld():
    import re
    html = open(os.path.join(RAIZ, "static", "index.html"), encoding="utf-8").read()
    dialog = html[html.index('<div id="einstDialog"'):html.index('<div id="assiDialog"')]
    panels = re.split(r'<div class="gf gt-panel[^"]*" data-gt="', dialog)[1:]
    assert len(panels) == 3
    kennungen = "eAnsicht eSek eFuellungR eFuellungT eOrtName eOrtSuche eOrtTreffer eKal eWetterTest eTelefone ePoUser ePoToken eAlarm ePoTest eMeldungen eMonatTest eAliase eAliasNeu ePinBlock ePinAlt ePinNeu ePinNeu2 ePinSetzen eAppQr eAppAdresse eTvUrl einstAbbrechen einstSpeichern".split()
    gesehen = {}
    for p in panels:
        tab = p[:p.index('"')]
        for k in kennungen:
            if f'id="{k}"' in p:
                assert k not in gesehen, (k, tab)
                gesehen[k] = tab
    for k in kennungen:
        assert k in gesehen or f'id="{k}"' in dialog, k
    assert gesehen["eSek"] == "allg" and gesehen["ePoUser"] == "meld" and gesehen["ePinNeu"] == "zug"
    for k in ("eFuellungT", "eKal", "eWetterTest", "eAlarm", "eMonatTest", "eAliase", "eTvUrl"):               # Feinheiten nur in der Profi-Ansicht
        i = dialog.index(f'id="{k}"')
        zeile_anfang = dialog.rfind("\n", 0, i)
        wrapper = dialog[max(0, dialog.rfind('<div class="nur-profi">', 0, i)):i]
        assert "nur-profi" in dialog[zeile_anfang:i] or ('<div class="nur-profi">' in wrapper and "</div>\n    </div>" not in wrapper), k
    assert "function etWaehlen(" in html and 'etWaehlen("allg")' in html
