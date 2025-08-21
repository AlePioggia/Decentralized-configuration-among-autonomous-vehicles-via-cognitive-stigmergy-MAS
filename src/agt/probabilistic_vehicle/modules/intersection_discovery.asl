+intersection_available(X, Y)[source(percept)] : not known_intersection(X, Y) <-
    +known_intersection(X, Y);
    .print("[intersection] discovered (", X, ", ", Y, ") ").

+intersection_discovered(Agent, X, Y)[source(percept)] : 
    get_name(ME) & Agent \== ME <-
    +known_intersection(X, Y);
    .print("[intersection] learned from ", Agent, ": (", X, ", ", Y, ")").

has_available_intersections(X, Y) :- known_intersection(X, Y).

+?get_available_intersections(X, Y, L) <-
    if (known_intersection(X, Y)) { L = [[X, Y]]; } else { L = []; }.