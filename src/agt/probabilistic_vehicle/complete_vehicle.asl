{ include("src/agt/probabilistic_vehicle/modules/perception.asl") }
{ include("src/agt/probabilistic_vehicle/modules/turn_discovery.asl") }
{ include("src/agt/probabilistic_vehicle/modules/movement.asl") }
{ include("src/agt/probabilistic_vehicle/modules/traffic_management.asl") }
{ include("src/agt/probabilistic_vehicle/modules/coordination.asl") }

+name(N) <- 
    +get_name(N);
    .print("Vehicle ", N, " initialized").

+step_completed[source(percept)] : get_name(ME) <- 
    .print(ME, " completed its step");
    !loop.

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
    
    ?get_available_turns(X, Y, AvailableTurns);
    
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
    
    ?get_available_turns(X, Y, AvailableTurns);
    
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
    !check_coordination(NextX, NextY).

+!fallback_movement(X, Y) : get_name(ME) <-
    .print(ME, "[FALLBACK MOVEMENT:] from (", X, ",", Y, ")");
    NextX = X + 1;
    NextY = Y;
    !check_coordination(NextX, NextY).

{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }