package com.jack.fleximall;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jack.fleximall.activity.DatabaseHelper;

import java.util.ArrayList;

public class ItemAdapter extends CursorAdapter {

    private DatabaseHelper databaseHelper;
    private TotalCommunication totalCommunication;
    private Activity activity;

    public ItemAdapter(Context context, Cursor cursor, DatabaseHelper databaseHelper, TotalCommunication obj, Activity activity){
        super(context,cursor,0);
        this.databaseHelper = databaseHelper;
        this.totalCommunication = obj;
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
                databaseHelper.updateQty(barcode, position + 1);
                totalCommunication.notifyTotal();
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
                if(databaseHelper.delete(barcode)){
                    changeCursor(databaseHelper.getAllItems());
                    totalCommunication.notifyTotal();
                }
                else{
                    Toast.makeText(context,"Something went wrong",Toast.LENGTH_SHORT).show();
                }
            }
        });
        txtWishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (databaseHelper.ifExistsInWishlist(barcode)){
                    Toast.makeText(context,"Item already present in Wishlist",Toast.LENGTH_SHORT).show();
                }
                else{
                    if(databaseHelper.moveToWishlist(barcode)){
                        changeCursor(databaseHelper.getAllItems());
                        totalCommunication.notifyTotal();
                    }
                    else{
                        Toast.makeText(context,"Something went wrong",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}