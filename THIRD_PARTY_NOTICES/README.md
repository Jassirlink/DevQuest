# Compose Lab review and attribution

Upstream: https://github.com/DimiVlachos/compose-lab
Reviewed checkout: `bd43f2e4129592a1de93cb1570548b7b50ad445a`.
License: MIT, copyright (c) 2026 Dimitris Vlachos. Full notice: `ComposeLab-LICENSE`.

Reviewed `popup/PopUpBook.kt`, `PopUpMath.kt`, `PopUpPainter.kt`, `pageturn/PageTurnMath.kt`, shared `perspective/Homography.kt`, and the upstream version catalog. The public `PopUpBook` API takes Painter cover art, PopUpSpread models and PopUpBookState. It relies on LabTheme, scene-specific geometry, bitmap preparation and multiplatform dependencies. Page-turn math models strips of a curled sheet. Neither component is a published Maven dependency used by this app.

Only `Homography.kt` was adapted into `:core:motion`. Changes: package relocation, attribution header and project formatting. It supplies plane projection, matrix conversion and inverse projection. DevQuest uses it for its own bounded architecture-board geometry, with reusable arrays and no bitmap allocation. Its tested inverse is retained for future planar hit testing; current inspection uses accessible buttons. The original popup/page-turn state machine, book artwork and demos are not shipped. Android uses its own Compose dependencies and original gesture/playback logic.

All boards, constellation points, emblem triangles, timeline lanes, colors, layouts, content and navigation are authored for DevQuest. No island, lighthouse, boat, windmill, chapel or demo narration is included.
