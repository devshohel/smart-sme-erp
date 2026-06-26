# Audit Architecture

Smart SME ERP records operational and security activity through two complementary tables:

- `activity_logs`: user-facing activity timeline with user, action, module, entity, IP address, user agent, old values, new values, and retention archive metadata.
- `audit_logs`: detailed record-level old/new JSON snapshots for business data changes.
- `login_history`: authentication success and failure history.

## Tracked Actions

The audit layer classifies action names into these compliance action types:

- `CREATE`
- `UPDATE`
- `DELETE`
- `RESTORE`
- `APPROVE`
- `REJECT`
- `POST`
- `CANCEL`
- `REVERSE`
- `LOGIN`
- `LOGOUT`
- `PASSWORD_CHANGE`

Refresh token use, failed logins, lockouts, and password changes are recorded as security activity.

## Modules Covered

Current logging coverage includes:

- Products, categories, brands, UOM
- Customers and suppliers
- Users, roles, and permissions
- Warehouses and inventory workflows where service methods emit activity logs
- Sales orders, sales invoices, sales returns
- Purchase orders, purchase receipts through purchase order receipt flow, purchase returns
- Expenses, supplier payments, customer receipts
- Stock adjustments and stock transfers

Where a service writes both `activity_logs` and `audit_logs`, the activity row gives the investigation timeline and the audit row gives the old/new JSON payload.

## API Endpoints

- `GET /api/v1/audit/activity-logs`
- `GET /api/v1/audit/activity-logs/export`
- `GET /api/v1/audit/activity-logs/export-excel`
- `GET /api/v1/audit/activity-logs/history/{entityName}/{entityId}`
- `GET /api/v1/audit/audit-logs`
- `GET /api/v1/audit/login-history`
- `GET /api/v1/audit/retention`
- `POST /api/v1/audit/activity-logs/archive-expired`
- `POST /api/v1/audit/archive-expired`

## Permissions

- `ACTIVITY_VIEW`: view activity history.
- `ACTIVITY_LOG_VIEW`: legacy-compatible activity log view permission.
- `AUDIT_VIEW`: view audit/security records and retention policy.
- `AUDIT_EXPORT`: export logs and archive expired logs.
- `ACTIVITY_LOG_EXPORT`: legacy-compatible activity export permission.

## Retention Policy

Defaults:

- Activity logs: 2 years.
- Security logs: 5 years.

Configuration:

- `ACTIVITY_LOG_RETENTION_YEARS`
- `SECURITY_LOG_RETENTION_YEARS`

Expired logs are archived by setting `archived_at` and `archive_reason`. Production logs are not automatically deleted by application startup or scheduled jobs.

## Performance Notes

The migration adds indexes for activity date, action type, entity history, and archive state. Keep the export cap conservative for browser downloads. For large production tenants, run archive operations during maintenance windows and export through reporting infrastructure if full-history extracts become large.

## Limitations

Audit coverage depends on service-layer calls and the activity aspect. Any future module that bypasses these service patterns must explicitly emit activity/audit records or be annotated/wired into the same aspect path. The product details screen has an entity history tab; other detail screens can reuse the same endpoint and `AuthService.getActivityHistory(...)` pattern.
