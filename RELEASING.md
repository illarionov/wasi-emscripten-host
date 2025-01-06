# Releasing

1. Change the version in `config/version.properties` to a non-SNAPSHOT version.
2. Update the `CHANGELOG.md` for the impending release.
3. Update the `README.md` with the new version.
4. Updates samples with the new version.
5. `git commit -am "Prepare for release X.Y.Z."` (where X.Y.Z is the new version)
6. `git tag -a X.Y.Z -m "Version X.Y.Z"` (where X.Y.Z is the new version)
7. `git push && git push --tags`
8. Check that the "publish" workflow completed successfully on Github Actions
9.  Visit [Sonatype Central Portal](https://central.sonatype.com/publishing/deployments) and publish new deployment
10. Update the `config/version.properties` to the next SNAPSHOT version.
11. `git commit -am "Prepare next development version."`
12. `git push`
