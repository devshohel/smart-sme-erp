# Deployment Checklist

## Before Deployment

- [ ] `SPRING_PROFILES_ACTIVE=prod` is set.
- [ ] `SERVER_PORT`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `CORS_ALLOWED_ORIGINS` are set.
- [ ] `JWT_SECRET` is at least 32 bytes and stored securely.
- [ ] `CORS_ALLOWED_ORIGINS` contains only production HTTPS origins.
- [ ] MySQL user has only required privileges for the ERP database.
- [ ] Flyway migrations have been reviewed and backed up against production data.
- [ ] Angular build uses `environment.prod.ts` with `/api/v1`.

## Deployment

- [ ] Run backend compile verification.
- [ ] Run backend test-compile verification.
- [ ] Run frontend production build.
- [ ] Deploy Angular static files.
- [ ] Deploy Spring Boot artifact.
- [ ] Apply Nginx reverse proxy configuration.
- [ ] Enable TLS.
- [ ] Restart services in order: MySQL, backend, frontend or Nginx.

## After Deployment

- [ ] Open `/actuator/health/readiness`.
- [ ] Open the Angular app and confirm login.
- [ ] Verify API calls use the production origin, not localhost.
- [ ] Confirm logs include `traceId`.
- [ ] Confirm file uploads persist to production storage.
- [ ] Confirm backups and restore procedure.
- [ ] Confirm firewall blocks public MySQL access.
