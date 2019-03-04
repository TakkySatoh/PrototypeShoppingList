package asia.takkyssquare.prototypeshoppinglist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import asia.takkyssquare.prototypeshoppinglist.ShoppingItemContent.ShoppingItem;
import asia.takkyssquare.prototypeshoppinglist.dummy.DummyContent;

public class MainActivity extends AppCompatActivity implements ShoppingListFragment.OnListFragmentInteractionListener, AdapterView.OnItemSelectedListener {

    public static final int CREATE_NEW_LIST = 100;
    public static final int DELETE_LIST = 900;

    private Toolbar mToolbar;
    private Spinner mSpinner;

    private ArrayAdapter<String> mSpAdapter;
    private List<String> mListNameList = new ArrayList<>();

//    private String[] mListNameArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        for (String listName : getResources().getStringArray(R.array.shopping_list)) {
            mListNameList.add(listName);
        }
//        mListNameList.add(getString(R.string.create_list));
//        mListNameArray = mListNameList.toArray(new String[mListNameList.size()]);

        if (savedInstanceState == null) {
            replaceFragment(0);
        }

        mSpinner = mToolbar.findViewById(R.id.spListName);
        mSpAdapter = new ArrayAdapter<>(this, android.R.layout.simple_selectable_list_item, mListNameList);
        mSpAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSpAdapter);

        mSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
        replaceFragment(position);
    }

    private void replaceFragment(int position) {
        Fragment fragment = new ShoppingListFragment();
        Bundle bundle = new Bundle();
        String listName = mListNameList.get(position);
        bundle.putInt("position", position);
        bundle.putString("listName", listName);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.opNewList:
                final EditText etNewListName = new EditText(MainActivity.this);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.alert_create_list)
                        .setView(etNewListName)
                        .setPositiveButton(R.string.reply_create_list, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListNameList.add(etNewListName.getText().toString());
                                mSpAdapter.notifyDataSetChanged();
                                replaceFragment(mListNameList.size() - 1);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                break;
            case R.id.opEditList:
                break;
            case R.id.opDeleteList:
                break;
            case R.id.opShareList:
                break;
        }
        return true;
    }

    @Override
    public void onListFragmentInteraction(ShoppingItem item, int requestCode) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public static class CustomDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setView(R.layout.dialog_create_new_list)
                    .create();
            return dialog;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {

        }
    }
}
