package com.example.android.storeinventory;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.storeinventory.data.InventoryDbHelper;
import com.example.android.storeinventory.data.InventoryContract.InventoryEntry;

public class AddInventoryItemActivity extends AppCompatActivity {

    private EditText mProductNameEditText;

    private EditText mProductQuantiyText;

    private EditText mProductPriceText;

    private EditText mSupplierNameText;

    private EditText mSupplierPhoneNumberText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_inventory_item);

        mProductNameEditText = (EditText) findViewById(R.id.edit_productName);
        mProductPriceText = (EditText) findViewById(R.id.edit_productPrice);
        mProductQuantiyText = (EditText) findViewById(R.id.edit_productQuantity);

        mSupplierNameText = (EditText) findViewById(R.id.edit_supplierName);
        mSupplierPhoneNumberText = (EditText) findViewById(R.id.edit_supplierPhoneNumber);
    }

    private void insertInventoryItem() {
        //Read from input fields
        String productNameString = mProductNameEditText.getText().toString().trim();
        String priceString = mProductPriceText.getText().toString().trim();
        int price = Integer.parseInt(priceString);
        String quantityString = mProductQuantiyText.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);

        String supplierNameString = mSupplierNameText.getText().toString().trim();
        String supplierPhoneNumberString = mSupplierPhoneNumberText.getText().toString().trim();

        InventoryDbHelper mDbHelper = new InventoryDbHelper(this);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(InventoryEntry.COLUMN_PRICE, price);
        values.put(InventoryEntry.COLUMN_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneNumberString);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_add_inventory_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ((item.getItemId())) {
            case R.id.action_save:
                insertInventoryItem();
                finish();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return  super.onOptionsItemSelected(item);
    }

}
