import re

with open("app/src/commonMain/kotlin/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt", "r") as f:
    code = f.read()

# Add missing TossDecision import if needed
if "import com.ntnkeshri.cricketscorekeeper.models.TossDecision" not in code:
    code = code.replace("import com.ntnkeshri.cricketscorekeeper.models.OverRecord",
                        "import com.ntnkeshri.cricketscorekeeper.models.OverRecord\nimport com.ntnkeshri.cricketscorekeeper.models.TossDecision\nimport com.ntnkeshri.cricketscorekeeper.models.BallEvent")

# Fix missing closing braces on the previous functions
code = code.replace("    fun resetMatch() {\n        _state.value = MatchState()", "    }\n\n    fun resetMatch() {\n        _state.value = MatchState()\n    }")

# Remove the broken duplicate functions
code = re.sub(r'    fun recordToss.*?handleEndOfOver\(next\)\)\n        }\n    }', '', code, flags=re.DOTALL)

with open("app/src/commonMain/kotlin/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt", "w") as f:
    f.write(code)

