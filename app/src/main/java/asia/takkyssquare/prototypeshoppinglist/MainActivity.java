package asia.takkyssquare.prototypeshoppinglist;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import asia.takkyssquare.prototypeshoppinglist.dummy.DummyContent;

public class MainActivity extends AppCompatActivity implements ShoppingListFragment.OnListFragmentInteractionListener {

    public static final int REQUEST_CODE = 100;
    public static final int RESULT_OK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            replaceFragment(0);
        }

        Spinner spinner = toolbar.findViewById(R.id.spListName);
        ArrayAdapter<CharSequence> spAdapter = ArrayAdapter.createFromResource(this, R.array.shopping_list, android.R.layout.simple_list_item_1);
        spAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(spAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                replaceFragment(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void replaceFragment(int position) {
        Fragment fragment = new ShoppingListFragment();
        Bundle bundle = new Bundle();
        String listName = getResources().getStringArray(R.array.shopping_list)[position];
        bundle.putInt("position", position);
        bundle.putString("listName", listName);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item, int requestCode) {
        Intent intent = new Intent(this, ShoppingItemEditorActivity.class);
        intent.putExtra("requestCode",requestCode);
        intent.putExtra("id", item.id);
        intent.putExtra("name", item.content);
        intent.putExtra("details", item.details);
        startActivityForResult(intent, requestCode);
    }
}
