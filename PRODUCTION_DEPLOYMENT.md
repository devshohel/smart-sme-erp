# Production Deployment

## Build

Backend:

```powershell
cd sme
mvnw.cmd clean compile
mvnw.cmd test-compile
```

Frontend:

```powershell
cd NexaOne
npm ci
npm run build
```

The Angular production build outputs to `NexaOne/dist/NexaOne`.

## Backend Runtime

Run Spring Boot with:

```powershell
java -jar smart-sme-erp.jar --spring.profiles.active=prod
```

All production secrets must come from environment variables. Do not commit `.env` files containing production credentials.

## Reverse Proxy

Use `deploy/nginx/smart-sme-erp.conf` as the baseline Nginx configuration.

Required behavior:

- Serve Angular static files.
- Forward `/api/` to Spring Boot.
- Keep `/api/v1` same-origin from the browser.
- Enable gzip.
- Cache hashed assets.
- Fall back SPA routes to `index.html`.
- Forward `X-Forwarded-*` and `X-Request-ID` headers.

## Health Endpoints

Production exposes only health endpoints:

- `/actuator/health`
- `/actuator/health/liveness`
- `/actuator/health/readiness`

Readiness includes database connectivity.

## Optional Docker

Docker support is optional:

```powershell
docker compose up --build
```

Provide the required variables from `ENVIRONMENT_VARIABLES.md` before starting the compose stack.

## Manual Infrastructure Tasks

These tasks must be completed outside the repository:

- VPS or server provisioning.
- DNS record creation.
- TLS certificate installation and renewal.
- Nginx installation and service hardening.
- Firewall rules for ports `80`, `443`, and restricted database access.
- MySQL backup scheduling and restore testing.
- Secret manager or secure environment variable injection.
