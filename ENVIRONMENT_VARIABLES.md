# Environment Variables

Production must run with `SPRING_PROFILES_ACTIVE=prod`.

Required backend variables:

| Variable | Purpose | Example |
| --- | --- | --- |
| `SERVER_PORT` | Spring Boot HTTP port | `8080` |
| `DB_URL` | MySQL JDBC URL | `jdbc:mysql://db-host:3306/sme?useSSL=true&serverTimezone=UTC` |
| `DB_USERNAME` | MySQL application user | `sme_app` |
| `DB_PASSWORD` | MySQL application password | set in secret manager |
| `JWT_SECRET` | HS256 signing secret, at least 32 bytes | set in secret manager |
| `CORS_ALLOWED_ORIGINS` | Comma-separated production origins | `https://erp.example.com` |

Optional backend variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `JWT_ACCESS_EXPIRATION_MS` | `900000` | Access token lifetime |
| `JWT_REFRESH_EXPIRATION_MS` | `1209600000` | Refresh token lifetime |
| `JWT_CLOCK_SKEW_SECONDS` | `60` | JWT clock skew |
| `MAX_FAILED_LOGIN_ATTEMPTS` | `5` | Account lock threshold |
| `ACCOUNT_LOCK_MINUTES` | `15` | Lock duration |
| `LOG_FILE` | `logs/smart-sme-erp.log` | Active log file path |
| `LOG_MAX_FILE_SIZE` | `20MB` | Rolling log file size |
| `LOG_MAX_HISTORY` | `30` | Retained rolling files |
| `LOG_TOTAL_SIZE_CAP` | `2GB` | Total log retention cap |

Frontend production builds use same-origin API routing by default:

```ts
apiUrl: '/api/v1'
```

For reverse proxy deployment, route `/api/` to the Spring Boot service and serve Angular static files from the same public origin.
