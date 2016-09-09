package com.rmatejczuk.battleshipmobilegame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Robert Matejczuk
 * Aktywnosci tej odpowiada uklad activity_play_battle.xml
 * Aktywnosc ta odpowiada za wybor konkretnego trybu gry nierankingowego. Gracz moze tutaj rozpoczac
 * rozgrywke z komputerem, lokalnie z innym graczem badz przez siec z innym graczem.
 */
public class PlayBattleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_battle);
    }

    public void onPlayAI (View view) {
        Intent intent = new Intent(this, PrepareShipsActivity.class);
        intent.putExtra(PrepareShipsActivity.GAME_TYPE, "playAI");
        startActivity(intent);
    }

    public void onPlayLocal (View view) {
        Intent intent = new Intent(this, PrepareShipsActivity.class);
        intent.putExtra(PrepareShipsActivity.GAME_TYPE, "playLocal");
        startActivity(intent);
    }

    public void onPlayOnline (View view) {
        Intent intent = new Intent(this, PrepareShipsActivity.class);
        intent.putExtra(PrepareShipsActivity.GAME_TYPE, "playOnline");
        startActivity(intent);
    }

    public void onBack (View view) {
        finish();
    }
}
