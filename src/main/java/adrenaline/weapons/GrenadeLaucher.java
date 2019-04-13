package adrenaline.weapons;

import adrenaline.*;

import java.util.LinkedList;

/**
 * 
 */
public class GrenadeLaucher extends WeaponCard {

    /**
     * Default constructor
     */
    public GrenadeLaucher() {
        price.add(new AmmoCube(AmmoCube.CubeColor.RED, AmmoCube.Effect.BASE,true));
        price.add(new AmmoCube(AmmoCube.CubeColor.RED, AmmoCube.Effect.OP1,false));
    }

    @Override
    public LinkedList<Object> fromCellsToTargets(LinkedList<CoordinatesWithRoom> list, CoordinatesWithRoom c, GameBoard g, Player p, GameModel m, EffectAndNumber en) {
        LinkedList<Object> targets = new LinkedList<>();
        if(en.getEffect()== AmmoCube.Effect.BASE){
             targets = super.fromCellsToTargets(list, c, g, p, m, en);

            // ASK WHICH TARGET TO DAMAGE, PUT IT IN TARGETS (REMOVE THE OTHERS)
            return targets;


        }
        else {    //  OP1 EFFECT

            // ASK WHICH CELL IN LIST TO DAMAGE, REMOVE THE OTHERS
            targets = super.fromCellsToTargets(list, c, g, p, m, en);

            return targets;

        }

    }

    @Override
    public void applyDamage(LinkedList<Object> targetList, Player p, EffectAndNumber e) {

        switch (e.getEffect()) {
            case BASE:  // 1 DAMAGE, 1 TARGET, THEN CAN MOVE IT
                break;

            case OP1:   // 1 DAMAGE, EVERY PLAYER, 1 SQUARE
                break;


        }
    }
}