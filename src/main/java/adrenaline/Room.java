package adrenaline;

import java.util.*;

/**
 * 
 */
public class Room {

    public int [][] room;
    public LinkedList<AmmoTile> tiles;
    public LinkedList<Player> players;
    public LinkedList<Spawnpoint> spawnpoints;

    /**
     * Default constructor
     */
    public Room(int x, int y) {

        spawnpoints = new LinkedList<Spawnpoint>();
        players = new LinkedList<Player>();
        tiles = new LinkedList<AmmoTile>();
        room = new int[x][y];
    }





}