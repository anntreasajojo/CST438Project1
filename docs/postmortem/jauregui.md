# Jayson Jauregui - Project 01 Retrospective

## My work
- Merged PRs: [#33 Jayson/favorites](https://github.com/anntreasajojo/CST438Project1/pull/33) | [#42 Updated Favorites page](https://github.com/anntreasajojo/CST438Project1/pull/42) | [#22 Jayson/categories](https://github.com/anntreasajojo/CST438Project1/pull/22) | [#20 Completed Categories Page](https://github.com/anntreasajojo/CST438Project1/pull/20) | [#17 Jayson/categories](https://github.com/anntreasajojo/CST438Project1/pull/17)
- My issues: [#14 Food type categories](https://github.com/anntreasajojo/CST438Project1/issues/14) | [#15 Favorites](https://github.com/anntreasajojo/CST438Project1/issues/15)
- What I built: Categories screen with collapsible food sections by meal type, heart button to favorite foods, navigation wired into MainActivity so the Favorites tab and Categories screen share the same data, and real macro values (carbs/protein/fat) surfaced in both screens.

## Biggest challenge
The hardest part was the kotlinx-serialization version mismatch that broke the build for everyone. The error wasn't obvious without deep research and it screwed everything up if you wanted to rerun the project an hour or day later.  it showed up as an `AbstractMethodError` deep in Gradle output and had nothing to do with any code I wrote. Diagnosing it took a long time because the error message pointed at library internals, not at the actual cause. I eventually fixed it by forcing a specific version of `kotlinx-serialization` in the Gradle config. On the git side, a botched pull request left my branch with a messy, repetitive commit history that kept getting rejected on push, and I ended up having to delete the branch and start fresh to get clean.

## Most valuable thing I learned
- Due to this being my first time using Android Studio, I wasn't aware of the built-in emulator that I had to download to see anything I was writing. My first pull to main was just a plain-text screen of basic info. Thanks to my teammates, they explained and guided me through the process of downloading everything.
- Don't run the preview on the files directly. I was left confused as to why my Categories page wasn't leading to anywhere. That's because I was running my file's preview and NOT running the entire app. (duh)

## What I carry into Project 02
1. Test that screens actually connect to each other before calling a feature done. I will know it worked if I can tap through the full user flow on the emulator before opening any PR.
2. Test on the emulator earlier, not just in the preview. I will know it worked if I run the app on a device at least once per feature before opening a PR, so I catch wiring problems before review instead of after.
