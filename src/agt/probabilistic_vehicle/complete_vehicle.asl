{ include("src/agt/probabilistic_vehicle/modules/perception.asl") }
{ include("src/agt/probabilistic_vehicle/modules/turn_discovery.asl") }
{ include("src/agt/probabilistic_vehicle/modules/movement.asl") }
{ include("src/agt/probabilistic_vehicle/modules/traffic_management.asl") }
{ include("src/agt/probabilistic_vehicle/modules/coordination.asl") }
{ include("src/agt/probabilistic_vehicle/modules/intersection_discovery.asl") }

+at(ME, X, Y) : goal(GX, GY) & not goal_reached(ME) <-
    isGoalReached(ME, X, Y, GX, GY, Reached);
    if (Reached) {
        +goal_reached(ME);
        .print("[goal-reached] ", ME, " (", GX, ",", GY, ") [distance <= 1]");
    }.

+name(N)[source(ml)] <-
    +get_name(N);
    .print("[init-msg] id=", N).

+env_ready : get_name(ME) <-  
    .print("[init] received env_ready, joining workspace");
    joinWorkspace("/main/w");
    lookupArtifact("trafficEnv", EnvId);
    focus(EnvId);
    lookupArtifact("light1", Light1Id);
    focus(Light1Id);
    lookupArtifact("light2", Light2Id);
    focus(Light2Id);
    publishGridSize;
    assignGoal;
    .wait(1000); 
    !start.  

get_name(ME) :- name(ME).

+!choose_action : get_name(ME) & not at(ME, _, _) <-
    .wait(200);
    !choose_action.

+!choose_action : get_name(ME) & at(ME, X, Y) & not direction(X, Y, _) <-
    .wait(200);
    !choose_action.
    
+name(N) <- 
    +get_name(N);
    .print("[init] id=", N).

+step_completed[source(percept)] : get_name(ME) & not goal_reached(ME) <- 
    .print("[step]");
    !loop.

+step_completed[source(percept)] : get_name(ME) & goal_reached(ME) <-
    .print("[step] ", ME, " has reached the goal and will not move.").

+!start : get_name(ME) & env_ready <-
    lookupArtifact("trafficEnv", EnvId);
    focus(EnvId);
    .print("[start]");
    !choose_action.

+!start[error(action_failed)] : get_name(ME) & env_ready <-
    .print("[start] trafficEnv not available, retrying...");
    .wait(500);
    !start.

+!start <- 
    .print("[wait_name]");
    .wait(500);
    !start.

+!loop : get_name(ME) & not goal_reached(ME) <- 
    .wait(100);
    !choose_action.

+!loop : get_name(ME) & goal_reached(ME) <-
    .print("[stop] ", ME, " simulation has been stopped").

+!choose_action : get_name(ME) & at(ME, X, Y) & direction(X, Y, Direction) <- 
    .print("[choose] pos=(", X, ",", Y, ") dir=", Direction);
    explore(ME, X, Y, Direction);
    .wait(100);

    ?goal_cells(GoalCells);
    if (.member([ME, X, Y], GoalCells)) {
        +goal(X, Y);
    } 
    if (.member([A, X, Y], GoalCells) & A \== ME) {
        discoverGoal(A, X, Y);
    }

    hasGoalBeenDiscovered(ME, Found, GX, GY);
    if (Found & not goal(GX, GY)) {
        +goal(GX, GY);
    }

    if (has_available_intersections(X, Y)) {
        if (goal(GX, GY)) {
            !decide_intersection_or_straight_goal_oriented(X, Y, GX, GY, Direction);
        } else {
            !decide_intersection_or_straight(X, Y, Direction);
        }
    } else {
        !proceed_straight(X, Y, Direction);
    }.


+!decide_intersection_or_straight_goal_oriented(X, Y, GX, GY, Direction) : get_name(ME) <-
    getBestIntersectionDirection(X, Y, GX, GY, Target);
    .print("[intersection-goal] target=", Target);
    !execute_intersection(Target).

+!decide_intersection_or_straight(X, Y, Direction) : get_name(ME) <-
    .random(R);
    if (R < 0.6) {
        Target = "straight";
    } 
    else {
        if (R < 0.85) {
            Target = "right";
        } else {
            Target = "left";
        }
    };
    .print("[intersection] target=", Target);
    !execute_intersection(Target).

+!execute_intersection(Target) : get_name(ME) <-
    .concat("intersection:", Target, Action);
    .print("[intersection] action=", Action);
    writeIntent(ME, Action).

+!proceed_straight(X, Y, Direction) : get_name(ME) <-
    ?next_position(X, Y, Direction, NextX, NextY);
    .print("[move] straight to=(", NextX, ",", NextY, ")");
    !execute_move(NextX, NextY).
    // !check_coordination(NextX, NextY).

+!fallback_movement(X, Y) : get_name(ME) <-
    NextX = X + 1;
    NextY = Y;
    .print("[fallback] to=(", NextX, ",", NextY, ")");
    !execute_move(NextX, NextY).
    // !check_coordination(NextX, NextY).

{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }