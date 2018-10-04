package com.example.android.storeinventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.storeinventory.data.InventoryContract.InventoryEntry;
import com.example.android.storeinventory.data.InventoryDbHelper;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private View mEmptyStateTextView;
    private ListView mInventoryListView;

    /** Identifier for the inventory data loader */
    private static final int INTEVENTORY_LOADER = 0;

    InventoryCursorAdapter mInventoryCursorAdapter;

    // Define a projection that specifies which columns from the database
    // you will actually use after this query.
    static final String[] projection = {
            InventoryEntry._ID,
            InventoryEntry.COLUMN_PRODUCT_NAME,
            InventoryEntry.COLUMN_QUANTITY,
            InventoryEntry.COLUMN_SUPPLIER_NAME,
            InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

    private InventoryDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddInventoryItemActivity.class);
                startActivity(intent);
            }
        });

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mInventoryListView = (ListView) findViewById(R.id.list);

        mEmptyStateTextView = findViewById(R.id.empty_view);
        mInventoryListView.setEmptyView(mEmptyStateTextView);

        mInventoryCursorAdapter = new InventoryCursorAdapter(this, null);
        // Attach the adapter to the ListView.
        mInventoryListView.setAdapter(mInventoryCursorAdapter);

        getLoaderManager().initLoader(INTEVENTORY_LOADER, null, this);

        mDbHelper = new InventoryDbHelper(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // update the database count whenever the MainActivity starts, not just onCreate
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_insert_generic_entry) {
            insertGenericInventoryItem();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void insertGenericInventoryItem() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, "Wunderbar Foobar");
        values.put(InventoryEntry.COLUMN_PRICE, 31);
        values.put(InventoryEntry.COLUMN_QUANTITY, 2);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, "Acme Inc.");
        values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER, "408-555-1234");

        long newRowId = db.insert(InventoryEntry.TABLE_NAME, null, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newRowId == -1) {
            // If the row ID is -1, then there was an error with insertion.
            Toast.makeText(this, "Error with saving inventory item", Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(this, "Inventory item saved with row id: " + newRowId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, InventoryEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mInventoryCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mInventoryCursorAdapter.swapCursor(null);
    }
}
