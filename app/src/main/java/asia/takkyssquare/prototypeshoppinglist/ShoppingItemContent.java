package asia.takkyssquare.prototypeshoppinglist;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ShoppingItemContent {

    public static final int CONTENT_TYPE_ITEM = 0;
    public static final int CONTENT_TYPE_HEADER = 1;
    public static final int CONTENT_TYPE_FOOTER = 2;

    private List<ShoppingItem> itemList = new ArrayList<>();

    public List<ShoppingItem> getItemList(Context context, int listId) {
        DBHelper dbHelper = new DBHelper(context);
        try {
            itemList = dbHelper.readItemList(listId);
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(DBHelper.TAG, "Error: You could not get Item List. " + e.toString());

        } finally {
            if (dbHelper != null) {
                dbHelper.closeDB();
            }
        }
        return itemList;
    }

    public List<ShoppingItem> createSampleItemList(Context context, int listId, int count, String place) {
        DBHelper dbHelper = new DBHelper(context);
        try {
            itemList = dbHelper.readItemList(listId);
            if (itemList.size() < 4) {
                itemList = dbHelper.createSampleItemList(count, place);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(DBHelper.TAG, "Error: You could not get sample list. " + e.toString());
        } finally {
            if (dbHelper != null) {
                dbHelper.closeDB();
            }
        }
        return itemList;
    }

    public ShoppingItem createItem(Intent data) {
        ShoppingItem newItem = new ShoppingItem(
                data.getBooleanExtra(DBOpenHelper.HAS_GOT, false),
                data.getIntExtra(DBOpenHelper.ITEM_ID, 0),
                data.getIntExtra(DBOpenHelper.LIST_ID, 0),
                data.getIntExtra(DBOpenHelper.ORDER, 0),
                data.getStringExtra(DBOpenHelper.NAME),
                data.getIntExtra(DBOpenHelper.AMOUNT, 0),
                data.getIntExtra(DBOpenHelper.PRICE, 0),
                data.getStringExtra(DBOpenHelper.COMMENT),
                data.getStringExtra(DBOpenHelper.PLACE),
                data.getLongExtra(DBOpenHelper.CREATE_AT, System.currentTimeMillis()),
                data.getLongExtra(DBOpenHelper.UPDATE_AT, System.currentTimeMillis())
        );
        return newItem;
    }

}
