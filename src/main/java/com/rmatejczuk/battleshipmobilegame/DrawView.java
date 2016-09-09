package com.rmatejczuk.battleshipmobilegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Robert Matejczuk
 * Klasa DrawView odpowiada za obszar w ukladzie na ktorym bedziemy rysowac i umieszczac
 * plansze (siatke 10x10) oraz statki
 * Dziedziczy ona po klasie View gdyz kazdy obiekt w ukladzie musi dziedziczyc po tej klasie
 */
public class DrawView extends View {
    private Paint paint = new Paint();
    private PlayField playField;
    private String drawType = "PREPARE";

    public DrawView(Context context) {
        super(context);
    }

    // Wysterczy 1 i 2 konstruktor. 3 jako dodatek na wszelki wypadek
    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Brak skalowania do wysokosci
     * Metoda dzieli dlugosc obszaru przeznaczonego na rysowanie na ustalona ilosc czesci
     *
     * @return zwraca dlugosc
     */
    public float getSquareWidth(boolean battle) {
        // getWidth() zwraca szerokosc obszaru, dzielimy go na 19 rownych czesci aby pomiescic
        // plansze i statki oraz aby wszystko bylo tej samej dlugosci
        if (battle) {
            float width = getWidth() / 11.0f;
            return width;
        } else {
            float width = getWidth() / 19.0f;
            return width;
        }
    }

    /**
     * Rysowanie obszaru na ktorym znajduja sie statki oraz plansza
     *
     * @param canvas jest to obszar, miejsce po ktorym mozemy rysowac
     */
    @Override
    protected void onDraw(Canvas canvas) {

        if (playField != null) {
            playField.draw(canvas, paint, drawType);
        }
    }

    /**
     * Setter do ustawienia zmiennej setPlayfield odpowiedajacej za ustawienie referencji do obiektu
     * zawierajacego informacje na temat statkow i pol/obszarow gry.
     *
     * @param playField obiekt klasy PlayField porzechowujacy informacje na temat statkow i siatki
     */
    public void setPlayfield(PlayField playField) {
        this.playField = playField;
    }

    /**
     * Do usstawienia odpowiedniego rysowania (Przygotowanie statkow - false, bitwa - true)
     *
     * @param drawType
     */
    public void setDrawType(String drawType) {
        this.drawType = drawType;
    }
}
