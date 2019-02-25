package asia.takkyssquare.prototypeshoppinglist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import asia.takkyssquare.prototypeshoppinglist.ShoppingListFragment.OnListFragmentInteractionListener;
import asia.takkyssquare.prototypeshoppinglist.dummy.DummyContent.DummyItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ItemRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {

    public static final int REQUEST_CODE_CREATE = 100;
    public static final int REQUEST_CODE_UPDATE = 200;

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM_TO_BUY = 1;
    private static final int VIEW_TYPE_ITEM_BOUGHT = 2;
    private static final int VIEW_TYPE_FOOTER = 3;

    private final List<DummyItem> mItemList;
    private final boolean mHasGot;
    private final OnListFragmentInteractionListener mListener;
    private final OnStartDragListener mDragStartListener;
    private final RecyclerViewEditListener mEditListener;

    private int toBuyItemAmount;
    private int boughtItemAmount;


    public ItemRecyclerViewAdapter(List<DummyItem> items, boolean hasGot, OnListFragmentInteractionListener listener, OnStartDragListener dragListener, RecyclerViewEditListener editListener) {
        mItemList = items;
        mHasGot = hasGot;
        mListener = listener;
        mDragStartListener = dragListener;
        mEditListener = editListener;
        for (DummyItem item : mItemList) {
            if (!item.isHasGot()) {
                toBuyItemAmount++;
            } else {
                boughtItemAmount++;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == toBuyItemAmount + 2) {
            return VIEW_TYPE_HEADER;
        } else if (position < toBuyItemAmount + 1) {
            return VIEW_TYPE_ITEM_TO_BUY;
        } else if (position == toBuyItemAmount + 1) {
            return VIEW_TYPE_FOOTER;
        } else {
            return VIEW_TYPE_ITEM_BOUGHT;
        }
    }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder (ViewGroup parent,int viewType){
            View view;
            if (!mHasGot) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_item_to_buy, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_item_bought, parent, false);
            }
            return (RecyclerView.ViewHolder) new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder ( final RecyclerView.ViewHolder holder, final int position){
            if (holder instanceof ItemViewHolder) {
                final ItemViewHolder _holder = (ItemViewHolder) holder;
                _holder.mItem = mItemList.get(position);
                _holder.mIdView.setText(mItemList.get(position).id);
                _holder.mContentView.setText(mItemList.get(position).content);

                _holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            // Notify the active callbacks interface (the activity, if the
                            // fragment is attached to one) that an item has been selected.
                            mListener.onListFragmentInteraction(_holder.mItem, REQUEST_CODE_UPDATE);
                        }
                    }
                });

                _holder.mCbHasGot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        _holder.mCbHasGot.setChecked(!isChecked);
                        mEditListener.moveItemBetweenRecyclerViews(mHasGot, position);
                    }
                });

                _holder.mHandle.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                            mDragStartListener.onStartDrag(holder);
                        }
                        return false;
                    }
                });

            }
        }

        @Override
        public int getItemCount () {
            return mItemList.size();
        }

        public boolean addItem (DummyItem item){
            mItemList.add(0, item);
            notifyItemInserted(0);
            notifyDataSetChanged();
            return true;
        }

        public DummyItem removeItem ( int position){
            DummyItem item = mItemList.remove(position);
            notifyItemRemoved(position);
            notifyDataSetChanged();
            return item;
        }

        @Override
        public boolean onItemMove ( int fromPosition, int toPosition){
            Collections.swap(mItemList, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onItemDismiss ( int position){
            mItemList.remove(position);
            notifyItemRemoved(position);
        }


        public class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

            public final View mView;
            public final CheckBox mCbHasGot;
            public final TextView mIdView;
            public final TextView mContentView;
            public final TextView mAmountView;
            public final ImageView mHandle;
            public DummyItem mItem;

            public ItemViewHolder(View view) {
                super(view);
                mView = view;
                mCbHasGot = (CheckBox) view.findViewById(R.id.cbHasGot);
                mIdView = (TextView) view.findViewById(R.id.item_number);
                mContentView = (TextView) view.findViewById(R.id.content);
                mAmountView = (TextView) view.findViewById(R.id.amount);
                mHandle = (ImageView) view.findViewById(R.id.handle);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }

            @Override
            public void onItemSelected() {
                mView.setElevation(16.0f);
            }

            @Override
            public void onItemClear() {
                mView.setElevation(0.0f);

            }
        }

    }