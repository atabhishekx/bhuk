# Bhuk Animated Landing Page

Responsive animated one-page landing page with a hero, sticky navigation, menu filtering, 50 menu items, hover effects, animated marquee, scroll reveals, mobile navigation and a demo cart.

## Run locally
Extract the ZIP, open a terminal in this directory and run `python -m http.server 8000`, then visit `http://localhost:8000`. Use a local server because menu data is loaded from `assets/menu.json`.

## Included
- `index.html`: full responsive site and interactions.
- `assets/menu.json`: 50 menu items with IDs, descriptions, categories, INR prices, dietary labels and image URLs.

## Important
Images are loaded from public Unsplash image URLs, so internet access is required and some images may not match the exact dish. Replace with approved item-specific images before production. Cart behavior is a frontend demo only and is not connected to the Bhuk backend, checkout or payment APIs. Verify menu prices and dietary labels before release.
