import re
import glob
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from scipy.stats import ttest_ind, mannwhitneyu, shapiro

PATTERN_STIGMERGY = "summary_complete_vehicle_*.txt"
PATTERN_NO_STIGMERGY = "summary_no_stigmergy_*.txt"

def parse_summary_file(filepath):
    metrics = {}
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()

        patterns = {
            "total_steps": r"Total steps:\s*([\d\.]+)",
            "goals_reached": r"Goals reached:\s*([\d\.]+)",
            "elapsed_time": r"Elapsed time:\s*([\d\.]+)",
            "step_stddev": r"Step stddev:\s*([\d\.]+)",
            "time_stddev": r"Time stddev:\s*([\d\.]+)",
            "step_equity": r"Step equity ratio:\s*([\d\.]+)",
            "time_equity": r"Time equity ratio:\s*([\d\.]+)",
            "iterations": r"Number of iterations:\s*([\d\.]+)"
        }

        for key, pattern in patterns.items():
            match = re.search(pattern, content)
            metrics[key] = float(match.group(1)) if match else np.nan

    return metrics

def load_all_results():
    stig_files = glob.glob(PATTERN_STIGMERGY)
    nostig_files = glob.glob(PATTERN_NO_STIGMERGY)

    data = []
    for f in stig_files:
        m = parse_summary_file(f)
        m["controller"] = "stigmergy"
        m["file"] = f
        data.append(m)

    for f in nostig_files:
        m = parse_summary_file(f)
        m["controller"] = "no_stigmergy"
        m["file"] = f
        data.append(m)

    df = pd.DataFrame(data)
    return df

def run_statistics(df, metric):
    group1 = df[df["controller"] == "stigmergy"][metric].dropna()
    group2 = df[df["controller"] == "no_stigmergy"][metric].dropna()

    print(f"\nAnalisi metrica: {metric}")
    print(f"Stigmergy: media={group1.mean():.2f}, std={group1.std():.2f}, median={group1.median():.2f}")
    print(f"No Stigmergy: media={group2.mean():.2f}, std={group2.std():.2f}, median={group2.median():.2f}")

    p_norm1 = shapiro(group1)[1]
    p_norm2 = shapiro(group2)[1]
    normal = p_norm1 > 0.05 and p_norm2 > 0.05

    if normal:
        stat, p = ttest_ind(group1, group2, equal_var=False)
        test_name = "t-test"
    else:
        stat, p = mannwhitneyu(group1, group2, alternative="two-sided")
        test_name = "Mann-Whitney U"

    print(f"🧪 {test_name}: stat={stat:.3f}, p-value={p:.5f}")
    if p < 0.01:
        print("Significant difference (p < 0.01)")
    else:
        print("No significant difference (p >= 0.01)")

def plot_distributions(df, metric):
    plt.figure(figsize=(8,5))
    sns.boxplot(x="controller", y=metric, data=df, palette=["red", "blue"])
    sns.stripplot(x="controller", y=metric, data=df, color="black", alpha=0.5)
    plt.title(f"Confronto {metric}")
    plt.show()

if __name__ == "__main__":
    df = load_all_results()
    print("Loaded files:", len(df))
    print(df.head())

    df["steps_per_goal"] = df["total_steps"] / df["goals_reached"].replace(0, np.nan)

    for metric in ["goals_reached", "total_steps", "steps_per_goal", "step_equity"]:
        run_statistics(df, metric)
        plot_distributions(df, metric)

    plt.figure(figsize=(7,5))
    sns.scatterplot(data=df, x="total_steps", y="goals_reached", hue="controller", style="controller", s=80)
    plt.title("Efficienza vs Efficacia")
    plt.show()
