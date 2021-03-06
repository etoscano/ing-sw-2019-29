package adrenaline.weapons;

import adrenaline.*;
import adrenaline.gameboard.GameBoard;

import java.util.List;

/**
 * 
 */
public class Zx_2 extends WeaponCard {

    /**
     * Default constructor
     */
    public Zx_2() {
        price.add(new AmmoCube(AmmoCube.CubeColor.YELLOW, AmmoCube.Effect.BASE,true));
        price.add(new AmmoCube(AmmoCube.CubeColor.RED, AmmoCube.Effect.BASE,false));
    }

    public boolean canShootBase(){
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
            case BASE:  // 1 DAMAGE, 2 MARKS, 1 TARGET
                if(targetList.get(0) instanceof Player){
                    int i =((Player) targetList.get(0)).marksByShooter(p);
                    i++;
                    ((Player) targetList.get(0)).addDamageToTrack(p,i);
                    ((Player) targetList.get(0)).addMarks(p,2);

                }
                else {
                    // DAMAGE SPAWNPOINT
                }
                break;

            case ALT:   // 1 MARK, UP TO 3 TARGETS
                for(int i = 0; i<targetList.size(); i++){
                    ((Player)targetList.get(i)).addMarks(p,1);
                }
                // TODO CANNOT USE ON SPAWNPOINT ???
                break;


        }

    }
    @Override
    public String toString() {
        return "Zx_2";
    }
}