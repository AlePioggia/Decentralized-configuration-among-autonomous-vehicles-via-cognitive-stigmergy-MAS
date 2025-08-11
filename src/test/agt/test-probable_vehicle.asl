{ include ("src/agt/probabilistic_vehicle/complete_vehicle.asl") }

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
        .fail("Belief not found: ", Belief);
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