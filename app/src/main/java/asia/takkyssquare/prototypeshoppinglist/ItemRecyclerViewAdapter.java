package asia.takkyssquare.prototypeshoppinglist;

import android.support.annotation.NonNull;
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

import asia.takkyssquare.prototypeshoppinglist.ShoppingItemContent.ShoppingItem;
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

    public static final int VIEW_TYPE_HEADER = 11;
    public static final int VIEW_TYPE_ITEM_TO_BUY = 1;
    public static final int VIEW_TYPE_ITEM_BOUGHT = 2;
    public static final int VIEW_TYPE_FOOTER = 21;

    private static ItemRecyclerViewAdapter.OnItemClickListener mClickListener;

    private final List<ShoppingItem> mItemList;
    private final boolean mHasGot;
    private final OnListFragmentInteractionListener mListener;
    private final OnStartDragListener mDragStartListener;
    private final RecyclerViewEditListener mEditListener;

    private int toBuyItemAmount;
    private int boughtItemAmount;

    /**
     * リスナ用インターフェイス
     * RecyclerViewの各項目タップ時の挙動を、RecyclerViewの表示クラスに移譲するために使用
     */
    public interface OnItemClickListener {
        void onItemClick(ShoppingItem item, int position, int requestCode);
    }

    /**
     * ビュータイプごとのViewHolderの生成とバインドを定義する列挙型
     */
    public enum ViewCategory {

        /**
         * ヘッダー
         */
        Header(VIEW_TYPE_HEADER, false) {
            @Override
            public RecyclerView.ViewHolder createViewHolder(LayoutInflater inflater, ViewGroup viewGroup) {
                return new HeaderViewHolder(inflater.inflate(R.layout.fragment_item_header, viewGroup, false));
            }

            @Override
            public void bindViewHolder(RecyclerView.ViewHolder holder, int position, List<ShoppingItem> itemList, OnListFragmentInteractionListener mListener, OnStartDragListener dragListener, RecyclerViewEditListener editListener) {
                final HeaderViewHolder _holder = (HeaderViewHolder) holder;
                /**
                 * ヘッダーの位置により表示文字列を変化
                 * 最前列…「購入予定」/それ以外…「購入済」
                 */
                switch (position) {
                    case 0:
                        _holder.mTvHeader.setText(R.string.title_to_buy);
                        break;
                    default:
                        _holder.mTvHeader.setText(R.string.title_bought);
                        break;
                }
            }
        },

        /**
         * フッター
         */
        Footer(VIEW_TYPE_FOOTER, false) {
            @Override
            public RecyclerView.ViewHolder createViewHolder(LayoutInflater inflater, ViewGroup viewGroup) {
                return new FooterViewHolder(inflater.inflate(R.layout.fragment_item_footer, viewGroup, false));
            }

            @Override
            public void bindViewHolder(RecyclerView.ViewHolder holder, final int position, List<ShoppingItem> itemList, final OnListFragmentInteractionListener mListener, OnStartDragListener dragListener, RecyclerViewEditListener editListener) {
                final FooterViewHolder _holder = (FooterViewHolder) holder;
                /**
                 * 項目タップ時の挙動を定義
                 * タップ後、アイテムの詳細情報を表示させるため、リスナを実装したクラス(リスト表示用Fragment)へ処理を移譲
                 */
                _holder.mTvTitle.setText(R.string.title_create);
                _holder.mTvTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mClickListener) {
                            // Notify the active callbacks interface (the activity, if the
                            // fragment is attached to one) that an item has been selected.
//                            mListener.onListFragmentInteraction(null, REQUEST_CODE_CREATE);
                            mClickListener.onItemClick(null, position, REQUEST_CODE_CREATE);
                        }
                    }
                });
            }
        },

        /**
         * 購入予定
         */
        ItemToBuy(VIEW_TYPE_ITEM_TO_BUY, false) {
            @Override
            public RecyclerView.ViewHolder createViewHolder(LayoutInflater inflater, ViewGroup viewGroup) {
                return new ItemViewHolder(inflater.inflate(R.layout.fragment_item_to_buy, viewGroup, false));
            }

            @Override
            public void bindViewHolder(RecyclerView.ViewHolder holder, int position, List<ShoppingItem> itemList, OnListFragmentInteractionListener mListener, OnStartDragListener dragListener, RecyclerViewEditListener editListener) {
                bindItemViewHolder(holder, position, itemList, mListener, dragListener, editListener);
            }
        },

        /**
         * 購入済
         */
        ItemBought(VIEW_TYPE_ITEM_BOUGHT, true) {
            @Override
            public RecyclerView.ViewHolder createViewHolder(LayoutInflater inflater, ViewGroup viewGroup) {
                return new ItemViewHolder(inflater.inflate(R.layout.fragment_item_bought, viewGroup, false));
            }

            @Override
            public void bindViewHolder(RecyclerView.ViewHolder holder, int position, List<ShoppingItem> itemList, OnListFragmentInteractionListener mListener, OnStartDragListener dragListener, RecyclerViewEditListener editListener) {
                bindItemViewHolder(holder, position, itemList, mListener, dragListener, editListener);
            }
        };

        /**
         * フィールドメンバ
         * id :ビュータイプ
         * hasGot :購入状況判定フラグ
         */
        private int id;
        private boolean hasGot;

        /**
         * コンストラクタ
         */
        ViewCategory(int id, boolean hasGot) {
            this.id = id;
            this.hasGot = hasGot;
        }

        abstract public RecyclerView.ViewHolder createViewHolder(
                LayoutInflater inflater, ViewGroup viewGroup);

        abstract public void bindViewHolder(RecyclerView.ViewHolder holder, int position, List<ShoppingItem> itemList, OnListFragmentInteractionListener mListener, OnStartDragListener dragListener, RecyclerViewEditListener editListener);

        /**
         * ビュータイプIDに基づき、ViewTypeの各要素を出力するメソッド
         *
         * @param viewTypeId :ビュータイプのID
         * @return viewType :列挙型の各要素
         */
        public static ViewCategory getViewType(int viewTypeId) {
            for (ViewCategory viewCategory : ViewCategory.values()) {
                if (viewCategory.id == viewTypeId) {
                    return viewCategory;
                }
            }
            return null;
        }

        /**
         * 「購入予定」「購入済」に共通のビューホルダバインドメソッド
         *
         * @param holder       :各ビューホルダのインスタンス
         * @param position     :タップされた位置情報
         * @param itemList     :アイテム情報の格納リスト
         * @param mListener    :当該インターフェイスを実装したクラス(親Activity)へ処理を引き継がせるためのリスナ
         * @param dragListener :当該インターフェイスを実装したクラス(リスト表示Fragment)へ処理を引き継がせるためのリスナ
         * @param editListener :当該インターフェイスを実装したクラス(リスト表示Fragment)へ処理を引き継がせるためのリスナ
         */
        public void bindItemViewHolder(final RecyclerView.ViewHolder holder, final int position, List<ShoppingItem> itemList, final OnListFragmentInteractionListener mListener, final OnStartDragListener dragListener, final RecyclerViewEditListener editListener) {
            final ItemViewHolder _holder = (ItemViewHolder) holder;
            _holder.mItem = itemList.get(position);
            _holder.mTvHeadlineAmount.setText(Integer.toString(itemList.get(position).getAmount()));
            _holder.mTvHeadlineName.setText(itemList.get(position).getName());

            /**
             * 項目タップ時の挙動 → リスト表示Fragmentに処理を移譲
             */
            _holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
//                        mListener.onListFragmentInteraction(_holder.mItem, REQUEST_CODE_UPDATE);
                        mClickListener.onItemClick(_holder.mItem, position, REQUEST_CODE_UPDATE);
                    }
                }
            });

            /**
             * チェックボックスの状態変化時の挙動 → リスト表示フラグメントへ処理を移譲
             */
            _holder.mCbHasGot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    _holder.mCbHasGot.setChecked(!isChecked);
                    editListener.moveItemBetweenRecyclerViews(isChecked, position);
                }
            });

            /**
             * ハンドルアイコンのタップ時の挙動 → リスト表示フラグメントへ処理を移譲
             */
            _holder.mHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        dragListener.onStartDrag(holder);
                    }
                    return false;
                }
            });

        }
    }

    /**
     * コンストラクタ
     *
     * @param items        :各アイテムを格納したリスト
     * @param hasGot       :購入状況判定
     * @param listener     :親Activityへ処理を移譲するためのリスナ
     * @param dragListener :リスト表示Fragmentへ処理を移譲するためのリスナ
     * @param editListener :リスト表示Fragmentへ処理を移譲するためのリスナ
     */
    public ItemRecyclerViewAdapter(List<ShoppingItem> items, boolean hasGot, OnListFragmentInteractionListener listener, OnStartDragListener dragListener, RecyclerViewEditListener editListener) {
        mItemList = items;
        mHasGot = hasGot;
        mListener = listener;
        mDragStartListener = dragListener;
        mEditListener = editListener;
//        アイテムの購入状況をメンバ変数に計上
        for (ShoppingItem item : mItemList) {
            if (!item.isHasGot()) {
                toBuyItemAmount++;
            } else {
                boughtItemAmount++;
            }
//            アイテムの種別が「アイテム」以外(ヘッダーまたはフッター)の場合、その数量分「購入予定数量」を減少
            if (item.getContentType() != ShoppingItemContent.CONTENT_TYPE_ITEM) {
                toBuyItemAmount--;
            }
        }
    }

    /**
     * OnItemClickListenerインスタンスをフィールドメンバへ設定
     *
     * @param listener :当該リスナを実装したクラス(リスト表示用Fragment)
     */
    public void setOnItemClickListener(ItemRecyclerViewAdapter.OnItemClickListener listener) {
        ItemRecyclerViewAdapter.mClickListener = listener;
    }

    /**
     * アイテムリスト/購入予定アイテム数量/購入済アイテム数量のgetter
     */
    public List<ShoppingItem> getItemList() {
        return mItemList;
    }

    public int getToBuyItemAmount() {
        return toBuyItemAmount;
    }

    public int getBoughtItemAmount() {
        return boughtItemAmount;
    }

    /**
     * ビュータイプ判定
     * 各アイテムのcontentTypeを呼び出し、その値を元に判定
     *
     * @param position :リストの位置情報
     * @return ビュータイプの値 (本クラスの定数を利用)
     */
    @Override
    public int getItemViewType(int position) {
        ShoppingItem item = mItemList.get(position);
        if (item.getContentType() == ShoppingItemContent.CONTENT_TYPE_HEADER) {
            return VIEW_TYPE_HEADER;
        } else if (item.getContentType() == ShoppingItemContent.CONTENT_TYPE_FOOTER) {
            return VIEW_TYPE_FOOTER;
        } else if (item.getContentType() == ShoppingItemContent.CONTENT_TYPE_ITEM && !item.isHasGot()) {
            return VIEW_TYPE_ITEM_TO_BUY;
        } else {
            return VIEW_TYPE_ITEM_BOUGHT;
        }
    }

    /**
     * ビューホルダ生成
     * 取得したビューホルダの値を元に、処理を列挙型へ移譲
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return ViewCategory.getViewType(viewType).createViewHolder(inflater, parent);
    }

    /**
     * ビューホルダのバインド
     * 生成したビューホルダの種別を元に、リストの各位置へビューホルダをバインド
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder != null) {
            ViewCategory.getViewType(holder.getItemViewType()).bindViewHolder(holder, position, mItemList, mListener, mDragStartListener, mEditListener);
        }
    }

    /**
     * RecyclerViewの表示要素数をカウント
     */
    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    /**
     * RecyclerViewへのアイテム追加を定義
     * 追加位置:
     * 新規かつ購入予定…購入予定行の最後尾 / 新規かつ購入済…購入済み行の先頭
     * それ以外…引数で指定された位置
     */
    public boolean addItem(ShoppingItem item, boolean hasGot, int position) {
        if (!hasGot) {
            if (position == -1) {
                position = toBuyItemAmount + 1;
            }
            toBuyItemAmount++;
        } else {
            if (position == -1) {
                position = toBuyItemAmount + 3;
            }
            boughtItemAmount++;
        }
        mItemList.add(position, item);
        notifyItemInserted(position);
        notifyDataSetChanged();
        if (hasGot) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * アイテムの削除を定義
     */
    public ShoppingItem removeItem(int position) {
        ShoppingItem item = mItemList.remove(position);
        boolean hasGot = item.isHasGot();
        if (!hasGot) {
            toBuyItemAmount--;
        } else {
            boughtItemAmount--;
        }
        notifyItemRemoved(position);
        notifyDataSetChanged();
        return item;
    }

    /**
     * アイテムの位置移動を定義
     */
    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItemList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    /**
     * アイテムの削除を定義
     */
    @Override
    public void onItemDismiss(int position) {
        mItemList.remove(position);
        notifyItemRemoved(position);
    }


    /**
     * アイテム用ビューホルダ
     */
    public static class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        public final View mView;
        public final CheckBox mCbHasGot;
        public final TextView mTvHeadlineName;
        public final TextView mTvHeadlineAmount;
        public final ImageView mHandle;
        public ShoppingItem mItem;

        public ItemViewHolder(View view) {
            super(view);
            mView = view;
            mCbHasGot = (CheckBox) view.findViewById(R.id.cbHeadlineHasGot);
            mTvHeadlineName = (TextView) view.findViewById(R.id.tvHeadlineName);
            mTvHeadlineAmount = (TextView) view.findViewById(R.id.tvHeadlineAmount);
            mHandle = (ImageView) view.findViewById(R.id.handle);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTvHeadlineName.getText() + "'";
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

    /**
     * ヘッダー用ビューホルダ
     */
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvHeader;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvHeader = itemView.findViewById(R.id.tvHeader);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTvHeader.getText() + "'";
        }
    }

    /**
     * フッター用ビューホルダ
     */
    public static class FooterViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private View mView;
        private TextView mTvTitle;

        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mTvTitle = itemView.findViewById(R.id.tvTitle);
            mTvTitle.setText(R.string.title_create);
        }

        @Override
        public void onItemSelected() {
            mView.setElevation(16.0f);
        }

        @Override
        public void onItemClear() {
            mView.setElevation(0.0f);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTvTitle.getText() + "'";
        }

    }
}
