package adrenaline.weapons;

import adrenaline.*;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 */
public class Thor extends WeaponCard {

    /**
     * Default constructor
     */
    public Thor() {
        price.add(new AmmoCube(AmmoCube.CubeColor.BLUE, AmmoCube.Effect.BASE,true));
        price.add(new AmmoCube(AmmoCube.CubeColor.RED, AmmoCube.Effect.BASE,false));
        price.add(new AmmoCube(AmmoCube.CubeColor.BLUE, AmmoCube.Effect.OP1,false));
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
    /**
     * applyDamage()
     * @param p player who is doing damage
     * @param e selected effect
     * @param targetList selected targets
     */
    @Override
    public void applyDamage(List<Object> targetList, Player p, EffectAndNumber e) {
        setDamaged(targetList,p);
            switch (e.getEffect()) {
                case BASE:  // 2 DAMAGE, 1 TARGET
                case OP2:   // 2 DAMAGE, DIFFERENT TARGET
                    if(targetList.get(0) instanceof Player) {
                        int i =((Player) targetList.get(0)).marksByShooter(p);
                        i=i+2;
                        ((Player) targetList.get(0)).addDamageToTrack(p,i);
                    }
                    else {
                        // DAMAGE SPAWNPOINT
                    }
                    break;

                case OP1:   // 1 DAMAGE, 1 DIFFERENT TARGET
                    if(targetList.get(0) instanceof Player) {
                        int i =((Player) targetList.get(0)).marksByShooter(p);
                        i++;
                        ((Player) targetList.get(0)).addDamageToTrack(p,i);
                    }
                    else {
                        // DAMAGE SPAWNPOINT
                    }

                    break;


            }

        }
    @Override
    public String toString() {
        return "Thor";
    }
    }