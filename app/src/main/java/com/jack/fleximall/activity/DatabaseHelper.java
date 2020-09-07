package com.jack.fleximall.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jack.fleximall.R;

import org.json.JSONException;
import org.json.JSONObject;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "products";
    private static final String TABLE_NAME = "cart_items";
    private static final String TABLE_WISHLIST = "wishlist_items";
    private static String COLUMN_BARCODE = null;
    private static String COLUMN_NAME = null;
    private static String COLUMN_PRICE = null;
    private static String COLUMN_QUANTITY = null;

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
        COLUMN_BARCODE = context.getString(R.string.column_barcode);
        COLUMN_NAME = context.getString(R.string.column_name);
        COLUMN_PRICE = context.getString(R.string.column_price);
        COLUMN_QUANTITY = context.getString(R.string.column_quantity);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+TABLE_NAME+" ("+COLUMN_BARCODE+" text primary key," +
                " "+COLUMN_NAME+" text, "+COLUMN_PRICE+" long, "+ COLUMN_QUANTITY+" int)");

        db.execSQL("create table "+TABLE_WISHLIST+" ("+COLUMN_BARCODE+" text primary key," +
                " "+COLUMN_NAME+" text, "+COLUMN_PRICE+" long, "+ COLUMN_QUANTITY+" int)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public boolean insertItem(String barcode,String name,long price) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_BARCODE, barcode);
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_PRICE, price);
        contentValues.put(COLUMN_QUANTITY,1);

        boolean status;
        try {
            status = db.insert(TABLE_NAME, null, contentValues)>0;
        }
        catch (Exception e){
            e.printStackTrace();
            status = false;
        }

        db.close();
        return status;
    }

    public boolean updateQty(String barcode,int qty){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_QUANTITY, qty);

        boolean status = db.update(TABLE_NAME,contentValues, COLUMN_BARCODE+" = ?",
                new String[] {barcode})>0;
        db.close();
        return status;
    }

    public boolean updateWishlistQty(String barcode,int qty){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_QUANTITY, qty);

        boolean status = db.update(TABLE_WISHLIST,contentValues, COLUMN_BARCODE+" = ?",
                new String[] {barcode})>0;
        db.close();
        return status;
    }

    public boolean moveToWishlist(String barcode){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from "+TABLE_NAME+" where "+COLUMN_BARCODE+" = '"+
                barcode+"'",null);

        if (cursor.getCount()>0){
            if(cursor.moveToFirst()){
                SQLiteDatabase writableDatabase = this.getWritableDatabase();
                ContentValues contentValues = new ContentValues();

                contentValues.put(COLUMN_BARCODE, barcode);
                contentValues.put(COLUMN_NAME, cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                contentValues.put(COLUMN_PRICE, cursor.getString(cursor.getColumnIndex(COLUMN_PRICE)));
                contentValues.put(COLUMN_QUANTITY,cursor.getString(cursor.getColumnIndex(COLUMN_QUANTITY)));

                boolean status;
                try {
                    writableDatabase.beginTransaction();
                    status = writableDatabase.insert(TABLE_WISHLIST, null, contentValues)>0;
                }
                catch (Exception e){
                    e.printStackTrace();
                    status = false;
                }

                if (status){

                    if(delete(barcode)){
                        writableDatabase.setTransactionSuccessful();
                        writableDatabase.endTransaction();
                        writableDatabase.close();
                        cursor.close();
                        db.close();
                        return true;
                    }
                    else{
                        writableDatabase.endTransaction();
                    }
                }
                else {
                    writableDatabase.endTransaction();
                }
                writableDatabase.close();
            }
        }

        cursor.close();
        db.close();
        return false;
    }

    public boolean moveToCart(String barcode){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from "+TABLE_WISHLIST+" where "+COLUMN_BARCODE+" = '"+
                barcode+"'",null);

        if (cursor.getCount()>0){
            if(cursor.moveToFirst()){
                SQLiteDatabase writableDatabase = this.getWritableDatabase();
                ContentValues contentValues = new ContentValues();

                contentValues.put(COLUMN_BARCODE, barcode);
                contentValues.put(COLUMN_NAME, cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                contentValues.put(COLUMN_PRICE, cursor.getString(cursor.getColumnIndex(COLUMN_PRICE)));
                contentValues.put(COLUMN_QUANTITY,cursor.getString(cursor.getColumnIndex(COLUMN_QUANTITY)));

                boolean status;
                try {
                    writableDatabase.beginTransaction();
                    status = writableDatabase.insert(TABLE_NAME, null, contentValues)>0;
                }
                catch (Exception e){
                    e.printStackTrace();
                    status = false;
                }

                if (status){

                    if(deleteFromWishlist(barcode)){
                        writableDatabase.setTransactionSuccessful();
                        writableDatabase.endTransaction();
                        writableDatabase.close();
                        cursor.close();
                        db.close();
                        return true;
                    }
                    else{
                        writableDatabase.endTransaction();
                    }
                }
                else {
                    writableDatabase.endTransaction();
                }
                writableDatabase.close();
            }
        }

        cursor.close();
        db.close();
        return false;
    }

    public boolean ifExists(String barcode){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from "+TABLE_NAME+" where "+COLUMN_BARCODE+" = '"+barcode+"'",null);
        return cursor.getCount()>0;
    }

    public boolean ifExistsInWishlist(String barcode){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from "+TABLE_WISHLIST+" where "+COLUMN_BARCODE+" = '"+barcode+"'",null);
        return cursor.getCount()>0;
    }

    public boolean delete(String barcode){
        SQLiteDatabase db = this.getReadableDatabase();

        return db.delete(TABLE_NAME, COLUMN_BARCODE + "='" + barcode + "'", null) > 0;
    }

    public boolean deleteFromWishlist(String barcode){
        SQLiteDatabase db = this.getReadableDatabase();

        return db.delete(TABLE_WISHLIST, COLUMN_BARCODE + "='" + barcode + "'", null) > 0;
    }

    public Cursor getAllItems(){

        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from "+TABLE_NAME,null);
    }

    public Cursor getWishlistItems(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from "+TABLE_WISHLIST,null);
    }

    public void dropAll(){
        this.getWritableDatabase().execSQL("DELETE FROM "+TABLE_NAME);
    }
}