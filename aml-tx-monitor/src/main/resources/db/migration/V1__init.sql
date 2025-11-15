-- ============================
--  V1 INIT for aml-tx-monitor
--  PostgreSQL 16 schema
--  - Core: alert table
--  - Enums: alert_status, alert_severity
--  - JSONB + TEXT[] with GIN
--  - Partial unique index for open/investigating dedupe
-- ============================

-- UUID generation (gen_random_uuid)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ----- Domain enums -----
-- Lifecycle status of an alert + Business severity
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'alert_status') THEN
    CREATE TYPE alert_status AS ENUM ('OPEN','INVESTIGATING','ESCALATED','DISMISSED','CLOSED');
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'alert_severity') THEN
    CREATE TYPE alert_severity AS ENUM ('LOW','MEDIUM','HIGH','CRITICAL');
  END IF;
END $$;

-- ----- Core table -----
CREATE TABLE IF NOT EXISTS alert (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

  -- Idempotency / external references
  external_id     VARCHAR(64) UNIQUE,       -- optional external reference (e.g., upstream ID)
  dedupe_key      VARCHAR(128),             -- used to group identical alerts (idempotency)

  -- Core content
  title           TEXT NOT NULL,
  status          alert_status   NOT NULL DEFAULT 'OPEN',
  severity        alert_severity NOT NULL DEFAULT 'MEDIUM',
  risk_score      NUMERIC(5,2)   NOT NULL DEFAULT 0
                  CHECK (risk_score >= 0 AND risk_score <= 100),

  source          VARCHAR(64)    NOT NULL,  -- e.g. "rule-engine", "ingest-api", "batch"
  rule_id         VARCHAR(64),              -- which rule produced it (free-form string/UUID)
  rule_version    VARCHAR(32),

  -- Business linkage (optional foreign keys to be introduced later)
  customer_id     VARCHAR(64),
  account_id      VARCHAR(64),

  -- Timing & payload
  detected_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
  payload         JSONB          NOT NULL DEFAULT '{}'::jsonb,
  labels          TEXT[]         NOT NULL DEFAULT '{}'::text[],

  -- Ownership & audit
  assigned_to     VARCHAR(128),
  created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ    NOT NULL DEFAULT now()
);

-- ----- Indexes for common queries -----
-- Status filter
CREATE INDEX IF NOT EXISTS ix_alert_status
  ON alert (status);

-- Recent first
CREATE INDEX IF NOT EXISTS ix_alert_detected_at
  ON alert (detected_at DESC);

-- High risk first
CREATE INDEX IF NOT EXISTS ix_alert_risk_score
  ON alert (risk_score DESC);

-- Labels array membership (ANY/contains)
CREATE INDEX IF NOT EXISTS ix_alert_labels_gin
  ON alert USING GIN (labels);

-- JSONB search (path operators / jsonpath)
CREATE INDEX IF NOT EXISTS ix_alert_payload_gin
  ON alert USING GIN (payload jsonb_path_ops);

-- Business lookups
CREATE INDEX IF NOT EXISTS ix_alert_customer_detected
  ON alert (customer_id, detected_at DESC);

CREATE INDEX IF NOT EXISTS ix_alert_source_detected
  ON alert (source, detected_at DESC);

-- Dedupe only for active alerts:
--  - Enforce uniqueness of dedupe_key while the alert is OPEN or INVESTIGATING.
--    (Closed/dismissed ones won't block new alerts with the same key.)
CREATE UNIQUE INDEX IF NOT EXISTS ux_alert_open_dedupe
  ON alert (dedupe_key)
  WHERE status IN ('OPEN'::alert_status,'INVESTIGATING'::alert_status);

-- Optional: ensure dedupe_key is not empty string if present
-- ALTER TABLE alert ADD CONSTRAINT chk_dedupe_key_not_empty CHECK (dedupe_key IS NULL OR length(dedupe_key) > 0);

-- ----- updated_at auto-maintenance -----
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at := now();
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'tr_alert_set_updated_at') THEN
    CREATE TRIGGER tr_alert_set_updated_at
      BEFORE UPDATE ON alert
      FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;

-- ----- Helpful comments -----
COMMENT ON TABLE alert IS 'Core alerts table created by rules/ingest; managed by analysts and workflows.';
COMMENT ON COLUMN alert.dedupe_key IS 'Business key for idempotency. Uniqueness enforced only for OPEN/INVESTIGATING.';
COMMENT ON COLUMN alert.payload IS 'Raw contextual data (JSONB). GIN-indexed for fast filtering.';
COMMENT ON COLUMN alert.labels IS 'Free-form tags for search and faceting.';
