# Audit Log Guide

## Logged Actions

The ERP records security and business activity in `activity_logs`.

Core actions:

- `CREATE`
- `UPDATE`
- `DELETE`
- `RESTORE`
- `LOGIN`
- `LOGOUT`
- `APPROVE`
- `REJECT`
- `POST`
- `REVERSE`

Existing domain-specific actions such as `PRODUCT_CREATE`, `SALES_INVOICE_POST`, and `LOGIN_FAILED` remain supported for backward compatibility.

## Database Tables

Primary audit table:

- `activity_logs`

Important columns:

- `user_id`
- `username`
- `action`
- `module`
- `table_name`
- `record_id`
- `entity_id`
- `old_value`
- `new_value`
- `ip_address`
- `user_agent`
- `details`
- `created_at`

Flyway migration `V6__audit_trail_activity_log_hardening.sql` also adds nullable audit metadata columns to the main master-data and transaction tables:

- `created_at`
- `created_by`
- `updated_at`
- `updated_by`
- `deleted_at`
- `deleted_by`
- `restored_at`
- `restored_by`

These columns are nullable and data-preserving so existing customer databases can upgrade safely.

## Automatic Logging

Activity logging is centralized in backend services.

- Existing explicit service-level activity logs remain the source of detailed domain messages.
- A service AOP fallback records mutating operations that do not already write an activity log.
- Controller-level manual logging is not required.
- Request context captures IP address and user agent when an HTTP request is available.

## Permissions

New permissions:

- `ACTIVITY_LOG_VIEW`
- `ACTIVITY_LOG_EXPORT`

Legacy `ACTIVITY_VIEW` remains accepted by the backend and frontend to avoid breaking existing roles.

## UI

Activity logs are available under:

Settings -> Activity Logs

Supported UI features:

- Date filter
- User filter
- Module filter
- Action filter
- Search
- Pagination
- CSV export

## Retention Policy

Recommended production retention:

- Keep at least 1 year of activity logs online.
- Archive older logs to low-cost storage before deletion.
- Do not delete audit logs without written business approval.

No automatic purge job is enabled by this migration.

## Performance Considerations

The migration creates indexes for common filters:

- `created_at`
- `username`
- `action`
- `module`

For very large installations, consider monthly partitioning by `created_at` and an archive process outside business hours.

## Rollback Strategy

This migration only adds tables, columns, indexes, and permissions. It does not remove production data.

Rollback options:

- Leave added columns/tables in place and disable the UI permission.
- Revoke `ACTIVITY_LOG_VIEW` and `ACTIVITY_LOG_EXPORT` from non-admin roles.
- If a DBA must remove objects, export `activity_logs` first.

## Remaining Limitations

The audit metadata columns are added safely at the database level. Some legacy entities still populate only their existing audit fields until each module is migrated to a shared mapped audit base. The activity log table is the authoritative trace for who, what, when, IP, and user-agent accountability.
