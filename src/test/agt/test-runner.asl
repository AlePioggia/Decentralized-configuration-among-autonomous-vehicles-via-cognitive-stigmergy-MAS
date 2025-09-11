{ include("src/test/agt/test-probable_vehicle.asl") }

!run_tests.

+!run_tests <- 
    !test_vehicle_initialization;
    !test_next_position_north;
    !test_next_position_south;
    !test_next_position_east;
    !test_next_position_west;
    !test_execute_move_mock;
    !test_perception_update;
    !test_traffic_light_percept;
    !test_traffic_light_changed;
    !test_turn;
    !test_intersection_discovery;
    !test_agent_position_update;
    .wait(2000);
    .print(" completed tests ");
    .stopMAS.