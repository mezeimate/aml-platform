CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
BEGIN
   IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'alert_status') THEN
      CREATE TYPE alert_status AS ENUM (
        'OPEN','INVESTIGATING','ESCALATED','DISMISSED','CLOSED'
      );
   END IF;
END$$;

CREATE TABLE IF NOT EXISTS alert (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  external_id   VARCHAR(64) UNIQUE,
  title         TEXT NOT NULL,
  status        alert_status NOT NULL DEFAULT 'OPEN',
  risk_score    NUMERIC(5,2) NOT NULL DEFAULT 0,
  source        VARCHAR(64) NOT NULL,
  detected_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  payload       JSONB NOT NULL DEFAULT '{}'::jsonb,
  labels        TEXT[] NOT NULL DEFAULT '{}'::text[],
  assigned_to   VARCHAR(128),
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS ix_alert_status      ON alert (status);
CREATE INDEX IF NOT EXISTS ix_alert_detected_at ON alert (detected_at DESC);
CREATE INDEX IF NOT EXISTS ix_alert_risk_score  ON alert (risk_score DESC);
CREATE INDEX IF NOT EXISTS ix_alert_labels      ON alert USING GIN (labels);

CREATE INDEX IF NOT EXISTS ix_alert_payload_gin ON alert USING GIN (payload jsonb_path_ops);

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
