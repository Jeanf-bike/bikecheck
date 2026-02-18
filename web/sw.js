// Service Worker — BikeCheck offline cache
const CACHE = 'bikecheck-v1';
const ASSETS = [
    './index.html',
    './style.css',
    './app.js',
    './manifest.json',
];

self.addEventListener('install', e => {
    e.waitUntil(
        caches.open(CACHE).then(cache => cache.addAll(ASSETS))
    );
    self.skipWaiting();
});

self.addEventListener('activate', e => {
    e.waitUntil(
        caches.keys().then(keys =>
            Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k)))
        )
    );
    self.clients.claim();
});

self.addEventListener('fetch', e => {
    // Weer-API altijd live ophalen (niet cachen)
    if (e.request.url.includes('openweathermap.org')) return;

    e.respondWith(
        caches.match(e.request).then(cached => cached || fetch(e.request).catch(() => cached))
    );
});
