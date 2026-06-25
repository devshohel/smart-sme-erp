-- Smart SME ERP baseline.
--
-- Existing customer databases are baselined at version 1 by:
--   spring.flyway.baseline-on-migrate=true
--
-- This migration is intentionally non-destructive. It marks the repository
-- migration chain starting point and does not attempt to recreate a legacy
-- production schema without an authoritative database dump.
SELECT 1;
