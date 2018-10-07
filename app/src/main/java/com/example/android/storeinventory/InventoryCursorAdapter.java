package com.example.android.storeinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.storeinventory.data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = InventoryCursorAdapter.class.getName();


    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find fields to populate
        TextView tvName = (TextView) view.findViewById(R.id.name);
        TextView tvPrice = (TextView) view.findViewById(R.id.price);
        TextView tvQuantity = (TextView) view.findViewById(R.id.quantity);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_NAME));
        int price = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRICE));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_QUANTITY));


        Button saleButton = (Button) view.findViewById(R.id.sale_button);

         final int position = cursor.getPosition();

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action on click
                cursor.moveToPosition(position);

                int quantityInteger = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_QUANTITY));

                Log.i(LOG_TAG, "Inside on Click " + cursor.getPosition() + " with quantity: " + quantityInteger );

                if(quantityInteger > 0) {
                    quantityInteger--;

                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_PRODUCT_NAME, cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_NAME)));
                    values.put(InventoryEntry.COLUMN_PRICE, cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRICE)));
                    values.put(InventoryEntry.COLUMN_QUANTITY, quantityInteger);

                    int itemIdColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
                    int itemId = cursor.getInt(itemIdColumnIndex);
                    Uri mCurrentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, itemId);

                    context.getContentResolver().update(mCurrentItemUri, values, null, null);

                    // TODO: fix the update weirdness


                } else {
                    Toast.makeText(context, "Can not reduce quantity to below 0.", Toast.LENGTH_SHORT).show();
                }



            }
        });

        tvName.setText(name);
        tvPrice.setText(Integer.toString(price));
        tvQuantity.setText(Integer.toString(quantity));

    }

}
