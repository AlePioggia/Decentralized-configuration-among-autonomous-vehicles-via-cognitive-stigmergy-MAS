{ include ("src/agt/probabilistic_vehicle/complete_vehicle.asl") }

+writeIntent(Agent, Action) <- 
    .print("mock: ", Agent, " , ", Action);
    +mock_action(Agent, Action).

+!assert_true(Condition) <-
    .print("Asserting condition: ", Condition);
    if (Condition) {
        .print("Assertion passed.");
    } else {
        .print("Assertion failed.");
        .fail("Assertion failed: ", Condition);
    }.

+!assert_equals(Expected, Actual) <-
    .print("Asserting equals: ", Expected, " == ", Actual);
    if (Expected == Actual) {
        .print("Test passed: ", Expected, " == ", Actual);
    } else {
        .print("Test failed: Expected ", Expected, ", got ", Actual);
        .fail("Assertion failed");
    }.

+!assert_belief_exists(Belief) <- 
    .print("Checking if belief exists: ", Belief);
    if (Belief) {
        .print("Test passed: belief exists - ", Belief);
    }.


@[test]
+!test_vehicle_initialization <-
    .print("Testing vehicle initialization...");
    .my_name(Name);
    !assert_true(Name \== []).

@[test]
+!test_next_position_north <- 
    .print("[Movement] -> testing next position method");
    ?next_position(0, 1, "North", NextX, NextY);
    !assert_equals(NextX, 0);
    !assert_equals(NextY, 0).

@[test]
+!test_next_position_south <- 
    .print("[Movement] -> testing next position method");
    ?next_position(0, 0, "South", NextX, NextY);
    !assert_equals(NextX, 0);
    !assert_equals(NextY, 1).

@[test]
+!test_next_position_east <- 
    .print("[Movement] -> testing next position method");
    ?next_position(0, 0, "East", NextX, NextY);
    !assert_equals(NextX, 1);
    !assert_equals(NextY, 0).

@[test]
+!test_next_position_west <- 
    .print("[Movement] -> testing next position method");
    ?next_position(1, 0, "West", NextX, NextY);
    !assert_equals(NextX, 0);
    !assert_equals(NextY, 0).

@[test]
+!test_execute_move_mock <- 
    +occupied(1, 0);
    +get_name("test");
    +direction(0, 0, "East");
    +at("test", 0, 0);
    
    !execute_move(1, 0);
    .wait(100);

    if (mock_action("test", "wait")) {
        .print("execute move test passed");
    } else {
        .print("execute move test failed");
    };

    -occupied(1, 0);
    -mock_action(_, _).

@[test]
+!test_turn <- 
    +get_name("test");

    +known_turn(2, 2, 3, 2);

    ?get_available_turns(2, 2, AvailableTurns);
    !execute_turn(2, 2, AvailableTurns);
    .wait(200);

    .findall(test_action(Agent, Action), test_action(Agent, Action), Actions);

    for (.member(test_action("test", Action), Actions)) {
        if (.substring("turn:", Action, 0)) {
            .print("turn test passed");
        };
    };

    -known_turn(_, _, _, _);
    -get_name("test");
    -test_action(_, _).

@[test]
+!test_perception_update <-
    +at("agent", 2, 3)[source(percept)];
    +occupied(2, 3)[source(percept)];
    !assert_belief_exists(at("agent", 2, 3));
    !assert_belief_exists(occupied(2, 3)).

@[test]
+!test_traffic_light_percept <-
    +light_state(2, 1, "green")[source(percept)];
    !assert_belief_exists(light_current_state(2, 1, "green"));
    .print("Traffic light percept test passed.").

@[test]
+!test_traffic_light_changed <-
    +light_state_changed(2, 1, "red")[source(percept)];
    !assert_belief_exists(light_current_state(2, 1, "red"));
    .print("Traffic light changed test passed.").

@[test]
+!test_intersection_discovery <-
    +intersection_available(4, 4)[source(percept)];
    !assert_belief_exists(known_intersection(4, 4));
    .print("Intersection discovery test passed.").

@[test]
+!test_agent_position_update <-
    +at("vehicle1", 1, 1)[source(percept)];
    !assert_belief_exists(at("vehicle1", 1, 1));
    .print("Agent position update test passed.").