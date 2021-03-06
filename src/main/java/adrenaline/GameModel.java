package adrenaline;

import adrenaline.gameboard.*;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Is the class that represents a Deck.
 * It has:
 * <ul>
 *     <li> a list of Players
 *     <li> a Game Mode
 *     <li> a Map
 * </ul>
 *
 * @author Eleonora Toscano
 * @version 1.0
 */
public class GameModel{

    final private int numMaxWeaponSpawnpoin=3;
    private LinkedList<Player> players;
    public enum Mode {
        DEATHMATCH, DOMINATION
    }
    public enum Bot {
        BOT, NOBOT
    }
    public enum FrenzyMode{
        ON,OFF
    }
    protected Mode mode;
    private Map mapUsed;
    protected Bot bot;
    protected FrenzyMode frenzyMode;
    private boolean doesntExists;
    public WeaponDeck weaponDeck;
    public PowerUpDeck powerUpDeck;
    public AmmoTileDeck ammoTileDeck;

    /**
     * Constructor with Mode, Bot and choice of map.
     *
     * @param m the mode
     * @param b the bot choice
     * @param chosenMap the map chosen
     */
    public GameModel(Mode m, Bot b,
                     int chosenMap, boolean frenzyChoice) {

        players = new LinkedList<>();

        switch (chosenMap) {
            case 1:
                mapUsed = new MapOne(m);
                break;
            case 2:
                mapUsed = new MapTwo(m);
                break;
            case 3:
                mapUsed = new MapThree(m);
                break;
            case 4:
            default:
                mapUsed = new MapFour(m);
                break;
        }
        mode = m;
        bot = b;


        weaponDeck = new WeaponDeck();
        weaponDeck.shuffleCards();
        powerUpDeck = new PowerUpDeck();
        powerUpDeck.shuffleCards();
        ammoTileDeck = new AmmoTileDeck();
        ammoTileDeck.shuffleCards();

        if(frenzyChoice){
            frenzyMode=FrenzyMode.ON;
        }else{
            frenzyMode=FrenzyMode.OFF;
        }

    }

    public boolean hasFrenzyOn(){
        if(frenzyMode==FrenzyMode.ON){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Adds a Player to the list.
     *
     * @param p a player to add
     */
    public void addPlayer(Player p){
        getPlayers().add(p);
    }

    public Map getMapUsed(){
        return mapUsed;
    }

    public List<Player> getPlayers(){
        return players;
    }

    /**
     *startingMap
     * all AmmoTile of all Rooms get initialized with free color
     * CONV: free color==there isn't an AmoTile to be draw
     **/
    public void startingMap(){
        for(Room room:getMapUsed().getGameBoard().getRooms()){

            for (AmmoTile a:room.getTiles()
                 ) {

                for (AmmoCube ac: a.getAmmoCubes()
                     ) {
                        ac.setCubeColor(AmmoCube.CubeColor.FREE);
                }
            }

            for (Spawnpoint spw:room.getSpawnpoints()
                 ) {
                spw.getWeaponCards().clear();
            }

        }
    }


    /**
     *populateMap
     * calls populateAmmoTile
     * assign to every spawnpoint 3 WeaponCards
     **/
    public void populateMap() {
        populateAmmoTile();
        for (Room room : getMapUsed().getGameBoard().getRooms()) {
            //here check all the rooms one by one

            for (Spawnpoint s : room.getSpawnpoints()
            ) {
                while (s.getWeaponCards().size() < numMaxWeaponSpawnpoin&&!this.weaponDeck.getList().isEmpty()) {
                    s.getWeaponCards().add(weaponDeck.pickUpWeapon());
                }
            }
        }

    }
    /**
     *populateAmmoTile
     * foreach Room add AmmoTile and set its color
     **/
    public void populateAmmoTile(){

     for (Room room : getMapUsed().getGameBoard().getRooms()) {
         for (int x = 1; x <= room.getRoomSizeX(); x++) {
             for (int y = 1; y <= room.getRoomSizeY(); y++) {
                 if(!isSpawnpointCoordinates(x,y,room)){
                     AmmoTile t = ammoTileDeck.pickUpAmmoTile();
                     t.setCoordinates(x,y);
                 room.getTiles().add(t);
                 System.out.println(t.toString()+" in "+t.getCoordinates().toString());
                 }
             }
         }
     }

    }

    /**
     *isSpawnpointCoordinates()
     * @param x coordinate x
     * @param y coordinate y
     * @param room room to check
     *
     * return if a spawnpoint's cell
     */
    public boolean isSpawnpointCoordinates(int x,int y,Room room){
        for (Room r: this.getMapUsed().getGameBoard().getRooms()
        ) {
            for (Spawnpoint spw:r.getSpawnpoints()
            ) {
                if(room.getToken()==r.getToken()&&x==spw.getSpawnpointX()&&y==spw.getSpawnpointY())
                    return true;
            }
        }
        return false;
    }

}










