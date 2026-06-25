# Smart SME ERP Database Migration Guide

## Strategy

Smart SME ERP now uses Flyway for repeatable version-to-version database upgrades.
The project is not treated as greenfield-only: existing customer databases are
baselined at version `1`, then incremental migrations are applied.

Hibernate schema mutation is profile-specific:

- Development: `spring.jpa.hibernate.ddl-auto=update`
- Production: `spring.jpa.hibernate.ddl-auto=validate`

Production deployments must not rely on Hibernate to mutate schema.

## Flyway Version History

| Version | File | Purpose |
| --- | --- | --- |
| 1 | `V1__baseline_existing_schema.sql` | Baselines existing customer databases without destructive DDL. |
| 2 | `V2__safe_legacy_schema_compatibility.sql` | Converts safe manual customer, supplier, warehouse, and sales item compatibility fixes into conditional Flyway DDL/backfills. |
| 3 | `V3__enum_contract_alignment.sql` | Aligns known MySQL enum columns with Java and Angular enum values. |
| 4 | `V4__stock_movement_legacy_column_alignment.sql` | Adds authoritative stock movement columns and backfills from legacy columns where present. |

## Migration Order

1. Take a full database backup.
2. Deploy the application package containing the Flyway migration files.
3. Start the application with the intended profile.
4. Flyway baselines existing non-empty schemas at version `1`.
5. Flyway applies versions `2+` in order.
6. Hibernate validates the schema in production.

## Existing Customer Upgrade Process

Use the `prod` profile for production upgrades:

```bash
java -jar sme.jar --spring.profiles.active=prod
```

Before upgrade:

- Confirm a backup exists.
- Review enum columns for unsupported values.
- Review legacy ID columns that may be missing `AUTO_INCREMENT`.
- Review stock movement legacy columns before removing anything.

After upgrade:

- Check the `flyway_schema_history` table.
- Confirm the application starts with `ddl-auto=validate`.
- Smoke-test login, products, customers, suppliers, expenses, sales, purchases,
  inventory, and payments.

## Baseline Strategy

The repository does not currently include a complete production schema dump.
Therefore `V1__baseline_existing_schema.sql` is a non-destructive baseline marker.
For an existing non-empty database, Flyway records version `1` and applies later
migrations.

For a fully production-ready fresh installation, an authoritative schema export
must be added as a future replacement baseline or a versioned creation migration.
Until then, fresh development databases can still be created by running the
application with the `dev` profile, where Hibernate update remains enabled.

## Rollback Strategy

Flyway migrations in this phase are additive or compatibility-oriented. They do
not drop columns or delete rows.

Rollback options:

- Preferred: restore the pre-upgrade database backup.
- For enum failures: correct unsupported values manually, then rerun migration.
- For added nullable columns: leave them in place unless a DBA-approved rollback
  plan removes them during maintenance.
- For backfilled stock movement columns: restore affected rows from backup if
  the legacy-to-authoritative mapping was incorrect.

## Manual DBA Review Required

Some changes cannot be safely automated without live schema inspection.

### customers.id

Table: `customers`

Column: `id`

Current state: may exist without `AUTO_INCREMENT` in legacy databases.

Required state: `BIGINT NOT NULL AUTO_INCREMENT`

Required SQL:

```sql
ALTER TABLE customers
    MODIFY id BIGINT NOT NULL AUTO_INCREMENT;
```

Risk level: Medium.

Manual action required: Yes, because foreign keys and existing table definition
must be inspected first.

### suppliers.id

Table: `suppliers`

Column: `id`

Current state: may exist without `AUTO_INCREMENT` in legacy databases.

Required state: `BIGINT NOT NULL AUTO_INCREMENT`

Required SQL:

```sql
ALTER TABLE suppliers
    MODIFY id BIGINT NOT NULL AUTO_INCREMENT;
```

Risk level: Medium.

Manual action required: Yes, because MySQL may require temporarily dropping and
recreating dependent foreign keys.

### warehouses final constraints

Table: `warehouses`

Columns: `warehouse_code`, `warehouse_name`

Current state: migration adds and backfills the columns when possible, but does
not force `NOT NULL` if legacy data is incomplete.

Required state: populated final columns suitable for backend validation.

Required SQL after confirming no nulls:

```sql
ALTER TABLE warehouses
    MODIFY warehouse_code VARCHAR(255) NOT NULL,
    MODIFY warehouse_name VARCHAR(255) NOT NULL;
```

Risk level: Medium.

Manual action required: Yes if any warehouse row cannot be safely backfilled.

### stock_movements legacy columns

Table: `stock_movements`

Legacy columns: `type`, `reference`

Authoritative columns: `movement_type`, `reference_type`, `reference_no`

Current state: migration adds/backfills authoritative columns when legacy columns
exist. It does not drop legacy columns.

Required SQL after audit, optional only:

```sql
-- Run only after all reports and services use movement_type/reference_no.
-- ALTER TABLE stock_movements DROP COLUMN type;
-- ALTER TABLE stock_movements DROP COLUMN reference;
```

Risk level: High.

Manual action required: Yes. Do not drop production data automatically.

## Enum Contract

Known enum contracts after this phase:

- `AccountingPaymentMethod`: `CASH`, `BANK`, `MOBILE_BANKING`, `OTHER`
- `SupplierPaymentStatus`: `DRAFT`, `POSTED`, `CANCELLED`, `REVERSED`
- `SupplierPaymentMethod`: `CASH`, `BANK`, `MOBILE_BANKING`, `CHEQUE`, `OTHER`
- `CustomerReceiptStatus`: `DRAFT`, `POSTED`, `CANCELLED`
- `CustomerReceiptPaymentMethod`: `CASH`, `BANK`, `MOBILE_BANKING`, `CHEQUE`, `OTHER`
- `PurchaseStatus`: `DRAFT`, `SUBMITTED`, `PENDING`, `APPROVED`, `REJECTED`,
  `PARTIAL_RECEIVED`, `RECEIVED`, `POSTED`, `PARTIAL_PAID`, `PAID`, `REVERSED`,
  `CANCELLED`
- `SalesOrderStatus`: `DRAFT`, `SUBMITTED`, `APPROVED`, `REJECTED`, `CONVERTED`,
  `CANCELLED`, `PENDING`
- `SalesInvoiceStatus`: `DRAFT`, `SUBMITTED`, `APPROVED`, `POSTED`,
  `PARTIAL_PAID`, `PAID`, `CANCELLED`, `REVERSED`, `PENDING`, `CONFIRMED`,
  `COMPLETED`
- `SalesPaymentStatus`: `PAID`, `PARTIAL`, `DUE`
- `ExpenseStatus`: `DRAFT`, `SUBMITTED`, `APPROVED`, `REJECTED`, `POSTED`,
  `REVERSED`, `CANCELLED`

## Fresh Installation Status

Current status: partially supported.

An empty development database can be created with the `dev` profile because
Hibernate update remains enabled there. A fresh production database still needs
an authoritative creation migration generated from a verified production schema
dump. The repository did not contain that dump during this phase, so a complete
fresh production install cannot honestly be guaranteed yet.

Required follow-up:

1. Export a verified production schema-only dump.
2. Convert it into a new authoritative creation migration.
3. Test an empty database with `spring.profiles.active=prod`.
4. Confirm first-login seed data is present and authentication succeeds.

## Deployment Notes

- Never run production with `spring.jpa.hibernate.ddl-auto=update`.
- Keep manual scripts under `src/main/resources/db/manual` as audit references
  only; future executable changes should be Flyway migrations.
- Do not edit applied Flyway migration files. Add a new version instead.
- Run migrations first in staging against a copy of production data.
