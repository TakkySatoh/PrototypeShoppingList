package asia.takkyssquare.prototypeshoppinglist;

import java.util.List;

public interface RecyclerViewEditListener {
    void insertToRecyclerView(ItemRecyclerViewAdapter adapter, List<ShoppingItemContent.ShoppingItem> list, ShoppingItemContent.ShoppingItem item);

    void updateToRecyclerView(ItemRecyclerViewAdapter adapter, List<ShoppingItemContent.ShoppingItem> list, ShoppingItemContent.ShoppingItem item);

    ShoppingItemContent.ShoppingItem deleteFromRecyclerView(ItemRecyclerViewAdapter adapter, List<ShoppingItemContent.ShoppingItem> list, ShoppingItemContent.ShoppingItem item);

    void moveItemBetweenRecyclerViews(boolean hasGot, int position);
}
