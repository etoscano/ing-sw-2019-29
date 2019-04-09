import adrenaline.AmmoCube;
import adrenaline.AmmoTile;
import adrenaline.Coordinates;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

public class AmmoTileTest {

    @Test
    public void testConstructor() {
        Coordinates c1 = new Coordinates(1,2);
        AmmoTile a = new AmmoTile(c1, AmmoCube.CubeColor.RED, AmmoCube.CubeColor.RED, AmmoCube.CubeColor.BLUE);
        AmmoTile b = new AmmoTile(c1, AmmoCube.CubeColor.POWERUP, AmmoCube.CubeColor.RED, AmmoCube.CubeColor.BLUE);
        Coordinates d = a.getCoordinates();
        a.setCoordinates(2,3);
        LinkedList<AmmoCube> f = a.getAmmoTile();
    }


}