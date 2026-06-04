ALTER TABLE udr.junior_report ADD COLUMN IF NOT EXISTS
    ratings JSONB NULL;
update udr.junior_report set ratings = jsonb('{
                                             	"competence": 1,
                                             	"process": 1,
                                             	"teamwork": 1,
                                             	"contribution": 1,
                                             	"motivation": 1
                                             }');
ALTER TABLE udr.junior_report
    ALTER COLUMN ratings SET NOT NULL,
    ALTER COLUMN ratings SET DEFAULT jsonb('{
        "competence": 1,
        "process": 1,
        "teamwork": 1,
        "contribution": 1,
        "motivation": 1
    }');
COMMENT ON COLUMN udr.junior_report.ratings IS 'Ratings json:
{
"competence": 1,
"process": 1,
"teamwork": 1,
"contribution": 1,
"motivation": 1
}
';
