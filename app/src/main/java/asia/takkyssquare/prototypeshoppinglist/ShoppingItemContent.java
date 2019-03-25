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

    public List<ShoppingItem> createSampleItemList(int count, String place) {
        itemList.add(new ShoppingItem(CONTENT_TYPE_HEADER));
        for (int i = 0; i < count; i++) {
            itemList.add(new ShoppingItem(
                    false,
                    count,
                    0,
                    count,
                    "アイテム" + (i + 1),
                    1,
                    100,
                    "これはアイテム" + (i + 1) + "です",
                    place,
                    System.currentTimeMillis(),
                    System.currentTimeMillis()));
            if (i >= count / 2) {
                itemList.get(i + 1).setHasGot(true);
            }
        }
        itemList.add(count / 2 + 1, new ShoppingItem(CONTENT_TYPE_FOOTER));
        itemList.add(count / 2 + 2, new ShoppingItem(CONTENT_TYPE_HEADER));
        return itemList;
    }

    public ShoppingItem createItem(Intent data) {
        ShoppingItem newItem = new ShoppingItem(
                data.getBooleanExtra("hasGot", false),
                data.getIntExtra("itemId", 0),
                data.getIntExtra("listId", 0),
                data.getIntExtra("order", 0),
                data.getStringExtra("name"),
                data.getIntExtra("amount", 0),
                data.getIntExtra("price", 0),
                data.getStringExtra("description"),
                data.getStringExtra("place"),
                data.getLongExtra("createDate", System.currentTimeMillis()),
                data.getLongExtra("lastUpdateDate", System.currentTimeMillis())
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
        private String description;
        private String place;
        private final long createDate;
        private long lastUpdateDate;

        public ShoppingItem(boolean hasGot, int itemId, int listId, int order, String name, int amount, int price, String description, String place, long createDate, long lastUpdateDate) {
            this.contentType = CONTENT_TYPE_ITEM;
            this.hasGot = hasGot;
            this.itemId = itemId;
            this.listId = listId;
            this.order = order;
            this.name = name;
            this.amount = amount;
            this.price = price;
            this.description = description;
            this.place = place;
            this.createDate = createDate;
            this.lastUpdateDate = lastUpdateDate;
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

        public ShoppingItem(int contentType) {
            this.contentType = contentType;
            this.createDate = System.currentTimeMillis();
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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPlace() {
            return place;
        }

        public void setPlace(String place) {
            this.place = place;
        }

        public long getCreateDate() {
            return createDate;
        }

        public long getLastUpdateDate() {
            return lastUpdateDate;
        }

        public void setLastUpdateDate(long lastUpdateDate) {
            this.lastUpdateDate = lastUpdateDate;
        }
    }
}
