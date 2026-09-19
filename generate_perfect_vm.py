import re

with open("app/src/commonMain/kotlin/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt", "r") as f:
    code = f.read()

# Make sure imports are present
if "import androidx.lifecycle.ViewModel" not in code:
    code = code.replace("import com.ntnkeshri.cricketscorekeeper.models.MatchState",
"""import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntnkeshri.cricketscorekeeper.repository.MatchRepository
import com.ntnkeshri.cricketscorekeeper.repository.InMemoryMatchRepository
import com.ntnkeshri.cricketscorekeeper.models.MatchState
import com.ntnkeshri.cricketscorekeeper.models.BallEvent
import com.ntnkeshri.cricketscorekeeper.models.TossDecision
import kotlinx.coroutines.launch""")

# Fix the class signature
if "class ScoreViewModel : ViewModel() {" in code:
    code = code.replace("class ScoreViewModel : ViewModel() {",
"""class ScoreViewModel(
    private val repository: MatchRepository = InMemoryMatchRepository()
) : ViewModel() {""")

if "import com.ntnkeshri.cricketscorekeeper.models.BallEvent\nimport com.ntnkeshri.cricketscorekeeper.models.BallEvent" in code:
    code = code.replace("import com.ntnkeshri.cricketscorekeeper.models.BallEvent\nimport com.ntnkeshri.cricketscorekeeper.models.BallEvent", "import com.ntnkeshri.cricketscorekeeper.models.BallEvent")

with open("app/src/commonMain/kotlin/com/ntnkeshri/cricketscorekeeper/viewmodel/ScoreViewModel.kt", "w") as f:
    f.write(code)
