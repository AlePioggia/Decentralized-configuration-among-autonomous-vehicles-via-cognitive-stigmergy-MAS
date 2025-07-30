+at(ME, X, Y)[source(percept)] <-
    -at(me, _, _);
    +at(me, X, Y).

+occupied(X, Y)[source(percept)] <-
    -occupied(X, Y);
    +occupied(X, Y).

+direction(X, Y, Direction)[source(percept)] <- 
    -direction(X, Y, _);
    +direction(X, Y, Direction);
    .print("Cell Direction, ", X, " ", Y, " ", Direction).

!start.

+!start <-
    .print("vehicle 1 starting");
    .print("Initial beliefs: ");
    !loop.

+!loop <- 
    !choose_action;
    .wait(1000);
    step;
    readIntents(Intents);
    !loop.

+!choose_action : at(me, X, Y) & direction(X, Y, Direction) <- 
    ?next_position(X, Y, Direction, NextX, NextY);

    if (not occupied(NextX, NextY)) {
        .print("agent can move.");
        .print("current position: ", X, " ", Y);
        writeIntent(me, "follow");
    } else {
        .print("agent wants to wait for its turn");
        writeIntent(me, "wait");
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