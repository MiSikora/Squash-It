# Releasing

## Library

 1. Change the version in [`gradle.properties`](gradle.properties) to a non-SNAPSHOT version.
 2. Update [the `CHANGELOG.md`](CHANGELOG.md) for the impending release.
 3. Update [the `README.md`](README.md) with the new version.
 4. `git commit -am "Prepare library for release X.Y.Z."` (where `X.Y.Z` is the new version).
 5. `git tag -a lib-X.Y.Z -m "Library version X.Y.Z"` (where `X.Y.Z` is the new version).
 6. Update the [`gradle.properties`](gradle.properties) to the next SNAPSHOT version.
 7. `git commit -am "Prepare next development version."`
 8. `git push && git push --tags`
 9. Wait for [the CI server](https://app.bitrise.io/app/d05c685963b4f009) to upload the artifact.
 10. Visit [Sonatype Nexus](https://oss.sonatype.org) and promote the artifact.

 ## Application

 1. Update app version in [`build.gradle`](squash-it/app/gradle.properties).
 2. `git commit -am "Prepare app for release X.Y.Z."` (where `X.Y.Z` is the new version).
 3. `git tag -a app-X.Y.Z -m "Application version X.Y.Z"` (where `X.Y.Z` is the new version).
 4. `git push && git push --tags`
 5. Wait for [the CI server](https://app.bitrise.io/app/d05c685963b4f009) to upload the artifact.
 6. Visit [Play Store Console](https://play.google.com/apps/publish) and perform a rollout.
