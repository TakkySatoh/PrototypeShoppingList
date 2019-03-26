package asia.takkyssquare.prototypeshoppinglist;

public class ShoppingList {

    private int listId;
    private String listName;
    private final long createAt;
    private long updateAt;

    public ShoppingList(int listId, String listName, long createAt, long updateAt) {
        this.listId = listId;
        this.listName = listName;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public ShoppingList() {
        createAt = System.currentTimeMillis();
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
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
