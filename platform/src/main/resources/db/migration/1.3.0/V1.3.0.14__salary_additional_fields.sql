ALTER TABLE sal.salary_request  ADD COLUMN IF NOT EXISTS
    req_new_position integer NULL REFERENCES dict.position (id);

COMMENT ON COLUMN sal.salary_request.req_new_position IS 'If new position requested';
COMMENT ON COLUMN sal.salary_request.impl_new_position IS 'If new position requested';


