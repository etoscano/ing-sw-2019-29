package adrenaline;

import adrenaline.Spawnpoint;
import org.junit.jupiter.api.Test;
public class Spawnpoint2Test {
/*testing Spawnpoint construction and getCoordinate methods*/
    @Test
    public void testConstructor() {
        Spawnpoint s = new Spawnpoint( 1 , 3 );
        int x = s.getSpawnpointX();
        int y = s.getSpawnpointY();
    }
}
