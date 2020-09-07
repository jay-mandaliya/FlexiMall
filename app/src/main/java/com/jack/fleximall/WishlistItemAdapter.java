package com.jack.fleximall;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jack.fleximall.activity.DatabaseHelper;

public class WishlistItemAdapter extends CursorAdapter {

    private DatabaseHelper databaseHelper;
    private Activity activity;

    public WishlistItemAdapter(Context context, Cursor cursor, DatabaseHelper databaseHelper, Activity activity){
        super(context,cursor,0);
        this.databaseHelper = databaseHelper;
        this.activity = activity;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        return LayoutInflater.from(context).inflate(R.layout.item_list_layout,viewGroup,false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView txtName = (TextView) view.findViewById(R.id.item_list_layout_txtName);
        TextView txtPrice = (TextView) view.findViewById(R.id.item_list_layout_txtPrice);
        TextView txtRemove = (TextView) view.findViewById(R.id.item_list_layout_txtRemove);
        TextView txtWishlist = (TextView) view.findViewById(R.id.item_list_layout_txtLater);

        final String barcode = cursor.getString(cursor.getColumnIndex(context.getString(R.string.column_barcode)));
        String name = cursor.getString(cursor.getColumnIndex(context.getString(R.string.column_name)));
        long price = cursor.getLong(cursor.getColumnIndex(context.getString(R.string.column_price)));

        Spinner dropdown = view.findViewById(R.id.item_list_layout_spinner);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(context, R.array.spinner_items,
                R.layout.spinner_layout);
        dropdown.setAdapter(adapter);
        dropdown.setSelection(cursor.getInt(cursor.getColumnIndex(context.getString(R.string.column_quantity))) - 1);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                databaseHelper.updateWishlistQty(barcode, position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        txtName.setText(name);
        txtPrice.setText(String.valueOf(price));
        txtRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(databaseHelper.deleteFromWishlist(barcode)){
                    changeCursor(databaseHelper.getWishlistItems());
                }
                else{
                    Toast.makeText(context,"Something went wrong",Toast.LENGTH_SHORT).show();
                }
            }
        });
        txtWishlist.setText("Move to cart");
        txtWishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (databaseHelper.ifExists(barcode)){
                    Toast.makeText(context,"Item present in cart",Toast.LENGTH_SHORT).show();
                }
                else{
                    if(databaseHelper.moveToCart(barcode)){
                        changeCursor(databaseHelper.getWishlistItems());
                    }
                    else{
                        Toast.makeText(context,"Something went wrong",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

}
