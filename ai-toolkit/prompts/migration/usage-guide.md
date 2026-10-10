# Migration Prompt — Usage Guide

Practical, step-by-step instructions for running the migration playbook in this folder
([`java-ee-migration-plan.md`](java-ee-migration-plan.md)) against a real project. For copy-paste
execution prompts, see [`examples/execute-migration-plan.md`](../../examples/execute-migration-plan.md).

## How this prompt works

`java-ee-migration-plan.md` is a **playbook**, not a slash command. It is a plain Markdown file with
a numbered list of steps that an AI coding agent (Claude Code or Kiro CLI) reads and executes in
order. Every step in turn invokes one of the toolkit's skills (`/workshop-get-java-ee-project-context`,
`/workshop-migrate-java-ee-dto-clases-reactive`, etc.) with the parameters you provide up front.

You don't run this file as a command — you tell the agent, in chat, to execute it and pass the
required parameters.

## Execution modes

The plan supports **two mutually exclusive modes** (see
[Migration Modes](java-ee-migration-plan.md#migration-modes)):

| Mode | Source & target | Version control |
|------|-----------------|-----------------|
| **A — Git** | Cloned from GitHub repositories | Changes are committed and pushed to the target branch |
| **B — Local paths** | Folders already on the local machine | Nothing is cloned and nothing is pushed; changes stay in the local target folder |

Pick exactly one mode and provide only that mode's parameters — do not mix Git parameters with
local-path parameters in the same run.

## Prerequisites

- Target stack: **Spring Boot 4.1.X**, **Java 25**, **Scaffold Clean Architecture**, **Gradle** (see
  [Prerequisites](java-ee-migration-plan.md#prerequisites)).
- The toolkit (skills + steering rules) installed for your CLI — see the root
  [`README.md`](../../README.md#-getting-started) (`install-toolkit.sh`).
- **Mode A only:** SSH access configured for the GitHub organization you clone/push to (both the
  source and target repos are accessed over `git@github.com:...`).
- If the **target** destination is an empty folder/repo, the agent scaffolds a new Scaffold Clean
  Architecture project into it using the `/workshop-scaffold-clean-architecture` skill
  (see [Step 3.1](java-ee-migration-plan.md#31-scaffold-target-project-if-empty-both-modes)).

## Step 1 — Gather the required parameters

### Mode A — Git

| Parameter | Required | Description |
|-----------|----------|-------------|
| `sourceGitHubUrlRepo` | Yes | Repo URL of the legacy project to be migrated |
| `sourceGitHubBranchRepo` | Optional | Branch of the legacy repo |
| `sourceFunctionality` | Optional | Specific functionality/class to scope the migration to |
| `targetGitHubUrlRepo` | Yes | Repo URL of the target microservice |
| `targetGitHubBranchRepo` | Yes | Branch of the target repo (ideally cut from `develop`) |

### Mode B — Local paths

| Parameter | Required | Description |
|-----------|----------|-------------|
| `sourceLocalPath` | Yes | Absolute path to the folder containing the legacy source project |
| `sourceFunctionality` | Optional | Specific functionality/class to scope the migration to (relative to `sourceLocalPath`) |
| `targetLocalPath` | Yes | Absolute path to the folder where the migrated project lives / must be written |

In both modes, omitting `sourceFunctionality` migrates the entire source project instead of scoping
to a single class.

## Step 2 — Tell the agent to execute the plan

Open a chat session with your agent (Claude Code or Kiro CLI) and reference the plan by name,
passing the parameters from Step 1.

**Mode A — Git:**

```text
Execute the java-ee-migration-plan.md for sourceGitHubUrlRepo: git@github.com:CompanyTI/SomeLegacyProject.git ; sourceGitHubBranchRepo: master-branch ; sourceFunctionality: SomeLegacyProject/src/main/java/co/company/accountservices/ejb/controller/SomeLegacyServicesBean.java ; targetGitHubUrlRepo: git@github.com:CompanyTI/some_project_ms.git ; targetGitHubBranchRepo: functionality-migration
```

**Mode B — Local paths:**

```text
Execute the java-ee-migration-plan.md for sourceLocalPath: /Users/me/projects/SomeLegacyProject ; sourceFunctionality: SomeLegacyProject/src/main/java/co/company/accountservices/ejb/controller/SomeLegacyServicesBean.java ; targetLocalPath: /Users/me/projects/some_project_ms
```

More ready-made prompts for both modes (including the full-project variants) are in
[`examples/execute-migration-plan.md`](../../examples/execute-migration-plan.md).

## Step 3 — What to expect

The plan runs the same 14 steps in both modes; only the clone/checkout (Steps 1 and 3) and the
commit/push blocks differ:

1. **Provide source** (Step 1) — clones the legacy repo (Mode A) or validates `sourceLocalPath`
   exists (Mode B).
2. **Analyze** (Step 2) — runs `/workshop-get-java-ee-project-context` and writes the analysis
   report into the source project folder.
3. **Provide target** (Step 3) — clones the target repo (Mode A) or validates `targetLocalPath`
   (Mode B). If the target is **empty**, it scaffolds a new project (Step 3.1), then validates the
   Scaffold Clean Architecture layout (Step 3.2).
4. **Migrate** (Steps 4–9) — DTOs, JPA entities, business logic, business utils, REST controllers,
   and JMS entry-points, each via its `/workshop-*` skill.
5. **Cross-cutting** (Steps 10–12) — logging (Logback), Actuator, and OpenAPI.
6. **Testing & docs** (Steps 13–14) — unit tests and the README.

After each step the agent reports a **title, what was done, and the files affected** (see
[Step Summary Format](java-ee-migration-plan.md#step-summary-format)), making progress auditable
even if a later step fails.

- **Mode A:** after each migration step the agent commits and pushes to `targetGitHubBranchRepo`.
- **Mode B:** no commits or pushes — all migrated code is written into `targetLocalPath` for you to
  review and version manually.

## Step 4 — Re-running and troubleshooting

- The plan is **idempotent** per step: cloning a repo that already exists, generating an analysis
  file that's already present, or scaffolding a target that already contains a project are all
  skipped automatically. You can safely re-issue the same "Execute the java-ee-migration-plan.md"
  prompt to resume after an interruption, or run it again with a different `sourceFunctionality`
  (e.g., migrate a second class) against the same source/target without redoing earlier steps.
- If a step fails, the agent's step summary tells you exactly which step to inspect. In Mode A,
  also check the last commit pushed to the target branch; in Mode B, inspect the current state of
  `targetLocalPath` and the most recent analysis/report file in the source folder.
- For the full technical detail behind any given step (what skill it calls, what parameters it
  passes), read the corresponding numbered step in
  [`java-ee-migration-plan.md`](java-ee-migration-plan.md).
