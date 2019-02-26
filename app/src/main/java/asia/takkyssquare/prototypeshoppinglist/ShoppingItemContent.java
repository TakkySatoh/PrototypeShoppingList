package asia.takkyssquare.prototypeshoppinglist;

import java.util.List;

public class ShoppingItemContent {

    private List<ShoppingItem> itemList;

    public List<ShoppingItem> createSampleItemList(int count,String place) {
        for (int i = 0; i < count; i++) {
            itemList.add(new ShoppingItem("アイテム" + (i + 1), 1, 100, "これはアイテム" + (i + 1) + "です", place, System.currentTimeMillis(), System.currentTimeMillis()));
        }
        return itemList;
    }

    public class ShoppingItem {
        private boolean hasGot;
        private String name;
        private int amount;
        private int price;
        private String description;
        private String place;
        private final long createDate;
        private long lastUpdateDate;

        public ShoppingItem(String name, int amount, int price, String description, String place, long createDate, long lastUpdateDate) {
            this.hasGot = false;
            this.name = name;
            this.amount = amount;
            this.price = price;
            this.description = description;
            this.place = place;
            this.createDate = createDate;
            this.lastUpdateDate = lastUpdateDate;
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
