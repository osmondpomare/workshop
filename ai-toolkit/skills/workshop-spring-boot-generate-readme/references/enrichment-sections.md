# Optional Enrichment Sections

The the company template in `readme-template.md` is the default for internal microservices — keep it
lean and skip these. Add sections from this file only when the project doesn't fit that mold, for
example:

- A shared library published for other teams to consume (not a deployed microservice).
- A CLI tool or developer utility.
- A repo intended to be read by an external/public audience.

These are adapted from the general-purpose README prompt (`prompts/documentation/readme-generator.md`).
Pick only what's relevant — don't pad an internal microservice README with sections nobody will read.

## Table of Contents
Add when the README exceeds ~150 lines after filling in the template. Link every `##` heading.

## Badges
Build status, coverage, version, license — only if the project actually has CI badges available
(don't fabricate a Sonar/coverage badge URL that doesn't resolve).

## Quick Start
A 3-5 line "clone, configure, run" snippet above the full Installation section, for libraries or
tools where a reader wants to try it in under a minute. Redundant with "Ejecución del Proyecto" in
the standard template — use one or the other, not both.

## Features & Capabilities
A bullet list of what the library/tool does, useful for consumer-facing packages. Skip for internal
microservices — the API endpoints section already documents the contract.

## Contributing Guidelines
How to propose changes, code style, PR process. Relevant for shared libraries with multiple
contributing teams. Link to `CONTRIBUTING.md` if one exists instead of duplicating it.

## Troubleshooting / FAQ
Common setup problems and fixes. Only add entries you can verify from actual issues seen in the
codebase (error handling code, known workarounds in comments) — don't invent generic advice.

## Changelog
Link to `CHANGELOG.md` or GitHub releases if the project maintains one. Don't hand-write a version
history inside the README.

## License
Only for repos meant for external distribution. Internal the company microservices don't need this
section.
