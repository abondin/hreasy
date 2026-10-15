ALTER TABLE ba.business_account ADD COLUMN external_id varchar(255) NULL;
ALTER TABLE ba.business_account_history ADD COLUMN external_id varchar(255) NULL;

COMMENT ON COLUMN ba.business_account.external_id IS 'Business account identifier in external systems';
COMMENT ON COLUMN ba.business_account_history.external_id IS 'Business account identifier in external systems';

CREATE UNIQUE INDEX business_account_external_id_unique
    ON ba.business_account (external_id)
    WHERE external_id IS NOT NULL;
