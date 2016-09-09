package com.rmatejczuk.battleshipmobilegame;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by Robert Matejczuk
 * Jest to klasa ktora przechowuje informacje o obiektach klasy Ship oraz obiektach klasy Square
 * Za jej pomoca ustalamy jak maja wygladac statki i siatka do gry (rozmiary, kolory etc.)
 * Dane przechowywane sa w dwoch ArrayList'ach
 */
public class PlayField {
    private ArrayList<Square> squareList = new ArrayList<>(); // lista pol planszy
    private ArrayList<Ship> shipList = new ArrayList<>(); // lista statkow
    private Ship lastTouchedShip; // obiekt statku ktory zostal dotkniety jako ostatni


    /**
     * Konstruktor inicjujacy plansze dla Androida
     *
     * @param androidAI
     * @param width
     * @param initialX
     * @param initialY
     */
    public PlayField(AndroidAI androidAI, float width, float initialX, float initialY) {
        // Tworzenie siatki do gry i zapamietanie kazdego pola w liscie squareList
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                float x = initialX + i * width; // poczatkowa wartosc + nr kratki * szerokosc
                float y = initialY + j * width;
                Square square = new Square(x, y, width, 0, String.valueOf(i + 1), numberToLetter(j));
                squareList.add(square);
            }
        }

        androidAI.setAndroidSquareList(squareList);
        androidAI.setShipPlacement(4, width);

        androidAI.setShipPlacement(3, width);
        androidAI.setShipPlacement(3, width);

        androidAI.setShipPlacement(2, width);
        androidAI.setShipPlacement(2, width);
        androidAI.setShipPlacement(2, width);

        androidAI.setShipPlacement(1, width);
        androidAI.setShipPlacement(1, width);
        androidAI.setShipPlacement(1, width);
        androidAI.setShipPlacement(1, width);
        squareList = androidAI.getAndroidSquareList();
    }

    /**
     * Konstruktor PlayField stworzony do przetwarzania danych z intencji ktora zawiera informacje
     * o rozmiesszczeniu statkow przez gracza z fazie przygotowan
     *
     * @param playFieldString
     * @param width
     * @param initialX
     * @param initialY
     */
    public PlayField(String playFieldString, float width, float initialX, float initialY) {
        // Tworzenie siatki do gry i zapamietanie kazdego pola w liscie squareList
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                float x = initialX + i * width; // poczatkowa wartosc + nr kratki * szerokosc
                float y = initialY + j * width;
                Square square = new Square(x, y, width, 0, String.valueOf(i + 1), numberToLetter(j));
                squareList.add(square);
            }
        }

        String[] ships = playFieldString.split(";");
        for (int i = 0; i < ships.length; i++) {
            shipList.add(new Ship(ships[i], squareList, width));
        }
    }

    /**
     * Tworzenie siatki do gry
     *
     * @param width    dlugosc kratki(boku) obliczana w zaleznosci od szerokosci pola DrawView
     * @param initialX poczatkowa wartosc wspolrzednej X
     * @param initialY poczatkowa wartosc wspolrzednej Y
     */
    public PlayField(float width, float initialX, float initialY) {
        // Tworzenie siatki do gry i zapamietanie kazdego pola w liscie squareList
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                float x = initialX + i * width; // poczatkowa wartosc + nr kratki * szerokosc
                float y = initialY + j * width;
                Square square = new Square(x, y, width, 0, String.valueOf(i + 1), numberToLetter(j));
                squareList.add(square);
            }
        }

        // 11*width to odleglosc statku rysowanego od siatki, width to dlugosc jednego kwadrata
        // wiec 11*width to jest jedna kratka odleglosci od siatki
        // *** 1 masztowce x4
        Ship s11 = new Ship(11 * width, 2 * width, 1, width);
        Ship s12 = new Ship(13 * width, 2 * width, 1, width);
        Ship s13 = new Ship(15 * width, 2 * width, 1, width);
        Ship s14 = new Ship(17 * width, 2 * width, 1, width);

        // *** 2 masztowce x3
        Ship s21 = new Ship(11 * width, 4 * width, 2, width);
        Ship s22 = new Ship(14 * width, 4 * width, 2, width);
        Ship s23 = new Ship(17 * width, 4 * width, 2, width);

        // *** 3 masztowce x2
        Ship s31 = new Ship(11 * width, 6 * width, 3, width);
        Ship s32 = new Ship(15 * width, 6 * width, 3, width);

        // *** 4 masztowiec
        Ship s41 = new Ship(12 * width, 8 * width, 4, width);

        // Dodawanie statkow do listy
        shipList.add(s11);
        shipList.add(s12);
        shipList.add(s13);
        shipList.add(s14);
        shipList.add(s21);
        shipList.add(s22);
        shipList.add(s23);
        shipList.add(s31);
        shipList.add(s32);
        shipList.add(s41);

    }

    /**
     * Metoda zamieniajaca wartosc liczbowa na literke w celu nadania identyfiaktora dla pola A-J
     *
     * @param i wartosc liczbowa
     * @return String bedacy literka odpowiadajaca wartosci liczbowej
     */
    private String numberToLetter(int i) {
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

    /**
     * Wywolanie metody draw dla kazdego statku
     *
     * @param canvas   miejsce w ktorym maja byc narysowane statki
     * @param paint    okreslone warunki i sposoby rysowania
     * @param drawType okresla jaki typ rysowania ma byc zastosowany
     */
    public void draw(Canvas canvas, Paint paint, String drawType) {
        if (drawType.equals("BATTLE")) {
            for (Ship s : shipList) {
                s.draw(canvas, paint);
                //System.out.println(s);
            }

            for (Square s : squareList) {
                s.draw(canvas, paint);
            }
        } else if (drawType.equals("PREPARE")) {
            for (Square s : squareList) {
                s.draw(canvas, paint);
            }

            for (Ship s : shipList) {
                s.draw(canvas, paint);
                //System.out.println(s);
            }
        } else if (drawType.equals("ANDROID")) {
            for (Square s : squareList) {
                s.draw(canvas, paint);
            }
        }
    }

    public void resetSquareCovering() {
        for (Square s : squareList) {
            s.setFill(false);
        }
    }

    public void checkSquareCovering(float xx, float yy, int length, float width, boolean orientation) {
        resetSquareCovering();
        if (orientation) {
            for (int i = 0; i < length; i++) {
                float wspY = yy + width * (float) i;
                for (Square s : squareList) {
                    if (s.isHovered(xx, wspY, getLastShip().getAdjastmentX(), getLastShip().getAdjastmentY())) {
                        s.setFill(true);
                    }
                }
            }
        } else {
            for (int i = 0; i < length; i++) {
                float wspX = xx + width * (float) i;
                for (Square s : squareList) {
                    if (s.isHovered(wspX, yy, getLastShip().getAdjastmentX(), getLastShip().getAdjastmentY())) {
                        s.setFill(true);
                    }
                }
            }
        }
    }

    public void resetFingerSquareCovering() {
        for (Square s : squareList) {
            s.setFingerHover(false);
        }
    }

    public void checkFingerSquareCovering(float xx, float yy, float width) {
        resetFingerSquareCovering();
        for (Square s : squareList) {
            if (s.isHovered(xx, yy, 0, 0)) {
                s.setFingerHover(true);
                //System.out.println("!!!Pole: " + s.getyID()+s.getxID());
            }
        }
    }

    public void aligmentShipToSquare(Ship ship) {
        for (Square s : squareList) {
            if (s.isHovered(ship.getX(), ship.getY(), ship.getAdjastmentX(), ship.getAdjastmentY())) {
                ship.moveTo(s.getX(), s.getY());
            }
        }
    }

    public void setOccupiedSquare(Ship ship, float width) {
        //int shipFieldsPlaced = 0;
        if (ship.isOrientation()) { // dla pionowych statkow
            // oznaczanie pol jako niedostepnych w okol statku
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < ship.getLength() + 2; j++) {
                    for (Square s : squareList) {
                        if (s.isHovered(ship.getX() - width * 0.5f + i * width, ship.getY() - width * 0.5f + j * width, 0, 0)) {
                            ship.setInWhatSquare(s);
                            s.setState(-1);
                        }
                    }
                }
            }
            // ustawienia pol zajmowanych przez statek
            for (int i = 0; i < ship.getLength(); i++) {
                float wspY = ship.getY() + width * (float) i;
                for (Square s : squareList) {
                    if (ship.getX() + 0.2f > s.getX() && wspY + 0.2f > s.getY() && ship.getX() - 0.2f + width < s.getX() + width && wspY - 0.2f + width < s.getY() + width) {
                        ship.setInWhatSquare(s);
                        s.setShipIfInSquare(ship);
                        s.setState(1);
                        //System.out.println("!!!!!!!!!!!! Wielkosc listy pol: " + ship.getInWhatSquare().size());
                    }
                }
            }
            // sasiednie pola dodawane na podstawie zajetych pol przez statek

        } else { // dla poziomych statkow
            // oznaczanie pol jako niedostepnych w okol statku
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < ship.getLength() + 2; j++) {
                    for (Square s : squareList) {
                        if (s.isHovered(ship.getX() - width * 0.5f + j * width, ship.getY() - width * 0.5f + i * width, 0, 0)) {
                            ship.setInWhatSquare(s);
                            s.setState(-1);
                        }
                    }
                }
            }
            // ustawienia pol zajmowanych przez statek
            for (int i = 0; i < ship.getLength(); i++) {
                float wspX = ship.getX() + width * (float) i;
                for (Square s : squareList) {
                    if (wspX + 0.2f > s.getX() && ship.getY() + 0.2f > s.getY() && wspX - 0.2f + width < s.getX() + width && ship.getY() - 0.2f + width < s.getY() + width) {
                        ship.setInWhatSquare(s);
                        s.setShipIfInSquare(ship);
                        s.setState(1);
                        //System.out.println("!!!!!!!!!!!! Wielkosc listy pol: " + ship.getInWhatSquare().size());
                    }
                }
            }
        }
    }

    public void clearOccupiedSquare(Ship ship) {
        if (ship != null) {
            for (int i = 0; i < ship.getInWhatSquare().size(); i++) {
                ship.getInWhatSquare().get(i).removeShipFromSquare();
                ship.getInWhatSquare().get(i).setState(0);
            }
            ship.clearInWhatSquare();
        }
    }

    /**
     * Metoda ktora dla kazdego statku wywoluje metode recalculate odpowiadajaca za usuniecie
     * duplikatow zajmowanych pol przez statek
     */
    public void recalculateShipSquares() {
        for (Ship sp : shipList) {
            sp.recalculate();
        }
    }

    public void recalculateShipInBattle(float width) {
        for (Square s : squareList) {
            s.removeShipFromSquare();
        }
        for (Ship sp : shipList) {
            setOccupiedSquare(sp, width);
        }
    }

    /**
     * Sprawdzamy czy dany statek moze byc umieszczony na upuszczonych polach.
     * Jezeli pole na ktorym chcemy umiescic statek ma stan -1 oznacza to, ze jest w obrebie juz
     * innego lezacego statku. Natomiast jesli pole posiada stan o wartosci 1 oznacza to, ze
     * pole jest zajete przez inny statek
     *
     * @param ship
     * @param width
     * @return
     */
    public boolean shipIsPlaced(Ship ship, float width) {
        for (int i = 0; i < ship.getLength(); i++) {
            for (Square s : squareList) {
                if (ship.isOrientation()) { // pionowo
                    if (s.isHovered(ship.getX() + width * 0.2f, ship.getY() + width * 0.2f + i * width, 0, 0)) {
//                        System.out.println("!!!!!!!!!!!!!!!!!!************!!!!!!!!****** "+ s.getxID()+" "+s.getyID());
//                        System.out.println("!!!!!!!!! STAN: " + s.getState());
                        if (s.getState() == -1 || s.getState() == 1) {
                            return false;
                        }
                    }
                } else { // poziomo
                    if (s.isHovered(ship.getX() + width * 0.2f + i * width, ship.getY() + width * 0.2f, 0, 0)) {
                        if (s.getState() == -1 || s.getState() == 1) {
                            return false;
                        }
//                        System.out.println("!!!!!!!!!!!!!!!!!!************!!!!!!!!****** "+ s.getxID()+" "+s.getyID());
                    }
                }
            }
        }
        return true;
    }

    /**
     * Metoda oddawania strzalow przez gracza. Sprawdza czy wskazany punkt dotyka jakiegos pola
     * jesli tak to sprawdza czy w dane pole gracz juz nie oddawal strzalu (za pomoca sprawdzenia
     * czy pole ma status -1 - otoczka statku, 0 - puste pole, 1 - statek) jesli ma status
     * 2 - trafiony badz 3 - pudlo to zostaje zignorowane i ignoruje touchEvent nie przekazujac
     * kolejki do Androida.
     * Przekazuje tez informacje o miejscach dokonania strzalu do Androida aby ten mogl sobie je
     * zapisac do listy ktore potem zostana przeslane do bazy danych
     *
     * @param xx
     * @param yy
     * @param width
     * @param androidAI
     * @return
     */
    public boolean isSquareHit(float xx, float yy, float width, AndroidAI androidAI) {
        for (Square s : squareList) {
            if (xx > s.getX() && yy > s.getY() && xx < s.getX() + width && yy < s.getY() + width) {
                if (s.getState() == -1 || s.getState() == 0) {
                    s.setState(3); // pudlo
                    s.setFill(true);
                    // zapis informacji do bazy danych o strzale gracza
                    androidAI.setListOfPlayerShotsDB(s);
                    //System.out.println("PUDLO!");
                    androidAI.setPlayerTurn(false);
                    androidAI.setPlayerShot(true);
                    return false;
                } else if (s.getState() == 1) {
                    s.setState(2); // trafiony
                    s.setFill(true);
                    // zapis informacji do bazy danych o strzale gracza
                    androidAI.setListOfPlayerShotsDB(s);
                    // zmniejszenie dzialalnosci statku
                    s.getShipFromSquareHit().decreaseWorkingParts(androidAI.getAndroidSquareList()); // czysci square list
                    if (s.getShipFromSquareHit().isDestroyed()) {
                        androidAI.getAndroidShipsList().remove(s.getShipFromSquareHit());
                        squareList = androidAI.getAndroidSquareList();
                        //System.out.println("STATEK ANDROIDA ZNISZCZONY! Rozmiar listy stakow: " + androidAI.getAndroidShipsList().size());
                    }

                    //System.out.println("TRAFIONY!");

                    if (androidAI.getAndroidShipsList().size() == 0) {
                        System.out.println(" KONIEC GRY!!");
                        androidAI.setBattleEnded(true);
                        androidAI.setPlayerWin(true); // gracz wygrywa
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean getAndroidShot(AndroidAI androidAI) {
        Square androidSquare = androidAI.androidShot();
//        System.out.println("ANDROID STRZELA W :" + androidSquare.getyID() + androidSquare.getxID() + "STATUS: " + androidSquare.getState());
        for (Square s : squareList) {
            if ((s.getyID() + s.getxID()).equals(androidSquare.getyID() + androidSquare.getxID())) {
                if (s.getState() == 1) {
                    s.setState(2); // trafiony
                    s.setFill(true);
                    androidAI.setPlayerSquareListToShotState(s, 2, true, false); // aktualizacja
                    androidAI.setListOfPlayerShipsDB(s); // Informacja do bazy danych o znalezionym statku
                    s.getShipFromSquareHit().decreaseWorkingParts(squareList);
                    //System.out.println("statek z pola " + s.getShipFromSquareHit());
                    if (s.getShipFromSquareHit().isDestroyed()) {
                        androidAI.setPlayerSquareListToShotState(s, 2, true, true);
                        // jezeli statek zostaje zniszczony to aktualizuj playerSquareListToShot aby android widzial otocze statku jako wystrzelana
                        shipList.remove(s.getShipFromSquareHit());
                        //System.out.println("STATEK GRACZA ZOSTAL ZNISZCZONY! Rozmiar listy stakow: " + shipList.size());
                    }
                    if (shipList.size() == 0) {
                        System.out.println(" KONIEC GRY!!");
                        androidAI.setBattleEnded(true);
                        androidAI.setPlayerWin(false); // Android wygrywa
                        return false;
                    }
                    return true;
                } else {
                    s.setState(3);
                    s.setFill(true); // pudlo
                    androidAI.setPlayerSquareListToShotState(s, 3, true, false);
                    androidAI.setPlayerTurn(true);
                    androidAI.setAndroidShot(true);
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Sprawdza czy wszystkie statki zostaly rozmieszczone
     *
     * @return true jesli tak false jesli nie
     */

    public boolean isAllShipPlaced() {
        for (Ship s : shipList) {
            if (s.getInWhatSquare().size() == 0) {
                return false;
            }
        }
        return true;
    }


    /**
     * Zwrocenie obiektu dotknietego statku
     *
     * @param x wspolrzedna x
     * @param y wspolrzedna y
     * @return zwraca obiekt statku
     */

    public Ship getTouchedShip(float x, float y) {
        for (Ship s : shipList) {
            if (s.isTouched(x, y)) {
                setLastShip(s);
                return s;
            }
        }
        return null;
    }

    /**
     * Otrzymaj informacje o ostatnim dotknietym statku
     *
     * @return
     */
    public Ship getLastShip() {
        return lastTouchedShip;
    }

    /**
     * Ustaw obiekt do ostatnio dotknietego statku
     *
     * @param ship
     */
    public void setLastShip(Ship ship) {
        lastTouchedShip = ship;
    }

    /**
     * Przekazywane informacje w intencji na temat pol zajmowanych przez statki
     *
     * @return
     */
    @Override
    public String toString() {
        String returnValue = "";
        for (Ship sp : shipList) {
            returnValue += sp.toString() + ";";
        }
        return returnValue;
    }
}
