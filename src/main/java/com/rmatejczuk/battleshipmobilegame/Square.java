package com.rmatejczuk.battleshipmobilegame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by Robert Matejczuk
 * Jest to klasa ktora przechowuje informacje na temat jednego pola gry
 */
public class Square {
    private float x; // wspolrzedna x prawo/lewo im bardziej w prawo tym wieksza wartosc
    private float y; // wspolrzedna y gora/dol im nizej tym wieksza wartosc
    private float width; // wielkosc boku jednego pola (jest to kwadrat). Obliczana wg wielkosci urzadzenia
    private int state; // stan pola: puste, trafione, pudlo
    private String xID; // wspolrzedna x jako wspolrzedne pola 1-10
    private String yID; // wspolrzedna y jako wspolrzedne pola A-J
    private Ship ship; // rodzaj statku (1 masztowie, 2 masztowiec etc.)
    private boolean fill;
    private boolean fingerHover; // na potrzeby bitwy gdy dotykamy pole palcem
    private int fieldValue; // do ocenienia przez Androida wartosci tego pola

    public Square(float x, float y, float width, int state, String xID, String yID) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.state = state;
        this.xID = xID;
        this.yID = yID;
        fill = false;
        fingerHover = false;
    }

    /**
     * Funkcja ktora przypisuje do danego pola statek
     *
     * @param ship
     */
    public void setShipIfInSquare(Ship ship) {
        this.ship = ship;
    }

    public Ship getShipFromSquareHit() {
        return this.ship;
    }

    public void removeShipFromSquare() {
        ship = null;
    }

    /**
     * Rysowanie siatki
     *
     * @param canvas jest to obszar, miejsce po ktorym mozemy rysowac
     * @param paint  jest to zbior danych i parametrow ktore okreslaja sposob w jaki rysujemy po canvas
     */
    public void draw(Canvas canvas, Paint paint) {
//        paint.setColor(Color.BLUE);
//        paint.setTextSize(30);
//        canvas.drawText(this.state + "", this.x + 0.5f * width, this.y + 0.5f * width, paint);
        //canvas.drawText((int) this.x + "x" + (int) this.y + "", this.x + 0.5f * width, this.y, paint);
        if (fill || fingerHover) {
            if (state == -1) {
                if (fingerHover) {
                    paint.setColor(Color.YELLOW);
                } else {
                    paint.setColor(Color.RED);
                }
            } else if (state == 0 || state == 1) {
                paint.setColor(Color.YELLOW);
            } else if (state == 2) { // trafiony
                paint.setColor(Color.RED);
            } else if (state == 3) { // pudlo
                paint.setColor(Color.BLUE);
            }
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(x, y, x + width, y + width, paint);
        }
//        paint.setColor(Color.YELLOW);
//        paint.setTextSize(30);
//        canvas.drawText(this.state + "", this.x + 0.5f * width, this.y + 0.5f * width, paint);

        paint.setStyle(Paint.Style.STROKE); // rysowanie bez wypelnienia
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2); // gdubisc linii
        canvas.drawRect(x, y, x + width, y + width, paint); // rysowanie kwadratow i tworzenie planszy
    }

    public boolean findSquareObjectOfID(String stringID) {
        if (stringID.equals(yID + xID + "")) {
            return true;
        }
        return false;
    }

    /**
     * Sprawdzenie czy dany punkt znajduje sie w polu
     *
     * @param xx
     * @param yy
     * @param adjastmentX
     * @param adjastmentY
     * @return
     */
    public boolean isHovered(float xx, float yy, float adjastmentX, float adjastmentY) {
        if (xx + adjastmentX > x && yy + adjastmentY > y && xx + adjastmentX < x + width && yy + adjastmentY < y + width) {
            return true;
        } else {
            return false;
        }
    }

    public void setFill(boolean fill) {
        this.fill = fill;
    }

    public void setFingerHover(boolean fingerHover) {
        this.fingerHover = fingerHover;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String getxID() {
        return xID;
    }

    public String getyID() {
        return yID;
    }

    public int getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(int fieldValue) {
        this.fieldValue = fieldValue;
    }

    /**
     * Przeciazona metoda w celu zwrocenia czytelnych informacji do przekazania w intencji
     *
     * @return
     */
    @Override
    public String toString() {
        return yID + xID + "!";
    }
}
