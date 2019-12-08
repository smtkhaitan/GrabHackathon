package com.example.test;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper  {

    public static final String DATABASE_NAME = "grabWalletDatabase.db";
    public static final String TRANSACTION_TABLE_NAME = "grabTransactionTable";
    public static final String WALLET_TABLE_NAME = "grabWalletTable";

    public static final String COL_1 = "Id";
    public static final String COL_2 = "walletAmount";
    public static final String COL_3 = "transactionCustomerId";
    public static final String COL_4 = "transactionAmount";
    public static final String COL_5 = "updatedToExternalDatabase";
    public static final String COL_6 = "paidOrReceived";

    public static final String COL_7 = "walletMoney";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+ TRANSACTION_TABLE_NAME + " (Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "walletAmount float, transactionCustomerId text, " +
                "transactionAmount float, " +
                "updatedToExternalDatabase bit," +
                "paidOrReceived text)");

        db.execSQL("create table "+ WALLET_TABLE_NAME + " (Id INTEGER, " +
                "walletMoney float)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+ TRANSACTION_TABLE_NAME);
        db.execSQL("drop table if exists "+ WALLET_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String walletAmount, String transactionCustomerId, String transactionAmount,
                              String updatedToExternalDatabase, String paidOrReceived) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, walletAmount);
        contentValues.put(COL_3, transactionCustomerId);
        contentValues.put(COL_4, transactionAmount);
        contentValues.put(COL_5, updatedToExternalDatabase);
        contentValues.put(COL_6, paidOrReceived);
        long result = database.insert(TRANSACTION_TABLE_NAME, null, contentValues);

        if(result != -1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean insertWalletMoney(String amount) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, 1);
        contentValues.put(COL_7, amount);
        long result = database.insert(WALLET_TABLE_NAME, null, contentValues);
        System.out.println("updating wallet first time results .. "+result);

        if(result != -1) {
            return true;
        } else {
            return false;
        }
    }

    public int updateWalletTable(float amount, String paidOrReceived) {
        Cursor cursor = getAllData(WALLET_TABLE_NAME);
        if(cursor.getCount() != 0 ) {
            while (cursor.moveToNext()) {
                float currentAmount = Float.parseFloat(cursor.getString(1));
                System.out.println("currentAmount : "+currentAmount);
                if (paidOrReceived.equalsIgnoreCase("paid")) {
                    currentAmount -= amount;
                } else if (paidOrReceived.equalsIgnoreCase("received")) {
                    currentAmount += amount;
                }
                System.out.println("currentAmountNew : "+currentAmount);
                updateWallet(currentAmount);
            }
        }
        // db is being inserted first time
        else {
            System.out.println("updating wallet first time.....");
            if (paidOrReceived.equalsIgnoreCase("received")) {
                insertWalletMoney(Float.toString(amount));
            } else {
                return -1;
            }
        }
        return 0;
    }

    public Cursor getAllData(String table) {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor res = database.rawQuery("select * from  "+ table, null);
        return res;
    }

    public boolean update(String id , String walletAmount, String transactionCustomerId, String transactionAmount,
                          String updatedToExternalDatabase, String paidOrReceived) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_2, walletAmount);
        contentValues.put(COL_3, transactionCustomerId);
        contentValues.put(COL_4, transactionAmount);
        contentValues.put(COL_5, updatedToExternalDatabase);
        contentValues.put(COL_6, paidOrReceived);
        database.update(TRANSACTION_TABLE_NAME, contentValues, "Id = ? ", new String[] {id} );
        return true;
    }

    private boolean updateWallet(float amount) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_7, amount);
        database.update(WALLET_TABLE_NAME, contentValues, "Id = ? ", new String[] {"1"} );
        return true;
    }
}
