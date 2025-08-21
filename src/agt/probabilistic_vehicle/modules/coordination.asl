// +agent_intention(Agent, X, Y, Action)[source(percept)] : get_name(ME) & Agent \== ME <-.
    // .print("[intention] notices that ", Agent, " wants to reach location: (", X, " ", Y, ")").

+!check_coordination(NextX, NextY) : get_name(ME) <-
    if (agent_intention(OtherAgent, NextX, NextY, _) & OtherAgent \== ME) {
        .print("[wait] ", OtherAgent, " at (", NextX, ",", NextY, ")");
        writeIntent(ME, "wait");
    } else {
        !check_traffic_light(NextX, NextY);
    }.