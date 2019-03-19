package asia.takkyssquare.prototypeshoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import asia.takkyssquare.prototypeshoppinglist.ShoppingItemContent.ShoppingItem;

public class DBHelper extends ContextWrapper {

    private static final String TAG = "DBHelper";

    private SQLiteDatabase mySQLiteDatabase = null;

    //コンストラクタ
    public DBHelper(Context base) {
        super(base);
        mySQLiteDatabase = new DBOpenHelper(base).getWritableDatabase();
    }

    //データベースクローズ
    public void closeDB() {
        mySQLiteDatabase.close();
        mySQLiteDatabase = null;
    }

    //買い物リスト読込
    public List<String> readListIndex(String tableName) {
        List<String> listIndex = new ArrayList<>();
        Cursor cursor = mySQLiteDatabase.query
                (tableName, null, null, null, null, null, "update_at desc", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String listName = cursor.getString(cursor.getColumnIndex("name"));
                listIndex.add(listName);
            } while (cursor.moveToNext());
        } else {
            listIndex.add(getString(R.string.spinner_empty));
        }
        cursor.close();
        return listIndex;
    }

    //買い物リスト新規追加・買い物リスト名変更
    public int updateListIndex(String oldListName, String newListName) {
        int count = 0;
        ContentValues values = new ContentValues();
        values.put("name", newListName);
        values.put("update_at", System.currentTimeMillis());
        count = mySQLiteDatabase.update
                (DBOpenHelper.LIST_ACTIVE, values, "name='" + oldListName + "'", null);
        if (count == 0) {
            ContentValues newValues = new ContentValues();
            newValues.put("_id", getCount(DBOpenHelper.LIST_INDEX) + 1);
            newValues.put("create_at", values.getAsLong("update_at"));
            count = (int) mySQLiteDatabase.insert(DBOpenHelper.LIST_INDEX, null, newValues);
            Log.d(TAG, "Completed: the list ["+newListName+"] add the table on the table ["+DBOpenHelper.ITEM_INDEX+"!");
            newValues.putAll(values);
            mySQLiteDatabase.insert(DBOpenHelper.LIST_ACTIVE, null, newValues);
            Log.d(TAG,"Completed: the list ["+newListName+"] add the table on the table ["+DBOpenHelper.ITEM_ACTIVE+"!");
        } else {
            Log.d(TAG,"Completed: the list ["+oldListName+"] has renamed to ["+newListName+"]!");
        }
        return count;
    }

    //買い物リスト削除(削除予定テーブルへ移動)
    public void moveToDeletedTable(String listName, String tableNameFrom) {
        ContentValues values = new ContentValues();
        Cursor cursor = mySQLiteDatabase.query(tableNameFrom, null, "name = ?", new String[]{listName}, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                values.put("_id", cursor.getLong(cursor.getColumnIndex("_id")));
                values.put("create_at", cursor.getLong(cursor.getColumnIndex("create_at")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        int count = getCount(DBOpenHelper.LIST_DELETED);
        if (mySQLiteDatabase.insert(DBOpenHelper.LIST_DELETED, null, values) == count + 1) {
            mySQLiteDatabase.delete(DBOpenHelper.LIST_ACTIVE,"name = ?",new String[]{listName});
            Log.d(TAG,"Complete: the list ["+listName+"] is deleted!");
        } else {
            Log.w(TAG, "Error: the list ["+listName+"] is still alive!");
        }
    }

    public List<ShoppingItem> readItemList(String tableName, String key) {
        List<ShoppingItem> itemIndex = new ArrayList<>();
        return itemIndex;
    }

    //データ更新
    public void updateDB(String tableName, String[] columus) {
        ContentValues values = new ContentValues();
//        values.put("item1", columus[0]);
//        values.put("item2", columus[1]);
//        values.put("item3", columus[2]);
        if (mySQLiteDatabase.update
                (tableName, values, "item1='" + columus[0] + "'", null) == 0) {
            mySQLiteDatabase.insert(tableName, null, values);
        }
    }

    //データ削除　オーバーロード
    public void deleteDB(String tableName) {
        //全件削除
        mySQLiteDatabase.delete(tableName, null, null);
    }

    public void deleteDB(String tableName, String key) {
        //１件削除
        mySQLiteDatabase.delete(tableName, "item1='" + key + "'", null);
    }

    //データ件数取得
    public int getCount(String tableName) {
        int count = 0;
        Cursor cursor = null;
        cursor = mySQLiteDatabase.query
                (tableName, null, null, null, null, null, null);
        count = cursor.getCount();
        cursor.close();
        return count;
    }

    //リスト名称よりリストIDを検索
    //該当するリスト名がない場合は、エラーコード「-1」を戻す →要:エラー表示を呼び出し先に記述
    public int getListId(String listName) {
        int id = 0;
        String[] columns = {"_id"};
        Cursor cursor = null;
        cursor = mySQLiteDatabase.query
                (DBOpenHelper.LIST_ACTIVE, columns, "name = ?", new String[]{listName}, null, null, null, null);
        if (cursor != null && cursor.getCount() == 1) {
            id = cursor.getInt(cursor.getColumnIndex("_id"));
        } else {
            id = -1;
        }
        cursor.close();
        return id;
    }

}
