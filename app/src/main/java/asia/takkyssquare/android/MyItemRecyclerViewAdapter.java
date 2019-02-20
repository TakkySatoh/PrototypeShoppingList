package asia.takkyssquare.android;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import asia.takkyssquare.android.ShoppingListFragment.OnListFragmentInteractionListener;
import asia.takkyssquare.android.dummy.DummyContent.DummyItem;

import java.util.Collections;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.RecyclerViewHolder> implements ItemTouchHelperAdapter {

    private final List<DummyItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final OnStartDragListener mDragStartListener;
    private final RecyclerViewEditListener mEditListener;

    public MyItemRecyclerViewAdapter(List<DummyItem> items, OnListFragmentInteractionListener listener, OnStartDragListener dragListener, RecyclerViewEditListener editListener) {
        mValues = items;
        mListener = listener;
        mDragStartListener = dragListener;
        mEditListener = editListener;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).content);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });

        holder.mCbHasGot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEditListener.moveItemBetweenRecyclerViews(isChecked, position);
            }
        });

        holder.mHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }


    public void addItem(DummyItem item) {
        mValues.add(0, item);
        notifyItemInserted(0);
        notifyDataSetChanged();
    }

    public DummyItem removeItem(int position) {
        DummyItem item = mValues.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
        return item;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mValues, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        mValues.remove(position);
        notifyItemRemoved(position);
    }


    public class RecyclerViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        public final View mView;
        public final CheckBox mCbHasGot;
        public final TextView mIdView;
        public final TextView mContentView;
        public final TextView mAmountView;
        public final ImageView mHandle;
        public DummyItem mItem;

        public RecyclerViewHolder(View view) {
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
