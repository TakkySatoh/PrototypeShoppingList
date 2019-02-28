package asia.takkyssquare.prototypeshoppinglist;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class ShoppingItemEditorActivity extends AppCompatActivity {

    public static final int RESULT_CODE_OK = 0;
    public static final int RESULT_CODE_DELETE = 99;

    private EditText mEtItemName;
    private EditText mEtItemAmount;
    private EditText mEtItemPrice;
    private EditText mEtItemTotalPrice;
    private EditText mEtPlace;
    private EditText mEtComment;
    private CheckBox mCbHasGot;
    private Button mBtDelete;
    private Button mBtCopyItem;
    private Button mBtReply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_item_editor);

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
            requestCode = intent.getIntExtra("requestCode", 100);
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

        Toolbar toolbar = findViewById(R.id.tbEditor);
        if (intent != null) {
            toolbar.setTitle(R.string.title_update);
        } else {
            toolbar.setTitle(R.string.title_create);
        }
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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

        if (itemName != null) {
            mEtItemName.setText(itemName);
        }
        if (comment != null) {
            mEtComment.setText(comment);
        }
        if (place != null) {
            mEtPlace.setText(place);
        }
        mEtItemAmount.setText(String.format("%,d", amount));
        mEtItemPrice.setText(String.format("%,d", price));
        mEtItemTotalPrice.setText(String.format("%,d", price * amount));

        mEtItemPrice.addTextChangedListener(new TotalPriceWatcher(mEtItemPrice));
        mEtItemAmount.addTextChangedListener(new TotalPriceWatcher(mEtItemAmount));

//        mEtItemName.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtItemName, R.string.item_name));
//        mEtItemAmount.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtItemAmount, mEtItemPrice, mEtItemTotalPrice));
//        mEtItemPrice.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtItemPrice, mEtItemAmount, mEtItemTotalPrice));
//        mEtItemTotalPrice.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtItemTotalPrice, R.string.total_price));
//        mEtPlace.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtPlace, R.string.place));
//        mEtComment.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtComment, R.string.comment));

        mCbHasGot = findViewById(R.id.cbHeadlineHasGot);
        mCbHasGot.setChecked(hasGot);

        mBtDelete = findViewById(R.id.btDelete);
        mBtDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = getIntent();
                setResult(RESULT_CODE_DELETE,data);
                finish();
            }
        });

        mBtCopyItem = findViewById(R.id.btCopyItem);
        mBtReply = findViewById(R.id.btReply);
        if (requestCode == ItemRecyclerViewAdapter.REQUEST_CODE_CREATE) {
            mBtReply.setText(R.string.reply_create);
        } else {
            mBtReply.setText(R.string.reply_update);
        }
        final int finalRequestCode = requestCode;
        mBtReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("requestCode", finalRequestCode);
                data.putExtra("hasGot", mCbHasGot.isChecked());
                data.putExtra("name", mEtItemName.getText().toString());
                data.putExtra("amount", Integer.parseInt(mEtItemAmount.getText().toString()));
                data.putExtra("price", Integer.parseInt(mEtItemPrice.getText().toString()));
                data.putExtra("place", mEtPlace.getText().toString());
                data.putExtra("description", mEtComment.getText().toString());
                data.putExtra("createDate", createDate);
                data.putExtra("lastUpdateDate", System.currentTimeMillis());
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    private String calculateTotalPrice() {
        String priceStr = mEtItemPrice.getText().toString();
        if (priceStr == null || priceStr.equals("")) priceStr = "0";
        String amountStr = mEtItemAmount.getText().toString();
        if (amountStr == null || amountStr.equals("")) amountStr = "0";
        return String.format("%,d", Integer.parseInt(priceStr) * Integer.parseInt(amountStr));
    }

//    private class OnEditTextFocusChangeListener implements View.OnFocusChangeListener {
//
//        private EditText _source1;
//        private EditText _source2;
//        private EditText _target;
//
//        public OnEditTextFocusChangeListener(EditText source1, EditText source2, EditText target) {
//            _source1 = source1;
//            _source2 = source2;
//            _target = target;
//        }
//
//        @Override
//        public void onFocusChange(View v, boolean hasFocus) {
//            if (!hasFocus) {
//                _target.setText(String.format("%,d", Integer.parseInt(_source1.getText().toString()) * Integer.parseInt(_source2.getText().toString())));
//            }
//        }
//    }

    private class TotalPriceWatcher implements TextWatcher {

        private EditText _editText;

        public TotalPriceWatcher(EditText editText) {
            this._editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            mEtItemTotalPrice.setText(calculateTotalPrice());
        }
    }
}
