package com.jack.fleximall.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.jack.fleximall.Global;
import com.jack.fleximall.Product;
import com.jack.fleximall.R;
import com.jack.fleximall.WishlistItemAdapter;

import java.util.ArrayList;

public class WishlistFragment extends Fragment{

    private Global global;
    private ArrayList<Product> products;
    private WishlistItemAdapter adapter;
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        products = new ArrayList<Product>();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wishlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        global = (Global) getActivity().getApplication();

        Cursor cursor = global.getDatabaseHelper().getWishlistItems();

        listView = view.findViewById(R.id.wishlist_frag_list);
        adapter = new WishlistItemAdapter(getContext(),cursor,
                global.getDatabaseHelper(), getActivity());
        listView.setAdapter(adapter);
    }
}
