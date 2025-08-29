{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }

+!start <- 
    lookupArtifact("trafficEnv", EnvId);
    focus(EnvId);
    getSimAgents(NofAgents);
    getSimSeed(Seed);
    .print("[spawner] spawned.. agents: ", NofAgents, " seed: ", Seed);
    !spawn_loop(1, NofAgents, Seed).

+!spawn_loop(I, N, Seed) <-
    if (I <= N) {
        pickRandomFreeRoadCell(Seed, X, Y);
        if (X \== -1 & Y \== -1) {
            .concat("vehicle", I, Name);
            Params = [name(Name)];
            .create_agent(Name, "probabilistic_vehicle/complete_vehicle.asl", Params);
            .send(Name, tell, name(Name));
            placeAgent(Name, X, Y, Result);
            .send(Name, tell, env_ready);
            .print("[spawner] ", Name, " position: ", X, ",", Y, " result=", Result);
            .wait(1000);
            I1 = I + 1;
            !spawn_loop(I1, N, Seed)
        } else {
            .print("[spawner] no free cell, retry...");
            .wait(300);
            !spawn_loop(I, N, Seed)
        }
    } else {
        .print("[spawner] done.");
    }.

    
+!spawn_loop(I, N, _) : I > N.

!start.