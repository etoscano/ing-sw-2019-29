package adrenaline;

import java.util.*;

/**
 * 
 */
public class GameBoard {

    protected LinkedList<Door> doors;



    /**
     * Default constructor
     */
    public GameBoard() {
        doors = new LinkedList<Door>();
    }


// VORREI NON FARGLI FARE NULLA PERCHé I METODI SONO NEI RISPETTIVI BOARD MA VUOLE CHE RITORNI QUALCOSA
    public Room getRoom(int i) {
        return this.getRoom(i);
    }

    public void addRoom(Room r){
    }
    public void addRoom(RoomDeath r){
    }
    public void addRoom(RoomDom r){
    }

    public void addDoor(Door d){
        doors.add(d);
    }


   /* *//**
     * 
     *//*
    public LinkedList<PositionWithRoom> oneTileDistant() {
        // TODO implement here
    }*/

    /**
     * 
     */
    public void twoTilesDistant() {
        // TODO implement here
    }

    /**
     * 
     */
    public void threeTilesDistant() {
        // TODO implement here
    }

}