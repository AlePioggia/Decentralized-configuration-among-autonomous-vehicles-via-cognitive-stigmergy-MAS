!move.

+!move <-
    writeIntent("2", "forward");
    .wait(1000);
    !move.

{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }