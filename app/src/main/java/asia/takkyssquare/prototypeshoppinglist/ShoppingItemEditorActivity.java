package asia.takkyssquare.prototypeshoppinglist;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class ShoppingItemEditorActivity extends AppCompatActivity {
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

        Intent intent = getIntent();
        int requestCode = (int) intent.getIntExtra("requestCode", 100);
        String itemName = intent.getStringExtra("name");
        String comment = intent.getStringExtra("details");

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

        mEtItemName.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtItemName, R.string.item_name));
        mEtItemAmount.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtItemAmount, R.string.amount));
        mEtItemPrice.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtItemPrice, R.string.price));
        mEtItemTotalPrice.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtItemTotalPrice, R.string.total_price));
        mEtPlace.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtPlace, R.string.place));
        mEtComment.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtComment, R.string.comment));

        mCbHasGot = findViewById(R.id.cbHasGot);

        mBtDelete = findViewById(R.id.btDelete);
        mBtCopyItem = findViewById(R.id.btCopyItem);
        mBtReply = findViewById(R.id.btReply);
        if (requestCode == MyItemRecyclerViewAdapter.REQUEST_CODE_CREATE) {
            mBtReply.setText(R.string.reply_create);
        } else {
            mBtReply.setText(R.string.reply_update);
        }
    }


    private class OnEditTextFocusChangeListener implements View.OnFocusChangeListener {

        private EditText _editText;
        private int _resID;

        public OnEditTextFocusChangeListener(EditText editText, int resID) {
            _editText = editText;
            _resID = resID;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                _editText.setBackgroundColor(Color.LTGRAY);
            } else {
                _editText.setBackgroundColor(Color.WHITE);
            }
        }
    }
}
