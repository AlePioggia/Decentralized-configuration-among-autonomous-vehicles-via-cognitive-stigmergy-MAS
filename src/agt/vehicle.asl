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

!start.

+!start : get_name(ME) <-
    .print("vehicle", ME, "starting");
    .print("Initial beliefs: ");
    !loop.

+!start <- 
    .print("waiting for the name drop...");
    .wait(500);
    !start.

+!loop : get_name(ME) <- 
    !choose_action;
    .wait(1000);
    step;
    readIntents(Intents);
    !loop.

+!choose_action : get_name(ME) & at(ME, X, Y) & direction(X, Y, Direction) <- 
    ?next_position(X, Y, Direction, NextX, NextY);

    if (not occupied(NextX, NextY)) {
        .print("agent can move.");
        .print("current position: ", X, " ", Y);
        writeIntent(ME, "follow");
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