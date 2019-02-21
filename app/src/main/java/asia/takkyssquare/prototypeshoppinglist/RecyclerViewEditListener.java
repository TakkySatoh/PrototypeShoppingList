package asia.takkyssquare.prototypeshoppinglist;

import java.util.List;

import asia.takkyssquare.prototypeshoppinglist.dummy.DummyContent;

public interface RecyclerViewEditListener {
    void insertToRecyclerView(MyItemRecyclerViewAdapter adapter, List<DummyContent.DummyItem> list, DummyContent.DummyItem item);

    void updateToRecyclerView(MyItemRecyclerViewAdapter adapter, List<DummyContent.DummyItem> list, DummyContent.DummyItem item);

    DummyContent.DummyItem deleteFromRecyclerView(MyItemRecyclerViewAdapter adapter, List<DummyContent.DummyItem> list, DummyContent.DummyItem item);

    void moveItemBetweenRecyclerViews(boolean isChecked, int position);
}
