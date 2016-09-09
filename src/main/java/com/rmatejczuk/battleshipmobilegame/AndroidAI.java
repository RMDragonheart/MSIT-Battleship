package com.rmatejczuk.battleshipmobilegame;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Sztuczna inteligencja Androida ktora laczy sie z baza danych SQLite w celu podejmowania
 * odpowiednich trafnych krokow dzialania.
 * Lsita statkow Androida zmienia sie tylko w obiekcie Androida. Jego pola sa przekazywane do
 * PlayField i na liscie squareList w obiekcie odpowiadajacym zarzadzaniu ruchami Androida znajduje
 * sie jego aktualna lista pol
 */
public class AndroidAI {
    // Zbior informacji z bazy danych na temat strzalow gracza.
    // Sluzy do pomocy przy ustawieniach statkow przez Androida
    private HashMap<String, Integer> mapWhereToPlace;
    // Zbior informacji z bazy danych na temat rozmieszczen statku przez  gracza.
    // Sluzy do pomocy przy podejmowaniu decysji strzalu przez Androida
    private HashMap<String, Integer> mapWhereToShot;

    private ArrayList<Ship> androidShipsList; // Lista statkow Androida (ile zostalo i zniszczenia)
    private ArrayList<Square> androidSquareList; // Lista siatki Androida  (ktore pola zajete)
    private ArrayList<Square> playerSquareListToShot; // lista pol po ktorych moze strzelac Android
    private ArrayList<Square> listOfPlayerShotsDB; // zapamietanie w ktore pola strzelal gracz i pozniejsza aktualizacja tych ktore maja wartosc 1.
    private ArrayList<Square> listOfPlayerShipsDB; // lista zapamietujaca gdzie znajdowaly sie statki przeciwnika wykryte(trafione) przez Adnroida
    private Random randomShipPlacement; // Pomocnik do podejmowania decyzji gdy istnieje wiecej mozliwosci rozmieszczenia
    private Random randomShoot; // Pomocnik do podejmowania decyzji gdy istnieje wiecej mozliwosci oddania strzalu
    private SQLiteDatabase db; // obiekt za pomoca ktorego laczymy sie z baza danych
    private boolean playerTurn = false;
    private boolean battleEnded = false;
    private boolean playerWin;
    private boolean playerShot = false;
    private boolean androidShot = false;
    private int roundCounter = 0;

    public AndroidAI(SQLiteDatabase db) {
        mapWhereToPlace = new HashMap<>();
        mapWhereToShot = new HashMap<>();
        androidShipsList = new ArrayList<>();
        androidSquareList = new ArrayList<>();
        playerSquareListToShot = new ArrayList<>();
        listOfPlayerShotsDB = new ArrayList<>();
        listOfPlayerShipsDB = new ArrayList<>();
        randomShipPlacement = new Random();
        randomShoot = new Random();

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                playerSquareListToShot.add(new Square(0, 0, 0, 0, String.valueOf(i + 1), numberToLetter(j)));
                listOfPlayerShotsDB.add(new Square(0, 0, 0, 0, String.valueOf(i + 1), numberToLetter(j)));
                listOfPlayerShipsDB.add(new Square(0, 0, 0, 0, String.valueOf(i + 1), numberToLetter(j)));
            }
        }
        this.db = db;
        getDataForWhereToPlace();
        getDataForWhereToShot();
        initializePlayerSquareListToShot();
    }

    /**
     * Zamkniecie polaczenia z baza danych
     */
    public void closeDBConnecion() {
        db.close();
    }

    /**
     * Metoda pobierajaca informacje z bazy danych odnosnie miejsc gdzie gracz najczesciej strzela
     * i na podstawie tego Android bedzie rozmieszczal statki
     */
    private void getDataForWhereToPlace() {
        Cursor cursor = db.query("PLAYER_SHOTS",
                new String[]{"_id", "SHOTS"},
                null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                mapWhereToPlace.put(cursor.getString(0), cursor.getInt(1));
            }
        }
        cursor.close();
    }

    /**
     * Metoda pobierajaca informacje z bazy danych odnosnie miejsc gdzie gracz potencjalnie moze
     * miec rozmieszczone statki i na podstawie tego Android bedzie podejmowal decyzje gdzie ma
     * oddac strzal
     */
    private void getDataForWhereToShot() {
        Cursor cursor = db.query("PLAYER_SHIPS",
                new String[]{"_id", "OCCURRENCE"},
                null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                mapWhereToShot.put(cursor.getString(0), cursor.getInt(1));
            }
        }
        cursor.close();
    }

    /**
     * listOfPlayerShotsDB -> Zawiera informacje o strzalach gracza, aktualizacja danych gdzie umieszczac
     * mapWhereToPlace - > Wartosci dla pol na postawie strzalow gracza
     * PLAYER_SHOTS _id SHOTS
     */
    public void updateDBWhereToPlace() {
        int fieldValue;

        for (Square s : listOfPlayerShotsDB) {
            if (s.getState() == 1) {
                fieldValue = mapWhereToPlace.get(s.getyID() + s.getxID());
                fieldValue++;

                ContentValues values = new ContentValues();
                values.put("SHOTS", fieldValue);

                db.update("PLAYER_SHOTS", values, "_id = ?", new String[]{s.getyID() + s.getxID()});
            }
        }
    }

    /**
     * listOfPlayerShipsDB -> Zawiera informacje o znalezionych statkach, aktualizacja miejsc statkow
     * mapWhereToShot -> Wartosci dla pol na podstawie znalezionych statkow
     * PLAYER_SHIPS _id OCCURRENCE
     */
    public void updateDBWhereToShot() {
        int fieldValue;

        for (Square s : listOfPlayerShipsDB) {
            if (s.getState() == 1) {
                fieldValue = mapWhereToShot.get(s.getyID() + s.getxID());
                fieldValue++;

                ContentValues values = new ContentValues();
                values.put("OCCURRENCE", fieldValue);

                db.update("PLAYER_SHIPS", values, "_id = ?", new String[]{s.getyID() + s.getxID()});
            }
        }
    }

    /**
     * Metoda aktualizujaca historie gier
     * Table GAMES_HISTORY
     * _id INTEGER AUTOINCREMENT
     * WINNER TEXT
     * HOW_MANY_TURNS INTEGER
     */
    public void updateDBGameHistory(boolean playerWin, int rounds) {
        ContentValues values = new ContentValues();
        if (playerWin) {
            values.put("WINNER", "PLAYER");
        } else {
            values.put("WINNER", "ANDROID");
        }
        values.put("HOW_MANY_TURNS", rounds);

        db.insert("GAMES_HISTORY", null, values);
    }


    /**
     * Inicjalizuje wartosci pol dla potencjalnych celow i sortuje liste
     */
    private void initializePlayerSquareListToShot() {
        String key;
        ArrayList<Square> helperSquareList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                key = numberToLetter(i) + String.valueOf(j + 1);
                for (Square s : playerSquareListToShot) {
                    if (s.findSquareObjectOfID(key)) {
                        s.setFieldValue(mapWhereToShot.get(key));
                        helperSquareList.add(s);
                    }
                }
            }
        }
        playerSquareListToShot.clear();
        playerSquareListToShot.addAll(helperSquareList); // Lista jest posegregowana A1, A2, A3
    }

    /**
     * GLOWNA LOGIKA MYSLENIA ANDROIDA PRZY ROZSTAWIANIU STATKOW
     * androidSquareList juz posiada informacje o polach utworzonych w widoku pol dla Androida
     * counter2++ cofa przechodzenie po HashMapie w celu przeszukania wszystkich kombinacji pol
     * przyklad: A1 A2 A3 A4 counter1 zglasza, ze zsumowal ilosc pol odpowiadajaca dlugosci statku
     * i counter2 ustawia sie jako wartosc j.
     * Przeszukujemy dzieki temu A2 A3 A4 A5 a nie A5 A6 A7 A8.
     *
     * @return
     */
    public void setShipPlacement(int length, float width) {
        int shipLength = length;
        String key;
        int shotsSum;
        int counter1; // odmierza cykl sumowania zmiennej shots odpowiednio do dolugosci statku
        int counter2;
        int minimum = Integer.MAX_VALUE;
        ArrayList<Square> helperList = new ArrayList<>();
        ArrayList<Square> potentialSquares = new ArrayList<>();
        ArrayList<Boolean> orientationList = new ArrayList<>();

        // **************************************************
        // sprawdzamy orientacje pozioma (orientation = false)
        // **************************************************
        for (int i = 0; i < 10; i++) {
            counter1 = 0;
            counter2 = 0;
            helperList.clear();
            shotsSum = 0;
            for (int j = 0; j < 10; j++) {
                key = numberToLetter(i) + String.valueOf(j + 1);
                for (Square s : androidSquareList) {
                    if (s.findSquareObjectOfID(key)) {
                        if (s.getState() == 0) {
                            helperList.add(s);
                            shotsSum += mapWhereToPlace.get(key);
                            counter1++;
                            break;
                        } else {
                            // resetowanie gdyz obiekt jest zajety i statek nie moze byc umieszczony w tym polu
                            counter1 = 0;
                            counter2++;
                            shotsSum = 0;
                            helperList.clear();
                            break;
                        }
                    }
                }

                // tutaj trafiam po break;
                if (counter1 == shipLength) {
                    counter1 = 0;
                    counter2++;
                    j = counter2 - 1;

                    if (minimum > shotsSum) {
                        minimum = shotsSum;
                        potentialSquares.clear();
                        orientationList.clear();
                        potentialSquares.addAll(helperList);
                        orientationList.add(false);
                        shotsSum = 0;
                        helperList.clear();
                    } else if (minimum == shotsSum) {
                        potentialSquares.addAll(helperList);
                        orientationList.add(false);
                        shotsSum = 0;
                        helperList.clear();
                    }
                }
            }
        }

        // **************************************************
        // Sprawdzamy orientacje pionowa (orientation = true)
        // **************************************************
        for (int i = 0; i < 10; i++) {
            counter1 = 0;
            counter2 = 0;
            helperList.clear();
            shotsSum = 0;
            for (int j = 0; j < 10; j++) {
                key = numberToLetter(j) + String.valueOf(i + 1); // zmiana w przeszukiwaniu pionowo
                for (Square s : androidSquareList) {
                    if (s.findSquareObjectOfID(key)) {
                        if (s.getState() == 0) {
                            helperList.add(s);
                            shotsSum += mapWhereToPlace.get(key);
                            counter1++;
                            break;
                        } else {
                            // resetowanie gdyz obiekt jest zajety i statek nie moze byc umieszczony w tym polu
                            counter1 = 0;
                            counter2++;
                            shotsSum = 0;
                            helperList.clear();
                            break;
                        }
                    }
                }

                // tutaj trafiam po break;
                if (counter1 == shipLength) {
                    counter1 = 0;
                    counter2++;
                    j = counter2 - 1;

                    if (minimum > shotsSum) {
                        minimum = shotsSum;
                        potentialSquares.clear();
                        orientationList.clear();
                        potentialSquares.addAll(helperList);
                        orientationList.add(true); // zmiana zapamietywanej orientacji
                        shotsSum = 0;
                        helperList.clear();
                    } else if (minimum == shotsSum) {
                        potentialSquares.addAll(helperList); // zmiana zapamietywanej orientacji
                        orientationList.add(true);
                        shotsSum = 0;
                        helperList.clear();
                    }
                }
            }
        }
        // sprawdzenie ilosci potencjalnych miejsc i wylosowanie tych ktore beda wybrane.
        // dodanie statku do listy statkow Androida, ustawienie odpowiednich pol w AndroidsquareList na 1
        // oznaczajace, ze sa zajete.
        if (orientationList.size() > 0) {
            int randomNumber = randomShipPlacement.nextInt(orientationList.size());
            Ship ship = new Ship(orientationList.get(randomNumber), potentialSquares.get(randomNumber * length).getX(), potentialSquares.get(randomNumber * length).getY(), shipLength, width);
            androidShipsList.add(ship);
            for (int i = randomNumber * shipLength; i < randomNumber * shipLength + shipLength; i++) {
                for (Square s : androidSquareList) {
                    if ((s.getyID() + s.getxID()).equals(potentialSquares.get(i).getyID() + potentialSquares.get(i).getxID())) {
                        s.setState(1);
                    }
                }
            }
            setOccupiedSquare(ship, width);
        }
    }

    /**
     * Glowny algorytm i serce pracy magisterskiej. Sztuczna inteligencja ktora pozyskujac wiedze
     * ze wczesniejszych gier z graczem probuje z nim wygrac analizujac zebrane dane i podejmujac
     * trafna decyzje w oddawaniu strzalow
     *
     * @return
     */
    public Square androidShot() {
        int r;

        // Na początek sprawdzamy czy mamy jakis niedobity statek i probujemy go zniszczyc
        for (Square s : playerSquareListToShot) {
            if (s.getState() == 2) { // jesli mamy trafienie w statek
                if (!s.getShipFromSquareHit().isDestroyed()) { // jezeli statek nie jest zniszczony
                    System.out.println("Nie jest zniszczony!");
                    ArrayList<Square> squareHelper = new ArrayList<>(); // Tworzymy asystenta ktory zbierze dla nas informacje o potencjalnych celach

                    // **********************************************
                    // Sprawdzamy 4 mozliwosci
                    // najpierw idziemy na lewo od miejsca trafienia
                    // **********************************************
                    for (Square s1 : playerSquareListToShot) { // pole na lewo od trafionego
                        if ((s.getyID() + String.valueOf(Integer.parseInt(s.getxID()) - 1)).equals(s1.getyID() + s1.getxID())) { // sprawdzamy czy takie pole istnieje
                            if (s1.getState() == 2) { // jesli tak i ma wartosc 2 znaczy to ze znamy orientacje statku i sprawdzamy dalsza strone idac w lewo
                                for (Square s5 : playerSquareListToShot) { // sprawdzamy istnienie poladalej na lewo od trafionego
                                    if ((s.getyID() + String.valueOf(Integer.parseInt(s.getxID()) - 2)).equals(s5.getyID() + s5.getxID())) { // jesli istnieje to sprawdzamy jego status
                                        if (s5.getState() == 2) { // jesli pole jest odkryte i mamy trafienie to idziemy dalej w lewo
                                            for (Square s6 : playerSquareListToShot) { // sprzewdzamy istnienie pola na lewo
                                                if ((s.getyID() + String.valueOf(Integer.parseInt(s.getxID()) - 3)).equals(s6.getyID() + s6.getxID())) { // jezeli to pole istnieje to osiagamy dlugosc 4 masztowca przeszukiwan
                                                    if (s6.getState() == 2) { // nie powinno miec to miejsca, w przeciwnym razie statek powinien byc zniszczony
                                                        System.out.println("Algorytm nasz zawiodl!!!");
                                                    } else if (s6.getState() < 2) {
                                                        return s6; // ostatnie pole, jesli tam nie strzelalismy to strzelamy.
                                                    }
                                                }
                                            }
                                        } else if (s5.getState() < 2) { // jesli pole nie jest trafione i nie jest odkryte to strzelamy gdyż potencjalnie tutaj moze byc statek EWENTUALNIE, dodac do listy i potem losowo strzelac, ale wtedy juz tylko na prawo zrobic przeszukiwanie, bo innych opcji nie bedzie
                                            return s5;
                                        }
                                    } // jezeli pole s5 nie jestnieje to przerywamy przeszukiwanie na lewo
                                }
                            } else if (s1.getState() < 2) { // jezeli najblizsze pole na lewo nie ma trafienia to dodajemy je do listy pomocnika gdyz nie znamy jeszcze orientacji
                                squareHelper.add(s1);
                            }
                        } // jezeli pole s1 nie jestnieje to przerywamy przeszukiwanie na lewo
                    }

                    // ***********************************
                    // idziemy w gore od miejsca trafienia
                    // ***********************************
                    for (Square s2 : playerSquareListToShot) { // pole w gore od trafionego
                        if ((numberToLetter(letterToNumber(s.getyID()) - 1) + s.getxID()).equals(s2.getyID() + s2.getxID())) { // jezeli pole istnieje to sprawdzamy je
                            if (s2.getState() == 2) { // sprawdzamy czy jest trafienie
                                for (Square s7 : playerSquareListToShot) {
                                    if ((numberToLetter(letterToNumber(s.getyID()) - 2) + s.getxID()).equals(s7.getyID() + s7.getxID())) { // jezeli pole istnieje to sprawdzamy je
                                        if (s7.getState() == 2) { // sprawdzamy czy jest trafienie
                                            for (Square s8 : playerSquareListToShot) {
                                                if ((numberToLetter(letterToNumber(s.getyID()) - 3) + s.getxID()).equals(s8.getyID() + s8.getxID())) { // jezeli pole istnieje to sprawdzamy je
                                                    if (s8.getState() == 2) { // nie powinno miec to miejsca, w przeciwnym razie statek powinien byc zniszczony
                                                        System.out.println("Algorytm nasz zawiodl!!!");
                                                    } else if (s8.getState() < 2) { // ostatnie pole, jesli tam nie strzelalismy to strzelamy
                                                        return s8;
                                                    }
                                                } // jesli pole s8 nie istnieje to przerywamy przeszukiwanie w gore
                                            }
                                        } else if (s7.getState() < 2) { // jesli nie ma trafienia a pole istnieje i jest nieodkryte to strzelamy
                                            return s7;
                                        }
                                    } // jesli pole s7 nie istnieje to przerywamy przeszukiwanie w gore
                                }
                            } else if (s2.getState() < 2) { // jesli nie jest trafienie to dodajemy pole do listy pomocnika gdyz nie znamy jeszcze orientacji
                                squareHelper.add(s2);
                            }
                        }
                    }

                    // ***************
                    // idziemy w prawo
                    // ***************
                    for (Square s3 : playerSquareListToShot) { // pole na prawo od trafionego
                        if ((s.getyID() + String.valueOf(Integer.parseInt(s.getxID()) + 1)).equals(s3.getyID() + s3.getxID())) { // jezeli istnieje to sprawdzamy je
                            if (s3.getState() == 2) { // mamy trafienie szukamy dalej w prawo
                                for (Square s9 : playerSquareListToShot) { // szukamy pola o jedno pole wyzej
                                    if ((s.getyID() + String.valueOf(Integer.parseInt(s.getxID()) + 2)).equals(s9.getyID() + s9.getxID())) { // sprawdzamy istnienie pola
                                        if (s9.getState() == 2) { // sprawdzamy czy trafione
                                            for (Square s10 : playerSquareListToShot) {
                                                if ((s.getyID() + String.valueOf(Integer.parseInt(s.getxID()) + 3)).equals(s10.getyID() + s10.getxID())) { // jesli pole istnieje to je sprawdzamy
                                                    if (s10.getState() == 2) { // nie powinno miec to miejsca, w przeciwnym razie powinien byc zniszczony
                                                        System.out.println("Algorytm nasz zawiodl!!!");
                                                    } else if (s10.getState() < 2) { // ostatnie pole, jesli tam nie strzelalismy to strzelamy
                                                        return s10;
                                                    }
                                                } // jesli pole s10 nie istnieje to przerywamy przeszukiwanie w prawo
                                            }
                                        } else if (s9.getState() < 2) { // jesli pole nie zostalo odkryte to strzelamy
                                            return s9;
                                        }
                                    } // jesli pole s9 nie istnieje to przerywamy przeszukiwanie w prawo
                                }
                            } else if (s3.getState() < 2) { // pole jest nieodkryte wiec dodajemy je do listy pomocnika gdyz nie znamy jeszcze orientacji
                                squareHelper.add(s3);
                            }
                        }
                    }

                    // *************
                    // idziemy w dol
                    // *************
                    for (Square s4 : playerSquareListToShot) { // pole w dol od trafionego
                        if ((numberToLetter(letterToNumber(s.getyID()) + 1) + s.getxID()).equals(s4.getyID() + s4.getxID())) { // sprawdzamy czy pole istnieje
                            if (s4.getState() == 2) { // jesli trafienie to idziemy w dol
                                for (Square s11 : playerSquareListToShot) { // idziemy w dol
                                    if ((numberToLetter(letterToNumber(s.getyID()) + 2) + s.getxID()).equals(s11.getyID() + s11.getxID())) {
                                        if (s11.getState() == 2) { // pole jest trafione idziemy dalej
                                            for (Square s12 : playerSquareListToShot) { // idziemy w dol
                                                if ((numberToLetter(letterToNumber(s.getyID()) + 3) + s.getxID()).equals(s12.getyID() + s12.getxID())) {
                                                    if (s12.getState() == 2) {// nie powinno miec to miejsca, w przeciwnym razie powinien byc zniszczony
                                                        System.out.println("Algorytm nasz zawiodl!!!");
                                                    } else if (s12.getState() < 2) { // ostatnie pole, jesli tam nie strzelalismy to strzelamy
                                                        return s12;
                                                    }
                                                }
                                            }
                                        } else if (s11.getState() < 2) { // pole jest nieodkryte oddajemy strzal
                                            return s11;
                                        }
                                    } // jesli pole s11 nie jstnieje to przerywamy przeszukiwanie w dol
                                }
                            } else if (s4.getState() < 2) { // jesli pole jest nieodkryte to dodajemy do listy pomocnika gdyz nie znamy orientacji
                                squareHelper.add(s4);
                            }
                        }
                    }
                    // losowanie z helpera // w przypadku size() = zera i dojscie do tego momentu statek powinien miec status 0 (To nie powinno sie stac)
                    r = randomShoot.nextInt(squareHelper.size());
                    return squareHelper.get(r);
                }
            }
        }

        // ****************************************************************************************
        // Mechanika podejmowana decyzji strzalu w przypadku gdy nie znalezlismy niedobitego statku
        // ****************************************************************************************
        ArrayList<Square> highestQuarterList = getHighestValuedQuarter(); // lista z cwiartka o najwiekszej szansie trafienia

        if (getSumedFieldValue(highestQuarterList) > 0) {
            return getHighestValueSquare(highestQuarterList);
        } else {
            // Sprawdzenie ile pozostalo pol, im mniejsza liczba tym trudniej trafic losujac sposrod wszystkich pol
            int counter = 0;
            ArrayList<Square> lastShotsHelperList = new ArrayList<>();
            for (Square sq : playerSquareListToShot) {
                if (sq.getState() < 2) {
                    counter++;
                    lastShotsHelperList.add(sq);
                }
            }
            if (counter < 6 && lastShotsHelperList.size() > 0) {
                return lastShotsHelperList.get(lastShotsHelperList.size() - 1);
            } else {
                // Jesli cwiartki maja wartosc 0 (nie mamy informacji, badz wyczerpalicmy zebrane
                // dotychczas informacje) i jest wiecej niz 5 pol do strzela, strzelamy losowo
                do {
                    r = randomShoot.nextInt(playerSquareListToShot.size());
                }
                while (playerSquareListToShot.get(r).getState() > 1);
                // System.out.println("STRZELAM W POLE: " + playerSquareListToShot.get(r).getyID() + playerSquareListToShot.get(r).getxID());
                return playerSquareListToShot.get(r);
            }
        }
    }

    /**
     * Glowna metoda zwracajaca liste ćwiartki z najwieksza potencjalna wartoscia (szansa) na trafienie
     *
     * @return
     */
    private ArrayList<Square> getHighestValuedQuarter() {
        ArrayList<Square> q1List;
        ArrayList<Square> q2List;
        ArrayList<Square> q3List;
        ArrayList<Square> q4List;

        q1List = getGridQuarter(1);
        q2List = getGridQuarter(2);
        q3List = getGridQuarter(3);
        q4List = getGridQuarter(4);

        ArrayList<Square> highestList = getListWithHighestValue(q1List, q2List, q3List, q4List);
        return highestList;
    }

    /**
     * Metoda wydzielajaca z glownej listy wszystkich pol wskazana cwiartke
     * Nie dodaje pol ktore sotaly juz odsloniete do wartosci cwiartki
     *
     * @param i
     * @return
     */
    private ArrayList<Square> getGridQuarter(int i) {
        ArrayList quarterList = new ArrayList();
        String id;
        if (i == 1) {
            for (int k = 0; k < 5; k++) {
                for (int j = 0; j < 5; j++) {
                    id = numberToLetter(k) + String.valueOf(j + 1);
                    for (Square s : playerSquareListToShot) {
                        if (s.getState() < 2 && id.equals(s.getyID() + s.getxID())) {
                            quarterList.add(s);
                        }
                    }
                }
            }
            return quarterList;

        } else if (i == 2) {
            for (int k = 0; k < 5; k++) {
                for (int j = 5; j < 10; j++) {
                    id = numberToLetter(k) + String.valueOf(j + 1);
                    for (Square s : playerSquareListToShot) {
                        if (s.getState() < 2 && id.equals(s.getyID() + s.getxID())) {
                            quarterList.add(s);
                        }
                    }
                }
            }
            return quarterList;

        } else if (i == 3) {
            for (int k = 5; k < 10; k++) {
                for (int j = 0; j < 5; j++) {
                    id = numberToLetter(k) + String.valueOf(j + 1);
                    for (Square s : playerSquareListToShot) {
                        if (s.getState() < 2 && id.equals(s.getyID() + s.getxID())) {
                            quarterList.add(s);
                        }
                    }
                }
            }
            return quarterList;

        } else if (i == 4) {
            for (int k = 5; k < 10; k++) {
                for (int j = 5; j < 10; j++) {
                    id = numberToLetter(k) + String.valueOf(j + 1);
                    for (Square s : playerSquareListToShot) {
                        if (s.getState() < 2 && id.equals(s.getyID() + s.getxID())) {
                            quarterList.add(s);
                        }
                    }
                }
            }

        }
        return quarterList;
    }

    /**
     * Metoda zwracajaca liste z najwieksza szansa na trafienie sposrod innych list (ćwiartek)
     *
     * @param q1List
     * @param q2List
     * @param q3List
     * @param q4List
     * @return
     */
    private ArrayList<Square> getListWithHighestValue(ArrayList<Square> q1List, ArrayList<Square> q2List, ArrayList<Square> q3List, ArrayList<Square> q4List) {
        ArrayList<Square> highestValuedList;
        ArrayList<ArrayList> listOfLists = new ArrayList<>();
        listOfLists.add(q1List);
        listOfLists.add(q2List);
        listOfLists.add(q3List);
        listOfLists.add(q4List);

        highestValuedList = q1List;
        for (ArrayList ar : listOfLists) {
            if (getSumedFieldValue(ar) > getSumedFieldValue(highestValuedList)) {
                highestValuedList = ar;
            }
        }
        return highestValuedList;
    }

    /**
     * Metoda zwracajaca sume wartosci listy (ćwiartki)
     *
     * @param squareSumList
     * @return
     */
    private int getSumedFieldValue(ArrayList<Square> squareSumList) {
        int sum = 0;
        for (Square s : squareSumList) {
            sum += s.getFieldValue();
        }
        return sum;
    }

    /**
     * Metoda Zwracajaca pole o najwiekszej wartosci z cwiartki o najwiekszej szansie na trafienie
     *
     * @param highestQuarterList
     * @return
     */
    private Square getHighestValueSquare(ArrayList<Square> highestQuarterList) {
        Square highestValuedSquare = highestQuarterList.get(0);
        for (Square sq : highestQuarterList) {
            if (sq.getFieldValue() > highestValuedSquare.getFieldValue()) {
                highestValuedSquare = sq;
            }
        }
        return highestValuedSquare;
    }

    /**
     * Metoda zamieniajaca wartosc liczbowa na litere w celu uzyskania id pola w formacie np. C5
     *
     * @param i
     * @return
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

    private int letterToNumber(String letter) {
        switch (letter) {
            case "A":
                return 0;
            case "B":
                return 1;
            case "C":
                return 2;
            case "D":
                return 3;
            case "E":
                return 4;
            case "F":
                return 5;
            case "G":
                return 6;
            case "H":
                return 7;
            case "I":
                return 8;
            case "J":
                return 9;
            default:
                return -1;
        }
    }

    public void setOccupiedSquare(Ship ship, float width) {
        //int shipFieldsPlaced = 0;
        if (ship.isOrientation()) { // dla pionowych statkow
            // oznaczanie pol jako niedostepnych w okol statku
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < ship.getLength() + 2; j++) {
                    for (Square s : androidSquareList) {
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
                for (Square s : androidSquareList) {
                    if (ship.getX() + 0.2f > s.getX() && wspY + 0.2f > s.getY() && ship.getX() - 0.2f + width < s.getX() + width && wspY - 0.2f + width < s.getY() + width) {
                        s.setShipIfInSquare(ship);
                        ship.setInWhatSquare(s);
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
                    for (Square s : androidSquareList) {
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
                for (Square s : androidSquareList) {
                    if (wspX + 0.2f > s.getX() && ship.getY() + 0.2f > s.getY() && wspX - 0.2f + width < s.getX() + width && ship.getY() - 0.2f + width < s.getY() + width) {
                        s.setShipIfInSquare(ship);
                        ship.setInWhatSquare(s);
                        s.setState(1);
                        //System.out.println("!!!!!!!!!!!! Wielkosc listy pol: " + ship.getInWhatSquare().size());
                    }
                }
            }
        }
    }

    public ArrayList<Square> getAndroidSquareList() {
        return androidSquareList;
    }

    public void setAndroidSquareList(ArrayList<Square> androidSquareList) {
        this.androidSquareList = androidSquareList;
    }

    public ArrayList<Ship> getAndroidShipsList() {
        return androidShipsList;
    }

    /**
     * Zapamietanie w liscie do bazy danych pol w ktore strzelal gracz
     *
     * @param squareToUpdate
     */
    public void setListOfPlayerShotsDB(Square squareToUpdate) {
        for (Square s : listOfPlayerShotsDB) {
            if ((s.getyID() + s.getxID()).equals(squareToUpdate.getyID() + squareToUpdate.getxID())) {
                s.setState(1);
                // System.out.println("Aktualizacja stanu listy STRZALOW do bazy! Pole: " + s.getyID() + s.getxID() + " Stan: " + s.getState());
            }
        }
    }

    /**
     * Zapamietanie w liscie do bazy danych pol na ktorych gracz umiescil statki i zostaly one zestrzelone przez Androida
     *
     * @param squareToUpdate
     */
    public void setListOfPlayerShipsDB(Square squareToUpdate) {
        for (Square s : listOfPlayerShipsDB) {
            if ((s.getyID() + s.getxID()).equals(squareToUpdate.getyID() + squareToUpdate.getxID())) {
                s.setState(1);
                // System.out.println("Aktualizacja stanu listy statkow do bazy! Pole: " + s.getyID() + s.getxID() + " Stan: " + s.getState());
            }
        }
    }

    /**
     * Zmiana stanu pola w liscie po ktorej strzela Android
     *
     * @param squareToUpdate
     * @param state
     */
    public void setPlayerSquareListToShotState(Square squareToUpdate, int state, boolean fill, boolean isDestroy) {
        for (Square s : playerSquareListToShot) {
            if ((s.getyID() + s.getxID()).equals(squareToUpdate.getyID() + squareToUpdate.getxID())) {
                s.setState(state);
                s.setFill(fill);
                s.setShipIfInSquare(squareToUpdate.getShipFromSquareHit());
            }
        }
        if (isDestroy) {
            ArrayList<Square> helperList = squareToUpdate.getShipFromSquareHit().getInWhatSquare(); // aby za kazdym razem nie pisac calej tej wiazanki
            // aktualizujemy otoczke statku aby Android wiedzial, ze juz tam strzelac nie musi
            for (int i = 0; i < helperList.size(); i++) {
                for (int j = 0; j < playerSquareListToShot.size(); j++) {
                    if (helperList.get(i).getState() == 3 && (helperList.get(i).getyID() +
                            helperList.get(i).getxID()).equals(playerSquareListToShot.get(j).getyID()
                            + playerSquareListToShot.get(j).getxID())) {
                        playerSquareListToShot.get(j).setState(3);
                    }
                }
            }
        }
    }

    public boolean isPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(boolean playerTurn) {
        this.playerTurn = playerTurn;
    }

    public boolean isBattleEnded() {
        return battleEnded;
    }

    public void setBattleEnded(boolean battleEnded) {
        this.battleEnded = battleEnded;
    }

    public boolean isPlayerWin() {
        return playerWin;
    }

    public void setPlayerWin(boolean playerWin) {
        this.playerWin = playerWin;
    }

    public void incrementRoundCounter() {
        roundCounter++;
    }

    public int getRoundCounter() {
        return roundCounter;
    }

    public boolean isPlayerShot() {
        return playerShot;
    }

    public void setPlayerShot(boolean playerShot) {
        this.playerShot = playerShot;
    }

    public boolean isAndroidShot() {
        return androidShot;
    }

    public void setAndroidShot(boolean androidShot) {
        this.androidShot = androidShot;
    }

    // **********FUNKCJE TESTOWE

    /**
     * Zwraca orientacje statku w postaci String. Do przekazania jako informacja w intencji
     *
     * @return
     */
    public String getStringOrientation(boolean orientation) {
        String stringOrientation = "";
        if (orientation) {
            stringOrientation = "true";
        } else {
            stringOrientation = "false";
        }
        return stringOrientation;
    }

    /**
     * Metoda sprawdzajaca czy tablice zostaly poprawnie zainicjowane
     *
     * @param tableName
     */
    public void printDBTable(String tableName) {
        Cursor cursor = null;
        if (tableName.equals("PLAYER_SHIPS")) {
            cursor = db.query(tableName,
                    new String[]{"_id", "OCCURRENCE"},
                    null, null, null, null, null);
        } else if (tableName.equals("PLAYER_SHOTS")) {
            cursor = db.query(tableName,
                    new String[]{"_id", "SHOTS"},
                    null, null, null, null, null);
        }

        if (cursor != null) {
            System.out.println("!!!ZAWARTOSC TABLICY " + tableName + " !!!");
            while (cursor.moveToNext()) {
                System.out.println("*** ID: " + cursor.getString(0) + " Wartosc: " + cursor.getInt(1));
            }
        } else {
            System.out.println("BLAD!! NIE MA KURSORA!!!!!!!!");
        }
    }

    public void printDBHistoryTable() {
        Cursor cursor = db.query("GAMES_HISTORY",
                new String[]{"_id", "WINNER", "HOW_MANY_TURNS"},
                null, null, null, null, null);

        if (cursor != null) {
            System.out.println("!!!ZAWARTOSC TABLICY GAMES_HISTORY !!!");
            while (cursor.moveToNext()) {
                System.out.println("*** ID: " + cursor.getInt(0) + " Zwyciezca: " + cursor.getString(1) + " W ile tru: " + cursor.getInt(2));
            }
        } else {
            System.out.println("BLAD!! NIE MA KURSORA!!!!!!!!");
        }
    }
}
