package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * Hot fix to avoid test fail because of lazy developer in V0.23 migration....
 * <p>
 * Never repeat it again:
 * <pre>
 * <code>-- You have to manually DROP PK constraint (i am too lazy to create a script for PK deletion). Something like
 * -- ALTER TABLE project_history DROP CONSTRAINT PK__project___096AA2E9E2C279C0 GO
 * </code>
 * </pre>
 * </p>
 */
public class V0_22_5__drop_pk_constraint extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try (var statement = context.getConnection().createStatement()) {
            try (var rows = statement.executeQuery("" +
                    "SELECT \n" +
                    "   A.CONSTRAINT_NAME FROM \n" +
                    "   INFORMATION_SCHEMA.TABLE_CONSTRAINTS A, \n" +
                    "   INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE B\n" +
                    "WHERE \n" +
                    "      CONSTRAINT_TYPE = 'PRIMARY KEY' \n" +
                    "   AND A.CONSTRAINT_NAME = B.CONSTRAINT_NAME\n" +
                    "   AND A.TABLE_NAME = 'project_history'\n" +
                    "   AND COLUMN_NAME='history_id'\n" +
                    "ORDER BY \n" +
                    "   A.TABLE_NAME")) {
                if (rows.next()) {
                    String pkName = rows.getString(1);
                    statement.execute("ALTER TABLE project_history DROP CONSTRAINT " + pkName);
                }
            }
        }
    }
}
