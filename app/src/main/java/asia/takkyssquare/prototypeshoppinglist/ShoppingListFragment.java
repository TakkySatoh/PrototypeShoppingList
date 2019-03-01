package asia.takkyssquare.prototypeshoppinglist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import asia.takkyssquare.prototypeshoppinglist.ShoppingItemContent.ShoppingItem;
import asia.takkyssquare.prototypeshoppinglist.dummy.DummyContent.DummyItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ShoppingListFragment extends Fragment implements OnStartDragListener, RecyclerViewEditListener, ItemRecyclerViewAdapter.OnItemClickListener {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        Context context = view.getContext();
        Bundle args = getArguments();
        String listName = args.getString("listName");
        int position = args.getInt("position");

        RecyclerView rvItemList = view.findViewById(R.id.rvItemList);
        rvItemList.setLayoutManager(new LinearLayoutManager(context));
        if (position == 0) {
            mRVAdapter = new ItemRecyclerViewAdapter(new ShoppingItemContent().createSampleItemList(10, listName), false, mListener, this, this);
        } else {
            mRVAdapter = new ItemRecyclerViewAdapter(new ShoppingItemContent().getItemList(), false, mListener, this, this);
        }
        mRVAdapter.setOnItemClickListener(this);

        rvItemList.setAdapter(mRVAdapter);
        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(mRVAdapter));
        mItemTouchHelper.attachToRecyclerView(rvItemList);

        return view;
    }


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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //    「購入予定」アイテムリストの項目長押し/ハンドル押下時に並び替えアクションを実行
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
        mRVAdapter.addItem(item, item.isHasGot(), -1);
    }

    @Override
    public void onItemClick(ShoppingItem item, int position, int requestCode) {
        mPosition = position;
        Intent intent = new Intent(getActivity(), ShoppingItemEditorActivity.class);
        intent.putExtra("requestCode", requestCode);
        if (item != null) {
            intent.putExtra("hasGot", item.isHasGot());
            intent.putExtra("name", item.getName());
            intent.putExtra("amount", item.getAmount());
            intent.putExtra("price", item.getPrice());
            intent.putExtra("place", item.getPlace());
            intent.putExtra("description", item.getDescription());
            intent.putExtra("createDate", item.getCreateDate());
            intent.putExtra("lastUpdateDate", item.getLastUpdateDate());
        }
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ItemRecyclerViewAdapter.REQUEST_CODE_CREATE && resultCode == ShoppingItemEditorActivity.RESULT_OK) || resultCode == ShoppingItemEditorActivity.RESULT_CODE_COPY) {
            if (data != null) {
                ShoppingItem newItem = new ShoppingItemContent().createItem(data);
                mRVAdapter.addItem(newItem, newItem.isHasGot(), -1);
                Toast.makeText(getActivity(), data.getStringExtra("name") + "を追加しました", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == ItemRecyclerViewAdapter.REQUEST_CODE_UPDATE && resultCode == ShoppingItemEditorActivity.RESULT_OK) {
            if (data != null) {
                ShoppingItem newItem = new ShoppingItemContent().createItem(data);
                boolean hasGot;
                if (mRVAdapter.getItemList().get(mPosition).isHasGot() == newItem.isHasGot()) {
                    hasGot = mRVAdapter.addItem(newItem, newItem.isHasGot(), mPosition + 1);
                } else {
                    hasGot = mRVAdapter.addItem(newItem, newItem.isHasGot(), -1);
                }
                if (!hasGot && mPosition >= mRVAdapter.getToBuyItemAmount() + 2) {
                    mRVAdapter.removeItem(mPosition + 1);
                } else {
                    mRVAdapter.removeItem(mPosition);
                }
                Toast.makeText(getActivity(), data.getStringExtra("name") + "を更新しました", Toast.LENGTH_LONG).show();
            }
        } else if (resultCode == ShoppingItemEditorActivity.RESULT_CODE_DELETE) {
            if (data != null) {
                mRVAdapter.removeItem(mPosition);
                Toast.makeText(getActivity(), data.getStringExtra("name") + "を削除しました", Toast.LENGTH_LONG).show();
            }
        }
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
