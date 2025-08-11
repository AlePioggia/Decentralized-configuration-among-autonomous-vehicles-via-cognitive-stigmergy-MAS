package discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import core.Position;

public class TurnDiscoveryServiceTest {

    @Test
    public void exploreTurns() {
        List<Turn> availableTurns = List.of(new Turn(new Position(0, 0), new Position(1, 0)),
                new Turn(new Position(1, 0), new Position(0, 0)),
                new Turn(new Position(0, 0), new Position(0, 1)),
                new Turn(new Position(0, 1), new Position(0, 0)));
        TurnDiscoveryService service = new TurnDiscoveryService(availableTurns);
        List<Turn> discoveredTurns = service.exploreTurns("agent", new Position(0, 0));

        assertEquals(2, discoveredTurns.size());
        assertTrue(discoveredTurns.contains(new Turn(new Position(0, 0), new Position(1, 0))));
        assertTrue(discoveredTurns.contains(new Turn(new Position(0, 0), new Position(0, 1))));
    }

}
//