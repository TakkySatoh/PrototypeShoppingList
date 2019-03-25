package asia.takkyssquare.prototypeshoppinglist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
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
    public static final int FINISH = 999;

    public static final String TAG = "DBHelper";

    public static List<String> mListNameList = new ArrayList<>();

    private Toolbar mToolbar;
    private Spinner mSpinner;

    private ArrayAdapter<String> mSpAdapter;

    private int listAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        /**
         * 既存の買物リストの名称一覧を取得
         */
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        try {
            if (dbHelper.getCount(DBOpenHelper.LIST_DELETED) != 0) {
                dbHelper.deleteDB(DBOpenHelper.LIST_DELETED);
            }
            if (dbHelper.getCount(DBOpenHelper.ITEM_DELETED) != 0){
                dbHelper.deleteDB(DBOpenHelper.ITEM_DELETED);
            }
            mListNameList = dbHelper.readListIndex(DBOpenHelper.LIST_ACTIVE);
            listAmount = dbHelper.getCount(DBOpenHelper.LIST_INDEX);
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Error: DBHelper could not get list index or list amount." + e.toString());
        } finally {
            dbHelper.closeDB();
        }

        /**
         * 買い物リスト名称一覧をドロップダウンメニューに格納
         * 詳細な挙動は別メソッドに記述
         */
        mSpinner = mToolbar.findViewById(R.id.spListName);
        mSpAdapter = new ArrayAdapter<>(this, android.R.layout.simple_selectable_list_item, mListNameList);
        mSpAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSpAdapter);
        mSpinner.setOnItemSelectedListener(this);
    }

    /**
     * 買い物リスト名称一覧のドロップメニューの挙動を定義
     * 任意のリスト名称を選択 → 該当のリスト名をドロップダウンメニューの先頭に移動
     * その後、選択したリストを元にFragmentを生成し、画面に表示
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
        if (position != 0) {
            String currentViewingListName = mListNameList.remove(position);
            mListNameList.add(0, currentViewingListName);
        }
        replaceFragment(mListNameList.get(0));
        mSpAdapter.notifyDataSetChanged();
        mSpinner.setOnItemSelectedListener(null);
        mSpinner.setSelection(0, false);
        mSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * 買い物リストの内容を表示するFragmentを生成
     *
     * @param listName :買い物リストのID。Fragmentインスタンスのタグとして利用
     */
    private void replaceFragment(String listName) {
        Fragment fragment = null;
        int listId = 0;
        String tag = null;
        if (listName.equals(getString(R.string.spinner_empty))) {
            fragment = new EmptyFragment();
            tag = "empty";
        } else {
            DBHelper dbHelper = new DBHelper(getApplicationContext());
            try {
                listId = dbHelper.getListId(listName);
                //ここにlistId未取得時のエラー処理を書く
                tag = Integer.toString(listId);
                fragment = new ShoppingListFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("listId", listId);
                bundle.putString("listName", listName);
                fragment.setArguments(bundle);
            } catch (Exception e) {
                e.printStackTrace();
                Log.w(TAG, "Error: DBHelper could not get list_id. " + e.toString());
            } finally {
                dbHelper.closeDB();
            }
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    /**
     * オプションメニューのレイアウトを生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * オプションメニュー選択時の挙動を定義。(対象: 名称一覧の先頭のリスト)
     * 内容:
     * ・opNewList … 買い物リストの新規作成
     * ・opEditList … 買い物リストの名称変更
     * ・opDeleteList … 買い物リストの削除
     * ・opShareList … 買い物リストを他のユーザと共有
     */
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
                Toast.makeText(getApplicationContext(), "ご案内: 当機能は未実装につき、ご利用いただけません", Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }

    /**
     * 買い物リストの新規生成/名称変更時の名称入力と実行の流れを定義
     *
     * @param itemId :オプションメニューの位置 (新規生成/名称変更の別を判定)
     */
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
                        String message = null;
                        newListName = newListName.replaceAll("　", " ");
                        boolean alreadyExists = false;
                        for (String listName : mListNameList) {
                            if (listName.equals(newListName)) {
                                alreadyExists = true;
                                break;
                            }
                        }
                        if (newListName == null || newListName.isEmpty()) {
                            Toast.makeText(getApplicationContext(), R.string.toast_error_empty, Toast.LENGTH_LONG).show();
                            return;
                        } else if (alreadyExists) {
                            Toast.makeText(getApplicationContext(), R.string.toast_error_duplicate, Toast.LENGTH_LONG).show();
                            return;
                        }
                        DBHelper dbHelper = new DBHelper(getApplicationContext());
                        int updatedRecordCount = 0;
                        try {
                            if (itemId == R.id.opNewList) {
                                dbHelper.updateListIndex(null, newListName);
                                mListNameList.add(0, newListName);
                                if (mListNameList.get(1).equals(getString(R.string.spinner_empty))) {
                                    mListNameList.remove(1);
                                }
                                mSpAdapter.notifyDataSetChanged();
                                mSpinner.setSelection(0);
                                replaceFragment(newListName);
                                message = getString(R.string.toast_finish_create_list, newListName);
                            } else {
                                String oldListName = mListNameList.remove(0);
                                dbHelper.updateListIndex(oldListName, newListName);
                                mListNameList.add(0, newListName);
                                mSpAdapter.notifyDataSetChanged();
                                message = getString(R.string.toast_finish_rename_list, oldListName, newListName);
                            }
                            listAmount = dbHelper.getCount(DBOpenHelper.LIST_INDEX);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.w(TAG, "Error: DBHelper could not finish editting table. " + e.toString());
                        } finally {
                            dbHelper.closeDB();
                        }
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), getString(R.string.toast_cancel), Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    /**
     * 買い物リスト削除時の挙動を定義
     * 削除時のダイアログ(GeneralDialogFragmentインスタンス)より戻ってきた値を利用
     *
     * @param requestCode :リクエストコード (本クラスの定数を利用)
     * @param resultCode  :リザルトコード (押下されたボタン種別判定のため、DialogInterfaceの定数を利用)
     * @param params      :各種データ格納用のBundle。今回は削除対象のリスト名称を格納
     */
    @Override
    public void onMyDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        if (requestCode == DELETE_LIST && resultCode == DialogInterface.BUTTON_POSITIVE) {
            String listName = params.getString("listName");
            mListNameList.remove(0);
            if (mListNameList.size() == 0) {
                mListNameList.add(getString(R.string.spinner_empty));
            }
            mSpAdapter.notifyDataSetChanged();
            mSpinner.setSelection(0);
            replaceFragment(mListNameList.get(0));
            DBHelper dbHelper = new DBHelper(getApplicationContext());
            try {
                dbHelper.moveToDeletedTable(listName, DBOpenHelper.LIST_ACTIVE);
            } catch (Exception e) {
                e.printStackTrace();
                Log.w(TAG, "Error: DBHelper could not move the list to deleted table." + e.toString());
            } finally {
                dbHelper.closeDB();
            }
            Toast.makeText(getApplicationContext(), getString(R.string.toast_finish_delete_list, listName), Toast.LENGTH_LONG).show();
        } else if (requestCode == FINISH && resultCode == DialogInterface.BUTTON_POSITIVE) {
            onDestroy();
            finish();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_cancel), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 買い物リスト削除の取り止め時の挙動を定義
     * 削除時のダイアログ(GeneralDialogFragmentインスタンス)より戻ってきた値を利用
     *
     * @param requestCode :リクエストコード (本クラスの定数を利用)
     * @param params      :各種データ格納用のBundle。今回は削除対象のリスト名称を格納
     */
    @Override
    public void onMyDialogCancelled(int requestCode, Bundle params) {
        if (requestCode == DELETE_LIST) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_cancel), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 買い物リストの項目を別のリストへ移動する際のダイアログ表示を記述
     * (ShoppingListFragmentのリスナに対するイベントハンドラ)
     *
     * @param item        :移動対象の項目を記述したJava Beans
     * @param requestCode :リクエストコード(呼び出し元の値をそのまま格納)
     */
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

    /**
     * 戻るキー押下時の動作 … アプリ終了の意思確認
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new GeneralDialogFragment.Builder(this)
                    .title(R.string.attention)
                    .message(R.string.alert_finish)
                    .requestCode(FINISH)
                    .positive(R.string.reply_finish)
                    .negative(R.string.cancel)
                    .show();
            return true;
        } else {
            return false;
        }
    }

    /**
     * アプリ終了時: 削除済リストテーブルにあるリストを全件削除
     */
    @Override
    protected void onDestroy() {
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        try {
            int deletedTableAmount = dbHelper.deleteDB(DBOpenHelper.LIST_DELETED);
            Log.d(TAG, "Completed: DBHelper cleaned " + DBOpenHelper.LIST_DELETED + " table with " + deletedTableAmount + " lists!");
            deletedTableAmount = dbHelper.deleteDB(DBOpenHelper.ITEM_DELETED);
            Log.d(TAG,"Completed: DBHelper cleaned " + DBOpenHelper.ITEM_DELETED + " table with " + deletedTableAmount + " items!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Error: DBHelper could not delete elements of deleted table." + e.toString());
        } finally {
            dbHelper.closeDB();
        }
        super.onDestroy();
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
