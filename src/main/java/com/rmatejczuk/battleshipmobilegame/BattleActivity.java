package com.rmatejczuk.battleshipmobilegame;

/*

* @startuml

* car --|> wheel

* @enduml

*/

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.Random;


/**
 * Created by Robert Matejczuk
 * Aktywnosc ta odpowiada za zarzadzanie bitwa. Gracz za pomoca tej aktywnosci prowadzi bitwe ze
 * wczesniej wybranym przeciwnikiem. Moze tutaj oddawac strzaly w celu zniszczenia statku, widzi
 * gdzie oddal juz swoje strzaly i ile statkow zniszczyl. Rowniez do jego dyspozycji jest widok
 * przedstawiajacy mu jego wlasne plywajace statki, zniszczone statki przez jego przeciwnika
 * jak rowniez miejsca w ktore przeciwnik oddal strzal.
 */
public class BattleActivity extends Activity {

    public static final String GAME_DATA = "game_data"; // przekazanie danych z ustawien statku za pomoca intencji
    private String gameData = ""; // string przechowujacy ciag danych pobranych z intencji
    private DrawView drawView1; // widok dla gracza
    private DrawView drawView2; // widok dla przeciwnika
    private PlayField playField1; // obiekt klasy obslugujacej statki graczy
    private PlayField playField2; // obiekt klasy obslugujacej statki przeciwnika
    private static final float INITIAL_X = 5; // stala poczatkowa wartosc X jako margines rysowania
    private AndroidAI androidAI;
    private boolean firstTurn = true;
    private Intent dialogIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);
        Intent intent = getIntent();
        gameData = intent.getStringExtra(GAME_DATA); // pobranie intencji zawierajacej dane o polozeniu statkow

        try {
            // Stworzenie obiektu sztucznej inteligencji Androida i przekazanie mu dostepu do bazy danych
            SQLiteOpenHelper battleDatabaseHelper = new BattleDatabaseHelper(this);
            SQLiteDatabase db = battleDatabaseHelper.getWritableDatabase();
            androidAI = new AndroidAI(db);
        } catch (SQLiteException e) {
            Toast.makeText(this, "Admiral przeciwnej floty nie moze uzyskac polaczenia z baza danych", Toast.LENGTH_SHORT).show();
        }

        dialogIntent = new Intent(this, MainMenuActivity.class);

        // sprawdzenie inicjalizacji tablic
        androidAI.printDBTable("PLAYER_SHIPS");
        androidAI.printDBTable("PLAYER_SHOTS");
        androidAI.printDBHistoryTable();
    }

    /**
     * Rysowanie plansz bitwy i inicjowanie polozen statkow gracza
     *
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // STATKI GRACZA
            drawView1 = (DrawView) findViewById(R.id.DrawView1);
            drawView1.setDrawType("BATTLE");
            drawView1.setBackgroundColor(Color.rgb(51, 51, 51));
            if (playField1 == null) {
                playField1 = new PlayField(gameData, drawView1.getSquareWidth(true), INITIAL_X, drawView1.getSquareWidth(true) * 1.5f);
                playField1.recalculateShipInBattle(drawView1.getSquareWidth(true));
            }
            drawView1.setPlayfield(playField1);
            drawView1.invalidate();


            // STATKI PRZECIWNIKA
            drawView2 = (DrawView) findViewById(R.id.DrawView2);
            drawView2.setDrawType("ANDROID");
            drawView2.setBackgroundColor(Color.rgb(51, 51, 51));
            if (playField2 == null) {
                // rozlozenie statkow przeciwnika
                playField2 = new PlayField(androidAI, drawView2.getSquareWidth(true), INITIAL_X, drawView2.getSquareWidth(true) * 1.5f);
            }
            drawView2.setPlayfield(playField2);
            drawView2.invalidate();

            if (firstTurn) {
                firstTurn = false;
                Random random = new Random();
                if (random.nextBoolean()) { // True zaczyna gracz, nic sie nie dzieje strzal dopiero w onTouchEvent
                    System.out.println("Zaczyna gracz");
                    androidAI.setPlayerTurn(true);
                    Toast.makeText(this, "Niech rozpocznie sie bitwa! Yarrr! Gracz zaczyna!", Toast.LENGTH_SHORT).show();
                } else { // False - zaczyna Android i oddaje od razu strzal
                    System.out.println("Zaczyna Android");
                    Toast.makeText(this, "Niech rozpocznie sie bitwa! Yarrr! Android zaczyna!", Toast.LENGTH_SHORT).show();
                    // pierwszy strzal komputera
                    while (!androidAI.isPlayerTurn()) {
                        // Android strzela
                        if (playField1.getAndroidShot(androidAI)) {
                            //Toast.makeText(this, "Zostales trafiony!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            drawView1.invalidate();
            drawView2.invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX() - drawView2.getLeft() - 5f;
        float y = event.getY() - drawView2.getTop() - 0.6f * drawView2.getSquareWidth(true);
        //System.out.println("Wspolrzedna X: " + x + " Y: " + y);
        float width = drawView2.getSquareWidth(true);
        playField2.checkFingerSquareCovering(x, y, width);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (playField2.isSquareHit(x, y, width, androidAI)) {
                //Toast.makeText(this, "TRAFIONY!", Toast.LENGTH_SHORT).show();
            }
            // Po strzale gracza sprawdzamy czy tura dobiegla konca (Gdyby Android zaczynal)
            if (androidAI.isPlayerShot() && androidAI.isAndroidShot()) {
                androidAI.incrementRoundCounter();
                androidAI.setPlayerShot(false);
                androidAI.setAndroidShot(false);
            }
            while (!androidAI.isPlayerTurn()) {
                // Android strzela
                if (playField1.getAndroidShot(androidAI)) {
                    //Toast.makeText(this, "Zostales trafiony!!", Toast.LENGTH_SHORT).show();
                }
            }
            // Po strzale Androida sprawdzamy czy tura dobiegla konca (Gdy Gracz zaczyna)
            if (androidAI.isPlayerShot() && androidAI.isAndroidShot()) {
                androidAI.incrementRoundCounter();
                androidAI.setPlayerShot(false);
                androidAI.setAndroidShot(false);
            }
        }
        drawView1.invalidate();
        drawView2.invalidate();

        if (androidAI.isBattleEnded()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (androidAI.isPlayerWin()) {
                builder.setTitle(R.string.dialog_ending_title)
                        .setMessage(R.string.dialog_ending_message_player_win)
                        .setPositiveButton(R.string.dialog_ending_message_back_to_menu, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialogIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(dialogIntent);
                            }
                        })
                        .setNegativeButton(R.string.dialog_ending_message_new_game, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
            } else {
                builder.setTitle(R.string.dialog_ending_title)
                        .setMessage(R.string.dialog_ending_message_player_lose)
                        .setPositiveButton(R.string.dialog_ending_message_back_to_menu, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialogIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(dialogIntent);
                            }
                        })
                        .setNegativeButton(R.string.dialog_ending_message_new_game, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
            }

            AlertDialog alert = builder.create();
            alert.show();
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("BAZA DANYCH ZAKTUALIZOWANA!");
        androidAI.updateDBWhereToShot();
        androidAI.updateDBWhereToPlace();
        androidAI.updateDBGameHistory(androidAI.isPlayerWin(), androidAI.getRoundCounter());
        androidAI.closeDBConnecion();
    }
}
