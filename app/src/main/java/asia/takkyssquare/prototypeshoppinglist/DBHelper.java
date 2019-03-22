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

    public static final String TAG = "DBHelper";

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

    //買い物リスト一覧読込
    //買い物リスト一覧が空白の場合 → 固定メッセージをListに格納
    public List<String> readListIndex(String tableName) {
        List<String> listIndex = new ArrayList<>();
        try (Cursor cursor = mySQLiteDatabase.query
                (tableName, null, null, null, null, null, "update_at desc", null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String listName = cursor.getString(cursor.getColumnIndex("name"));
                    listIndex.add(listName);
                } while (cursor.moveToNext());
            } else {
                listIndex.add(getString(R.string.spinner_empty));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Error: Leading list index failed." + e.toString());
        }
        Log.d(TAG, "Completed: DBHelper finished to read list index!");
        return listIndex;
    }

    //買い物リスト新規追加・買い物リスト名変更
    //(mySQLiteDatabase#update()の戻り値=0 → 新規追加扱い。INDEX→ACTIVEの順に登録を行う)
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
            Log.d(TAG, "Completed: the list [" + newListName + "] add the table on the table [" + DBOpenHelper.ITEM_INDEX + "!");
            newValues.putAll(values);
            count = (int) mySQLiteDatabase.insert(DBOpenHelper.LIST_ACTIVE, null, newValues);
            Log.d(TAG, "Completed: the list [" + newListName + "] add the table on the table [" + DBOpenHelper.ITEM_ACTIVE + "!");
        } else {
            Log.d(TAG, "Completed: the list [" + oldListName + "] has renamed to [" + newListName + "]!");
        }
        return count;
    }

    //買い物リスト削除(削除予定テーブルへ移動)
    public void moveToDeletedTable(String listName, String tableNameFrom) {
        ContentValues values = new ContentValues();
        try (Cursor cursor = mySQLiteDatabase.query(tableNameFrom, null, "name = ?", new String[]{listName}, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    values.put("_id", cursor.getLong(cursor.getColumnIndex("_id")));
                    values.put("create_at", cursor.getLong(cursor.getColumnIndex("create_at")));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Error: Moving a list to deleted table failed. " + e.toString());
        }
        int listToDelete = (int) mySQLiteDatabase.insert(DBOpenHelper.LIST_DELETED, null, values);
        if (listToDelete != -1) {
            mySQLiteDatabase.delete(DBOpenHelper.LIST_ACTIVE, "name = ?", new String[]{listName});
            Log.d(TAG, "Complete: the list [" + listName + "] is deleted!");
        } else {
            Log.w(TAG, "Error: the list [" + listName + "] is still alive!");
        }
    }

    //買い物リスト読み込み
    public List<ShoppingItem> readItemList(int listId) {
        List<ShoppingItem> itemIndex = new ArrayList<>();
        ShoppingItem item;
        int toBuyAmount = 0;
        String sql = "select * from "
                + DBOpenHelper.ORDER_INDEX +
                " inner join " + DBOpenHelper.ITEM_ACTIVE
                + " on " + DBOpenHelper.ORDER_INDEX + ".item_id = " + DBOpenHelper.ITEM_ACTIVE
                + "._id where " + DBOpenHelper.ORDER_INDEX + ".list_id = " + listId
                + "order by " + DBOpenHelper.ORDER_INDEX + ".order_number asc"
                + ";";
        try (Cursor cursor = mySQLiteDatabase.rawQuery(sql, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    boolean hasGot = false;
                    if (cursor.getLong(cursor.getColumnIndex("has_got")) == DBOpenHelper.YES) {
                        hasGot = true;
                    }
                    item = new ShoppingItem(
                            hasGot,
                            cursor.getString(cursor.getColumnIndex("name")),
                            cursor.getInt(cursor.getColumnIndex("amount")),
                            cursor.getInt(cursor.getColumnIndex("price")),
                            cursor.getString(cursor.getColumnIndex("comment")),
                            cursor.getString(cursor.getColumnIndex("place")),
                            cursor.getLong(cursor.getColumnIndex("create_at")),
                            cursor.getLong(cursor.getColumnIndex("update_at"))
                    );
                    if (!item.isHasGot()) {
                        itemIndex.add(item);
                        toBuyAmount++;
                    } else {
                        itemIndex.add(toBuyAmount + 1, item);
                    }
                } while (cursor.moveToNext());
                itemIndex.add(new ShoppingItem(ShoppingItemContent.CONTENT_TYPE_HEADER));
                itemIndex.add(toBuyAmount + 1, new ShoppingItem(ShoppingItemContent.CONTENT_TYPE_FOOTER));
                itemIndex.add(toBuyAmount+2,new ShoppingItem(ShoppingItemContent.CONTENT_TYPE_HEADER));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Error: Reading Item List is failed. " + e.toString());
        }
        return itemIndex;
    }

    public ShoppingItem updateItem() {

        return null;
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
    public int deleteDB(String tableName) {
        //全件削除
        int count = mySQLiteDatabase.delete(tableName, null, null);
        return count;
    }

    public void deleteDB(String tableName, String key) {
        //１件削除
        mySQLiteDatabase.delete(tableName, "item1='" + key + "'", null);
    }

    //データ件数取得
    public int getCount(String tableName) {
        int count = 0;
        try (Cursor cursor = mySQLiteDatabase.query
                (tableName, null, null, null, null, null, null)) {
            count = cursor.getCount();
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Error: You could not get amount of the rows. " + e.toString());
        }
        return count;
    }

    //リスト名称よりリストIDを検索
    //該当するリスト名がない場合は、エラーコード「-1」を戻す →要:エラー表示を呼び出し先に記述
    public int getListId(String listName) {
        int id = 0;
        String[] columns = {"_id"};
        try (Cursor cursor = mySQLiteDatabase.query
                (DBOpenHelper.LIST_ACTIVE, columns, "name = ?", new String[]{listName}, null, null, null, null)) {
            if (cursor != null && cursor.getCount() == 1 && cursor.moveToFirst()) {
                do {
                    id = cursor.getInt(cursor.getColumnIndex("_id"));
                } while (cursor.moveToNext());
            } else {
                id = -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Error: You could not get List_id. " + e.toString());
        }
        return id;
    }

}
