# Java EE Migration Plan

## Prerequisites

- Spring Boot target version: **4.1.X**
- Java version: **25**
- Architecture: **Scaffold Clean Architecture**
- Build tool: **Gradle**

## Migration Modes

This plan supports two mutually exclusive execution modes. Pick one based on how the source and
target projects are made available:

- **Mode A — Git (remote repositories):** the source legacy project and the target microservice
  project are cloned from GitHub. Changes are committed and pushed back to the target branch.
  Use the parameters in [Required Input Parameters — Mode A](#required-input-parameters--mode-a-git).
- **Mode B — Local paths (no clone, no push):** the source and target projects already exist on the
  local machine. Nothing is cloned and nothing is pushed; the migration reads from a local source
  folder and writes into a local target folder. Use the parameters in
  [Required Input Parameters — Mode B](#required-input-parameters--mode-b-local-paths).

> Choose exactly one mode. Do not mix Git parameters with local-path parameters in the same run.
> Steps that differ between modes include a **Mode A** and a **Mode B** variant; perform only the
> variant matching the mode you selected.

### Required Input Parameters — Mode A (Git)

| Parameter | Description |
|-----------|-------------|
| `sourceGitHubUrlRepo` | Repo URL from Legacy Repo to be migrated |
| `sourceGitHubBranchRepo` | Repo Branch from Legacy Repo to be migrated (Optional) |
| `sourceFunctionality` | Specific functionality name to be migrated (Optional) |
| `targetGitHubUrlRepo` | Repo URL from Target Repo that will contains the code from migration |
| `targetGitHubBranchRepo` | Repo Branch from Target that will contain the code from migration (Ideal that branch comes from develop branch) |

### Required Input Parameters — Mode B (Local paths)

| Parameter | Description |
|-----------|-------------|
| `sourceLocalPath` | Absolute path on the local machine to the folder containing the legacy Java EE source project to be migrated |
| `sourceFunctionality` | Specific functionality name to be migrated (Optional) |
| `targetLocalPath` | Absolute path on the local machine to the folder where the migrated project lives / must be written |

> In Mode B, `[sourceProject]` resolves to `sourceLocalPath` and `[targetProject]` resolves to
> `targetLocalPath`. No repository is cloned, no branch is checked out, and no commit/push is
> performed — all changes remain in the local target folder for the user to review and version
> manually.

---

### Step Summary Format

After completing each step, generate a summary with the following structure:

#### Title

- The **skill name** in a human-readable format (e.g., "Spring Boot Project Created", "Java EE DTOs Migrated", "GitHub Issue Created")

#### Summary

- **What was done**: Brief description of the actions performed
- **Files affected**: List of files created, modified, or deleted during execution

#### On Failure

- The Title should still appear with a failure indicator (e.g., "Spring Boot Project Creation — Failed")
- The Summary should include what was attempted, where it failed, and any partial results

## Steps

> **Version control per mode:** The `git add` / `git commit` / `git push` blocks shown in the
> migration steps below apply to **Mode A (Git)** only. In **Mode B (Local paths)**, skip every
> `git` block entirely — do **not** commit and do **not** push. All changes stay in
> `[targetLocalPath]` for the user to review and version manually. The migration work performed in
> each step (the `/workshop-*` skill executions) is identical in both modes; only the
> clone/checkout (Steps 1 and 3) and the commit/push blocks differ.

### Step 1 — Provide Source Java EE Project

**Mode A — Git:**
Execute `git clone [sourceGitHubUrlRepo]` if the repo on local already cloned, go to the next step.
Execute `git checkout [sourceGitHubBranchRepo]` if was indicated, if not, go to the next step.

> **Idempotency**: If the repo was already cloned, this step is skipped automatically.

**Mode B — Local path:**
No clone is performed. Verify that `[sourceLocalPath]` exists and points to a readable Java EE
project folder. Use `[sourceLocalPath]` as `[sourceProject]` for all subsequent steps.

> **Validation**: If `[sourceLocalPath]` does not exist or is not a valid project folder, stop and
> report the failure using the Step Summary failure format.

### Step 2 — Analyze Java EE Project

Execute `/workshop-get-java-ee-project-context` for the project `[sourceProject]`. If the param `[sourceFunctionality]` was indicated, focus on this functionality.
Save the analyze file into the `[sourceProject]` folder. Do not execute any additional commit.

> **Idempotency**: If the analyze file already exists on the `[sourceProject]`, this step is skipped automatically.

### Step 3 — Provide Target Microservice Project

**Mode A — Git:**
Execute `git clone [targetGitHubUrlRepo]` if the repo on local already cloned, go to the next step.
Execute `git checkout [targetGitHubBranchRepo]` if was indicated, if not, go to the next step.

> **Idempotency**: If the repo was already cloned, this step is skipped automatically.

**Mode B — Local path:**
No clone is performed. Verify that `[targetLocalPath]` exists and points to a writable target
folder — either an existing Scaffold Clean Architecture project or an empty folder (which will be
scaffolded in Step 3.1). Use `[targetLocalPath]` as `[targetProject]` for all subsequent steps; all
migrated code is written into this folder.

> **Validation**: If `[targetLocalPath]` does not exist, stop and report the failure using the Step
> Summary failure format. If it exists but is **empty**, do not fail — proceed to Step 3.1 to
> scaffold a new project into it.

#### 3.1 Scaffold Target Project If Empty (both modes)

This applies to **both Mode A and Mode B**. Determine whether the target destination is empty:

- **Mode A — Git:** the cloned target repository folder contains no project files (e.g., only a
  `.git` folder and/or a bare `README.md`/`LICENSE`, no `build.gradle`/`settings.gradle`).
- **Mode B — Local path:** `[targetLocalPath]` is an empty folder (no project files).

If the target destination is **empty**, create the project with the
`/workshop-scaffold-clean-architecture` skill using its **Generate Project** task
(`1-generate-project.md`):

```shell
gradle cleanArchitecture --package=[basePackage] --type=reactive --name=[projectName] --lombok=true --java-version=25
```

- `--type=reactive` is mandatory (this plan targets Spring WebFlux + Project Reactor).
- `--java-version=25` to match the Prerequisites.
- `[basePackage]` and `[projectName]` are derived from the target project (ask the user if they
  cannot be inferred from `[targetProject]`).

> **Caution**: Per the task, running the generator over an existing project overwrites
> `main.gradle`, `build.gradle`, and `gradle.properties`. Only run it when the destination is
> **empty**. If the destination already contains a project, **skip this sub-step**.

> **Idempotency**: If the target already contains a Scaffold Clean Architecture project, this
> sub-step is skipped automatically.

#### 3.2 Validate Target Project

Execute `/workshop-scaffold-clean-architecture` for the project `[targetProject]` to get understanding about the Scaffold Clean Architecture implementation, based on the target project that will be updated in the next steps.

### Step 4 — Migrate DTO Classes

Execute `/workshop-migrate-java-ee-dto-clases-reactive` using the output from Step 2 and the project provided in Step 3.

**Mode A — Git only (skip in Mode B):**

```bash
git add .
git commit -m "feature: Migrate DTO classes"
git push origin [targetGitHubBranchRepo]
```

### Step 5 — Migrate JPA Entities

Execute `/workshop-migrate-java-ee-db-entity-clases-reactive` to migrate JPA Entity classes, Service Interfaces, and Implementations, using the output from Step 2 and the project provided in Step 3.

**Mode A — Git only (skip in Mode B):**

```bash
git add .
git commit -m "feature: Migrate JPA entity classes"
git push origin [targetGitHubBranchRepo]
```

### Step 6 — Migrate Business Logic Classes

Execute `/workshop-migrate-java-ee-business-logic-classes-reactive` to migrate Business Logic classes, Service Interfaces, and Implementations, using the output from Step 2 and the project provided in Step 3.

**Mode A — Git only (skip in Mode B):**

```bash
git add .
git commit -m "feature: Migrate business logic classes"
git push origin [targetGitHubBranchRepo]
```

### Step 7 — Migrate Business Utils Dependencies

Execute `/workshop-migrate-java-ee-business-utils-classes-reactive` to complement the logic migration from Step 6, using the output from Step 2 and the project provided in Step 3.

**Mode A — Git only (skip in Mode B):**

```bash
git add .
git commit -m "feature: Migrate business utils dependencies"
git push origin [targetGitHubBranchRepo]
```

### Step 8 — Migrate REST Controller Classes

Execute `/workshop-migrate-java-ee-controller-clases-reactive` to migrate Entry-Point classes, Service Interfaces, and Implementations, using the output from Step 2 and the project provided in Step 3.

**Mode A — Git only (skip in Mode B):**

```bash
git add .
git commit -m "feature: Migrate REST controller classes"
git push origin [targetGitHubBranchRepo]
```

### Step 9 — Migrate JMS Entry-Point Classes

Execute `/workshop-migrate-java-ee-jms-clases-reactive` to migrate JMS Entry-Point classes, Service Interfaces, and Implementations, using the output from Step 2 and the project provided in Step 3.

**Mode A — Git only (skip in Mode B):**

```bash
git add .
git commit -m "feature: Migrate JMS entry-point classes"
git push origin [targetGitHubBranchRepo]
```

### Step 10 — Implement Logging

Execute `/workshop-spring-boot-implement-logback-reactive` to implement logging classes and configuration properties, using the project provided in Step 3.

**Mode A — Git only (skip in Mode B):**

```bash
git add .
git commit -m "feature: Implement Logback logging"
git push origin [targetGitHubBranchRepo]
```

### Step 11 — Enable Actuator

Execute `/workshop-spring-boot-implement-actuator-reactive` to enable actuator endpoints, using the project provided in Step 3.

**Mode A — Git only (skip in Mode B):**

```bash
git add .
git commit -m "feature: Enable Spring Boot Actuator"
git push origin [targetGitHubBranchRepo]
```

### Step 12 — Enable OpenApi

Execute `/workshop-spring-boot-implement-openapi-reactive` to enable openapi report, using the project provided in Step 3.

**Mode A — Git only (skip in Mode B):**

```bash
git add .
git commit -m "feature: Enable Spring Boot OpenApi"
git push origin [targetGitHubBranchRepo]
```

### Step 13 — Implement Unit Testing

Execute `/workshop-spring-boot-implement-unit-testing-reactive` to implement unit tests for the migrated project provided in Step 3.

#### Test Coverage Targets

| # | Layer | Annotations / Classes |
|---|-------|-----------------------|
| 1 | Service | `@Service` classes |
| 2 | Controller | `@RestController` classes |
| 3 | JMS Entry-Point | JMS listener classes |
| 4 | Entity | `@Entity` classes |
| 5 | Utility | Util/helper classes |

**Mode A — Git only (skip in Mode B):**

```bash
git add .
git commit -m "feature: Adding Unit Testing"
git push origin [targetGitHubBranchRepo]
```

### Step 14 — Generate README

Execute `/workshop-spring-boot-generate-readme` to update the README.md file documentation for the migrated project provided in Step 3.