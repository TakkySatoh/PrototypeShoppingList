package asia.takkyssquare.prototypeshoppinglist;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shoppinglist.db";
    private static final int DATABASE_VERSION = 1;

    public static final String LIST_INDEX = "list_index";
    public static final String LIST_ACTIVE = "list_index_active";
    public static final String LIST_DELETED = "list_index_deleted";

    public static final String ITEM_INDEX = "item_index";
    public static final String ITEM_ACTIVE = "item_index_active";
    public static final String ITEM_DELETED = "item_index_deleted";

    public static final String ORDER_INDEX = "order_index";

    public static final int YES = 1;
    public static final int NO = 0;

    private final Context mContext;

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String sqlListIndex = createSqlListTable(LIST_INDEX);
            String sqlListActive = createSqlListTable(LIST_ACTIVE);
            String sqlListDeleted = createSqlListTable(LIST_DELETED);

            String sqlItemIndex = createSqlItemTable(ITEM_INDEX);
            String sqlItemActive = createSqlItemTable(ITEM_ACTIVE);
            String sqlItemDeleted = createSqlItemTable(ITEM_DELETED);

            String sqlOrderIndex = createOrderIndex(ORDER_INDEX);

            db.execSQL(sqlListIndex);
            db.execSQL(sqlListActive);
            db.execSQL(sqlListDeleted);
            db.execSQL(sqlItemIndex);
            db.execSQL(sqlItemActive);
            db.execSQL(sqlItemDeleted);
            db.execSQL(sqlOrderIndex);

        } catch (SQLException e) {
            e.printStackTrace();
            Log.d("Error", "DB create Error; " + e.toString());
        }
    }

    public String createSqlListTable(String tableName) {
        StringBuilder sb = new StringBuilder()
                .append("create table if not exists " + tableName + " (")
                .append("_id integer primary key");
        if (!tableName.contains("deleted")) {
            sb.append(" autoincrement");
        }
        if (tableName.contains("active")) {
            sb.append(",name text not null")
                    .append(",update_at integer not null");
        }
        sb.append(",create_at integer not null")
                .append(");");
        return sb.toString();
    }

    private String createSqlItemTable(String tableName) {
        StringBuilder sb = new StringBuilder()
                .append("create table if not exists " + tableName + " (")
                .append("_id integer primary key");
        if (!tableName.contains("deleted")) {
            sb.append(" autoincrement");
        }
        if (tableName.contains("active")) {
            sb.append(",name text not null")
                    .append(",amount integer not null")
                    .append(",price integer not null")
                    .append(",place text")
                    .append(",comment text")
                    .append(",has_got integer not null")
                    .append(",update_at integer not null");
        }
        sb.append(",create_at integer not null")
                .append(");");
        return sb.toString();
    }

    private String createOrderIndex(String tableName) {
        StringBuilder sb = new StringBuilder()
                .append("create table if not exists " + tableName + " (")
                .append("_id integer primary key").append(" autoincrement")
                .append(",item_id integer not null")
                .append(",list_id integer not null")
                .append(",order_number integer not null")
                .append(");");
        return sb.toString();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
