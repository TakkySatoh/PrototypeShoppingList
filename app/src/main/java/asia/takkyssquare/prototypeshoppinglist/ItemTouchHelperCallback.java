package asia.takkyssquare.prototypeshoppinglist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;

/**
 * ItemTouchHelper.Callbackの詳細定義クラス
 */
class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private ItemRecyclerViewAdapter mAdapter;

    public ItemTouchHelperCallback(ItemRecyclerViewAdapter adapter) {
        mAdapter = adapter;
    }
//          Callbackの抽象メソッド3種をオーバーライド

    //          これより以下のメソッドの稼働条件を設定
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
//                稼働条件 … 「購入予定」の位置にあるItemViewHolderのインスタンスが上または下方向にドラッグされた場合
        if (viewHolder.getItemViewType() == ItemRecyclerViewAdapter.VIEW_TYPE_ITEM_TO_BUY) {
            return makeMovementFlags(ItemTouchHelper.UP | DOWN, 0);
        } else {
            return 0;
        }
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    //            ViewHolderがドラッグされた場合の動作 (viewTypeが「購入予定」の場合のみ有効)
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target) {
//                ViewHolderの移動内容を通知
        final int fromPosition = source.getAdapterPosition();
        final int toPosition = target.getAdapterPosition();
//                ViewHolderの要素(Map型インスタンス)をListより取り出し、その要素を削除
//                削除したインスタンスを一時変数に格納の上、ドロップした箇所に挿入
        mAdapter.onItemMove(fromPosition, toPosition);
        return true;
    }

    //            ドラッグされている最中のViewHolderの挙動
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
//                ViewHolderがドラッグされた状態 ＝ actionStateの値が「2」の時、以下の処理を実施
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
//                    ViewHolderが保持するitemView(画面部品)に対し、透過度を「0.5」(半透明)に設定
            if (viewHolder instanceof ItemRecyclerViewAdapter.ItemViewHolder) {
                ItemRecyclerViewAdapter.ItemViewHolder itemViewHolder = (ItemRecyclerViewAdapter.ItemViewHolder) viewHolder;
                itemViewHolder.itemView.setAlpha(0.5f);
                itemViewHolder.onItemSelected();
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    //            ドロップされた直後のViewHolderの挙動
    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
//                オーバーライド元のメソッドを呼び出し、"ViewHolderがnullとなった時"の処理を実施
        super.clearView(recyclerView, viewHolder);
//                ViewHolderが保持するitemViewに対し、透過度を「1.0」(不透明)に設定
        if (viewHolder instanceof ItemRecyclerViewAdapter.ItemViewHolder) {
            ItemRecyclerViewAdapter.ItemViewHolder itemViewHolder = (ItemRecyclerViewAdapter.ItemViewHolder) viewHolder;
            itemViewHolder.itemView.setAlpha(1.0f);
            itemViewHolder.onItemClear();
        }
    }

    //            ViewHolderがスワイプされた場合の動作
//            (※今回は動作なしのため、未記述)
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }
}
