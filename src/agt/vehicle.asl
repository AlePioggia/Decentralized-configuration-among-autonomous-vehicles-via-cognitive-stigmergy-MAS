+name(N) <- 
    +get_name(N).

+at(ME, X, Y)[source(percept)] : get_name(ME) <-
    -at(ME, _, _);
    +at(ME, X, Y).

+occupied(X, Y)[source(percept)] <-
    -occupied(X, Y);
    +occupied(X, Y).

+direction(X, Y, Direction)[source(percept)] <- 
    -direction(X, Y, _);
    +direction(X, Y, Direction);
    .print("Cell Direction, ", X, " ", Y, " ", Direction).

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
    .print(ME, " has seen traffic light at ", X, " , ", Y).

+light_state_changed(X, Y, State)[source(percept)] <- 
    -light_current_state(X, Y, _);
    +light_current_state(X, Y, State);
    .print(ME, " noticed that traffic light at ", X, " , ", Y, " changed").

!start.

+!start : get_name(ME) <-
    .print("vehicle ", ME, " starting");
    !choose_action.

+!start <- 
    .print("waiting for the name drop...");
    .wait(500);
    !start.

+!loop : get_name(ME) <- 
    !choose_action.

+!choose_action : get_name(ME) & at(ME, X, Y) & direction(X, Y, Direction) <- 
    ?next_position(X, Y, Direction, NextX, NextY);

    hasTrafficLightAt(NextX, NextY, HasLight);

    if (HasLight) {
        !traffic_light_handler(NextX, NextY);
    } else {
        !ordinary_logic(NextX, NextY);
    }.

+!traffic_light_handler(NextX, NextY) : get_name(ME) <-
    if (light_current_state(NextX, NextY, "green")) {
        .print(ME, " is in front of a green light, it can proceed");
        !ordinary_logic(NextX, NextY);
    } else {
        .print(ME, "stop at light");
        writeIntent(ME, "wait");
    }.

+!traffic_light_handler(NextX, NextY) : get_name(ME) & 
    not light_current_state(NextX, NextY, _) <-
    .print(ME, "stop at light");
    writeIntent(ME, "wait").

+!ordinary_logic(NextX, NextY) : get_name(ME) <- 
    if (not occupied(NextX, NextY)) {
        if (agent_intention(OtherAgent, NextX, NextY, _) & OtherAgent \== ME) {
            writeIntent(ME, "wait");
        } else {
            .print("agent can move.");
            writeIntent(ME, "follow");
        }
    } else {
        .print("agent wants to wait for its turn");
        writeIntent(ME, "wait");
    }.

+?next_position(X, Y, "East", NextX, NextY) <-
    NextX = X + 1;
    NextY = Y.

+?next_position(X, Y, "West", NextX, NextY) <-
    NextX = X -1 ;
    NextY = Y.

{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }   