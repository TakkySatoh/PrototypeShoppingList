package asia.takkyssquare.prototypeshoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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
                    String listName = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NAME));
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
                    if (cursor.getLong(cursor.getColumnIndex(DBOpenHelper.HAS_GOT)) == DBOpenHelper.YES) {
                        hasGot = true;
                    }
                    item = new ShoppingItem(
                            hasGot,
                            cursor.getInt(cursor.getColumnIndex(DBOpenHelper.ITEM_ID)),
                            cursor.getInt(cursor.getColumnIndex(DBOpenHelper.LIST_ID)),
                            cursor.getInt(cursor.getColumnIndex(DBOpenHelper.ORDER)),
                            cursor.getString(cursor.getColumnIndex(DBOpenHelper.NAME)),
                            cursor.getInt(cursor.getColumnIndex(DBOpenHelper.AMOUNT)),
                            cursor.getInt(cursor.getColumnIndex(DBOpenHelper.PRICE)),
                            cursor.getString(cursor.getColumnIndex(DBOpenHelper.COMMENT)),
                            cursor.getString(cursor.getColumnIndex(DBOpenHelper.PLACE)),
                            cursor.getLong(cursor.getColumnIndex(DBOpenHelper.CREATE_AT)),
                            cursor.getLong(cursor.getColumnIndex(DBOpenHelper.UPDATE_AT)));
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
        int rest = toBuyAmount;
        int i = 0;
        while (rest > 0) {
            if (itemIndex.get(i).isHasGot()) {
                itemIndex.add(itemIndex.size() - 1, itemIndex.remove(i));
            } else {
                rest--;
                i++;
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
        values.put(DBOpenHelper.UPDATE_AT, System.currentTimeMillis());
        if (oldListName != null) {
            values.put(DBOpenHelper.NAME, newListName);
            count = mySQLiteDatabase.update
                    (DBOpenHelper.LIST_ACTIVE, values, "name = ?", new String[]{oldListName});
        } else {
            count = mySQLiteDatabase.update
                    (DBOpenHelper.LIST_ACTIVE, values, "name = ?", new String[]{newListName});
            values.put(DBOpenHelper.NAME, newListName);
        }
        if (count == 0) {
            ContentValues newValues = new ContentValues();
            newValues.put("_id", getCount(DBOpenHelper.LIST_INDEX) + 1);
            newValues.put(DBOpenHelper.CREATE_AT, values.getAsLong(DBOpenHelper.UPDATE_AT));
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

    //アイテム情報更新・新規追加(アイテム詳細画面からの操作)
    public int updateItem(Intent data) {
        boolean isCreate = false;
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.CREATE_AT, data.getLongExtra(DBOpenHelper.CREATE_AT, 0));
        int itemId = data.getIntExtra(DBOpenHelper.ITEM_ID, 0);
        if (itemId == 0) {
            isCreate = true;
            itemId = (int) mySQLiteDatabase.insert(DBOpenHelper.ITEM_INDEX, null, values);
        }
        if (data.getBooleanExtra(DBOpenHelper.HAS_GOT, false)) {
            values.put(DBOpenHelper.HAS_GOT, 1);
        } else {
            values.put(DBOpenHelper.HAS_GOT, 0);
        }
        values.put(DBOpenHelper.UPDATE_AT, data.getLongExtra(DBOpenHelper.UPDATE_AT, 0));
        values.put(DBOpenHelper.NAME, data.getStringExtra(DBOpenHelper.NAME));
        values.put(DBOpenHelper.AMOUNT, data.getIntExtra(DBOpenHelper.AMOUNT, 0));
        values.put(DBOpenHelper.PRICE, data.getIntExtra(DBOpenHelper.PRICE, 0));
        values.put(DBOpenHelper.PLACE, data.getStringExtra(DBOpenHelper.PLACE));
        values.put(DBOpenHelper.COMMENT, data.getStringExtra(DBOpenHelper.COMMENT));
        if (isCreate && itemId != -1) {
            itemId = (int) mySQLiteDatabase.insert(DBOpenHelper.ITEM_ACTIVE, null, values);
        } else {
            mySQLiteDatabase.update(DBOpenHelper.ITEM_ACTIVE, values, "_id = ?", new String[]{Integer.toString(itemId)});
        }
        return itemId;
    }

    //アイテム情報更新・新規追加(アイテム一覧からの操作)
    public int updateItem(ShoppingItem item) {
        boolean isCreate = false;
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.CREATE_AT, item.getCreateAt());
        int itemId = item.getItemId();
        if (itemId == 0) {
            isCreate = true;
            itemId = (int) mySQLiteDatabase.insert(DBOpenHelper.ITEM_INDEX, null, values);
        }
        if (item.isHasGot()) {
            values.put(DBOpenHelper.HAS_GOT, 1);
        } else {
            values.put(DBOpenHelper.HAS_GOT, 0);
        }
        values.put(DBOpenHelper.UPDATE_AT, item.getUpdateAt());
        values.put(DBOpenHelper.NAME, item.getName());
        values.put(DBOpenHelper.AMOUNT, item.getAmount());
        values.put(DBOpenHelper.PRICE, item.getPrice());
        values.put(DBOpenHelper.PLACE, item.getPlace());
        values.put(DBOpenHelper.COMMENT, item.getComment());
        if (isCreate && itemId != -1) {
            itemId = (int) mySQLiteDatabase.insert(DBOpenHelper.ITEM_ACTIVE, null, values);
        } else {
            mySQLiteDatabase.update(DBOpenHelper.ITEM_ACTIVE, values, "_id = ?", new String[]{Integer.toString(itemId)});
        }
        return itemId;
    }

    //サンプルデータ生成
    public List<ShoppingItem> createSampleItemList(int count, String place) {
        int listId = getListId(place);
        List<ShoppingItem> itemList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ShoppingItem item = new ShoppingItem(
                    false,
                    0,
                    listId,
                    0,
                    "アイテム" + (i + 1),
                    1,
                    100,
                    "これはアイテム" + (i + 1) + "です",
                    place,
                    System.currentTimeMillis(),
                    System.currentTimeMillis());
            itemList.add(item);
            if (i >= count / 2) {
                itemList.get(i).setHasGot(true);
            }
            int itemId = updateItem(itemList.get(i));
            itemList.get(i).setItemId(itemId);
            itemList.get(i).setOrder(itemId);
            updateOrder(itemList.get(i));
        }
        itemList.add(0, new ShoppingItem(ShoppingItemContent.CONTENT_TYPE_HEADER));
        itemList.add(count / 2 + 1, new ShoppingItem(ShoppingItemContent.CONTENT_TYPE_FOOTER));
        itemList.add(count / 2 + 2, new ShoppingItem(ShoppingItemContent.CONTENT_TYPE_HEADER));
        return itemList;
    }

    //並び順データ更新・新規追加
    public void updateOrder(ShoppingItem item) {
        int count = 0;
        int itemId = item.getItemId();
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.ORDER, item.getOrder());
        count = mySQLiteDatabase.update(DBOpenHelper.ORDER_INDEX, values, "item_id = ?", new String[]{Integer.toString(itemId)});
        if (count == 0) {
            values.put(DBOpenHelper.ITEM_ID, itemId);
            values.put(DBOpenHelper.LIST_ID, item.getListId());
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
    public int moveListToDeletedTable(String listName, String tableNameFrom) {
        int deletedListId = 0;
        ContentValues values = new ContentValues();
        try (Cursor cursor = mySQLiteDatabase.query(tableNameFrom, null, "name = ?", new String[]{listName}, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    deletedListId = (int) cursor.getLong(cursor.getColumnIndex("_id"));
                    values.put("_id", cursor.getLong(cursor.getColumnIndex("_id")));
                    values.put(DBOpenHelper.CREATE_AT, cursor.getLong(cursor.getColumnIndex(DBOpenHelper.CREATE_AT)));
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
        return deletedListId;
    }

    //買い物アイテム削除(削除予定テーブルへ移動)
    public void moveToDeletedTable(Intent data) {
        ContentValues values = new ContentValues();
        int itemId = data.getIntExtra(DBOpenHelper.ITEM_ID, 0);
        try (Cursor cursor = mySQLiteDatabase.query(DBOpenHelper.ITEM_ACTIVE, null, "_id = ?", new String[]{Integer.toString(itemId)}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    values.put("_id", cursor.getLong(cursor.getColumnIndex("_id")));
                    values.put(DBOpenHelper.CREATE_AT, cursor.getLong(cursor.getColumnIndex(DBOpenHelper.CREATE_AT)));
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
        int itemId = data.getIntExtra(DBOpenHelper.ITEM_ID, 0);
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

    public ShoppingList getListInfo(String listName) {
        int listId = getListId(listName);
        ShoppingList list = null;
        try (Cursor cursor = mySQLiteDatabase.query(DBOpenHelper.LIST_ACTIVE, null, "_id = ?", new String[]{Integer.toString(listId)}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list = new ShoppingList(
                            listId,
                            listName,
                            cursor.getLong(cursor.getColumnIndex(DBOpenHelper.CREATE_AT)),
                            cursor.getLong(cursor.getColumnIndex(DBOpenHelper.UPDATE_AT))
                    );
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Error: You could not get List info." + e.toString());
        }
        return list;
    }

    public ShoppingList getListInfo(int listId) {
        ShoppingList list = null;
        try (Cursor cursor = mySQLiteDatabase.query(DBOpenHelper.LIST_ACTIVE, null, "_id = ?", new String[]{Integer.toString(listId)}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list = new ShoppingList(
                            listId,
                            cursor.getString(cursor.getColumnIndex(DBOpenHelper.NAME)),
                            cursor.getLong(cursor.getColumnIndex(DBOpenHelper.CREATE_AT)),
                            cursor.getLong(cursor.getColumnIndex(DBOpenHelper.UPDATE_AT))
                    );
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Error: You could not get List info." + e.toString());
        }
        return list;

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
        if (mySQLiteDatabase != null) {
            mySQLiteDatabase.beginTransaction();
        }
    }

    public void setTransactionSuccessful() {
        if (mySQLiteDatabase != null) {
            mySQLiteDatabase.setTransactionSuccessful();
        }
    }

    public void endTransaction() {
        if (mySQLiteDatabase != null) {
            mySQLiteDatabase.endTransaction();
        }
    }
}
