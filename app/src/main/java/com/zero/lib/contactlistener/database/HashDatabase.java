package com.zero.lib.contactlistener.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Manish on 27/12/16.
 */

public class HashDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "hashDatabase";
    private static final int DB_VERSION = 1;
    private static final String TABLE_CONTACT = "contacts";
    private static final String ID = "_id";
    private static final String HASH = "hash";


    private static HashDatabase instance = null;

    public static synchronized HashDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new HashDatabase(context);
        }
        return instance;
    }


    public HashDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CONTACT +
                "(" +
                ID + " INTEGER PRIMARY KEY," +
                HASH + " TEXT" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    static private final String getCreateTableString(final String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName +
                "(" +
                ID + " INTEGER PRIMARY KEY," +
                HASH + " INTEGER" +
                ")";
    }

    public void setContactHashData(HashSet<String> incrementalState) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT + " ");
        db.execSQL(getCreateTableString(TABLE_CONTACT));
        if (incrementalState.size() > 0) {
            final SQLiteStatement statement = db.compileStatement("INSERT INTO " + TABLE_CONTACT + " VALUES( NULL, ? )");
            for (String incrementalValue : incrementalState) {
                statement.bindString(1, incrementalValue);
                statement.executeInsert();
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }


    public HashSet<String> getContactHashData() {
        final SQLiteDatabase db = getReadableDatabase();
        HashSet<String> incrementalState = new HashSet<String>();
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT " + HASH + " FROM " + TABLE_CONTACT + " ORDER BY " + HASH + " ASC", null);
            if (c.moveToFirst()) {
                do {
                    incrementalState.add(c.getString(0));
                } while (c.moveToNext());
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } finally {
            c.close();
        }
        db.close();
        return incrementalState;
    }
}
