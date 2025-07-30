+at(ME, X, Y)[source(percept)] : true <-
    -at(me, _, _);
    +at(me, X, Y).
+occupied(X, Y)[source(percept)] : true <-
    -occupied(X, Y);
    +occupied(X, Y).

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

+!choose_action : at(me, X, Y) & not occupied(X + 1, Y) <-
    .print("agent 1 can move.");
    .print("current position: ", X, " ", Y);
    writeIntent(me, forward).

+!choose_action <- 
    .print("Agent 1 cannot move, waiting...").

{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }   