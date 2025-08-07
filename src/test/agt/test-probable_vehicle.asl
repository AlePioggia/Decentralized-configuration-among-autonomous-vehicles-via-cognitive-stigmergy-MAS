{ include ("src/agt/probabilistic_vehicle.asl") }

+!assert_true(Condition) <-
    .print("Asserting condition: ", Condition);
    if (Condition) {
        .print("Assertion passed.");
    } else {
        .print("Assertion failed.");
        .fail("Assertion failed: ", Condition);
    }.

@[test]
+!test_vehicle_initialization <-
    .print("Testing vehicle initialization...");
    .my_name(Name);
    !assert_true(Name \== []).