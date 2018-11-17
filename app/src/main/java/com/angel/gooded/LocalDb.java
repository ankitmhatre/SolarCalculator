package com.angel.gooded;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocalDb extends SQLiteOpenHelper {

    private static LocalDb sInstance;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "goodeddb";
    private static final String PLACES_TABLE = "places";
    public static final String PLACE_ID = "id";
    public static final String LAT = "lat";
    public static final String LONG = "long";
    public static final String NAME = "name";

    public LocalDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized LocalDb getInstance(Context context) {

        if (sInstance == null)
            sInstance = new LocalDb(context.getApplicationContext());

        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String[] logincols = {LAT, LONG, NAME};

        String[] logincolstype = {"TEXT ", "TEXT ", "TEXT "};

        String createNewsTable = buildCreateStatement(PLACES_TABLE,
                logincols,
                logincolstype);

        db.execSQL(createNewsTable);
    }

    private String buildCreateStatement(String tableName, String[] columnNames, String[] columnTypes) {
        String createStatement = "";
        if (columnNames.length == columnTypes.length) {
            createStatement += "CREATE TABLE IF NOT EXISTS " + tableName + "("
                    + PLACE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ";

            for (int i = 0; i < columnNames.length; i++) {

                if (i == columnNames.length - 1) {
                    createStatement += columnNames[i]
                            + " "
                            + columnTypes[i]
                            + ")";
                } else {
                    createStatement += columnNames[i]
                            + " "
                            + columnTypes[i]
                            + ", ";
                }

            }

        }

        return createStatement;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Cursor getAllEntries() {
        String query = "SELECT * FROM " + PLACES_TABLE;//+ " ORDER BY " + NEWS_ID + " DESC";
        return getReadableDatabase().rawQuery(query, null);

    }

    public void addAPlace(String name, String lat, String lng) {

        ContentValues values = new ContentValues();

        values.put(LAT, lat);
        values.put(LONG, lng);
        values.put(NAME, name);


        getWritableDatabase().insert(PLACES_TABLE, "", values);


        Log.d("dataWritten", values.toString());
    }


}
