package com.jack.fleximall.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.jack.fleximall.Global;
import com.jack.fleximall.ItemAdapter;
import com.jack.fleximall.Product;
import com.jack.fleximall.R;
import com.jack.fleximall.TotalCommunication;
import com.jack.fleximall.activity.ScanActivity;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class MainFragment extends Fragment implements View.OnClickListener , TotalCommunication {

    private Global global;
    private ArrayList<Product> products;
    private ItemAdapter adapter;
    private ListView listView;
    private AlertDialog alertDialog;
    private ProgressDialog dialog;

    private TextView txtQty,txtPrice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        products = new ArrayList<Product>();
        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Finding your item");
        dialog.setCancelable(false);

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        global = (Global) getActivity().getApplication();

        Cursor cursor = global.getDatabaseHelper().getWishlistItems();

        txtQty = view.findViewById(R.id.main_frag_txtQtyValue);
        txtPrice = view.findViewById(R.id.main_frag_txtPriceValue);

        listView = view.findViewById(R.id.main_frag_list);
        adapter = new ItemAdapter(getContext(),global.getDatabaseHelper().getAllItems(),
                global.getDatabaseHelper(),this, getActivity());
        listView.setAdapter(adapter);

        setTotal();
        FloatingActionButton add = (FloatingActionButton) view.findViewById(R.id.main_frag_add);
        add.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.main_frag_add:
                onAdd();
                break;
        }
    }

    private void onAdd(){
        startActivityForResult(new Intent(getContext(),ScanActivity.class),global.getScanRequestCode());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == global.getScanRequestCode()){
            if (resultCode == RESULT_OK){
                String barcode = data.getDataString();
                addItem(barcode);
            }
        }
    }

    private void addItem(final String barcode){
        dialog.show();
        global.getProductRef().child(barcode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                dialog.dismiss();
                if (dataSnapshot.exists()) {

                    String name = dataSnapshot.child(getString(R.string.column_name)).getValue().toString();
                    long price = (long)dataSnapshot.child(getString(R.string.column_price)).getValue();

                    if (global.getDatabaseHelper().ifExists(barcode))
                        Toast.makeText(getContext(),"Item already present in your cart",Toast.LENGTH_LONG).show();
                    else {
                        if (!global.getDatabaseHelper().insertItem(barcode, name,price)) {
                            Toast.makeText(getContext(), "database insert error", Toast.LENGTH_LONG).show();
                        } else{
                            adapter.changeCursor(global.getDatabaseHelper().getAllItems());
                            setTotal();
                        }
                    }
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("We could not find your item in our warehouse").setTitle("Item not found");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog = builder.create();
                    alertDialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dialog.dismiss();
                Toast.makeText(getContext(),databaseError.toString()+" "+barcode,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setTotal(){
        Cursor cursor = global.getDatabaseHelper().getAllItems();
        int price = 0,qty = 0;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int temp = Integer.parseInt(cursor.getString(cursor.getColumnIndex(getString(R.string.column_quantity))));
                    qty += temp;
                    price += temp * Integer.parseInt(cursor.getString(cursor.getColumnIndex(getString(R.string.column_price))));
                } while (cursor.moveToNext());
            }
        }

        txtQty.setText(String.valueOf(qty));
        txtPrice.setText(String.valueOf(price).concat(" ".concat(getString(R.string.rs_symbol))));
    }

    @Override
    public void notifyTotal() {
        setTotal();
    }
}