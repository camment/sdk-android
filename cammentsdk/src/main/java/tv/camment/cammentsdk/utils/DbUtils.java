package tv.camment.cammentsdk.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by petrushka on 11/08/2017.
 */

public class DbUtils {

    public static final String TAG = "DbUtils";

    DbUtils() { /* protected */ }

    /**
     * Helper class for creating DB tables.
     */
    public static final class TableBuilder {
        private static final String TAG = DbUtils.TAG + "." + "TableBuilder";

        private static final int TYPE_INT = 1;
        private static final int TYPE_TEXT = 2;
        private String mTable;
        private String mID;
        private List<ColumnDef> mColumns = new ArrayList<ColumnDef>();
        private List<String> mUnique = new ArrayList<String>();

        public static TableBuilder table(String name) {
            TableBuilder helper = new TableBuilder();
            helper.mTable = name;
            return helper;
        }

        public TableBuilder primaryKey(String ID) {
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

        public TableBuilder columnIntNotNull(String name) {
            return column(name, TYPE_INT, true, false);
        }

        public TableBuilder columnIntUnique(String name) {
            return column(name, TYPE_INT, true, true);
        }

        public TableBuilder columnInt(String name) {
            return column(name, TYPE_INT, false, false);
        }

        public TableBuilder columnTextNotNull(String name) {
            return column(name, TYPE_TEXT, true, false);
        }

        public TableBuilder columnTextUnique(String name) {
            return column(name, TYPE_TEXT, true, true);
        }

        public TableBuilder columnText(String name) {
            return column(name, TYPE_TEXT, false, false);
        }

        public String build() {
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

            public ColumnDef(String name, int type, boolean notNull) {
                super();
                this.name = name;
                this.type = type;
                this.notNull = notNull;
            }
        }

    }

    /**
     * Helper class for selection from DB creation.
     */
    public static final class SelectionBuilder {

        private String mTable = null;
        private static final String TAG = DbUtils.TAG + "." + "SelectionBuilder";
        private Map<String, String> mProjectionMap = new HashMap<String, String>();
        private StringBuilder mSelection = new StringBuilder();
        private ArrayList<String> mSelectionArgs = new ArrayList<String>();

        /**
         * Reset any internal state, allowing this builder to be recycled.
         */
        public SelectionBuilder reset() {
            mTable = null;
            mSelection.setLength(0);
            mSelectionArgs.clear();
            return this;
        }

        /**
         * Append the given equal selection clause to the internal state. Each clause is surrounded with parenthesis and combined using
         * {@code AND}.
         */
        public SelectionBuilder whereEquals(String column, String value) {
            return where(column + "=? ", value);
        }

        /**
         * Append the given selection clause to the internal state. Each clause is surrounded with parenthesis and combined using {@code AND}.
         */
        public SelectionBuilder where(String selection, String... selectionArgs) {
            if (TextUtils.isEmpty(selection)) {
                if (selectionArgs != null && selectionArgs.length > 0) {
                    throw new IllegalArgumentException("Valid selection required when including arguments=");
                }

                // Shortcut when clause is empty
                return this;
            }

            if (mSelection.length() > 0) {
                mSelection.append(" AND ");
            }

            mSelection.append("(").append(selection).append(")");
            if (selectionArgs != null) {
                for (String arg : selectionArgs) {
                    mSelectionArgs.add(arg);
                }
            }

            return this;
        }

        public SelectionBuilder table(String table) {
            mTable = table;
            return this;
        }

        private void assertTable() {
            if (mTable == null) {
                throw new IllegalStateException("Table not specified");
            }
        }

        public SelectionBuilder mapToTable(String column, String table) {
            mProjectionMap.put(column, table + "." + column);
            return this;
        }

        public SelectionBuilder map(String fromColumn, String toClause) {
            mProjectionMap.put(fromColumn, toClause + " AS " + fromColumn);
            return this;
        }

        /**
         * Return selection string for current internal state.
         *
         * @see #getSelectionArgs()
         */
        public String getSelection() {
            return mSelection.toString();
        }

        /**
         * Return selection arguments for current internal state.
         *
         * @see #getSelection()
         */
        public String[] getSelectionArgs() {
            return mSelectionArgs.toArray(new String[mSelectionArgs.size()]);
        }

        private void mapColumns(String[] columns) {
            for (int i = 0; i < columns.length; i++) {
                final String target = mProjectionMap.get(columns[i]);
                if (target != null) {
                    columns[i] = target;
                }
            }
        }

        @Override
        public String toString() {
            return "SelectionBuilder[table=" + mTable + ", selection=" + getSelection() + ", selectionArgs="
                    + Arrays.toString(getSelectionArgs()) + "]";
        }

        /**
         * Execute query using the current internal state as {@code WHERE} clause.
         */
        public Cursor query(SQLiteDatabase db, String[] columns, String orderBy) {
            return query(db, columns, null, null, orderBy, null);
        }

        /**
         * Execute query using the current internal state as {@code WHERE} clause.
         */
        public Cursor query(SQLiteDatabase db, String[] columns, String groupBy, String having, String orderBy, String limit) {
            assertTable();
            if (columns != null) mapColumns(columns);
            return db.query(mTable, columns, getSelection(), getSelectionArgs(), groupBy, having, orderBy, limit);
        }

        /**
         * Execute update using the current internal state as {@code WHERE} clause.
         */
        public int update(SQLiteDatabase db, ContentValues values) {
            assertTable();
            return db.update(mTable, values, getSelection(), getSelectionArgs());
        }

        /**
         * Execute delete using the current internal state as {@code WHERE} clause.
         */
        public int delete(SQLiteDatabase db) {
            assertTable();
            return db.delete(mTable, getSelection(), getSelectionArgs());
        }

    }

    /**
     * Drops the table with given name.
     */
    public static String dropTable(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }

}
