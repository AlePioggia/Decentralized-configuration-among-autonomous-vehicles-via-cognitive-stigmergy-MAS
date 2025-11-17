# Decentralized configuration among autonomous vehicles via cognitive stigmergy: Design of a multi-agent system

This project concerns the creation of a multi-agent system that manages the coordination and modeling of autonomous vehicles. Its distinctive feature is the approach to communication and coordination between agents, which is entirely based on the concept of cognitive stigmergy.

## User guide

Quick start guide for the **traffic MAS** project.

### Prerequisites

- **Git**;
- **Gradle**;
- **Java (recent)**.

The sections below explain how to:

- Clone and set up the project;
- start the simulation.

### 1. Clone the project

```
git clone https://github.com/AlePioggia/Decentralized-configuration-among-autonomous-vehicles-via-cognitive-stigmergy-MAS.git
```

### 2. Run the code

Once you're inside the project directory, run: 
```
./gradlew run
```

## Thesis experiments and statistical tests

This guide shows how to reproduce the experiments, you can skip the experiments part, if you want to just look at the results

### Experiments

In order to try out the software, run the simulation (default: 30 runs) you should run the experiments script, in the project root (python needed):

```
python3 experiments.py
```

This test will make 30 runs locally and for each one of them, a file containing its metrics will be saved in the results folder. 
The experiments script by default considers the no stigmergy version of the controller, in order to modify this, you should:

#### 1. Modify the script in src/agt/probabilistic_vehicle/modules folder, named spawner.asl

You can choose between these two versions, in order to test the no stigmergy version or the stigmergy version. You can simply copy and substitute the spawn_loop rule with the version its needed to test.

Version for controller with no stigmergy:

```
+!spawn_loop(I, N, Seed) <-
    if (I <= N) {
        pickRandomFreeRoadCell(Seed, X, Y);
        if (X \== -1 & Y \== -1) {
            .concat("vehicle", I, Name);
            Params = [name(Name)];
            .create_agent(Name, "probabilistic_vehicle/no_stigmergy.asl", Params);
            .send(Name, tell, name(Name));
            placeAgent(Name, X, Y, Result);
            .send(Name, tell, env_ready);
            .print("[spawner] ", Name, " position: ", X, ",", Y, " result=", Result);
            .wait(500);
            I1 = I + 1;
            !spawn_loop(I1, N, Seed)
        } else {
            .print("[spawner] no free cell, retry...");
            .wait(300);
            !spawn_loop(I, N, Seed)
        }
    } else {
        .print("[spawner] done.");
        setSpawnComplete;
    }.
```
Version for controller using stigmergy: 
```
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
            .wait(500);
            I1 = I + 1;
            !spawn_loop(I1, N, Seed)
        } else {
            .print("[spawner] no free cell, retry...");
            .wait(300);
            !spawn_loop(I, N, Seed)
        }
    } else {
        .print("[spawner] done.");
        setSpawnComplete;
    }.
```

#### 2. Change metrics destination, in directory src/env/traffic, in the TrafficEnvironment class (Cartago)

Controller with stigmergy version:
```
    private void writeSummaryToFile() {
        try {
            String controller = System.getProperty("sim.controller", "complete_vehicle");
            String outDir = System.getProperty("sim.outdir", "results");
            String runId = System.getProperty("sim.runId", Long.toString(System.currentTimeMillis()));

            java.nio.file.Path dir = java.nio.file.Paths.get(outDir, controller);
            java.nio.file.Files.createDirectories(dir);

            java.nio.file.Path out = dir.resolve("summary_" + controller + "_" + runId + ".txt");
            String summary = (metrics != null && metrics.getSummaryString() != null)
                    ? metrics.getSummaryString()
                    : "No summary";
            java.nio.file.Files.writeString(out, summary);
            System.out.println("[SUMMARY] written to " + out.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("[SUMMARY][ERROR] " + e.getMessage());
        }
    }
```

Controller with no stigmergy version:
```
    private void writeSummaryToFile() {
        try {
            String controller = System.getProperty("sim.controller", "no_stigmergy");
            String outDir = System.getProperty("sim.outdir", "results");
            String runId = System.getProperty("sim.runId", Long.toString(System.currentTimeMillis()));

            java.nio.file.Path dir = java.nio.file.Paths.get(outDir, controller);
            java.nio.file.Files.createDirectories(dir);

            java.nio.file.Path out = dir.resolve("summary_" + controller + "_" + runId + ".txt");
            String summary = (metrics != null && metrics.getSummaryString() != null)
                    ? metrics.getSummaryString()
                    : "No summary";
            java.nio.file.Files.writeString(out, summary);
            System.out.println("[SUMMARY] written to " + out.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("[SUMMARY][ERROR] " + e.getMessage());
        }
    }
```

### Statistical tests

Once the experiments are done, only after, statistical tests can be conducted by running:

```
python3 statistical_test.py
```

The results will be shown in the console and in the python plots.

