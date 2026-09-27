# Doyoung Yang - Project 01 Retrospective

Repo: https://github.com/anntreasajojo/CST438Project1

## My work
- Merged PRs:
    - [#19 Landing page with meal sections, favorites, and profile](https://github.com/anntreasajojo/CST438Project1/pull/19)
    - [#23 Add Register page](https://github.com/anntreasajojo/CST438Project1/pull/23)
    - [#27 Add PMD, detekt, and CI for ./gradlew check](https://github.com/anntreasajojo/CST438Project1/pull/27)
    - [#35 Add Login page](https://github.com/anntreasajojo/CST438Project1/pull/35)
    - [#37 Add nutrition tracking chart page](https://github.com/anntreasajojo/CST438Project1/pull/37)
- My issues:
    - Closed: [#1 Login page](https://github.com/anntreasajojo/CST438Project1/issues/1) | [#2 Register Page](https://github.com/anntreasajojo/CST438Project1/issues/2) | [#3 Landing Page](https://github.com/anntreasajojo/CST438Project1/issues/3) | [#10 nutrition tracking chart page](https://github.com/anntreasajojo/CST438Project1/issues/10) | [#26 Add PMD static analysis checks](https://github.com/anntreasajojo/CST438Project1/issues/26)
    - Still open: [#9 Reminders](https://github.com/anntreasajojo/CST438Project1/issues/9). It was assigned to me and I did not build it.
- PRs I reviewed and merged for teammates: [#18](https://github.com/anntreasajojo/CST438Project1/pull/18) | [#22](https://github.com/anntreasajojo/CST438Project1/pull/22) | [#33](https://github.com/anntreasajojo/CST438Project1/pull/33) (requested changes first, then approved) | [#38](https://github.com/anntreasajojo/CST438Project1/pull/38) | [#41](https://github.com/anntreasajojo/CST438Project1/pull/41) | [#45](https://github.com/anntreasajojo/CST438Project1/pull/45)
- What I built:
    - The landing ("Today") screen that the other meal screens hang off: calorie total against the daily goal, macro totals, one section per meal, and the bottom tab bar with Favorites and Profile. I opened it in the first week so the rest of the team had a layout and a navigation shell to plug their screens into.
    - The register and login flow. Registration is a two-step wizard: account details, then onboarding answers that calculate daily calorie and macro targets. Passwords are hashed with PBKDF2 and a per-user salt instead of being stored in plain text. Login is required on app start.
    - The team's quality tooling: PMD and detekt wired into `./gradlew check`, and a GitHub Actions workflow that runs the same check on every PR and uploads the reports.
    - The Charts page (daily and weekly graphs, monthly calendar), plus dated meal records saved per user in Room, with a migration from database version 2 to 3 so existing data was kept.
    - A build fix (commit `16c2b3b`) for a crash that made every build after the first one fail.
## Biggest challenge
The biggest challenge was a build that only failed the second time. A fresh clone built and ran fine, but every build after that crashed while Room was writing its schema JSON, so the project broke on anyone's machine as soon as they rebuilt. It was hard because the first build succeeding made it look like each person's local setup was the problem, not the project, and the stack trace pointed at Room and KSP internals rather than at anything we had written. Several of us were hitting it at the same time. I handled it by reproducing it on purpose with consecutive clean builds, which showed it was tied to the Kotlin 2.0.0 and KSP versions rather than to any one machine. I upgraded Kotlin to 2.1.20 with the matching KSP version, removed a dependency we did not need, and checked the fix with repeated clean builds and the instrumented tests before pushing. I also noted in the commit that Android Studio might need an update, because the Compose compiler version moves with Kotlin.

## Most valuable thing I learned
A well-built, well-documented feature can still be a problem for the team if the decision to build it was not made together. My PRs had long descriptions and passed CI, and teammates said the descriptions were helpful. But the team feedback also said I added things like goal and body-metric targets after the team had agreed to keep Project 01 simple, and that I did not say upfront when I was using AI to produce code. Neither of those is a code-quality problem. They are agreement problems, and they had a real cost: the dated meal records I added for the Charts page covered the same data as a teammate's open entries-table PR ([#29](https://github.com/anntreasajojo/CST438Project1/pull/29)), which was then closed unmerged. A short message before I started would have avoided that duplicated work. Explaining a change after it is written is not the same as agreeing on it before it is written, and a large PR that arrives as a surprise puts reviewers in the position of either accepting it or throwing away a lot of work. I also learned that tooling is one of the most useful things one person can give a team: once `./gradlew check` ran in CI, reviewers could start from "the check passes" instead of pulling and building every branch.

## What I carry into Project 02
Both of these come directly from the Final Teammate Review. I accept both pieces of feedback and am not dismissing either.

1. **Agree on scope before I code anything beyond the issue.** Before I start work that goes past what an issue's description asks for (extra screens, extra calculations, extra options), I will post the proposed addition as a comment on the issue or as a new issue and wait for at least one teammate to agree before writing code. I will know it worked if, at the Project 02 midpoint, every one of my PRs either matches its linked issue's description or links to a scope issue or comment that a teammate agreed to before the PR was opened, and no teammate has been surprised by a feature in one of my PRs.
2. **Say upfront when I use AI, and say it in writing.** At the Project 02 kickoff I will raise AI use as a team agreement question so the group decides what is acceptable. After that, every PR I open will have an "AI use" section that says whether I used AI, for which parts, and what I checked by hand. I will know it worked if 100% of my Project 02 PRs contain that section, and if the team's AI agreement is written down in the repo (README or docs) by the end of the first week.
 