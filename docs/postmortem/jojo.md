# Ann Jojo - Project 01 Retrospective

## My work

- Merged PRs:
  - [#45 Initialize README with project details and setup instructions](https://github.com/anntreasajojo/CST438Project1/pull/45)
  - [#43 Ann/add food button functionality](https://github.com/anntreasajojo/CST438Project1/pull/43)
  - [#38 Connected lunch and dinner screen to the landing page](https://github.com/anntreasajojo/CST438Project1/pull/38)
  - [#30 Ann/dinner page](https://github.com/anntreasajojo/CST438Project1/pull/30)
  - [#24 Ann/lunch page](https://github.com/anntreasajojo/CST438Project1/pull/24)
  - [#16 Ann/breakfast page](https://github.com/anntreasajojo/CST438Project1/pull/16)

- My issues:
  - [#39 Implement functionality for the Add Food button](https://github.com/anntreasajojo/CST438Project1/issues/39)
  - [#36 Connect Meal Screens to Landing Screen](https://github.com/anntreasajojo/CST438Project1/issues/36)
  - [#6 Dinner Page](https://github.com/anntreasajojo/CST438Project1/issues/6)
  - [#5 Lunch Page](https://github.com/anntreasajojo/CST438Project1/issues/5)
  - [#4 Breakfast Page](https://github.com/anntreasajojo/CST438Project1/issues/4)

- What I built: I helped build the nutrition app by creating the breakfast, lunch, and dinner screens, adding the Add Food button functionality, and connecting the meal screens to the landing page. I also helped write to the project README with setup instructions. Overall, my contributions were on creating part of the screens users interact with and making sure the screens connected correctly within the rest of the application.

## Biggest challenge

My biggest challenge was learning Kotlin and getting the Android application to run correctly on my device. The emulator did not connect to my device many times which made it difficult to test my work and figure out whether my errors came from the code or from the development environment. I also had to connect my screens to the landing page which was done by another teammate. This required me to understand code that I had not written and figure out how my screens would fit into the existing structure.

I handled this by watching youTube tutorials about Android Studio, running the entire application instead of just previewing individual files I was responsible for, and understanding the landing page code before connecting my screens. I also communicated with my teammates when I was unsure about how their code, specifically when I had to connect the screens to the landing page and  when I was switching from temporary data to API returned data and they were able to help  me out.

## Most valuable thing I learned

The most valuable thing I learned was that a feature is not complete just because the screen looks right. It also needs to work when connected to the rest of the application and be able to handle unexpected user input. For example, while I was testing the Dinner screen, I created a test for calorie values less than zero. The test failed at first, which showed that I needed to fix that edge case. However, after updating the screen logic, all seven tests passed.

This taught me to test both the expected user behavior and expected ones too that could break the app. I also learned that testing early is very important because separate screens can work individually but can fail when the user moves through the full application.

## What I carry into Project 02

1. I will request at least one reviewer when I open each pull request and give them enough time to look at the changes before merging. I will know this worked if every feature PR I open in Project 02 has a reviewer requested before it is merged.

2. I will open pull requests as soon as a feature is ready instead of letting completed work sit around too long. I will know this worked if my Project 02 PRs are opened on the same day as the last commit on the feature branch.
