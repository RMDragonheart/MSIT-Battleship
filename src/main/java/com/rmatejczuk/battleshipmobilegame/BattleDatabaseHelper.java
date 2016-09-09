package com.rmatejczuk.battleshipmobilegame;

import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Klasa Helpera do obslugi bazy danych
 */
public class BattleDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "AndroidMemory"; // nazwa naszej bazy danych
    private static final int DB_VERSION = 1; // Nr wersji bazy danych

    BattleDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE PLAYER_SHIPS (" +
                "_id TEXT PRIMARY KEY, " +
                "OCCURRENCE INTEGER);");

        db.execSQL("CREATE TABLE PLAYER_SHOTS (" +
                "_id TEXT PRIMARY KEY, " +
                "SHOTS INTEGER);");

        db.execSQL("CREATE TABLE GAMES_HISTORY (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "WINNER TEXT, " +
                "HOW_MANY_TURNS INTEGER);");

        insertFields("PLAYER_SHIPS", db);
        insertFields("PLAYER_SHOTS", db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static void insertFields(String tableName, SQLiteDatabase db) {
        ContentValues fieldsValues = new ContentValues();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                fieldsValues.put("_id", numberToLetter(i) + String.valueOf(j + 1) + "");

                if (tableName.equals("PLAYER_SHOTS")) {
                    fieldsValues.put("SHOTS", 0);
                } else if (tableName.equals("PLAYER_SHIPS")) {
                    fieldsValues.put("OCCURRENCE", 0);
                }

                db.insert(tableName, null, fieldsValues);
            }
        }
    }

    /**
     * Metoda zamieniajaca wartosc liczbowa na litere w celu uzyskania id pola w formacie np. C5
     *
     * @param i
     * @return
     */
    private static String numberToLetter(int i) {
        switch (i) {
            case 0:
                return "A";
            case 1:
                return "B";
            case 2:
                return "C";
            case 3:
                return "D";
            case 4:
                return "E";
            case 5:
                return "F";
            case 6:
                return "G";
            case 7:
                return "H";
            case 8:
                return "I";
            case 9:
                return "J";
            default:
                return "ERROR";
        }
    }
}
