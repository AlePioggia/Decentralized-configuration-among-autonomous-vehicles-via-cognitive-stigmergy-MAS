+turn_available(FromX, FromY, ToX, ToY)[source(percept)] : get_name(ME) <-
    +known_turn(FromX, FromY, ToX, ToY);
    .print(ME, "[DISCOVERED TURN:] (", FromX, ",", FromY, ") → (", ToX, ",", ToY, ")").

+turn_discovered(Agent, FromX, FromY, ToX, ToY)[source(percept)] : 
    get_name(ME) & Agent \== ME <-
    +known_turn(FromX, FromY, ToX, ToY);
    .print(ME, "[LEARNED:] from ", Agent, ": turn (", FromX, ",", FromY, ") → (", ToX, ",", ToY, ")").

+?has_available_turns(X, Y, AvailableTurns) <-
    .findall([ToX, ToY], known_turn(X, Y, ToX, ToY), AvailableTurns);
    AvailableTurns \== [].

+?get_available_turns(X, Y, AvailableTurns) <-
    .findall([ToX, ToY], known_turn(X, Y, ToX, ToY), AvailableTurns).