package asia.takkyssquare.prototypeshoppinglist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;

/**
 * ItemTouchHelper.Callbackの詳細定義クラス
 */
class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private MyItemRecyclerViewAdapter mAdapter;

    public ItemTouchHelperCallback(MyItemRecyclerViewAdapter adapter) {
        mAdapter = adapter;
    }
//          Callbackの抽象メソッド3種をオーバーライド

    //          これより以下のメソッドの稼働条件を設定
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
//                稼働条件 … ViewHolderのインスタンスが上または下方向にドラッグされた場合
        return makeMovementFlags(ItemTouchHelper.UP | DOWN, 0);
    }

    //            ViewHolderがドラッグされた場合の動作
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//                ViewHolderの移動内容を通知
        final int fromPosition = viewHolder.getAdapterPosition();
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
            if (viewHolder instanceof MyItemRecyclerViewAdapter.RecyclerViewHolder) {
                MyItemRecyclerViewAdapter.RecyclerViewHolder recyclerViewHolder = (MyItemRecyclerViewAdapter.RecyclerViewHolder) viewHolder;
                recyclerViewHolder.itemView.setAlpha(0.5f);
                recyclerViewHolder.onItemSelected();
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
        if (viewHolder instanceof MyItemRecyclerViewAdapter.RecyclerViewHolder) {
            MyItemRecyclerViewAdapter.RecyclerViewHolder recyclerViewHolder = (MyItemRecyclerViewAdapter.RecyclerViewHolder) viewHolder;
            recyclerViewHolder.itemView.setAlpha(1.0f);
            recyclerViewHolder.onItemClear();
        }
    }

    //            ViewHolderがスワイプされた場合の動作
//            (※今回は動作なしのため、未記述)
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }
}
