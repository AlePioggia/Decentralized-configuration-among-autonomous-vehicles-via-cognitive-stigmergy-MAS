+name(N) <-
    +get_name(N).

step_completed[source(percept)] : get_name(ME) <-
    !loop.

!start.

+!start : get_name(ME) <-
    !choose_action.

+start <-
    .print("[WAITING] name drop incoming...");
    .wait(500);
    !start.

!loop : get_name(ME) <-
    .wait(200);
    !choose_action.

+!choose_action : get_name(ME)
    .print("choosing action...").