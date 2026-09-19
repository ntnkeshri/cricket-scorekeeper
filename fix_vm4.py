with open("app/src/commonMain/kotlin/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt", "r") as f:
    lines = f.readlines()

out = []
seen_imports = set()
for line in lines:
    if line.startswith("import "):
        if line in seen_imports:
            continue
        seen_imports.add(line)
    
    if line.startswith("import java") or line.startswith("import android"):
        continue

    # Skip all these invalid copy arguments
    if any(x in line for x in ["totalRuns = s", "totalExtras = s", "validBalls = s", "wickets = s", "@SuppressLint"]):
        continue

    out.append(line)

with open("app/src/commonMain/kotlin/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt", "w") as f:
    f.writelines(out)
