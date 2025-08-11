+!check_traffic_light(NextX, NextY) : get_name(ME) <-
    hasTrafficLightAt(NextX, NextY, HasLight);
    
    if (HasLight) {
        !handle_traffic_light(NextX, NextY);
    } else {
        !execute_move(NextX, NextY);
    }.

+!handle_traffic_light(NextX, NextY) : get_name(ME) <-
    if (light_current_state(NextX, NextY, "green")) {
        .print(ME, "Green light at (", NextX, ",", NextY, "), proceeding");
        !execute_move(NextX, NextY);
    } else {
        .print(ME, "Red light at (", NextX, ",", NextY, "), waiting");
        writeIntent(ME, "wait");
    }.

+!handle_traffic_light(NextX, NextY) : get_name(ME) & 
    not light_current_state(NextX, NextY, _) <-
        .print(ME, "Unknown light state at (", NextX, ",", NextY, "), waiting");
        writeIntent(ME, "wait").