package adrenaline.weapons;

import adrenaline.*;

import java.util.LinkedList;

/**
 * 
 */
public class Furnace extends WeaponCard {

    /**
     * Default constructor
     */
    public Furnace() {
        price.add(new AmmoCube(AmmoCube.CubeColor.RED, AmmoCube.Effect.BASE, true));
        price.add(new AmmoCube(AmmoCube.CubeColor.BLUE, AmmoCube.Effect.BASE, false));
    }

    @Override
    public LinkedList<CoordinatesWithRoom> getPossibleTargetCells(CoordinatesWithRoom c, AmmoCube.Effect e, GameBoard g) {
        LinkedList<CoordinatesWithRoom> list = new LinkedList<>();
        if(e== AmmoCube.Effect.BASE) {

            LinkedList<Room> possibleRooms = new LinkedList<>();

            int x = c.getRoom().getRoomSizeX();
            int y = c.getRoom().getRoomSizeY();

            for (int k = 0; k < g.getDoors().size(); k++) {
                if (c.getX() == g.getDoors().get(k).getCoordinates1().getX() &&
                        c.getY() == g.getDoors().get(k).getCoordinates1().getY() &&
                        c.getRoom().getToken() == g.getDoors().get(k).getRoom1().getToken()) {

                    possibleRooms.add(g.getDoors().get(k).getRoom2());
                }

                if (c.getX() == g.getDoors().get(k).getCoordinates2().getX() &&
                        c.getY() == g.getDoors().get(k).getCoordinates2().getY() &&
                        c.getRoom().getToken() == g.getDoors().get(k).getRoom2().getToken()) {

                    possibleRooms.add(g.getDoors().get(k).getRoom1());

                }
            }

            ///// ASK PLAYER FOR A CELL!!!!!!
            // SEND PLAYER LIST OF ROOMS possibleRooms
            /// get index or something about that room

            int u = 0 //= possibleRooms.get(i).getRoomSizeX()
                    ;
            int q = 0 //= possibleRooms.get(i).getRoomSizeY()
                    ;

            for (int i = 1; i <= u; i++) {
                for (int j = 1; j <= q; j++) {
                    list.add(new CoordinatesWithRoom(i, j, possibleRooms.get(i)));
                }
            }
            return list;
        }
        else{
            list = c.oneTileDistant(g);

            //// ASK PLAYER WHICH TILE, GET ONE BACK IN THAT LIST

            return list;
        }
    }

    @Override
    public void applyDamage(LinkedList<Object> targetList, Player p, EffectAndNumber e) {

        switch (e.getEffect()) {

            case BASE:  // 1 DAMAGE, EVERY PLAYER
            case ALT:   // 1 DAMAGE, 1 MARK, EVERY PLAYER
                for(int j=0;j<targetList.size();j++){
                    if(targetList.get(j) instanceof Player) {
                        int i =((Player) targetList.get(0)).marksByShooter(p);
                        i++;

                        ((Player) targetList.get(0)).addDamageToTrack(p,i);

                        if(e.getEffect()== AmmoCube.Effect.ALT){
                            ((Player) targetList.get(0)).addMarks(p,1);

                        }
                    }
                    else{
                        // DAMAGE SPAWNPOINT
                    }

                }
                break;

        }
    }
}