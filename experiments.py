import subprocess
import os

NUM_RUNS = 30
PROJECT_DIR = os.path.dirname(os.path.abspath(__file__))
LOG_DIR = os.path.join(PROJECT_DIR, "logs")
os.makedirs(LOG_DIR, exist_ok=True)

for i in range(1, NUM_RUNS + 1):
    log_file = os.path.join(LOG_DIR, f"run_{i}.log")

    with open(log_file, "w") as f:
        process = subprocess.Popen(
            ["gradlew.bat", "run"],
            cwd=PROJECT_DIR,
            stdout=f,
            stderr=subprocess.STDOUT
        )
        process.wait()