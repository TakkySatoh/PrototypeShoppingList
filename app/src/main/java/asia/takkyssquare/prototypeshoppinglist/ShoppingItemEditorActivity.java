package asia.takkyssquare.prototypeshoppinglist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_item_editor);

        Intent intent = getIntent();
        String itemName = intent.getStringExtra("name");
        String comment = intent.getStringExtra("details");

        Toolbar toolbar = findViewById(R.id.toolbar);
        if(intent != null){
            toolbar.setTitle(R.string.title_update);
        } else {
            toolbar.setTitle(R.string.title_create);
        }
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mEtItemName = findViewById(R.id.etItemName);
        mEtItemName.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtItemName,R.string.item_name));
        if (itemName != null){
            mEtItemName.setText(itemName);
        }
        mEtItemAmount = findViewById(R.id.etItemAmount);
        mEtItemAmount.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtItemAmount,R.string.amount));
        mEtItemPrice = findViewById(R.id.etItemPrice);
        mEtItemPrice.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtItemPrice,R.string.price));
        mEtItemTotalPrice = findViewById(R.id.etItemTotalPrice);
        mEtItemTotalPrice.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtItemTotalPrice,R.string.total_price));
        mEtPlace = findViewById(R.id.etPlace);
        mEtPlace.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtPlace,R.string.place));
        mEtComment = findViewById(R.id.etComment);
        mEtComment.setOnFocusChangeListener(new OnEditTextFocusChangeListener(mEtComment,R.string.comment));
        if (comment != null){
            mEtComment.setText(comment);
        }
        mCbHasGot = findViewById(R.id.cbHasGot);
    }

    private class OnEditTextFocusChangeListener implements View.OnFocusChangeListener {

        private EditText _editText;
        private int _resID;

        public OnEditTextFocusChangeListener(EditText editText,int resID) {
            _editText = editText;
            _resID = resID;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus){
                if (_editText.getText().toString().equals(getString(R.string.message,getString(_resID)))){
                    _editText.clearComposingText();
                    _editText.setAlpha(1.0f);
                }
            } else {
                if (_editText.getText().toString() == null){
                    _editText.setText(getString(R.string.message,getString(_resID)));
                    _editText.setAlpha(0.5f);
                }
            }
        }
    }
}
