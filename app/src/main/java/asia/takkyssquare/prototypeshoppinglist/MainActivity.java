package asia.takkyssquare.prototypeshoppinglist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import java.util.List;

import asia.takkyssquare.prototypeshoppinglist.ShoppingItemContent.ShoppingItem;

public class MainActivity extends AppCompatActivity implements ShoppingListFragment.OnListFragmentInteractionListener, AdapterView.OnItemSelectedListener, GeneralDialogFragment.Callback {

    public static final int CREATE_NEW_LIST = 100;
    public static final int DELETE_LIST = 900;

    public static List<String> mListNameList = new ArrayList<>();

    private Toolbar mToolbar;
    private Spinner mSpinner;

    private ArrayAdapter<String> mSpAdapter;

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
            replaceFragment(mListNameList.get(0));
        }

        mSpinner = mToolbar.findViewById(R.id.spListName);
        mSpAdapter = new ArrayAdapter<>(this, android.R.layout.simple_selectable_list_item, mListNameList);
        mSpAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSpAdapter);

        mSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
        String currentViewingListName;
        if (position == 0) {
            currentViewingListName = mListNameList.get(0);
        } else {
            currentViewingListName = mListNameList.remove(position);
            mListNameList.add(0, currentViewingListName);
        }
        replaceFragment(currentViewingListName);
        mSpAdapter.notifyDataSetChanged();
        mSpinner.setOnItemSelectedListener(null);
        mSpinner.setSelection(0, false);
        mSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void replaceFragment(String listName) {
        Fragment fragment = new ShoppingListFragment();
        Bundle bundle = new Bundle();
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
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.opNewList:
                configureList(itemId);
                break;
            case R.id.opEditList:
                configureList(itemId);
                break;
            case R.id.opDeleteList:
                String listName = mListNameList.get(0);
                Bundle params = new Bundle();
                params.putString("listName", listName);
                new GeneralDialogFragment.Builder(this)
                        .title(R.string.attention)
                        .message("リスト「" + listName + "」" + getString(R.string.alert_delete))
                        .requestCode(DELETE_LIST)
                        .params(params)
                        .positive(R.string.reply_delete)
                        .negative(R.string.cancel)
                        .show();
                break;
            case R.id.opShareList:
                Toast.makeText(getApplicationContext(),"ご案内: 当機能は未実装につき、ご利用いただけません",Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }

    public void configureList(final int itemId) {
        final EditText etNewListName = new EditText(MainActivity.this);
        String title;
        String positive;
        switch (itemId) {
            case R.id.opNewList:
                title = getString(R.string.alert_create_list);
                positive = getString(R.string.reply_create_list);
                break;
            case R.id.opEditList:
                title = getString(R.string.alert_rename_list, mListNameList.get(0));
                positive = getString(R.string.reply_change);
                break;
            default:
                title = null;
                positive = null;
        }
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setView(etNewListName)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newListName = etNewListName.getText().toString().trim();
                        String message;
                        newListName = newListName.replaceAll("　", " ");
                        if (newListName == null || newListName.isEmpty()) {
                            Toast.makeText(getApplicationContext(), R.string.toast_error_empty, Toast.LENGTH_LONG).show();
                        } else {
                            if (itemId == R.id.opNewList) {
                                mListNameList.add(0, newListName);
                                mSpAdapter.notifyDataSetChanged();
                                mSpinner.setSelection(0);
                                replaceFragment(newListName);
                                message = getString(R.string.toast_finish_create_list, newListName);
                            } else {
                                String oldListName = mListNameList.get(0);
                                mListNameList.set(0, newListName);
                                mSpAdapter.notifyDataSetChanged();
                                message = getString(R.string.toast_finish_rename_list, oldListName, newListName);
                            }
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),getString(R.string.toast_cancel),Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    @Override
    public void onMyDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        if (requestCode == DELETE_LIST && resultCode == DialogInterface.BUTTON_POSITIVE) {
            String listName = params.getString("listName");
            mListNameList.remove(0);
            mSpAdapter.notifyDataSetChanged();
            mSpinner.setSelection(0);
            replaceFragment(mListNameList.get(0));
            Toast.makeText(getApplicationContext(), getString(R.string.toast_finish_delete_list, listName), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_cancel), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMyDialogCancelled(int requestCode, Bundle params) {
        if (requestCode == DELETE_LIST){
            Toast.makeText(getApplicationContext(),getString(R.string.toast_cancel),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onListFragmentInteraction(ShoppingItem item, int requestCode) {
        new GeneralDialogFragment.Builder(this)
                .title(R.string.alert_move_to)
                .items(MainActivity.mListNameList.toArray(new String[MainActivity.mListNameList.size()]))
                .requestCode(requestCode)
                .positive(R.string.reply_move)
                .negative(R.string.cancel)
                .show();
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
