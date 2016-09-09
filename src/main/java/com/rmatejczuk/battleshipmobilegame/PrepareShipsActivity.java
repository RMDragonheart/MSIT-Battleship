package com.rmatejczuk.battleshipmobilegame;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Robert Matejczuk
 * Aktywnosci tej odpowiada uklad activity_prepare_ships.xml
 * Ta aktywnosc odpowiada za przedstawienie planszy jak i statkow. Uzytkownik podczas tej aktywnosci
 * moze wchodzic w interakcje ze statkami (moze je podnosic, przesuwac i upuszczac) w celu ustawienia
 * ich na planszy wg wlasnego uznania (rozmieszcza statki na planszy).
 */
public class PrepareShipsActivity extends Activity {

    public static final String GAME_TYPE = "game type";
    private DrawView drawView; // obszar bazowy na ktorym rysujemy
    private PlayField playField; // obiekt posiadajacy informacje o rysowanych obiektach
    private Ship touchedShip; // dotkniety statek
    private final int INITIAL_X = 5; // poczatkowa wartosc X

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prepare_ships);
        Intent intent = getIntent();
        String gameType = intent.getStringExtra(GAME_TYPE);
        TextView textGameType = (TextView) findViewById(R.id.game_type);

        // Ustawienia informacji o trybie gry
        switch (gameType) {
            case "playAI":
                textGameType.setText("VS AI Battle");
                Button button = (Button) findViewById(R.id.next);
                button.setVisibility(View.INVISIBLE);
                break;
            case "playLocal":
                textGameType.setText("Local Battle Player 1");
                Button buttonFight = (Button) findViewById(R.id.fight);
                buttonFight.setVisibility(View.INVISIBLE);
                break;
            case "nextPlayer":
                textGameType.setText("Local Battle Player 2");
                Button buttonNext = (Button) findViewById(R.id.next);
                buttonNext.setVisibility(View.INVISIBLE);
                Button buttonBack = (Button) findViewById(R.id.back);
                buttonBack.setVisibility(View.INVISIBLE);
                break;
            case "playOnline":
                textGameType.setText("Online Battle");
                Button bNext = (Button) findViewById(R.id.next);
                bNext.setVisibility(View.INVISIBLE);
                Button bFight = (Button) findViewById(R.id.back);
                bFight.setVisibility(View.INVISIBLE);
                break;
            default:
                textGameType.setText("Cos poszlo nie tak");
        }
    }

    /**
     * Pole do rysowania DrawView zostaje stworzone w momencie gdy obraz jest widoczny
     * gdyz wczesniej gdyby ten fragment kodu zostal wykonany np. w metodzie onCreate zostal zglaszany
     * wyjatek, ze nie jest jeszcze DrawView w ukladzie stworzone
     *
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            drawView = (DrawView) findViewById(R.id.DrawView);
            drawView.setBackgroundColor(Color.rgb(51, 51, 51));
            if (playField == null) {
                playField = new PlayField(drawView.getSquareWidth(false), INITIAL_X, drawView.getSquareWidth(false) * 1.5f);
            }
            drawView.setPlayfield(playField);
            drawView.invalidate();
        }
    }

    /**
     * Moment kiedy dotykamy ekranu. Mozemy przenosic statki
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - drawView.getLeft();
        float y = event.getY() - drawView.getTop() - 0.5f * drawView.getSquareWidth(false);

        // przenoszenie obiektu statku gdy juz go dotykamy
        if (touchedShip != null) {
            touchedShip.moveTo(x, y);
            playField.checkSquareCovering(touchedShip.getX(), touchedShip.getY(), touchedShip.getLength(), drawView.getSquareWidth(false), touchedShip.isOrientation());
            drawView.invalidate();
        }
        // podniesienie statku (moment kiedy trafimy palcem statek)
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchedShip = playField.getTouchedShip(x, y); // zamapietujemy obiekt dotknietego statku
            // zrestartowanie listy pol zajmowanych przez statek
            playField.clearOccupiedSquare(touchedShip);


        } else if (event.getAction() == MotionEvent.ACTION_UP) { // podniesienie palca ze statku
            if (touchedShip != null) {
                touchedShip.isNotTouched(); // ustawienie nowych wspolrzednych statku z wzieciem pod uwage miejsca trzymania statku

                // najpierw po upuszczeniu statku wyrownujemy go do pol, nastepnie
                // sprawdzamy czy statek wystaje po za obramowanie, jesli tak to wraca na swoja
                // domyslna pozycje
                float width = drawView.getSquareWidth(false);
                float initialY = width * 1.5f;

                if (touchedShip.isOrientation()) { // statek jest pionowo
                    playField.aligmentShipToSquare(touchedShip); // wyrownanie do pol statku
                    drawView.invalidate();
                    // ustawienie listy pol na ktorych lezy statek jak i jego sasiednich pol

                    if (touchedShip.getX() < INITIAL_X || touchedShip.getY() < initialY ||
                            touchedShip.getX() - 0.5f + width > INITIAL_X + width * 10 ||
                            touchedShip.getY() - 0.5f + touchedShip.getLength() * width > initialY + width * 10) {
                        playField.getLastShip().returnToStartingPosition();
                        playField.resetSquareCovering();
                        // zrestartowanie listy pol zajmowanych przez statek
                        playField.clearOccupiedSquare(touchedShip);
                    } else if (playField.shipIsPlaced(touchedShip, width)) {
                        playField.setOccupiedSquare(touchedShip, width);
                    } else {
                        playField.getLastShip().returnToStartingPosition();
                        playField.resetSquareCovering();
                        // zrestartowanie listy pol zajmowanych przez statek
                        playField.clearOccupiedSquare(touchedShip);
                    }
                } else { // statek jest poziomo
                    playField.aligmentShipToSquare(touchedShip);
                    drawView.invalidate();
                    // ustawienie listy pol na ktorych lezy statek jak i jego sasiednich pol

                    if (touchedShip.getX() < INITIAL_X || touchedShip.getY() < initialY ||
                            touchedShip.getY() - 0.5f + width > initialY + width * 10 ||
                            touchedShip.getX() - 0.5f + touchedShip.getLength() * width > INITIAL_X + width * 10) {
                        playField.getLastShip().returnToStartingPosition();
                        playField.resetSquareCovering();
                        // zrestartowanie listy pol zajmowanych przez statek
                        playField.clearOccupiedSquare(touchedShip);
                    } else if (playField.shipIsPlaced(touchedShip, width)) {
                        playField.setOccupiedSquare(touchedShip, width);
                    } else {
                        playField.getLastShip().returnToStartingPosition();
                        playField.resetSquareCovering();
                        // zrestartowanie listy pol zajmowanych przez statek
                        playField.clearOccupiedSquare(touchedShip);
                    }
                }
            }
            touchedShip = null;
        }
        playField.recalculateShipSquares();
        return super.onTouchEvent(event);
    }

    // *** Funkcje przyciskow ***
    public void onRotate(View view) {
        Ship lastTouchedShip = playField.getLastShip();
        if (lastTouchedShip != null) {
            lastTouchedShip.changeOrientation();
            playField.aligmentShipToSquare(lastTouchedShip);
            drawView.invalidate();
            // usuniecie listy zajmowanych pol
            playField.clearOccupiedSquare(lastTouchedShip);
            playField.recalculateShipSquares();
            // Sprawdzenie czy po odwroceniu statek nie wychodzi za plansze, jesli tak to wraca do
            // puli innych statkow zachowujac swoja nowa orientacje
            if (lastTouchedShip.getY() - 0.5f + lastTouchedShip.getLength() * drawView.getSquareWidth(false)
                    > drawView.getSquareWidth(false) * 1.5f + drawView.getSquareWidth(false) * 10 ||
                    lastTouchedShip.getX() - 0.5f + lastTouchedShip.getLength() * drawView.getSquareWidth(false)
                            > INITIAL_X + drawView.getSquareWidth(false) * 10) {
                lastTouchedShip.returnToStartingPosition();
                playField.resetSquareCovering();
            } else if (playField.shipIsPlaced(lastTouchedShip, drawView.getSquareWidth(false))) {
                //playField.checkSquareCovering(lastTouchedShip.getX(), lastTouchedShip.getY(), lastTouchedShip.getLength(), drawView.getSquareWidth(false), lastTouchedShip.isOrientation());
                playField.resetSquareCovering();
                playField.setOccupiedSquare(lastTouchedShip, drawView.getSquareWidth(false));
            } else {
                lastTouchedShip.changeOrientation();
                Toast.makeText(this, "Adbmiral powiedzal, ze nie mozemy sie obrocic", Toast.LENGTH_SHORT).show();
                // playField.checkSquareCovering(lastTouchedShip.getX(), lastTouchedShip.getY(), lastTouchedShip.getLength(), drawView.getSquareWidth(), lastTouchedShip.isOrientation());
                playField.resetSquareCovering();
                playField.setOccupiedSquare(lastTouchedShip, drawView.getSquareWidth(false));
            }
        }
        playField.recalculateShipSquares();
    }

    public void onBack(View view) {
        finish();
    }

    public void onNext(View view) {
        Intent intent = new Intent(this, PrepareShipsActivity.class);
        intent.putExtra(PrepareShipsActivity.GAME_TYPE, "nextPlayer");
        startActivity(intent);
    }

    public void onFight(View view) {
        if (playField.isAllShipPlaced()) {

            Intent intent = new Intent(this, BattleActivity.class);
            intent.putExtra(BattleActivity.GAME_DATA, playField.toString());
            startActivity(intent);

            // Wyswietlenie przekazywanej intencji
            //System.out.println("****************************] "+playField);
        } else {
            CharSequence text = "Admirał domaga się rozstawienia wszystkich statnów!";
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }

    }
}
