package adrenaline;

import adrenaline.gameboard.GameBoard;

import java.io.Serializable;
import java.util.*;

public class WeaponCard extends Card{


    protected LinkedList<AmmoCube> price;
    private EffectAndNumber effectAndNumber;
    private CoordinatesWithRoom coordinates;
    private boolean reload;
    private boolean reloadAlt;
    public WeaponCard() {
        price = new LinkedList<>();
        reload =false;
    }


    // THEY ARE IN ORDER
                /*
                for(int i=0;i<effectsList.size();i++){
                proposeTargetsShoot(.....effectsList.get(i)...); -> view (SELECT THE APPROPRIATE NUMBER ONLY IF NECESSARY) // selectedTargets = (at least one)
                view -> list of targets for this effect
                shoot(....effectsList.get(i)...) (APPLIED DAMAGE)
                list of targets selected maybe it's removed from possible new targets
                }

                */


    // CELLS IN WEAPON RANGE
    // TO BE OVERRIDDEN
    public List<CoordinatesWithRoom> getPossibleTargetCells(CoordinatesWithRoom c, EffectAndNumber en, GameBoard g){

        // CELLS OF EVERY ROOM I SEE
        // MAYBE OVERRIDDEN
        List<CoordinatesWithRoom> list = new LinkedList<>();
        int x = c.getRoom().getRoomSizeX();
        int y = c.getRoom().getRoomSizeY();

        for(int i=1;i<=x;i++){
            for(int j=1;j<=y;j++){
                list.add(new CoordinatesWithRoom(i,j,c.getRoom()));
            }
        }
        for(int k=0;k<g.getDoors().size();k++){
           if(c.getX()==g.getDoors().get(k).getCoordinates1().getX()&&
              c.getY()==g.getDoors().get(k).getCoordinates1().getY()&&
              c.getRoom().getToken()==g.getDoors().get(k).getCoordinates1().getRoom().getToken()){

               for(int i=1;i<=g.getDoors().get(k).getCoordinates2().getRoom().getRoomSizeX();i++){
                   for(int j=1;j<=g.getDoors().get(k).getCoordinates2().getRoom().getRoomSizeY();j++){
                       list.add(new CoordinatesWithRoom(i,j,g.getDoors().get(k).getCoordinates2().getRoom()));
                   }
               }
           }

            if(c.getX()==g.getDoors().get(k).getCoordinates2().getX()&&
              c.getY()==g.getDoors().get(k).getCoordinates2().getY()&&
              c.getRoom().getToken()==g.getDoors().get(k).getCoordinates2().getRoom().getToken()){

                for(int i=1;i<=g.getDoors().get(k).getCoordinates1().getRoom().getRoomSizeX();i++){
                    for(int j=1;j<=g.getDoors().get(k).getCoordinates1().getRoom().getRoomSizeY();j++){
                        list.add(new CoordinatesWithRoom(i,j,g.getDoors().get(k).getCoordinates1().getRoom()));
                    }
                }

            }

        }

        return list;
    }


    // GETS EVERYBODY IN WEAPON RANGE (DEPENDING ON THE WEAPON getPossibleTargetCells)
    // IT JUST TRANSFORMS CELLS INTO PLAYERS
    public List<Object> fromCellsToTargets(List<CoordinatesWithRoom> list, CoordinatesWithRoom c, GameBoard g, Player p, GameModel m, EffectAndNumber en) {
        LinkedList<Object> targetList = new LinkedList<>();

        // FROM CELLS IN WEAPON RANGE GET ALL THE POSSIBLE TARGETS
           for(int k=0;k<m.getPlayers().size();k++) {
               for (CoordinatesWithRoom coordinatesWithRoom : list) {
                   if (m.getPlayers().get(k).getPlayerRoom() == coordinatesWithRoom.getRoom() &&
                           m.getPlayers().get(k).getPlayerPositionX() == coordinatesWithRoom.getX() &&
                           m.getPlayers().get(k).getPlayerPositionY() == coordinatesWithRoom.getY()) {
                       targetList.add(m.getPlayers().get(k));
                       break;
                   }
               }
        }
        // TODO ADD POSSIBLE SPAWNPOINTS TO TARGETLIST

        targetList.remove(p);

        return targetList;
    }


    public boolean canShootBase(){
        return false;
    }
    public boolean canShootAlt(){
        return false;
    }
    public boolean canShootOp1(){
        return false;
    }
    public boolean canShootOp2(){
        return false;
    }


    // TO BE OVERRIDDEN
    public void applyDamage(List<Object> targetList, Player p, EffectAndNumber e){
    }

    public List<AmmoCube> getPrice(){
        return price;
    }

    public void placeWeaponOnMap(CoordinatesWithRoom c){
        this.coordinates=c;
    }
    public CoordinatesWithRoom getCoordinatesOnMap(){
        return this.coordinates;
    }
    public  void setReload(){this.reload=true;}
    public  void setNotReload(){reload=false;}
    public  void setReloadAlt(boolean reloadStatus){this.reloadAlt=reloadStatus;}
    public  boolean getReloadAlt(){return reloadAlt;}
    public  boolean getReload(){return reload;}

    @Override
    public String toString() {
        return "Generic weapon card";
    }
    /**
     *setDamaged()
     * @param shooter
     * @param targets
     */
    public void setDamaged(List<Object> targets,Player shooter){
        for(Object o : targets){
            if(o instanceof Player){
                ((Player) o).setDamagedStatus(true);
                ((Player) o).setShooter(shooter.toString());
            }
        }
    }

}
