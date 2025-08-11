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

+light_state(X, Y, State)[source(percept)] <-
    -light_current_state(X, Y, _);
    +light_current_state(X, Y, State);
    .print(ME, " has seen traffic light at (", X, ",", Y, ") = ", State).

+light_state_changed(X, Y, State)[source(percept)] <- 
    -light_current_state(X, Y, _);
    +light_current_state(X, Y, State);
    .print(ME, " noticed that traffic light at (", X, ",", Y, ") changed to ", State).

+interval(I)[source(percept)] <-
    +simulation_interval(I).