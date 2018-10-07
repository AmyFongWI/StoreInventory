package com.example.android.storeinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.storeinventory.data.InventoryContract.InventoryEntry;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivity.class.getName();


    private View mEmptyStateTextView;
    private ListView mInventoryListView;

    /** Identifier for the inventory data loader */
    private static final int INVENTORY_LOADER = 0;

    InventoryCursorAdapter mInventoryCursorAdapter;

    // Define a projection that specifies which columns from the database
    // you will actually use after this query.
    static final String[] projection = {
            InventoryEntry._ID,
            InventoryEntry.COLUMN_PRODUCT_NAME,
            InventoryEntry.COLUMN_PRICE,
            InventoryEntry.COLUMN_QUANTITY,
            InventoryEntry.COLUMN_SUPPLIER_NAME,
            InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

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
                Intent intent = new Intent(MainActivity.this, EditInventoryItemActivity.class);
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

        mInventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditInventoryItemActivity}
                Intent intent = new Intent(MainActivity.this, EditInventoryItemActivity.class);

                Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentInventoryUri);

                // Launch the {@link EditInventoryItemActivitu} to display the data for the current item.
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_generic_entry:
                insertGenericInventoryItem();
                return true;
            case R.id.action_delete_all_entries:
                DialogInterface.OnClickListener deleteAllButtonListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Delete" button, delete the item then navigate to parent activity.
                                deleteAllItems();
                            }
                        };
                showDeleteAllConfirmationDialog(deleteAllButtonListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void insertGenericInventoryItem() {
        ContentValues values = new ContentValues();

        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, "Wunderbar Foobar");
        values.put(InventoryEntry.COLUMN_PRICE, 31);
        values.put(InventoryEntry.COLUMN_QUANTITY, 2);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, "Acme Inc.");
        values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER, "408-555-1234");

        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, InventoryEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mInventoryCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mInventoryCursorAdapter.swapCursor(null);
    }

    /**
     * Helper method to delete all items in the database.
     */
    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.v(LOG_TAG, rowsDeleted + " rows deleted from inventory database");
    }

    /**
     * Confirmation dialog for deleting the item
     * @param deleteAllButtonConfirmListener
     */
    private void showDeleteAllConfirmationDialog(
            DialogInterface.OnClickListener deleteAllButtonConfirmListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_item_confirmation);
        builder.setPositiveButton(R.string.delete, deleteAllButtonConfirmListener);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
