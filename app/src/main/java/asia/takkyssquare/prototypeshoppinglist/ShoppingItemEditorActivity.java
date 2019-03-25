package asia.takkyssquare.prototypeshoppinglist;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class ShoppingItemEditorActivity extends AppCompatActivity implements GeneralDialogFragment.Callback {

    public static final int RESULT_CODE_MOVE = 29;
    public static final int RESULT_CODE_COPY = 49;
    public static final int RESULT_CODE_DELETE = 99;

    public static final String TAG = "DBHelper";

    private EditText mEtItemName;
    private EditText mEtItemAmount;
    private EditText mEtItemPrice;
    private EditText mEtItemTotalPrice;
    private EditText mEtPlace;
    private EditText mEtComment;
    private CheckBox mCbHasGot;
    private Button mBtDelete;
    private Button mBtCopyItem;
    private Button mBtMove;
    private Button mBtReply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_item_editor);

        /**
         * Intentインスタンスより受け取るデータを予め宣言
         * Intentインスタンスのnullチェック後に各種データを取り出す
         */
        int requestCode = 0;

        int amount = 0;
        int price = 0;
        boolean hasGot = false;
        String itemName = null;
        String place = null;
        String comment = null;
        final long createDate;
        long lastUpdateDate = 0;

        Intent intent = getIntent();
        if (intent != null) {
            requestCode = intent.getIntExtra("requestCode", ItemRecyclerViewAdapter.REQUEST_CODE_CREATE);
            hasGot = intent.getBooleanExtra("hasGot", false);
            itemName = intent.getStringExtra("name");
            amount = intent.getIntExtra("amount", 0);
            price = intent.getIntExtra("price", 0);
            place = intent.getStringExtra("place");
            comment = intent.getStringExtra("description");
            createDate = intent.getLongExtra("createDate", System.currentTimeMillis());
            lastUpdateDate = intent.getLongExtra("lastUpdateDate", System.currentTimeMillis());
        } else {
            createDate = System.currentTimeMillis();
        }

        /**
         * Toolbarを設定
         * リクエストコードに応じてタイトル設定を変える
         */
        Toolbar toolbar = findViewById(R.id.tbEditor);
        if (intent.getIntExtra("requestCode", ItemRecyclerViewAdapter.REQUEST_CODE_CREATE) == ItemRecyclerViewAdapter.REQUEST_CODE_UPDATE) {
            toolbar.setTitle(R.string.title_update);
        } else {
            toolbar.setTitle(R.string.title_create);
        }
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        /**
         * Toolbarに戻るボタンを表示
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        /**
         * 各EditTextをレイアウトより取得し、ヒントを設定
         */
        mEtItemName = findViewById(R.id.etItemName);
        mEtItemAmount = findViewById(R.id.etItemAmount);
        mEtItemPrice = findViewById(R.id.etItemPrice);
        mEtItemTotalPrice = findViewById(R.id.etItemTotalPrice);
        mEtPlace = findViewById(R.id.etPlace);
        mEtComment = findViewById(R.id.etComment);

        mEtItemName.setHint(getString(R.string.hint, getString(R.string.item_name)));
        mEtItemAmount.setHint(getString(R.string.hint, getString(R.string.amount)));
        mEtItemPrice.setHint(getString(R.string.hint, getString(R.string.price)));
        mEtItemTotalPrice.setHint(getString(R.string.hint, getString(R.string.total_price)));
        mEtPlace.setHint(getString(R.string.hint, getString(R.string.place)));
        mEtComment.setHint(getString(R.string.hint, getString(R.string.comment)));

        /**
         * 「商品名」「説明」「場所」の情報有無確認後、既存データある場合にEditTextへ表示
         */
        if (itemName != null) {
            mEtItemName.setText(itemName);
        }
        if (comment != null) {
            mEtComment.setText(comment);
        }
        if (place != null) {
            mEtPlace.setText(place);
        }
        /**
         * 「数量」「単価」の情報をEditTextへ表示。3桁ごとのカンマ区切りを設定
         * また、「合計金額」を「数量」と「単価」を元に計算の上、同様に表示
         */
        mEtItemAmount.setText(String.format("%,d", amount));
        mEtItemPrice.setText(String.format("%,d", price));
        mEtItemTotalPrice.setText(String.format("%,d", price * amount));

        /**
         * 「数量」「単価」のEditTextへTextChangeListenerを設定
         */
        mEtItemPrice.addTextChangedListener(new TotalPriceWatcher(mEtItemPrice));
        mEtItemAmount.addTextChangedListener(new TotalPriceWatcher(mEtItemAmount));

        /**
         * 「購入済フラグ」のCheckBoxをレイアウトより取得し、Intentの格納内容に応じて設定
         */
        mCbHasGot = findViewById(R.id.cbHeadlineHasGot);
        mCbHasGot.setChecked(hasGot);

        /**
         * 「項目を削除」のボタンをレイアウトより取得し、リスナを設定
         *  ※項目の新規生成時は無効化する
         */
        mBtDelete = findViewById(R.id.btDelete);
        if (requestCode == ItemRecyclerViewAdapter.REQUEST_CODE_UPDATE) {
            mBtDelete.setOnClickListener(new OnButtonClickListener(this));
        } else {
            mBtDelete.setEnabled(false);
        }

        /**
         * 「項目をコピー」のボタンをレイアウトより取得し、リスナを設定
         *  ※今回は無効化する
         */
        mBtCopyItem = findViewById(R.id.btCopyItem);
//        if (requestCode == ItemRecyclerViewAdapter.REQUEST_CODE_UPDATE) {
//            mBtCopyItem.setOnClickListener(new OnButtonClickListener(this));
//        } else {
            mBtCopyItem.setEnabled(false);
//        }

        /**
         * 「項目を移動」のボタンをレイアウトより取得し、リスナを設定
         *  ※今回は無効化する
         */
        mBtMove = findViewById(R.id.btMove);
//        if (requestCode == ItemRecyclerViewAdapter.REQUEST_CODE_UPDATE) {
//            mBtMove.setOnClickListener(new OnButtonClickListener(this));
//        } else {
            mBtMove.setEnabled(false);
//        }

        /**
         * 「項目を作成」「内容を更新」のボタンをレイアウトより取得し、リスナを設定
         *  ※ボタンへの表示文字列はリクエストコードにより判定
         */
        mBtReply = findViewById(R.id.btReply);
        if (requestCode == ItemRecyclerViewAdapter.REQUEST_CODE_CREATE) {
            mBtReply.setText(R.string.reply_create);
        } else {
            mBtReply.setText(R.string.reply_update);
        }
        mBtReply.setOnClickListener(new OnButtonClickListener(this));
    }

    /**
     * 合計金額の自動計算
     * 「数量」「単価」欄がいずれも空欄となる場合は、強制的に"0"を入力させるように設定
     */
    private String calculateTotalPrice() {
        String priceStr = mEtItemPrice.getText().toString();
        if (priceStr == null || priceStr.equals("")) priceStr = "0";
        String amountStr = mEtItemAmount.getText().toString();
        if (amountStr == null || amountStr.equals("")) amountStr = "0";
        return String.format("%,d", Integer.parseInt(priceStr) * Integer.parseInt(amountStr));
    }

    /**
     * GeneralDialogFragmentからのコールバックを記述
     * 項目のコピー時、削除時に利用し、許諾時はsetResult()により、その後の処理を呼び出し元へ引き継ぐ
     *
     * @param requestCode :リクエストコード
     * @param resultCode  :リザルトコード(ダイアログのボタン種別判定に利用)
     * @param params      :ダイアログ生成時に利用するBundleインスタンス。今回未使用
     */
    @Override
    public void onMyDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        //アイテム削除
        if (requestCode == RESULT_CODE_DELETE && resultCode == DialogInterface.BUTTON_POSITIVE) {
            Intent data = getIntent();
            DBHelper dbHelper = new DBHelper(getApplicationContext());
            try {
                dbHelper.moveToDeletedTable(data);
            } catch (Exception e) {
                e.printStackTrace();
                Log.w(TAG, "Error: DBHelper could not move the item to deleted table." + e.toString());
            } finally {
                dbHelper.closeDB();
            }
            setResult(RESULT_CODE_DELETE, data);
            finish();
            //アイテム複製(今回は無効)
//        } else if (requestCode == RESULT_CODE_COPY && resultCode == DialogInterface.BUTTON_POSITIVE) {
//            Intent data = new OnButtonClickListener(this).addAndUpdateItem(RESULT_CODE_COPY);
//            int itemId = 0;
//            DBHelper dbHelper = new DBHelper(getApplicationContext());
//            try {
//                itemId = dbHelper.updateItem(data);
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (dbHelper != null) {
//                    dbHelper.closeDB();
//                }
//            }
//            if (data.getIntExtra("itemId", 0) == 0) {
//                data.removeExtra("itemId");
//                data.putExtra("itemId", itemId);
//            }
//            setResult(RESULT_CODE_COPY, data);
//            finish();
        }
    }

    @Override
    public void onMyDialogCancelled(int requestCode, Bundle params) {

    }

    /**
     * 「数量」「単価」の各EditTextを常時監視するクラス
     */
    private class TotalPriceWatcher implements TextWatcher {

        private EditText _editText;

        /**
         * コンストラクタ
         * 自身のEditTextを引数として渡し、フィールドメンバに格納
         */
        public TotalPriceWatcher(EditText editText) {
            this._editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        /**
         * 入力後の動作を記述
         * 「合計金額」のEditTextに、ShoppingItemEditorActivity#calculateTotalPrice()の結果を表示
         */
        @Override
        public void afterTextChanged(Editable s) {
            mEtItemTotalPrice.setText(calculateTotalPrice());
        }
    }

    /**
     * 画面上の各種ボタンのクリックリスナを定義
     */
    private class OnButtonClickListener implements View.OnClickListener {

        private ShoppingItemEditorActivity mActivity;

        /**
         * コンストラクタ
         * イベントハンドラとして利用するGeneralDialogFragmentの呼び出しのため、
         * GeneralDialogFragment.Callbackを実装したクラスの型として、本クラスそのものをメンバに格納
         */
        public OnButtonClickListener(ShoppingItemEditorActivity activity) {
            mActivity = activity;
        }

        /**
         * ボタンの位置により挙動が変化
         * 画面右端(新規or更新)と項目移動 … 画面部品上の記述内容に基づくIntentを新規生成の上、
         * setResult()によりその後の処理を呼び出し元へ移譲
         * 項目削除とリスト間移動 … 画面部品上の記述内容に基づくBundleを新規生成の上、
         * GeneralDialogFragmentにその後の処理を移譲
         *
         * @param v :各ボタンのスーパークラス
         */
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //アイテム情報更新・追加
                case R.id.btReply:
                    Intent data = addAndUpdateItem(0);
                    int itemId = 0;
                    DBHelper dbHelper = new DBHelper(getApplicationContext());
                    try {
                        itemId = dbHelper.updateItem(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (dbHelper != null) {
                            dbHelper.closeDB();
                        }
                    }
                    //DB書き込み時の戻り値が0 →新規追加。dataに戻り値のitemIdを格納
                    if (data.getIntExtra("itemId", 0) == 0) {
                        data.removeExtra("itemId");
                        data.putExtra("itemId", itemId);
                    }
                    setResult(RESULT_OK, data);
                    finish();
                    break;
//                case R.id.btMove:
//                    data = addAndUpdateItem(RESULT_CODE_MOVE);
//                    setResult(RESULT_CODE_MOVE, data);
//                    finish();
//                    break;
                case R.id.btDelete:
                    Bundle extras = getIntent().getExtras();
                    new GeneralDialogFragment.Builder(mActivity)
                            .title(R.string.attention)
                            .message(extras.getString("name") + getString(R.string.alert_delete))
                            .requestCode(RESULT_CODE_DELETE)
                            .positive(R.string.reply_delete)
                            .negative(R.string.cancel)
                            .show();
                    break;
//                case R.id.btCopyItem:
//                    new GeneralDialogFragment.Builder(mActivity)
//                            .title(R.string.attention)
//                            .message(getString(R.string.alert_copy))
//                            .requestCode(RESULT_CODE_COPY)
//                            .positive(R.string.reply_copy)
//                            .negative(R.string.cancel)
//                            .show();
            }
        }

        /**
         * 項目の新規作成/更新/移動時のsetResult()用Intentを生成
         *
         * @param copyCheck :リクエストコードの格納内容を判定させるコード
         * @return data :setResult()の引数に格納するためのIntentインスタンス
         */
        public Intent addAndUpdateItem(int copyCheck) {
            Intent intent = getIntent();
            Intent data = new Intent();
            data.putExtra("hasGot", mCbHasGot.isChecked());
            data.putExtra("order", intent.getIntExtra("order", 0));
            data.putExtra("name", mEtItemName.getText().toString());
            data.putExtra("amount", Integer.parseInt(mEtItemAmount.getText().toString()));
            data.putExtra("price", Integer.parseInt(mEtItemPrice.getText().toString()));
            data.putExtra("place", mEtPlace.getText().toString());
            data.putExtra("description", mEtComment.getText().toString());
            data.putExtra("createDate", intent.getLongExtra("createDate", System.currentTimeMillis()));
            data.putExtra("lastUpdateDate", System.currentTimeMillis());
            if (copyCheck == RESULT_CODE_COPY || copyCheck == RESULT_CODE_MOVE) {
                data.putExtra("requestCode", ItemRecyclerViewAdapter.REQUEST_CODE_CREATE);
                if (copyCheck == RESULT_CODE_COPY) {
                    data.putExtra("itemId", 0);
                    data.putExtra("listId", intent.getIntExtra("listId", 0));
                } else {
                    data.putExtra("itemId", intent.getIntExtra("itemId", 0));
                    data.putExtra("listId", 0);
                }
            } else {
                data.putExtra("requestCode", intent.getIntExtra("requestCode", ItemRecyclerViewAdapter.REQUEST_CODE_CREATE));
                data.putExtra("itemId", intent.getIntExtra("itemId", 0));
                data.putExtra("listId", intent.getIntExtra("listId", 0));
            }
            return data;
        }
    }
}
