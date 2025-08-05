+name(N) <- 
    +get_name(N);
    .print("Vehicle ", N, " initialized").

+at(ME, X, Y)[source(percept)] : get_name(ME) <-
    -at(ME, _, _);
    +at(ME, X, Y).

+occupied(X, Y)[source(percept)] <-
    -occupied(X, Y);
    +occupied(X, Y).

+direction(X, Y, Direction)[source(percept)] <- 
    -direction(X, Y, _);
    +direction(X, Y, Direction);
    .print("Cell Direction: (", X, ",", Y, ") = ", Direction).

+turn_available(FromX, FromY, ToX, ToY)[source(percept)] : get_name(ME) <-
    +known_turn(FromX, FromY, ToX, ToY);
    .print(ME, "[DISCOVERED TURN:] (", FromX, ",", FromY, ") → (", ToX, ",", ToY, ")").

+turn_discovered(Agent, FromX, FromY, ToX, ToY)[source(percept)] : 
    get_name(ME) & Agent \== ME <-
    +known_turn(FromX, FromY, ToX, ToY);
    .print(ME, "[LEARNED:] from ", Agent, ": turn (", FromX, ",", FromY, ") → (", ToX, ",", ToY, ")").

+interval(I)[source(percept)] <-
    +simulation_interval(I).

+step_completed[source(percept)] : get_name(ME) <- 
    .print(ME, " completed its step");
    !loop.

+agent_intention(Agent, X, Y, Action)[source(percept)] : get_name(ME) & Agent \== ME <-
    .print(ME, " notices that ", Agent, " wants to reach location: (", X, " ", Y, ")").

+light_state(X, Y, State)[source(percept)] <-
    -light_current_state(X, Y, _);
    +light_current_state(X, Y, State);
    .print(ME, " has seen traffic light at (", X, ",", Y, ") = ", State).

+light_state_changed(X, Y, State)[source(percept)] <- 
    -light_current_state(X, Y, _);
    +light_current_state(X, Y, State);
    .print(ME, " noticed that traffic light at (", X, ",", Y, ") changed to ", State).

!start.

+!start : get_name(ME) <-
    .print("Vehicle ", ME, " starting");
    !choose_action.

+!start <- 
    .print("Waiting for the name drop...");
    .wait(500);
    !start.

+!loop : get_name(ME) <- 
    .wait(200);
    !choose_action.

+!choose_action : get_name(ME) & at(ME, X, Y) & direction(X, Y, Direction) <- 
    .print("=== ", ME, "[CHOOSING ACTION] at (", X, ",", Y, ") dir=", Direction, " ===");
    explore(ME, X, Y, Direction);
    .wait(100);
    .findall([ToX, ToY], known_turn(X, Y, ToX, ToY), AvailableTurns);

    if (AvailableTurns \== []) {
        .print(ME, "Available turns from (", X, ",", Y, "): ", AvailableTurns);
        !decide_turn_or_straight(X, Y, Direction, AvailableTurns);
    } else {
        .print(ME, "No turns available, proceeding straight");
        !proceed_straight(X, Y, Direction);
    }.

+!choose_action : get_name(ME) & at(ME, X, Y) <- 
    .print(ME, " no direction info at (", X, ",", Y, "), using fallback");
    explore(ME, X, Y, "unknown");
    .wait(100);
    .findall([ToX, ToY], known_turn(X, Y, ToX, ToY), AvailableTurns);
    
    if (AvailableTurns \== []) {
        !decide_turn_or_straight(X, Y, "East", AvailableTurns); 
    } else {
        !fallback_movement(X, Y);
    }.

+!decide_turn_or_straight(X, Y, Direction, AvailableTurns) : get_name(ME) <-
    .random(RandomValue);

    TurnProbability = 0.8;
    
    if (RandomValue < TurnProbability) {
        .print(ME, "[DECISION:] Taking a turn (", RandomValue, " < ", TurnProbability, ")");
        !execute_turn(X, Y, AvailableTurns);
    } else {
        .print(ME, "[DECISION:] Going straight (", RandomValue, " >= ", TurnProbability, ")");
        !proceed_straight(X, Y, Direction);
    }.

+!execute_turn(X, Y, AvailableTurns) : get_name(ME) <-
    .length(AvailableTurns, NumTurns);
    .random(TurnIndex);
    TurnChoiceIndex = math.floor(TurnIndex * NumTurns);
    .nth(TurnChoiceIndex, AvailableTurns, [ToX, ToY]);
    .print(ME, "[EXECUTING TURN:] to (", ToX, ",", ToY, ")");
    .term2string(ToX, ToXStr);
    .term2string(ToY, ToYStr);
    .concat("turn:", ToXStr, Part1);
    .concat(Part1, ",", Part2);
    .concat(Part2, ToYStr, TurnAction);
    .print(ME, " Turn action: ", TurnAction);
    writeIntent(ME, TurnAction).

+!proceed_straight(X, Y, Direction) : get_name(ME) <-
    ?next_position(X, Y, Direction, NextX, NextY);
    .print(ME, "[PROCEEDING STRAIGHT:] to (", NextX, ",", NextY, ")");
    !check_traffic_and_move(NextX, NextY).

+!fallback_movement(X, Y) : get_name(ME) <-
    .print(ME, "[FALLBACK MOVEMENT:] from (", X, ",", Y, ")");
    NextX = X + 1;
    NextY = Y;
    !check_traffic_and_move(NextX, NextY).

+!check_traffic_and_move(NextX, NextY) : get_name(ME) <-
    hasTrafficLightAt(NextX, NextY, HasLight);
    
    if (HasLight) {
        !traffic_light_handler(NextX, NextY);
    } else {
        !ordinary_logic(NextX, NextY);
    }.

+!traffic_light_handler(NextX, NextY) : get_name(ME) <-
    if (light_current_state(NextX, NextY, "green")) {
        .print(ME, "Green light at (", NextX, ",", NextY, "), proceeding");
        !ordinary_logic(NextX, NextY);
    } else {
        .print(ME, "Red light at (", NextX, ",", NextY, "), waiting");
        writeIntent(ME, "wait");
    }.

+!traffic_light_handler(NextX, NextY) : get_name(ME) & 
    not light_current_state(NextX, NextY, _) <-
        .print(ME, "Unknown light state at (", NextX, ",", NextY, "), waiting");
        writeIntent(ME, "wait").

+!ordinary_logic(NextX, NextY) : get_name(ME) <- 
    if (not occupied(NextX, NextY)) {
        if (agent_intention(OtherAgent, NextX, NextY, _) & OtherAgent \== ME) {
            .print(ME, "Waiting for ", OtherAgent, " at (", NextX, ",", NextY, ")");
            writeIntent(ME, "wait");
        } else {
            .print(ME, "Moving to (", NextX, ",", NextY, ")");
            writeIntent(ME, "follow");
        }
    } else {
        .print(ME, "Cell (", NextX, ",", NextY, ") occupied, waiting");
        writeIntent(ME, "wait");
    }.

+?next_position(X, Y, "East", NextX, NextY) <-
    NextX = X + 1;
    NextY = Y.

+?next_position(X, Y, "West", NextX, NextY) <-
    NextX = X - 1;
    NextY = Y.

+?next_position(X, Y, "North", NextX, NextY) <-
    NextX = X;
    NextY = Y - 1.

+?next_position(X, Y, "South", NextX, NextY) <-
    NextX = X;
    NextY = Y + 1.

+?next_position(X, Y, _, NextX, NextY) <-
    NextX = X + 1;
    NextY = Y.

{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }