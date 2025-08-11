{ include("src/test/agt/test-probable_vehicle.asl") }

!run_tests.

+!run_tests <- 
    !test_vehicle_initialization;
    !test_next_position_north;
    !test_next_position_south;
    !test_next_position_east;
    !test_next_position_west;
    .print(" completed tests ");
    .stopMAS.