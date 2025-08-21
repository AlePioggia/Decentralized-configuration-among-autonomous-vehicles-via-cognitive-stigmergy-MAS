+?next_position(X, Y, "East", NextX, NextY) <-
    NextX = X + 1;
    NextY = Y.

+?next_position(X, Y, "West", NextX, NextY) <-
    NextX = X - 1;
    NextY = Y.

+?next_position(X, Y, "North", NextX, NextY) <-
    NextX = X;
    NextY = Y - 1.

+?next_position(X, Y, "South", NextX, NextY) <-
    NextX = X;
    NextY = Y + 1.

+?next_position(X, Y, _, NextX, NextY) <-
    NextX = X + 1;
    NextY = Y.

+!execute_move(NextX, NextY) : get_name(ME) <-
    if (not occupied(NextX, NextY)) {
        writeIntent(ME, "follow");
    } else {
        .print("[wait] Cell (", NextX, ",", NextY, ") occupied, waiting");
        writeIntent(ME, "wait");
    }.