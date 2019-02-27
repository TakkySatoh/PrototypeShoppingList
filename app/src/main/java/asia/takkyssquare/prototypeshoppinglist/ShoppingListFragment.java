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

import java.util.List;

import asia.takkyssquare.prototypeshoppinglist.dummy.DummyContent.DummyItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ShoppingListFragment extends Fragment implements OnStartDragListener, RecyclerViewEditListener {

    private static final int REQUEST_CODE_CREATE = 100;
    private static final int REQUEST_CODE_UPDATE = 200;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private ItemRecyclerViewAdapter mRVAdapter;
    private ItemTouchHelper mItemTouchHelper;

//    private MyItemRecyclerViewAdapter mToBuyAdapter;
//    private MyItemRecyclerViewAdapter mBoughtAdapter;
//    private ItemTouchHelper mToBuyItemTouchHelper;
//    private ItemTouchHelper mBoughtItemTouchHelper;
//    private TextView tvListName;

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
    public void insertToRecyclerView(ItemRecyclerViewAdapter adapter, List<DummyItem> list, DummyItem item) {
        list.add(0, item);
        adapter.notifyItemInserted(0);
    }

    /**
     * Update object to RecyclerView
     *
     * @param item
     */
    public void updateToRecyclerView(ItemRecyclerViewAdapter adapter, List<DummyItem> list, DummyItem item) {
        if (list != null) {
            int index = list.indexOf(item);
            if (-1 != index) {
                adapter.notifyItemChanged(index, item);
            }
        }
    }

    /**
     * Delete object from RecyclerView
     *
     * @param item
     */
    public DummyItem deleteFromRecyclerView(ItemRecyclerViewAdapter adapter, List<DummyItem> list, DummyItem item) {
        DummyItem charge = null;
        if (list != null) {
            int index = list.indexOf(item);
            if (-1 != index) {
                charge = list.remove(index);
                boolean isDelete = list.remove(item);
                if (isDelete) {
                    adapter.notifyItemRemoved(index);
                }
            }
        }
        return charge;
    }

    @Override
    public void insertToRecyclerView(ItemRecyclerViewAdapter adapter, List<ShoppingItemContent.ShoppingItem> list, ShoppingItemContent.ShoppingItem item) {

    }

    @Override
    public void updateToRecyclerView(ItemRecyclerViewAdapter adapter, List<ShoppingItemContent.ShoppingItem> list, ShoppingItemContent.ShoppingItem item) {

    }

    @Override
    public ShoppingItemContent.ShoppingItem deleteFromRecyclerView(ItemRecyclerViewAdapter adapter, List<ShoppingItemContent.ShoppingItem> list, ShoppingItemContent.ShoppingItem item) {
        return null;
    }

    //    両RecyclerViewの項目中、チェックボックスの状態遷移に応じて、両リスト間を項目が移動
    public void moveItemBetweenRecyclerViews(boolean hasGot, int position) {
        ShoppingItemContent.ShoppingItem item = mRVAdapter.removeItem(position);
        item.setHasGot(hasGot);
        mRVAdapter.addItem(item, item.isHasGot());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CREATE && resultCode == MainActivity.RESULT_OK) {

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
        void onListFragmentInteraction(ShoppingItemContent.ShoppingItem item, int requestCode);
    }

}
