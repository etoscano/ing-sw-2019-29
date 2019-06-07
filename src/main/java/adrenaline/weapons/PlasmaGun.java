package adrenaline.weapons;

import adrenaline.*;
import adrenaline.gameboard.GameBoard;

import java.util.List;

/**
 * 
 */
public class PlasmaGun extends WeaponCard {

    /**
     * Default constructor
     */
    public PlasmaGun() {
        price.add(new AmmoCube(AmmoCube.CubeColor.BLUE, AmmoCube.Effect.BASE,true));
        price.add(new AmmoCube(AmmoCube.CubeColor.YELLOW, AmmoCube.Effect.BASE,false));
        price.add(new AmmoCube(AmmoCube.CubeColor.BLUE, AmmoCube.Effect.OP2,false));
    }

    public boolean canShootBase(){
        return true;
    }
    public boolean canShootOp1(){
        return true;
    }
    public boolean canShootOp2(){
        return true;
    }

    @Override
    public List<CoordinatesWithRoom> getPossibleTargetCells(CoordinatesWithRoom c, EffectAndNumber en, GameBoard g) {

        if(en.getEffect()== AmmoCube.Effect.OP1) {
            List<CoordinatesWithRoom> list = c.tilesSameDirection(2, g, false);
            list.remove(c);

            return list;
        }
        else{
            return super.getPossibleTargetCells(c,en,g);
        }
    }

    @Override
    public void applyDamage(List<Object> targetList, Player p, EffectAndNumber e) {

            switch (e.getEffect()) {
                case BASE:  // 2 DAMAGE, 1 TARGET
                case OP2:   // 1 DAMAGE, SAME TARGET
                    if(targetList.get(0) instanceof Player) {
                        int i =((Player) targetList.get(0)).marksByShooter(p);

                        if(e.getEffect()== AmmoCube.Effect.BASE){
                            i++;
                        }
                        i++;

                        ((Player) targetList.get(0)).addDamageToTrack(p,i);
                    }

                    else {
                        // DAMAGE SPAWNPOINT
                    }
                    break;

                case OP1:   // MOVE 1 OR 2 SQUARES
                    // TODO
                    break;

            }


    }
    @Override
    public String toString() {
        return "PlasmaGun";
    }
}