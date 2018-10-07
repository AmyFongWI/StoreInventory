package com.example.android.storeinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.storeinventory.data.InventoryContract.InventoryEntry;

public class EditInventoryItemActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mProductNameEditText;

    private EditText mProductQuantityText;

    private EditText mProductPriceText;

    private EditText mSupplierNameText;

    private EditText mSupplierPhoneNumberText;

    private TextView mDisplayProductNameText;

    private TextView mDisplayProductQuantityText;

    private TextView mDisplayProductPriceText;

    private TextView mDisplaySupplierNameText;

    private TextView mDisplaySupplierPhoneNumberText;

    enum MenuMode
    {
        Add,
        Edit,
        Display
    }

    MenuMode mMenuMode;


    /** Content URI for the existing inventory item (null if it's a new item) */
    private Uri mCurrentInventoryItemUri;

    /** Identifier for the inventory item data loader */
    private static final int EXISTING_INVENTORY_ITEM_LOADER = 0;

    /** Boolean flag that keeps track of whether the inventory item has been edited (true) or not (false) */
    private boolean mItemHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mItemHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_inventory_item);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new item or editing an existing one.
        Intent intent = getIntent();
        mCurrentInventoryItemUri = intent.getData();

        // If the intent DOES NOT contain a item content URI, then we know that we are
        // creating a new tiem.
        if (mCurrentInventoryItemUri == null) {
            // set the Menu to Add item menu options
            mMenuMode = MenuMode.Add;
            invalidateOptionsMenu();

            // This is a new item, so change the app bar to say "Add an Inventory Item"
            setTitle(getString(R.string.editor_activity_title_new_item));

        } else {
            // set the Menu to Display item menu options
            mMenuMode = MenuMode.Display;
            invalidateOptionsMenu();
            // Otherwise this is an existing item, so change app bar to say "Inventory Item"
            setTitle(getString(R.string.editor_activity_title_display_item));
            //Change to Display Item mode
            displayItemMode();

            // Initialize a loader to read the item data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_INVENTORY_ITEM_LOADER, null, this);
        }

        mProductNameEditText = (EditText) findViewById(R.id.edit_productName);
        mProductPriceText = (EditText) findViewById(R.id.edit_productPrice);
        mProductQuantityText = (EditText) findViewById(R.id.edit_productQuantity);

        mSupplierNameText = (EditText) findViewById(R.id.edit_supplierName);
        mSupplierPhoneNumberText = (EditText) findViewById(R.id.edit_supplierPhoneNumber);

        mDisplayProductNameText = (TextView) findViewById(R.id.display_productName);
        mDisplayProductPriceText = (TextView) findViewById(R.id.display_productPrice);
        mDisplayProductQuantityText = (TextView) findViewById(R.id.display_productQuantity);

        mDisplaySupplierNameText = (TextView) findViewById(R.id.display_supplierName);
        mDisplaySupplierPhoneNumberText = (TextView) findViewById(R.id.display_supplierPhone);


        mProductNameEditText.setOnTouchListener(mTouchListener);
        mProductPriceText.setOnTouchListener(mTouchListener);
        mProductQuantityText.setOnTouchListener(mTouchListener);

        mSupplierNameText.setOnTouchListener(mTouchListener);
        mSupplierPhoneNumberText.setOnTouchListener(mTouchListener);


        // Set the onClickLister to open the dialer on the supplier phone number
        ImageButton callButton = (ImageButton) findViewById(R.id.callButton);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = mDisplaySupplierPhoneNumberText.getText().toString().trim();

                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
            }
        });
    }

    private void saveInventoryItem() {
        //Read from input fields
        String productNameString;
        String priceString;
        String quantityString;

        String supplierNameString;
        String supplierPhoneNumberString;

        if(mMenuMode == MenuMode.Display) {
             productNameString = mDisplayProductNameText.getText().toString().trim();
             priceString = mDisplayProductPriceText.getText().toString().trim();
             quantityString = mDisplayProductQuantityText.getText().toString().trim();

             supplierNameString = mDisplaySupplierNameText.getText().toString().trim();
             supplierPhoneNumberString = mDisplaySupplierPhoneNumberText.getText().toString().trim();
        } else {
             productNameString = mProductNameEditText.getText().toString().trim();
             priceString = mProductPriceText.getText().toString().trim();
             quantityString = mProductQuantityText.getText().toString().trim();

             supplierNameString = mSupplierNameText.getText().toString().trim();
             supplierPhoneNumberString = mSupplierPhoneNumberText.getText().toString().trim();
        }

        // Check if this is supposed to be a new pet
        // and check if all the fields in the editor are blank
        if (mCurrentInventoryItemUri == null &&
                TextUtils.isEmpty(productNameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierNameString) && TextUtils.isEmpty(supplierPhoneNumberString)) {
            // Since no fields were modified, we can return early without creating a new item.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();

        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);

        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(InventoryEntry.COLUMN_PRICE, price);

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(InventoryEntry.COLUMN_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneNumberString);

        // Determine if this is a new or existing pet by checking if mCurrentInventoryItemUri is null or not
        if (mCurrentInventoryItemUri == null) {
            // This is a NEW inventory item, so insert a new item into the provider,
            // returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_fail),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_success),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING item, so update the item with content URI: mCurrentInventoryItemUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentInventoryItemUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentInventoryItemUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_item_fail),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_success),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_edit_inventory_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ((item.getItemId())) {
            case R.id.action_save:
                saveInventoryItem();
                finish();
                return true;
            case R.id.action_delete:
                deleteInventoryItem();
                finish();
                return true;
            case R.id.action_edit:
                // go to edit mode so change the menu
                mMenuMode = MenuMode.Edit;
                invalidateOptionsMenu();
                // and change field to edit mode
                editItemMode();
                return true;
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditInventoryItemActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditInventoryItemActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return  super.onOptionsItemSelected(item);
    }

    /**
     * Changes what is button is display based on value in mMenuMode
     * Must call invalidateOptionsMenu to trigger
     * @param menu
     * @return true
     */
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem saveButton = menu.findItem(R.id.action_save);
        MenuItem editButton = menu.findItem(R.id.action_edit);
        MenuItem deleteButton = menu.findItem(R.id.action_delete);

        switch(mMenuMode){
            case Display:
                saveButton.setVisible(false);
                editButton.setVisible(true);
                deleteButton.setVisible(true);
                return true;
            case Add:
                saveButton.setVisible(true);
                editButton.setVisible(false);
                deleteButton.setVisible(false);
                return true;
            case Edit:
                saveButton.setVisible(true);
                editButton.setVisible(false);
                deleteButton.setVisible(true);
                return true;
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentInventoryItemUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            // Update the views on the screen with the values from the database
            mProductNameEditText.setText(name);
            mProductPriceText.setText(Integer.toString(price));
            mProductQuantityText.setText(Integer.toString(quantity));
            mSupplierNameText.setText(supplierName);
            mSupplierPhoneNumberText.setText(supplierPhone);

            // Update the views on the screen with the values from the database
            mDisplayProductNameText.setText(name);
            mDisplayProductPriceText.setText(Integer.toString(price));
            mDisplayProductQuantityText.setText(Integer.toString(quantity));
            mDisplaySupplierNameText.setText(supplierName);
            mDisplaySupplierPhoneNumberText.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductNameEditText.setText("");
        mProductPriceText.setText("");
        mProductQuantityText.setText("");
        mSupplierNameText.setText("");
        mSupplierPhoneNumberText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void displayItemMode() {
        setTitle(getString(R.string.editor_activity_title_display_item));

        LinearLayout productLinearView = findViewById(R.id.edit_productFields);
        productLinearView.setVisibility(View.GONE);
        LinearLayout supplierLinearView = findViewById(R.id.edit_supplierFields);
        supplierLinearView.setVisibility(View.GONE);

        productLinearView = findViewById(R.id.display_productFields);
        productLinearView.setVisibility(View.VISIBLE);
        supplierLinearView = findViewById(R.id.display_supplierFields);
        supplierLinearView.setVisibility(View.VISIBLE);
    }

    private void editItemMode() {
        setTitle(getString(R.string.editor_activity_title_edit_item));

        LinearLayout productLinearView = findViewById(R.id.edit_productFields);
        productLinearView.setVisibility(View.VISIBLE);
        LinearLayout supplierLinearView = findViewById(R.id.edit_supplierFields);
        supplierLinearView.setVisibility(View.VISIBLE);

        productLinearView = findViewById(R.id.display_productFields);
        productLinearView.setVisibility(View.GONE);
        supplierLinearView = findViewById(R.id.display_supplierFields);
        supplierLinearView.setVisibility(View.GONE);
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteInventoryItem() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentInventoryItemUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentInventoryItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_success),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    /**
     *  Decrease Quantity by 1 in Display mode
     * @param view
     */
    public void decreaseQuantity (View view) {
        String quantityString = mDisplayProductQuantityText.getText().toString().trim();
        int quantityNumber = Integer.parseInt(quantityString);

        if(quantityNumber <= 0) {
            Toast.makeText(this, getString(R.string.editor_decrease_quantity_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            quantityNumber--;
            mDisplayProductQuantityText.setText(Integer.toString(quantityNumber));
            saveInventoryItem();
        }

    }

    /**
     * Increase Quantity by 1 in Display mode
     * @param view
     */
    public void increaseQuantity (View view) {
        String quantityString = mDisplayProductQuantityText.getText().toString().trim();
        int quantityNumber = Integer.parseInt(quantityString);
        quantityNumber++;
        mDisplayProductQuantityText.setText(Integer.toString(quantityNumber));

        saveInventoryItem();
    }


}
