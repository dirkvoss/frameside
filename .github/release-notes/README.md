# Release notes

Before tagging `vX.Y.Z`, add `X.Y.Z.md` here (English). CI turns it into the GitHub release.

- First line: `# Short title` (becomes "X.Y.Z – Short title").
- Sections: `### New`, `### Changed`, `### Fixed`.
- **Upgrade notes:** if anything needs action from users (renamed settings, new permissions, manual steps), add a `### Upgrade notes` section.
- Without a file, CI falls back to the matching CHANGELOG section.
