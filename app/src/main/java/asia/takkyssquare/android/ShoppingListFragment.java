package asia.takkyssquare.android;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import asia.takkyssquare.android.dummy.DummyContent;
import asia.takkyssquare.android.dummy.DummyContent.DummyItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ShoppingListFragment extends Fragment implements OnStartDragListener, RecyclerViewEditListener {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private MyItemRecyclerViewAdapter mToBuyAdapter;
    private MyItemRecyclerViewAdapter mBoughtAdapter;
    private ItemTouchHelper mToBuyItemTouchHelper;
    private ItemTouchHelper mBoughtItemTouchHelper;
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

        // Set the adapter
//        if (view instanceof RecyclerView) {
        Context context = view.getContext();
//        tvListName = view.findViewById(R.id.tvListName);
        Bundle args = getArguments();
//            String[] listNames = mActivity.getResources().getStringArray(R.array.shopping_list);
//            int position = args.getInt("position");
//        String listName = args.getString("listName");
//        tvListName.setText(listName);
//            tvListName.setText(listNames[position]);
        RecyclerView rvListToBuy = view.findViewById(R.id.rvListToBuy);
        if (mColumnCount <= 1) {
            rvListToBuy.setLayoutManager(new LinearLayoutManager(context));
        } else {
            rvListToBuy.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        mToBuyAdapter = new MyItemRecyclerViewAdapter(DummyContent.toBuyItems, mListener, this, this);
        rvListToBuy.setAdapter(mToBuyAdapter);
//        }
        mToBuyItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(mToBuyAdapter));
        mToBuyItemTouchHelper.attachToRecyclerView(rvListToBuy);

        RecyclerView rvListBought = view.findViewById(R.id.rvListBought);
        if (mColumnCount <= 1) {
            rvListBought.setLayoutManager(new LinearLayoutManager(context));
        } else {
            rvListBought.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        mBoughtAdapter = new MyItemRecyclerViewAdapter(DummyContent.boughtItems, mListener, this, this);
        mBoughtItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(mBoughtAdapter));
        mBoughtItemTouchHelper.attachToRecyclerView(rvListBought);

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

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mToBuyItemTouchHelper.startDrag(viewHolder);
    }

    /**
     * Insert object to RecyclerView
     *
     * @param item
     */
    public void insertToRecyclerView(MyItemRecyclerViewAdapter adapter, List<DummyItem> list, DummyItem item) {
        list.add(0, item);
        adapter.notifyItemInserted(0);
    }

    /**
     * Update object to RecyclerView
     *
     * @param item
     */
    public void updateToRecyclerView(MyItemRecyclerViewAdapter adapter, List<DummyItem> list, DummyItem item) {
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
    public DummyItem deleteFromRecyclerView(MyItemRecyclerViewAdapter adapter, List<DummyItem> list, DummyItem item) {
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

    public void moveItemBetweenRecyclerViews(boolean isChecked, int position) {
        if (isChecked) {
            mBoughtAdapter.addItem(mToBuyAdapter.removeItem(position));
        } else {
            mToBuyAdapter.addItem(mBoughtAdapter.removeItem(position));
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
        void onListFragmentInteraction(DummyItem item);
    }

}
