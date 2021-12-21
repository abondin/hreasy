CREATE SCHEMA IF NOT EXISTS article;

CREATE SEQUENCE IF NOT EXISTS article.ARTICLE_ID_SEQ;
CREATE TABLE IF NOT EXISTS article.article (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('article.ARTICLE_ID_SEQ'),
	article_group varchar(255) NULL,
	"name" varchar(255) NULL,
	description text NULL,
	"content" text NULL,
	moderated boolean NOT NULL default TRUE,
	archived boolean NOT NULL default FALSE,
	created_at timestamp with time zone NULL,
	created_by integer NULL REFERENCES empl.employee (id),
	updated_at timestamp with time zone NULL,
	updated_by integer NULL REFERENCES empl.employee (id)
);

COMMENT ON TABLE article.article IS 'Article';
COMMENT ON COLUMN article.article.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN article.article.article_group IS 'Article group. In business logic only shared is now supported (2021/11/21)';
COMMENT ON COLUMN article.article.name IS 'Article name';
COMMENT ON COLUMN article.article.description IS 'Short article description';
COMMENT ON COLUMN article.article.content IS 'The article content';
COMMENT ON COLUMN article.article.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN article.article.moderated IS 'Only moderated articles show to all users';
COMMENT ON COLUMN article.article.archived IS 'Archived articles hide from interface';
COMMENT ON COLUMN article.article.created_at IS 'Created at';
COMMENT ON COLUMN article.article.created_by IS 'Created by (link to employee)';
COMMENT ON COLUMN article.article.updated_at IS 'Updated at';
COMMENT ON COLUMN article.article.updated_by IS 'Updated by (link to employee)';


CREATE SEQUENCE IF NOT EXISTS article.ARTICLE_HISTORY_ID_SEQ;
CREATE TABLE IF NOT EXISTS article.article_history (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('article.ARTICLE_HISTORY_ID_SEQ'),
	article_id integer NOT NULL,
	article_group varchar(255) NULL,
	"name" varchar(255) NULL,
	description text NULL,
	"content" text NULL,
	moderated boolean NOT NULL default TRUE,
	archived boolean NOT NULL default FALSE,
	created_at timestamp with time zone NULL,
	created_by integer NULL
);

COMMENT ON TABLE article.article_history IS 'Article History';
COMMENT ON COLUMN article.article_history.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN article.article_history.article_id IS 'Link to article';
COMMENT ON COLUMN article.article_history.article_group IS 'Article group. In business logic only shared is now supported (2021/11/21)';
COMMENT ON COLUMN article.article_history.name IS 'Article name';
COMMENT ON COLUMN article.article_history.description IS 'Short article description';
COMMENT ON COLUMN article.article_history.content IS 'The article content';
COMMENT ON COLUMN article.article_history.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN article.article_history.moderated IS 'Only moderated articles show to all users';
COMMENT ON COLUMN article.article_history.archived IS 'Archived articles hide from interface';
COMMENT ON COLUMN article.article_history.created_at IS 'Record created at';
COMMENT ON COLUMN article.article_history.created_by IS 'Record created by (link to employee)';
