package asia.takkyssquare.prototypeshoppinglist;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import asia.takkyssquare.prototypeshoppinglist.ShoppingItemContent.ShoppingItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ShoppingListFragment extends Fragment implements OnStartDragListener, RecyclerViewEditListener, ItemRecyclerViewAdapter.OnItemClickListener, GeneralDialogFragment.Callback {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    private int listId;
    private OnListFragmentInteractionListener mListener;

    private ItemRecyclerViewAdapter mRVAdapter;
    private ItemTouchHelper mItemTouchHelper;

    private int mPosition;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ShoppingListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
//    public static ShoppingListFragment newInstance(int columnCount) {
//        ShoppingListFragment fragment = new ShoppingListFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_COLUMN_COUNT, columnCount);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        listId = Integer.parseInt(getTag());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /**
         * Bundleインスタンスを呼び出し元Activityより取得
         * その後、リスト名のデータを取り出す
         */
        Bundle args = getArguments();
        String listName = args.getString("listName");
        if (listId == 0) {
            listId = args.getInt("_id");
        }

        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        Context context = view.getContext();

        /**
         * リストの内容を表示するRecyclerViewを生成
         * その後、LayoutManager(LinearLayout)、RecyclerView.AdapterのサブクラスをRecyclerViewへ設定
         */
        RecyclerView rvItemList = view.findViewById(R.id.rvItemList);
        rvItemList.setLayoutManager(new LinearLayoutManager(context));
//        if (listName.equals("サンプル")) {
//            mRVAdapter = new ItemRecyclerViewAdapter(new ShoppingItemContent().createSampleItemList(10, listName), false, mListener, this, this);
//        } else {
        mRVAdapter = new ItemRecyclerViewAdapter(new ShoppingItemContent().getItemList(getContext(), listId), false, mListener, this, this);
//        }
        mRVAdapter.setOnItemClickListener(this);
        rvItemList.setAdapter(mRVAdapter);

        /**
         * RecyclerViewに対し、ItemTouchHelperを設定
         * 詳細な挙動は、別途カスタムコールバッククラスにて定義
         */
        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(mRVAdapter));
        mItemTouchHelper.attachToRecyclerView(rvItemList);

        return view;
    }

    /**
     * Fragment再生成時にFragment用コールバックリスナを再度読み込み
     * コールバックリスナ未設定(該当InterfaceをActivityに実装していない)場合、エラーをスロー
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    /**
     * Fragment破棄時の挙動を記述
     * メモリリーク回避のため、コールバックリスナを外す
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /*
     *「購入予定」アイテムリストの項目長押し/ハンドル押下時に並び替えアクションを実行
     */
    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    /**
     * Insert object to RecyclerView
     *
     * @param item
     */
    @Override
    public void insertToRecyclerView(ItemRecyclerViewAdapter adapter, List<ShoppingItem> list, ShoppingItem item) {
        int footerPosition = mRVAdapter.getToBuyItemAmount() + 1;
        if (list != null) {
            if (-1 != mPosition) {
                if (item.isHasGot()) {
                    list.add(footerPosition + 2, item);
                } else {
                    if (mPosition == footerPosition) {
                        list.add(footerPosition, item);
                    } else {
                        list.add(mPosition + 1, item);
                    }
                    adapter.notifyItemInserted(list.indexOf(item));
                }
            }
        }
    }

    /**
     * Update object to RecyclerView
     *
     * @param item
     */
    @Override
    public void updateToRecyclerView(ItemRecyclerViewAdapter
                                             adapter, List<ShoppingItem> list, ShoppingItem item) {
        insertToRecyclerView(adapter, list, item);
        deleteFromRecyclerView(adapter, list, null);
        adapter.notifyDataSetChanged();
    }

    /**
     * Delete object from RecyclerView
     *
     * @param item
     */
    @Override
    public void deleteFromRecyclerView(ItemRecyclerViewAdapter
                                               adapter, List<ShoppingItem> list, ShoppingItem item) {
        int index = 0;
        boolean isDelete = false;
        if (list != null) {
            if (item != null) {
                index = list.indexOf(item);
                if (-1 != index) {
                    isDelete = list.remove(item);
                }
            } else {
                index = mPosition;
                ShoppingItem oldItem = list.remove(mPosition);
                if (!oldItem.equals(list.get(mPosition))) {
                    isDelete = true;
                }
            }
            if (isDelete) {
                adapter.notifyItemRemoved(index);
            }
        }
    }

    //    両RecyclerViewの項目中、チェックボックスの状態遷移に応じて、両リスト間を項目が移動
    public void moveItemBetweenRecyclerViews(boolean hasGot, int position) {
        ShoppingItem item = mRVAdapter.removeItem(position);
        item.setHasGot(hasGot);
        mRVAdapter.addItem(getContext(), item, item.isHasGot(), -1);
        DBHelper dbHelper = new DBHelper(getContext());
        try {
            dbHelper.updateItem(item);
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(DBHelper.TAG,"Error: DBHelper could not update has_got status."+e.toString());
        } finally {
            if (dbHelper != null) {
                dbHelper.closeDB();
                Log.d(DBHelper.TAG,"Completed: DBHelper finished to update has_got status!");
            }
        }
    }

    /**
     * ItemRecyclerViewAdapter.OnItemClickListenerの抽象メソッドの実装
     * RecyclerViewの項目タップ時の挙動を定義
     * 受け取った引数を元に、対象アイテムの詳細情報表示・編集用Activityを呼び出す
     *
     * @param item        :対象アイテムの情報を格納するJava Beansのインスタンス
     * @param position    :タップされた位置情報
     * @param requestCode :リクエストコード
     */
    @Override
    public void onItemClick(ShoppingItem item, int position, int requestCode) {
        mPosition = position;
        Intent intent = new Intent(getActivity(), ShoppingItemEditorActivity.class);
        intent.putExtra("requestCode", requestCode);
        if (item != null) {
            intent.putExtra("hasGot", item.isHasGot());
//            if (requestCode == ItemRecyclerViewAdapter.REQUEST_CODE_CREATE) {
//                intent.putExtra("itemId", 0);
//                intent.putExtra("listId", listId);
//                intent.putExtra("order", 0);
//            } else {
            intent.putExtra("itemId", item.getItemId());
            intent.putExtra("listId", item.getListId());
            intent.putExtra("order", item.getOrder());
//            }
            intent.putExtra("name", item.getName());
            intent.putExtra("amount", item.getAmount());
            intent.putExtra("price", item.getPrice());
            intent.putExtra("place", item.getPlace());
            intent.putExtra("description", item.getDescription());
            intent.putExtra("createDate", item.getCreateDate());
            intent.putExtra("lastUpdateDate", item.getLastUpdateDate());
        } else {
            intent.putExtra("itemId", 0);
            intent.putExtra("listId", listId);
            intent.putExtra("order", 0);
        }
        startActivityForResult(intent, requestCode);
    }

    /**
     * 上記Activity呼び出し後の処理を記述
     * リクエストコードとリザルトコードにより処理分岐
     * 主な流れ … 引数のIntentインスタンスを元にItemインスタンスを生成
     * → アイテム新規生成時はRecyclerViewの「購入予定の末尾」または「購入済の先頭」、
     * アイテム情報更新時は該当箇所+1番目にitemを挿入
     * → アイテム情報更新時、アイテム削除時は該当箇所のitemをRecyclerViewより削除
     * ※アイテムのリスト間移動時は、リスナを通じて処理を親Activityへ委譲する
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ItemRecyclerViewAdapter.REQUEST_CODE_CREATE && resultCode == ShoppingItemEditorActivity.RESULT_OK) || resultCode == ShoppingItemEditorActivity.RESULT_CODE_COPY) {  // 新規アイテム追加・複製
            if (data != null) {
                ShoppingItem newItem = new ShoppingItemContent().createItem(data);
                mRVAdapter.addItem(getContext(), newItem, newItem.isHasGot(), -1);
                DBHelper dbHelper = new DBHelper(getContext());
                try {
                    dbHelper.updateOrder(newItem);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.w(DBHelper.TAG, "Error: DBHelper has a problem. " + e.toString());
                } finally {
                    if (dbHelper != null) {
                        dbHelper.closeDB();
                    }
                }
                Toast.makeText(getActivity(), data.getStringExtra("name") + "を追加しました", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == ItemRecyclerViewAdapter.REQUEST_CODE_UPDATE && resultCode == ShoppingItemEditorActivity.RESULT_OK) {  //アイテムデータ更新
            if (data != null) {
                ShoppingItem newItem = new ShoppingItemContent().createItem(data);
                boolean hasGot;
                if (mRVAdapter.getItemList().get(mPosition).isHasGot() == newItem.isHasGot()) {
                    //購入済フラグ書き換え無し…更新対象アイテムの一つ下に更新済アイテムを追加
                    hasGot = mRVAdapter.addItem(getContext(), newItem, newItem.isHasGot(), mPosition + 1);
                } else {
                    //購入済フラグ書き換え有り…各々の新規アイテム追加位置にアイテムを追加
                    hasGot = mRVAdapter.addItem(getContext(), newItem, newItem.isHasGot(), -1);
                    if (!newItem.isHasGot()) {
                        mRVAdapter.sortItems();
                    }
                }
                if (!hasGot && mPosition >= mRVAdapter.getToBuyItemAmount() + 2) {
                    //更新後アイテムが購入済フラグ無し&更新前アイテムが購入済 →購入予定位置に追加につき、更新対象アイテムの一つ下を削除
                    mRVAdapter.removeItem(mPosition + 1);
                } else {
                    //上記以外 →購入済位置に追加につき、更新対象アイテムを削除
                    mRVAdapter.removeItem(mPosition);
                }
                DBHelper dbHelper = new DBHelper(getContext());
                try {
                    dbHelper.updateOrder(newItem);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.w(DBHelper.TAG, "Error: DBHelper has a problem. " + e.toString());
                } finally {
                    if (dbHelper != null) {
                        dbHelper.closeDB();
                    }
                }
                Toast.makeText(getActivity(), data.getStringExtra("name") + "を更新しました", Toast.LENGTH_LONG).show();
            }
        } else if (resultCode == ShoppingItemEditorActivity.RESULT_CODE_DELETE) {   //アイテム削除
            if (data != null) {
                mRVAdapter.removeItem(mPosition);
                DBHelper dbHelper = new DBHelper(getContext());
                try {
                    dbHelper.removeOrder(data);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.w(DBHelper.TAG, "Error: DBHelper has a problem. " + e.toString());
                } finally {
                    if (dbHelper != null) {
                        dbHelper.closeDB();
                    }
                }
                Toast.makeText(getActivity(), data.getStringExtra("name") + "を削除しました", Toast.LENGTH_LONG).show();
            }
//        } else if (resultCode == ShoppingItemEditorActivity.RESULT_CODE_MOVE) {
//            if (data != null) {
//                ShoppingItem newItem = new ShoppingItemContent().createItem(data);
//                mListener.onListFragmentInteraction(newItem, resultCode);
//                new GeneralDialogFragment.Builder(this)
//                        .title(R.string.alert_move_to)
//                        .items(MainActivity.mListNameList.toArray(new String[MainActivity.mListNameList.size()]))
//                        .requestCode(resultCode)
//                        .positive(R.string.reply_move)
//                        .negative(R.string.cancel)
//                        .show();
//            }
        }
    }

    @Override
    public void onMyDialogSucceeded(int requestCode, int resultCode, Bundle params) {

    }

    @Override
    public void onMyDialogCancelled(int requestCode, Bundle params) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(ShoppingItem item, int requestCode);
    }

}
