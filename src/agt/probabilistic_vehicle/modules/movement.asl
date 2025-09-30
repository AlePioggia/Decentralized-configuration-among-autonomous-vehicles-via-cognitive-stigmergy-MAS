+?next_position(X, Y, "East", NextX, NextY) <-
    ?grid_width(W);
    NextX = (X + 1) mod W;
    NextY = Y.

+?next_position(X, Y, "West", NextX, NextY) <-
    ?grid_width(W);
    NextX = (X - 1 + W) mod W;
    NextY = Y.

+?next_position(X, Y, "North", NextX, NextY) <-
    ?grid_height(H);
    NextX = X;
    NextY = (Y - 1 + H) mod H.

+?next_position(X, Y, "South", NextX, NextY) <-
    ?grid_height(H);
    NextX = X;
    NextY = (Y + 1) mod H.

// +?next_position(X, Y, _, NextX, NextY) <-
//     NextX = (X + 1);
//     NextY = Y.

+?next_position(X, Y, Dir, NextX, NextY) : not grid_width(_) | not grid_height(_) <-
    .print("[error] grid dimension not known, waiting...");
    .wait(200);
    ?next_position(X, Y, Dir, NextX, NextY).

+!execute_move(NextX, NextY) : get_name(ME) <-
    ?occupants(List);
    ?intentions(Intentions);
    .print("[DEBUG][Jason] occupants: ", List);
    if (not .member([NextX, NextY], List) & not .member([NextX, NextY], Intentions)) {
        writeIntent(ME, "follow");
    } else {
        .print("[wait] Cell (", NextX, ",", NextY, ") occupied, waiting");
        writeIntent(ME, "wait");
    }.