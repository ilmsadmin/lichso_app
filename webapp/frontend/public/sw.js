/// <reference lib="webworker" />

const CACHE_NAME = "lichso-v1";
const STATIC_CACHE = "lichso-static-v1";
const API_CACHE = "lichso-api-v1";

// Static assets to pre-cache
const PRECACHE_URLS = [
  "/",
  "/favicon.svg",
  "/manifest.json",
];

// Install event — pre-cache static resources
self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(STATIC_CACHE).then((cache) => {
      return cache.addAll(PRECACHE_URLS);
    })
  );
  // Activate immediately
  self.skipWaiting();
});

// Activate event — clean old caches
self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames
          .filter((name) => {
            return (
              name !== CACHE_NAME &&
              name !== STATIC_CACHE &&
              name !== API_CACHE
            );
          })
          .map((name) => caches.delete(name))
      );
    })
  );
  self.clients.claim();
});

// Fetch event — strategy based on request type
self.addEventListener("fetch", (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // Skip non-GET requests
  if (request.method !== "GET") return;

  // Skip chrome-extension and other non-http(s) requests
  if (!url.protocol.startsWith("http")) return;

  // API requests — Network First, fallback to cache
  if (url.pathname.startsWith("/api/")) {
    event.respondWith(networkFirst(request, API_CACHE));
    return;
  }

  // Static assets (_next/static, images, fonts) — Cache First
  if (
    url.pathname.startsWith("/_next/static/") ||
    url.pathname.startsWith("/icons/") ||
    url.pathname.match(/\.(png|jpg|jpeg|svg|gif|ico|woff|woff2|ttf|eot)$/)
  ) {
    event.respondWith(cacheFirst(request, STATIC_CACHE));
    return;
  }

  // Navigation requests (HTML pages) — Network First
  if (request.mode === "navigate") {
    event.respondWith(networkFirst(request, CACHE_NAME));
    return;
  }

  // Default — Network First
  event.respondWith(networkFirst(request, CACHE_NAME));
});

// Cache First strategy
async function cacheFirst(request, cacheName) {
  const cached = await caches.match(request);
  if (cached) return cached;

  try {
    const response = await fetch(request);
    if (response.ok) {
      const cache = await caches.open(cacheName);
      cache.put(request, response.clone());
    }
    return response;
  } catch {
    return new Response("Offline", { status: 503 });
  }
}

// Network First strategy
async function networkFirst(request, cacheName) {
  try {
    const response = await fetch(request);
    if (response.ok) {
      const cache = await caches.open(cacheName);
      cache.put(request, response.clone());
    }
    return response;
  } catch {
    const cached = await caches.match(request);
    if (cached) return cached;

    // Fallback for navigation requests
    if (request.mode === "navigate") {
      const fallback = await caches.match("/");
      if (fallback) return fallback;
    }

    return new Response("Offline", { status: 503 });
  }
}
