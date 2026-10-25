# Bhuk Frontend — Enhanced UI Edition

This ZIP contains the existing React + TypeScript + Vite frontend, restyled while preserving the current routes, authentication flow, API client, cart, checkout, orders, menu, staff/delivery/admin pages and existing source components.

## Run locally
Requirements: Node.js version compatible with the project's Vite version, and the Bhuk backend available for API-driven features.

```bash
npm install
npm run dev
```

## Production build
```bash
npm run build
npm run preview
```

## API configuration
The existing Axios client still uses `/api/v1` and reads the `bhuk_token` token from local storage. Configure the Vite development proxy or deployment reverse proxy as you already do for the backend. No API routes or request payloads were intentionally changed.

## UI improvements
- Refined Bhuk brand styling, typography, warm cream canvas and food-inspired orange accents.
- Sticky glass-effect responsive navigation with mobile menu and role-aware links.
- Updated shared card, button, form, focus and hover states.
- Improved menu-card image treatment and responsive spacing.
- Reduced-motion accessibility preference retained.

## Important
This is the existing app's frontend source, not a replacement standalone demo. Backend-dependent flows still require your backend and valid configuration. `node_modules` is excluded from the ZIP; `npm install` restores dependencies from the retained lockfile.
