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

    public static class ShoppingItem {

        private int contentType;
        private boolean hasGot;
        private int itemId;
        private int listId;
        private int order;
        private String name;
        private int amount;
        private int price;
        private String comment;
        private String place;
        private final long createAt;
        private long updateAt;

        public ShoppingItem(boolean hasGot, int itemId, int listId, int order, String name, int amount, int price, String comment, String place, long createAt, long updateAt) {
            this.contentType = CONTENT_TYPE_ITEM;
            this.hasGot = hasGot;
            this.itemId = itemId;
            this.listId = listId;
            this.order = order;
            this.name = name;
            this.amount = amount;
            this.price = price;
            this.comment = comment;
            this.place = place;
            this.createAt = createAt;
            this.updateAt = updateAt;
        }

        public ShoppingItem(int contentType) {
            this.contentType = contentType;
            this.createAt = System.currentTimeMillis();
        }

        public int getItemId() {
            return itemId;
        }

        public void setItemId(int itemId) {
            this.itemId = itemId;
        }

        public int getListId() {
            return listId;
        }

        public void setListId(int listId) {
            this.listId = listId;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public int getContentType() {
            return contentType;
        }

        public void setContentType(int contentType) {
            this.contentType = contentType;
        }

        public boolean isHasGot() {
            return hasGot;
        }

        public void setHasGot(boolean hasGot) {
            this.hasGot = hasGot;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getPlace() {
            return place;
        }

        public void setPlace(String place) {
            this.place = place;
        }

        public long getCreateAt() {
            return createAt;
        }

        public long getUpdateAt() {
            return updateAt;
        }

        public void setUpdateAt(long updateAt) {
            this.updateAt = updateAt;
        }
    }
}
