---
name: workshop-spring-boot-generate-readme
description: Generates or updates a project's README.md by analyzing the actual codebase (build files, application config, OpenAPI spec, developer-portal/Backstage metadata, Docker/CI files) and filling in the company's standard Spring Boot / clean-architecture documentation template with real, verified values. Use when a project has no README, an outdated one, or needs one refreshed after a migration or feature change.
allowed-tools: Read, Write, Edit, Bash, Glob, Grep
---

# Generate/Update Project README

## Overview
- Produces a README.md grounded in facts extracted from the repository — not generic placeholder
  text. Every value in the output must be traceable to a file in the repo, or explicitly marked as
  unknown (see "Placeholders" below).
- Default output follows the the company standard template for Spring Boot / clean-architecture
  microservices (`references/readme-template.md`), written in Spanish, matching the style used
  across the company repos (`references/example-filled-readme.md` is a real, fully populated sample).
- Works in two modes: **create** (no README.md exists) and **update** (a README.md exists and needs
  refreshing without destroying content the user wrote by hand).

## When to Use
- Trigger: "generate a README for this project" / "create the README" — no README.md exists yet.
- Trigger: "update the README" / "the README is outdated" — refresh stale facts (versions, env
  vars, endpoints) while preserving hand-written prose.
- Trigger: after running a Java EE → Spring Boot migration skill, to document the resulting service.
- Trigger: "document this microservice" for a project that already has code but never had proper docs.

## Instructions

### 1. Detect mode and project shape
- Check for an existing `README.md` at the project root with `Read` (or `Glob`). If present →
  **update mode**; if absent → **create mode**.
- Determine the build tool: `build.gradle`/`build.gradle.kts` + `settings.gradle` (Gradle) vs
  `pom.xml` (Maven).
- Determine architecture style: presence of `co.com.bancolombia.cleanArchitecture` plugin and the
  `domain/model`, `domain/usecase`, `infrastructure/entry-points/*`, `infrastructure/driven-adapters/*`
  folder layout signals the Gradle Clean Architecture pattern; otherwise treat it as a standard
  layered Spring Boot project.
- Check for `.metadata/catalog-info.yaml` and `.metadata/api-info.yaml` — their presence signals an
  internal the company microservice (use the standard template as-is). Their absence plus a `pom.xml`
  `<packaging>jar</packaging>` with no `@SpringBootApplication` main class may signal a shared
  library — consider `references/enrichment-sections.md` instead of forcing the microservice
  template on it.
> **Validate**: you can state in one sentence which build tool, architecture style, and template
> variant apply before writing anything.

### 2. Gather facts
- Walk `references/data-sources.md` section by section — it maps every template placeholder to the
  exact file/pattern to grep for (project name, Java/Spring Boot versions, databases, env vars,
  endpoints, deployment, contact/owner).
- Use `Grep` for the `${VAR_NAME:default}` pattern across all `application*.yml`/`.properties`
  files in every module (not just the top-level one) to build the complete environment variable table.
- If an OpenAPI spec exists (`.metadata/openapi.yaml` or similar), parse its `paths:` to document
  endpoints; otherwise grep controller/router classes for actual routes.
- Do not run any commands that mutate the repo, hit the network, or start services — this is a
  read-only analysis pass.
> **Validate**: every fact you plan to write down has a specific file path (or command output) as
> its source.

### 3. Build the content
- Follow `references/readme-template.md` section-by-section, in the exact order given.
- Fill each `{{PLACEHOLDER}}` with the fact gathered in step 2.
- **Placeholders you cannot fill:**
  - Secrets/credentials with no default value in config → literal text `*Solicitar a the company*`.
  - Non-derivable info (architecture diagram image, wiki URL, Sonar/FluidAttacks project slug not
    already recorded anywhere) → literal text `_Pendiente por definir_`.
  - **Never** invent a plausible-looking hostname, port, table name, or credential to fill a gap.
- If the project doesn't match the internal-microservice mold (see step 1), pull additional
  sections from `references/enrichment-sections.md` instead of, or alongside, the standard template
  — use judgment, don't mechanically append every optional section.
> **Validate**: search the draft for the literal string `{{` — zero matches means every placeholder
> was resolved or explicitly marked.

### 4. Update mode specifics
- Read the existing README fully before changing anything.
- For each template section: if it exists and its content is still accurate, leave it; if it exists
  but contains stale facts (old version numbers, removed env vars, endpoints that no longer exist),
  refresh only the stale parts — keep the surrounding hand-written prose (project description,
  architecture narrative) intact.
- If a required template section is missing from the existing README, add it in the correct
  position (per `readme-template.md` ordering).
- If the existing README has extra custom sections not in the template (e.g. a "Known Issues" or
  team-specific runbook section), **keep them** — don't delete content you didn't generate.
- Use `Edit` for targeted section updates rather than rewriting the whole file with `Write`, so the
  diff stays reviewable.
> **Validate**: diff the before/after — every removed line should correspond to a fact that is
> actually wrong or obsolete, never to prose that was merely hard to regenerate.

### 5. Final review
- Re-check the environment variable table against the actual config files — no row invented, no
  real variable missing.
- Re-check that command blocks (`build`, `run`, `test`, `docker`) match the detected build tool
  exactly (don't emit Maven commands for a Gradle project or vice versa).
- Confirm no real secret value was written into the file (grep the draft for suspicious `=` or `:`
  followed by a long random-looking string near `PASSWORD`/`SECRET`/`TOKEN`/`KEY`).
> **Validate**: the file renders as valid Markdown (tables aligned, code fences closed) and contains
> no leaked credentials.

## Examples

### Create mode — minimal env var table row
Given this line found in `applications/app-service/src/main/resources/application.yaml`:
```yaml
infrastructure:
  r2dbc:
    postgresql:
      host: "${DB_POSTGRESQL_HOST:localhost}"
```
Produces:
```markdown
| DB_POSTGRESQL_HOST | Host de conexión a la base de datos PostgreSQL | localhost | Sí |
```

### Update mode — refreshing a stale fact
Existing README says:
```markdown
- Spring Boot 3.2
```
`build.gradle` now shows `springBootVersion = '4.1.0'`. Update only that line to:
```markdown
- Spring Boot (4.1.0)
```
Leave every other line in that section untouched.

## Best Practices
- Ground every sentence in something read from the repo; treat the README as documentation, not
  marketing copy.
- Keep the standard template's section order — engineers scanning multiple the company repos rely on that
  consistency.
- Prefer `references/example-filled-readme.md` as the calibration for tone and level of detail, not
  as a source of literal values.
- When in doubt about whether to include an optional section, lean toward the leaner internal
  template over the enrichment sections.

## Constraints and Warnings
- Never write a real secret, password, or token value into the README, even if you found one in a
  local `.env` or config file — use `*Solicitar a the company*` instead.
- Never fabricate links (SonarCloud, FluidAttacks, Grafana, wiki) that you did not find in the repo
  — leave the generic anchor (`#`) or `_Pendiente por definir_` rather than guessing a URL.
- Never delete hand-written content in update mode just because it's easier to regenerate from
  scratch — preserve what the user or a previous author wrote unless it is factually wrong.
- Never name individual people in the "Contacto" section — team/channel only.

## Reference Materials
- [README template](references/readme-template.md) — the section-by-section skeleton to fill in.
- [Fully populated example](references/example-filled-readme.md) — real reference README for tone/depth.
- [Data sources](references/data-sources.md) — where to find the value for every placeholder.
- [Optional enrichment sections](references/enrichment-sections.md) — for libraries/CLI tools/public repos.
