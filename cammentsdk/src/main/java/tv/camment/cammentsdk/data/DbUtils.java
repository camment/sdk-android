package tv.camment.cammentsdk.data;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;


class DbUtils {

    private static final String TAG = "DbUtils";

    DbUtils() { /* protected */ }

    /**
     * Helper class for creating DB tables.
     */
    static final class TableBuilder {
        private static final String TAG = DbUtils.TAG + "." + "TableBuilder";

        private static final int TYPE_INT = 1;
        private static final int TYPE_TEXT = 2;
        private String mTable;
        private String mID;
        private List<ColumnDef> mColumns = new ArrayList<>();
        private List<String> mUnique = new ArrayList<>();

        static TableBuilder table(String name) {
            TableBuilder helper = new TableBuilder();
            helper.mTable = name;
            return helper;
        }

        TableBuilder primaryKey(String ID) {
            this.mID = ID;
            return this;
        }

        private TableBuilder column(String name, int type, boolean notNull, boolean unique) {
            mColumns.add(new ColumnDef(name, type, notNull));
            if (unique) {
                mUnique.add(name);
            }
            return this;
        }

        TableBuilder columnIntNotNull(String name) {
            return column(name, TYPE_INT, true, false);
        }

        TableBuilder columnIntUnique(String name) {
            return column(name, TYPE_INT, true, true);
        }

        TableBuilder columnInt(String name) {
            return column(name, TYPE_INT, false, false);
        }

        TableBuilder columnTextNotNull(String name) {
            return column(name, TYPE_TEXT, true, false);
        }

        TableBuilder columnTextUnique(String name) {
            return column(name, TYPE_TEXT, true, true);
        }

        TableBuilder columnText(String name) {
            return column(name, TYPE_TEXT, false, false);
        }

        String build() {
            StringBuilder ddl = new StringBuilder();
            ddl.append("CREATE TABLE ").append(mTable).append(" ( \n");
            ddl.append(mID).append(" INTEGER PRIMARY KEY AUTOINCREMENT");
            for (ColumnDef c : mColumns) {
                ddl.append(",\n");
                ddl.append(c.name);
                switch (c.type) {
                    case TYPE_INT:
                        ddl.append(" INTEGER ");
                    case TYPE_TEXT:
                        ddl.append(" TEXT ");
                }
                if (c.notNull) {
                    ddl.append(" NOT NULL");
                }
            }
            if (mUnique.size() > 0) {
                ddl.append(",\n");
                ddl.append("UNIQUE (");
                boolean firstTime = true;
                for (String column : mUnique) {
                    if (firstTime) {
                        firstTime = false;
                    } else {
                        ddl.append(", ");
                    }
                    ddl.append(column);
                }
                ddl.append(") ON CONFLICT REPLACE");
            }
            ddl.append(")");
            String ddlString = ddl.toString();
            Log.d(TAG, ddlString);
            return ddlString;
        }

        private static class ColumnDef {
            String name;
            int type;
            boolean notNull;

            ColumnDef(String name, int type, boolean notNull) {
                super();
                this.name = name;
                this.type = type;
                this.notNull = notNull;
            }
        }

    }

}
