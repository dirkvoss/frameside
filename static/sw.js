// Service Worker: schnellerer Start + freundliche Offline-Meldung. Daten (/api) werden NIE zwischengespeichert; nur die grossen Fotos fuer Rahmen/Fernseher (/api/vorschau/...?s=gross|voll)
// werden kurz aufgehoben, damit ein Rahmen bei kurzem Netzausfall weiterlaeuft (zeigt dann schon gesehene Fotos). Braucht HTTPS (oder localhost).
const VERSION = "v1";
const BILDER = "bilder-v1", BILDER_MAX = 150;
const SCHALE = ["/", "/manifest.webmanifest", "/icon-192.png", "/icon-512.png", "/apple-touch-icon.png"];
self.addEventListener("install", e => { e.waitUntil(caches.open(VERSION).then(c => c.addAll(SCHALE)).then(() => self.skipWaiting())); });
self.addEventListener("activate", e => {
  e.waitUntil(caches.keys().then(ks => Promise.all(ks.filter(k => k !== VERSION && k !== BILDER).map(k => caches.delete(k)))).then(() => self.clients.claim()));
});
self.addEventListener("fetch", e => {
  const u = new URL(e.request.url);
  if (e.request.method !== "GET" || u.origin !== location.origin) return;
  if (u.pathname.startsWith("/api/vorschau/") && /[?&]s=(gross|voll)\b/.test(u.search)) { e.respondWith(bildAbrufen(e.request)); return; }
  if (u.pathname.startsWith("/api/")) return;
  // Oberflaeche: erst Netz (damit Updates sofort ankommen), sonst Zwischenspeicher
  e.respondWith(fetch(e.request).then(r => { if (r.ok) { const k = r.clone(); caches.open(VERSION).then(c => c.put(e.request, k)); } return r; })
    .catch(() => caches.match(e.request).then(r => r || caches.match("/"))));
});

async function bildAbrufen(anfrage) {
  const c = await caches.open(BILDER);
  try {
    const r = await Promise.race([fetch(anfrage), new Promise((_, nein) => setTimeout(() => nein(new Error("zeit")), 15000))]);
    if (r.ok) { c.put(anfrage, r.clone()).then(() => c.keys()).then(ks => { for (const k of ks.slice(0, Math.max(0, ks.length - BILDER_MAX))) c.delete(k); }).catch(() => {}); }
    return r;
  } catch (fehler) {                                              // Netz weg: dasselbe Foto aus dem Speicher, sonst irgendein schon gesehenes
    const treffer = await c.match(anfrage);
    if (treffer) return treffer;
    const ks = await c.keys();
    if (ks.length) return c.match(ks[Math.floor(Math.random() * ks.length)]);
    return Response.error();
  }
}
