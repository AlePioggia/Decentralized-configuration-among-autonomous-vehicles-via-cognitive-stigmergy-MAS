package core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PositionTest {

    @Test
    public void testPositionConstructor() {
        Position position = new Position(1, 2);
        assertEquals(1, position.getX());
        assertEquals(2, position.getY());
    }

    @Test
    public void testPositionGetters() {
        Position position = new Position(1, 2);
        assertEquals(1, position.getX());
        assertEquals(2, position.getY());
    }

    @Test
    public void testPositionSetters() {
        Position position = new Position(1, 2);
        position.setX(3);
        position.setY(4);
        assertEquals(3, position.getX());
        assertEquals(4, position.getY());
    }

}
