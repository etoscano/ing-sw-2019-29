import adrenaline.*;
import org.junit.jupiter.api.Test;

import static adrenaline.GameModel.Mode.DEATHMATCH;

public class GameBoardTestWithMapOne {

    @Test
    public void testConstructor() {

        Map map = new MapOne(DEATHMATCH);
    }

}
