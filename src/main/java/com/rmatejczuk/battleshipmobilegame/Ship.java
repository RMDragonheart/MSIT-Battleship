package com.rmatejczuk.battleshipmobilegame;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by Robert Matejczuk
 * Jest to klasa przechowujaca informacje na temat statkow i ich parametrow
 */
public class Ship {
    private float x; // wspolrzedna x prawo/lewo
    private float y; // wspolrzedna y gora/dol im nizej tym wieksza wartosc
    private int length; // dlugosc statku jego typ
    private float width; // dlugosc statku, odpowiada dlugosci jednego boku pola, obliczana wg wielkosci urzadzenia
    private boolean orientation; // orientacja statku. false oznacza, ze statek jest ulozony poziomo, true - statek jest ulozony pionowo
    private float adjastmentX; // zmienna ktora powoduje, ze obiekt nie przesuwa sie do gornej lewej krawedzi przy przesuwaniu
    private float adjastmentY; // zmienna ktora powoduje, ze obiekt nie przesuwa sie do gornej lewej krawedzi przy przesuwaniu
    private final float startingX; // poczatkowa wspolrzedna x
    private final float startingY; // poczatkowa wspolrzedna y
    private ArrayList<Square> inWhatSquare; // lista obiektow pol na ktorych obecnie jest statek
    private int workingParts;
    private boolean destroyed = false;


    /**
     * Konstruktor stworzony do aktywnosci PrepareShipsActivity.java
     *
     * @param x
     * @param y
     * @param length
     * @param width
     */
    public Ship(float x, float y, int length, float width) {
        this.x = x;
        this.y = y;
        this.length = length;
        this.width = width;
        orientation = false;
        startingX = x;
        startingY = y;
        inWhatSquare = new ArrayList<>();
    }

    /**
     * Konstruktor przystosowany do przetworzenia otrzymanej intencji w postaci Stringa i
     * rozmieszczenia statkow w BattleActivity.java
     *
     * @param shipString
     * @param squareList
     * @param width
     */
    public Ship(String shipString, ArrayList<Square> squareList, float width) {
        startingX = 0;
        startingY = 0;
        this.width = width;
        inWhatSquare = new ArrayList<>();

        String[] squares = shipString.split("!");
        // bo dlugosc tablicy zawiera informacje o orientacji co powoduje, ze statki mialy
        // by o jeden maszt za duzo
        this.length = squares.length-1;

        // zmienna ktora bedzie sprawdzana czy statek ma jakies funkcjonujace czesci podczas bitwy
        this.workingParts = this.length;

        // ustawiamy orientacje statku
        if (squares[length].equals("true")) {
            orientation = true;
        } else if (squares[length].equals("false")) {
            orientation = false;
        }

        //length--; // usuwamy ifnroacje o orientacji, gdyz juz ja ustawilismy

        // dodanie obiektow pol do listy zajmowanych pol przez statek na podstawie ID pol
        for (int i = 0; i < this.length; i++) {
            for (Square s : squareList) {
                // Wypisanie wszystkich stworzonych pol w squareList
                // System.out.println(s);

                // sprawdzenie czy dane pole jest zajete przez statek, jest tak to dodajemy je do listy
                // pol zajmowanych przez dany statek
                if ((s.getyID() + s.getxID()).equals(squares[i])) {
                    inWhatSquare.add(s);
                }
            }
        }
        // Sprzewdzenie przekazanych wartosci do konstruktora Ship podczas bitwy
        //System.out.println("Ship String: " + shipString + "\n" + "inWhatSquare: " + inWhatSquare);

        // ustawienie wspolrzednych statkow na podstawie wspolrzednych pol ktore zajmuja
        x = Float.MAX_VALUE;
        y = Float.MAX_VALUE;
        for (Square s : inWhatSquare) {
            if (s.getX() < x && s.getY() < y) {
                x = s.getX();
                y = s.getY();
            }
        }
    }

    public Ship(boolean orientation, float x, float y, int length, float width) {
        startingY = 0;
        startingX = 0;
        this.orientation = orientation;
        this.x = x;
        this.y = y;
        this.length = length;
        this.width = width;
        this.workingParts = this.length;
        inWhatSquare = new ArrayList<>();
    }

    /**
     * Rysowanie statkow, przy przemieszczaniu ich rowniez styl.FILL bo inaczej bedzie tylko obramowanie
     *
     * @param canvas obiekt obszaru na ktorym rysujemy
     * @param paint  obiekt z informacjami o stylach rysowania
     */
    public void draw(Canvas canvas, Paint paint) {
        if (orientation) {
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(x + adjastmentX, y + adjastmentY, x + adjastmentX + width, y + adjastmentY + width * length, paint);
        } else {
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(x + adjastmentX, y + adjastmentY, x + adjastmentX + length * width, y + adjastmentY + width, paint);
        }
    }

    /**
     * Ustawienie odchylenia dotykanego punktu wzgledem lewej gorniej krawedzi statku
     *
     * @param xx
     * @param yy
     */
    private void setTouchAdjustment(float xx, float yy) {
        adjastmentX = x - xx;
        adjastmentY = y - yy;
    }

    /**
     * Kiedy statek przestaje byc dotykany, ustawiamy nowe wspolrzedne dodane o miejsce trzymania
     * statku i zerujemy je
     */
    public void isNotTouched() {
        x = x + adjastmentX;
        y = y + adjastmentY;
        adjastmentX = 0;
        adjastmentY = 0;
    }

    /**
     * Sprawdzenie czy statek jest dotkniety (czy trafilismy palcem w statek)
     *
     * @param xx
     * @param yy
     * @return
     */
    public boolean isTouched(float xx, float yy) {
        if (orientation) {
            // jezeli jest true to jest pionowo
            if (xx > x && xx < x + width && yy > y && yy < y + width * length) {
                setTouchAdjustment(xx, yy);
                return true;
            } else {
                return false;
            }
            // w przeciwnym wypadku poziomo
        } else {
            if (xx > x && xx < x + width * length && yy > y && yy < y + width) {
                setTouchAdjustment(xx, yy);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Przesuniecie statku (zmiana jego wspolrzednych x,y)
     *
     * @param x
     * @param y
     */
    public void moveTo(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Zmiana orientacji statku
     */
    public void changeOrientation() {
        if (orientation) {
            orientation = false;
        } else {
            orientation = true;
        }
    }

    /**
     * Metoda ustawiajaca statek na pozycje startowa
     */
    public void returnToStartingPosition() {
        x = startingX;
        y = startingY;
    }

    /**
     * Metoda usuwajaca duplikaty w ArrayLiscie inWhatSquare posiadajacej obiekty typu Square
     * w celu zapamietana na jakich polach lezy dany statek
     */
    public void recalculate() {
        for (int i = 0; i < inWhatSquare.size(); i++) {
            Square s1 = inWhatSquare.get(i);
            for (int j = 0; j < inWhatSquare.size(); j++) {
                Square s2 = inWhatSquare.get(j);
                if ((s1.getState() == 1) && (s2.getState() == 1) && (s1.getxID().equals(s2.getxID())) && (s1.getyID().equals(s2.getyID())) && i != j) {
                    inWhatSquare.remove(j);
                    j--;
                }
            }
            if (s1.getState() == 0) {
                s1.setState(-1);
            }
        }
    }

    /**
     * Metoda ustawiajaca stan zajmowanych pol przez statki na 1
     */
    public void battleRecalculate() {
        for (Square s : inWhatSquare) {
            s.setState(1);
        }
    }

    /**
     * Dodanie pola do listy pol zajmowanych przez statek jak i pol w jego sasiedztwie
     *
     * @param s
     */
    public void setInWhatSquare(Square s) {
        inWhatSquare.add(s);
    }

    /**
     * Wyczysszczenie listy zajmowanych pol = uzycie: Przeneczoenie, podniesienie, rotacja
     * powrot statku na poczatkowe miejsca
     */
    public void clearInWhatSquare() {
        inWhatSquare.clear();
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void decreaseWorkingParts(ArrayList<Square> squareList) {
        this.workingParts = this.workingParts - 1;
        if (workingParts == 0) {
            destroyed = true;
            for (int i = 0; i < inWhatSquare.size(); i++) {
                for (int j = 0; j < squareList.size(); j++) {
                    if (inWhatSquare.get(i).getState() == -1 && (inWhatSquare.get(i).getyID() +
                            inWhatSquare.get(i).getxID()).equals(squareList.get(j).getyID() +
                            squareList.get(j).getxID())) {

                        squareList.get(j).setState(3);
                        squareList.get(j).setFill(true);
                    }
                }
            }
        }
    }

    /**
     * otrzymanie listy z zajmowanymi polami przez dany statek
     *
     * @return
     */
    public ArrayList<Square> getInWhatSquare() {
        return inWhatSquare;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getLength() {
        return length;
    }

    public boolean isOrientation() {
        return orientation;
    }

    public float getAdjastmentX() {
        return adjastmentX;
    }

    public float getAdjastmentY() {
        return adjastmentY;
    }

    /**
     * Zwraca orientacje statku w postaci String. Do przekazania jako informacja w intencji
     *
     * @return
     */
    public String getStringOrientation() {
        String stringOrientation = "";
        if (orientation) {
            stringOrientation = "true";
        } else {
            stringOrientation = "false";
        }
        return stringOrientation;
    }

    /**
     * Spreparowany toString aby odpowiednio przeslac informacje za pomoca intencji
     *
     * @return
     */
    @Override
    public String toString() {
//        System.out.println("Lista: " + inWhatSquare);
        String returnValue = "";
        for (Square sq : inWhatSquare) {
            if (sq.getState() == 1) {
                returnValue += sq.toString();
            }
        }
        returnValue += getStringOrientation();
        //return "X: " + x + " Y: " + y + " Length: " + length + " Orientation :" + orientation+";";
        return returnValue;
    }

}
