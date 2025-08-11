package discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import core.Position;

public class TurnTest {

    @Test
    public void testComputeTurnTypeEastTurn() {
        Position from = new Position(0, 0);
        Position to = new Position(1, 0);
        Turn turn = new Turn(from, to);
        turn.computeTurnType();
        assertEquals("east", turn.getTurnType());
    }

    @Test
    public void testComputeTurnTypeWestTurn() {
        Position from = new Position(1, 0);
        Position to = new Position(0, 0);
        Turn turn = new Turn(from, to);
        turn.computeTurnType();
        assertEquals("west", turn.getTurnType());
    }

    @Test
    public void testComputeTurnTypeNorthTurn() {
        Position from = new Position(0, 0);
        Position to = new Position(0, 1);
        Turn turn = new Turn(from, to);
        turn.computeTurnType();
        assertEquals("north", turn.getTurnType());
    }

    @Test
    public void testComputeTurnTypeSouthTurn() {
        Position from = new Position(0, 1);
        Position to = new Position(0, 0);
        Turn turn = new Turn(from, to);
        turn.computeTurnType();
        assertEquals("south", turn.getTurnType());
    }
}
