# DevQuest architecture
Android-only, single activity, Compose UI. Features render immutable state and emit typed actions to the activity-scoped Hilt ViewModel; navigation state and temporary selections use SavedStateHandle / saveable state. Domain and model are pure Kotlin. Repositories expose Flow; data implements transactional Room writes and Preferences DataStore. Feature modules never see DAOs.

Content uses stable IDs and a versioned bundled JSON document. Content tables and user tables are separate. Completion and rewards are one Room transaction. Reward keys provide exactly-once credit, including zero-credit first quiz attempts. Activities store ISO local calendar dates at recording time; Clock is injectable. Reset deletes user tables only.

Motion is local, no remote assets. Only the small Compose Lab Homography helper is adapted, with MIT attribution. Original architecture boards use that projection; other scenes use Canvas and bounded graphicsLayer transforms. Explicit play controls, lifecycle cancellation and reduced motion keep animation bounded.
