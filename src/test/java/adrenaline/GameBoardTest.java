package adrenaline;

import adrenaline.gameboard.GameBoard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class GameBoardTest {
/*testing creation of Gameboard*/
    @Test
    public void testConstructor() {

        GameBoard g1;

        g1 = new DominationBoard();
        assertTrue(g1 instanceof DominationBoard);

        g1 = new DeathmatchBoard();
        assertTrue(g1 instanceof DeathmatchBoard);

    }

}
