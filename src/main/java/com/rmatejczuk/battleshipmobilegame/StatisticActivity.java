package com.rmatejczuk.battleshipmobilegame;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.ListAdapter;
import android.view.View.MeasureSpec;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

/**
 * Created by Robert Matejczuk
 * Aktywnosci tej odpowiada uklad activity_statistic.xml
 * Aktywnosc ta po wywolaniu przedstawia graczowi jego statystyki.
 */
public class StatisticActivity extends Activity {

    private SQLiteDatabase db;
    private Cursor historyCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        ListView historyGameList = (ListView) findViewById(R.id.game_history_list);
        setListViewHeightBasedOnChildren(historyGameList);
        try {
            // Stworzenie obiektu sztucznej inteligencji Androida i przekazanie mu dostepu do bazy danych
            SQLiteOpenHelper battleDatabaseHelper = new BattleDatabaseHelper(this);
            db = battleDatabaseHelper.getWritableDatabase();
            historyCursor = db.query("GAMES_HISTORY",
                    new String[]{"_id", "WINNER", "HOW_MANY_TURNS"},
                    null, null, null, null, null);
            SimpleCursorAdapter historyAdapter = new SimpleCursorAdapter(getBaseContext(),
                    R.layout.layout_item,
                    historyCursor,
                    new String[]{"_id", "WINNER", "HOW_MANY_TURNS"},
                    new int[]{R.id._idDB, R.id.winnderDB, R.id.roundsDB}, 0);

            historyGameList.setAdapter(historyAdapter);

        } catch (SQLiteException e) {
            Toast.makeText(this, "Admiral przeciwnej floty nie moze uzyskac polaczenia z baza danych", Toast.LENGTH_SHORT).show();
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup) {
                listItem.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            }
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
