{ include("src/test/agt/test-probable_vehicle.asl") }

!run_tests.

+!run_tests <- 
    !test_vehicle_initialization;
    .print(" completed tests ");
    .stopMAS.