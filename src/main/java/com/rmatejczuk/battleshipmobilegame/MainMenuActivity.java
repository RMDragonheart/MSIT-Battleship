package com.rmatejczuk.battleshipmobilegame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Robert Matejczuk
 * Glowne menu programu. Jest to aktywnosc startowa czyli zostaje uruchomiona jako pierwsza
 * w momencie uruchomienia aplikacji.
 * Odpowiada jej uklad activity_main_menu.xml
 * Za pomoca tej aktywnosci uzytkownik moze wybrac w jaki tryb gry chce zagrac oraz sprawdzic swoje
 * statystyki jak rowniez zalogowac sie na swoje konto
 */
public class MainMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    // *** FUNKCJE PRZYCISKOW ***
    // Rozpoczyna standardowa rozgrywke
    public void onPlayBattle(View view) {
        Intent intent = new Intent(this, PlayBattleActivity.class);
        startActivity(intent);
    }

    // Rozpoczyna rankingowa rozgrywke
    public void onRankedBattle(View view) {
        CharSequence text = "Rozgrywasz Rankingowa Bitwe!";
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.show();
    }

    // Wyswietla statystyki gracza
    public void onStats(View view) {
        Intent intent = new Intent(this, StatisticActivity.class);
        startActivity(intent);
    }

    // Wyswietla tworcow aplikacji
    public void onCredits(View view) {
        CharSequence text = "Tworca tej aplikacji jest Robert Matejczuk";
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.show();
    }

    // Logowanie na swoje konto
    public void onLogin(View view) {
        CharSequence text = "Logowanie do konta";
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.show();
    }
}
