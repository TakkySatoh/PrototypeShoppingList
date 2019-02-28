package asia.takkyssquare.prototypeshoppinglist;

import java.util.List;

import asia.takkyssquare.prototypeshoppinglist.ShoppingItemContent.ShoppingItem;

public interface RecyclerViewEditListener {
    void insertToRecyclerView(ItemRecyclerViewAdapter adapter, List<ShoppingItem> list, ShoppingItem item);

    void updateToRecyclerView(ItemRecyclerViewAdapter adapter, List<ShoppingItem> list, ShoppingItem item);

    void deleteFromRecyclerView(ItemRecyclerViewAdapter adapter, List<ShoppingItem> list, ShoppingItem item);

    void moveItemBetweenRecyclerViews(boolean hasGot, int position);
}
