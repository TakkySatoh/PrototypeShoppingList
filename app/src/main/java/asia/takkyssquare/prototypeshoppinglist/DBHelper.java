package asia.takkyssquare.prototypeshoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

    //買い物アイテム一覧読み込み
    public List<ShoppingItem> readItemList(int listId) {
        List<ShoppingItem> itemIndex = new ArrayList<>();
        ShoppingItem item;
        int toBuyAmount = 0;
        String sql = "select * from "
                + DBOpenHelper.ORDER_INDEX +
                " inner join " + DBOpenHelper.ITEM_ACTIVE
                + " on " + DBOpenHelper.ORDER_INDEX + ".item_id = " + DBOpenHelper.ITEM_ACTIVE
                + "._id where " + DBOpenHelper.ORDER_INDEX + ".list_id = " + listId
                + " order by " + DBOpenHelper.ORDER_INDEX + ".order_number asc"
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
                            cursor.getInt(cursor.getColumnIndex("item_id")),
                            cursor.getInt(cursor.getColumnIndex("list_id")),
                            cursor.getInt(cursor.getColumnIndex("order_number")),
                            cursor.getString(cursor.getColumnIndex("name")),
                            cursor.getInt(cursor.getColumnIndex("amount")),
                            cursor.getInt(cursor.getColumnIndex("price")),
                            cursor.getString(cursor.getColumnIndex("comment")),
                            cursor.getString(cursor.getColumnIndex("place")),
                            cursor.getLong(cursor.getColumnIndex("create_at")),
                            cursor.getLong(cursor.getColumnIndex("update_at")));
                    itemIndex.add(item);
                    if (!item.isHasGot()) {
                        toBuyAmount++;
                    }
                } while (cursor.moveToNext());
            } else {
                int count = cursor.getCount();
                Log.w(TAG, "Error: Cursor has some problems. count of results = " + count + ".");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Error: Reading Item List is failed. " + e.toString());
        }
        for (int i = 0; i < itemIndex.size(); i++) {
            if (itemIndex.get(i).isHasGot()) {
                itemIndex.add(toBuyAmount, itemIndex.remove(i));
            }
        }
        itemIndex.add(0, new ShoppingItem(ShoppingItemContent.CONTENT_TYPE_HEADER));
        itemIndex.add(toBuyAmount + 1, new ShoppingItem(ShoppingItemContent.CONTENT_TYPE_FOOTER));
        itemIndex.add(toBuyAmount + 2, new ShoppingItem(ShoppingItemContent.CONTENT_TYPE_HEADER));
        return itemIndex;
    }

    //買い物リスト新規追加・買い物リスト名変更
    //(mySQLiteDatabase#update()の戻り値=0 → 新規追加扱い。INDEX→ACTIVEの順に登録を行う)
    public int updateListIndex(String oldListName, String newListName) {
        int count = 0;
        ContentValues values = new ContentValues();
        values.put("update_at", System.currentTimeMillis());
        if (oldListName != null) {
            values.put("name", newListName);
            count = mySQLiteDatabase.update
                    (DBOpenHelper.LIST_ACTIVE, values, "name = ?", new String[]{oldListName});
        } else {
            count = mySQLiteDatabase.update
                    (DBOpenHelper.LIST_ACTIVE, values, "name = ?", new String[]{newListName});
        }
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

    //アイテム情報更新・新規追加
    public int updateItem(Intent data) {
        boolean isCreate = false;
        ContentValues values = new ContentValues();
        values.put("create_at", data.getLongExtra("create_at", 0));
        int itemId = data.getIntExtra("itemId", 0);
        if (itemId == 0) {
            isCreate = true;
            itemId = (int) mySQLiteDatabase.insert(DBOpenHelper.ITEM_INDEX, null, values);
        }
        if (data.getBooleanExtra("hasGot", false)) {
            values.put("has_got", 1);
        } else {
            values.put("has_got", 0);
        }
        values.put("update_at", data.getLongExtra("update_at", 0));
        values.put("name", data.getStringExtra("name"));
        values.put("amount", data.getIntExtra("amount", 0));
        values.put("price", data.getIntExtra("price", 0));
        values.put("place", data.getStringExtra("place"));
        values.put("comment", data.getStringExtra("comment"));
        if (isCreate && itemId != -1) {
            itemId = (int) mySQLiteDatabase.insert(DBOpenHelper.ITEM_ACTIVE, null, values);
        } else {
            mySQLiteDatabase.update(DBOpenHelper.ITEM_ACTIVE, values, "_id = ?", new String[]{Integer.toString(itemId)});
        }
        return itemId;
    }

    public int updateItem(ShoppingItem item){
        boolean isCreate = false;
        ContentValues values = new ContentValues();
        values.put("create_at", item.getCreateDate());
        int itemId = item.getItemId();
        if (itemId == 0) {
            isCreate = true;
            itemId = (int) mySQLiteDatabase.insert(DBOpenHelper.ITEM_INDEX, null, values);
        }
        if (item.isHasGot()) {
            values.put("has_got", 1);
        } else {
            values.put("has_got", 0);
        }
        values.put("update_at", item.getLastUpdateDate());
        values.put("name", item.getName());
        values.put("amount", item.getAmount());
        values.put("price", item.getPrice());
        values.put("place", item.getPlace());
        values.put("comment", item.getDescription());
        if (isCreate && itemId != -1) {
            itemId = (int) mySQLiteDatabase.insert(DBOpenHelper.ITEM_ACTIVE, null, values);
        } else {
            mySQLiteDatabase.update(DBOpenHelper.ITEM_ACTIVE, values, "_id = ?", new String[]{Integer.toString(itemId)});
        }
        return itemId;
    }

    public List<ShoppingItem> createSampleItemList(int listId){
        List<ShoppingItem> itemIndex = new ArrayList<>();
        return itemIndex;
    }

    //並び順データ更新・新規追加
    public void updateOrder(ShoppingItem item) {
        int count = 0;
        int itemId = item.getItemId();
        ContentValues values = new ContentValues();
        values.put("order_number", item.getOrder());
        count = mySQLiteDatabase.update(DBOpenHelper.ORDER_INDEX, values, "item_id = ?", new String[]{Integer.toString(itemId)});
        if (count == 0) {
            values.put("item_id", itemId);
            values.put("list_id", item.getListId());
            count = (int) mySQLiteDatabase.insert(DBOpenHelper.ORDER_INDEX, null, values);
            if (count == 0) {
                Log.w(TAG, "Error: DBHelper could not insert new order data.");
            } else {
                Log.d(TAG, "Complete: DBHelper finished to insert new order data!");
            }
        } else {
            Log.d(TAG, "Complete: DBHelper finished to update the order data!");
        }
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

    //買い物アイテム削除(削除予定テーブルへ移動)
    public void moveToDeletedTable(Intent data) {
        ContentValues values = new ContentValues();
        int itemId = data.getIntExtra("itemId", 0);
        try (Cursor cursor = mySQLiteDatabase.query(DBOpenHelper.ITEM_ACTIVE, null, "_id = ?", new String[]{Integer.toString(itemId)}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    values.put("_id", cursor.getLong(cursor.getColumnIndex("_id")));
                    values.put("create_at", cursor.getLong(cursor.getColumnIndex("create_at")));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Error: Moving an item to deleted table failed. " + e.toString());
        }
        int itemToDelete = (int) mySQLiteDatabase.insert(DBOpenHelper.ITEM_DELETED, null, values);
        if (itemToDelete != -1) {
            mySQLiteDatabase.delete(DBOpenHelper.ITEM_ACTIVE, "_id = ?", new String[]{Integer.toString(itemId)});
            Log.d(TAG, "Complete: the item [" + itemId + "] is deleted!");
        } else {
            Log.w(TAG, "Error: the item [" + itemId + "] is still alive!");
        }
    }

    //テーブル削除　オーバーロード
    public int deleteDB(String tableName) {     //全件削除
        int count = mySQLiteDatabase.delete(tableName, null, null);
        return count;
    }

    public void deleteDB(String tableName, String key) {   //１件削除
        mySQLiteDatabase.delete(tableName, "item1='" + key + "'", null);
    }

    //並び順データ削除　オーバーロード
    public void removeOrder(Intent data) {  //アイテム単位
        int count = 0;
        int itemId = data.getIntExtra("itemId", 0);
        if (itemId != 0) {
            count = mySQLiteDatabase.delete(DBOpenHelper.ORDER_INDEX, "item_id = ?", new String[]{Integer.toString(itemId)});
        } else {
            Log.w(TAG, "Error: You could not get item_id.");
        }
        if (count != 0) {
            Log.d(TAG, "Completed: DBHelper finished to delete order info of itemId=" + itemId + "!");
        } else {
            Log.w(TAG, "Error: DBHelper could not delete order info of itemId=" + itemId + ".");
        }
    }

    public void removeOrder(String listName) {  //リスト単位
        int count = 0;
        int listId = getListId(listName);
        if (listId != 0) {
            count = mySQLiteDatabase.delete(DBOpenHelper.ORDER_INDEX, "list_id = ?", new String[]{Integer.toString(listId)});
        } else {
            Log.w(TAG, "Error: You could not get list_id.");
        }
        if (count != 0) {
            Log.d(TAG, "Completed: DBHelper finished to delete order info of listId=" + listId + "!");
        } else {
            Log.w(TAG, "Error: DBHelper could not delete order info of listId=" + listId + ".");
        }
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

    public void beginTransaction() {
        if(mySQLiteDatabase != null){
            mySQLiteDatabase.beginTransaction();
        }
    }

    public void setTransactionSuccessful() {
        if(mySQLiteDatabase != null){
            mySQLiteDatabase.setTransactionSuccessful();
        }
    }

    public void endTransaction() {
        if (mySQLiteDatabase != null){
            mySQLiteDatabase.endTransaction();
        }
    }
}
