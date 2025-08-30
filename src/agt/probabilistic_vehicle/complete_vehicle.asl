{ include("src/agt/probabilistic_vehicle/modules/perception.asl") }
{ include("src/agt/probabilistic_vehicle/modules/turn_discovery.asl") }
{ include("src/agt/probabilistic_vehicle/modules/movement.asl") }
{ include("src/agt/probabilistic_vehicle/modules/traffic_management.asl") }
{ include("src/agt/probabilistic_vehicle/modules/coordination.asl") }
{ include("src/agt/probabilistic_vehicle/modules/intersection_discovery.asl") }

+name(N)[source(ml)] <-
    +get_name(N);
    .print("[init-msg] id=", N).

+env_ready : get_name(ME) <-  
    .print("[init] received env_ready, joining workspace");
    joinWorkspace("/main/w");
    publishGridSize;
    .wait(1000); 
    !start.  

get_name(ME) :- name(ME).

+!choose_action : get_name(ME) & not at(ME, _, _) <-
    .wait(200);
    !choose_action.

+!choose_action : get_name(ME) & at(ME, X, Y) & not direction(X, Y, _) <-
    .wait(200);
    !choose_action.
    
+name(N) <- 
    +get_name(N);
    .print("[init] id=", N).

+step_completed[source(percept)] : get_name(ME) <- 
    .print("[step]");
    !loop.

+!start : get_name(ME) & env_ready <-
    lookupArtifact("trafficEnv", EnvId);
    focus(EnvId);
    .print("[start]");
    !choose_action.

+!start[error(action_failed)] : get_name(ME) & env_ready <-
    .print("[start] trafficEnv not available, retrying...");
    .wait(500);
    !start.

+!start <- 
    .print("[wait_name]");
    .wait(500);
    !start.

+!loop : get_name(ME) <- 
    .wait(200);
    !choose_action.

+!choose_action : get_name(ME) & at(ME, X, Y) & direction(X, Y, Direction) <- 
    .print("[choose] pos=(", X, ",", Y, ") dir=", Direction);
    explore(ME, X, Y, Direction);
    .wait(100);
    
    // ?get_available_intersections(X, Y, AvailableIntersections);
    ?get_available_turns(X, Y, AvailableTurns);
    
    if (has_available_intersections(X, Y)) {
        .print("[intersection] seen pos=(", X, ",", Y, ")");
        !decide_intersection_or_straight(X, Y, Direction);
    } else {
        if (AvailableTurns \== []) {
            .print("[turns] available=", AvailableTurns);
            !decide_turn_or_straight(X, Y, Direction, AvailableTurns);
        } else {
            !proceed_straight(X, Y, Direction);
        }
    }.


+!choose_action : get_name(ME) & at(ME, X, Y) <- 
    .print("[choose] pos=(", X, ",", Y, ") dir=unknown");
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
    TurnProbability = 1;
    
    if (RandomValue < TurnProbability) {
        .print("[decision] turn (r=", RandomValue, " < p=", TurnProbability, ")");
        !execute_turn(X, Y, AvailableTurns);
    } else {
        .print("[decision] straight (r=", RandomValue, " >= p=", TurnProbability, ")");
        !proceed_straight(X, Y, Direction);
    }.

+!decide_intersection_or_straight(X, Y, Direction) : get_name(ME) <-
    .random(R);
    if (R < 0.6) {
        Target = "straight";
    } 
    else {
        if (R < 0.85) {
            Target = "right";
        } else {
            Target = "left";
        }
    };
    .print("[intersection] target=", Target);
    !execute_intersection(Target).

+!execute_turn(X, Y, AvailableTurns) : get_name(ME) <-
    .length(AvailableTurns, NumTurns);
    .random(TurnIndex);
    TurnChoiceIndex = math.floor(TurnIndex * NumTurns);
    .nth(TurnChoiceIndex, AvailableTurns, [ToX, ToY]);
    .term2string(ToX, ToXStr);
    .term2string(ToY, ToYStr);
    .concat("turn:", ToXStr, Part1);
    .concat(Part1, ",", Part2);
    .concat(Part2, ToYStr, TurnAction);
    .print("[turn] to=(", ToX, ",", ToY, ") action=", TurnAction);
    writeIntent(ME, TurnAction).

+!execute_intersection(Target) : get_name(ME) <-
    .concat("intersection:", Target, Action);
    .print("[intersection] action=", Action);
    writeIntent(ME, Action).

+!proceed_straight(X, Y, Direction) : get_name(ME) <-
    ?next_position(X, Y, Direction, NextX, NextY);
    .print("[move] straight to=(", NextX, ",", NextY, ")");
    !check_coordination(NextX, NextY).

+!fallback_movement(X, Y) : get_name(ME) <-
    NextX = X + 1;
    NextY = Y;
    .print("[fallback] to=(", NextX, ",", NextY, ")");
    !check_coordination(NextX, NextY).

{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }