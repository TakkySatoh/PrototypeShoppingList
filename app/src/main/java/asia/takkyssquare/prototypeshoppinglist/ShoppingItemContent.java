package asia.takkyssquare.prototypeshoppinglist;

import java.util.List;

public class ShoppingItemContent {

    private List<ShoppingItem> itemList;




    public class ShoppingItem {
        private boolean hasGot;
        private String name;
        private String amount;
        private String price;
        private String description;
        private String place;

        public ShoppingItem(String name, String amount, String price, String description, String place) {
            this.hasGot = false;
            this.name = name;
            this.amount = amount;
            this.price = price;
            this.description = description;
            this.place = place;
        }
    }
}
