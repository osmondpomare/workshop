# Executing Java EE Migration Plan

Runs [Plan](../prompts/migration/java-ee-migration-plan.md) — a lightweight, reactive migration directly into an existing target repository using Scaffold Clean Architecture and Spring WebFlux.

## User Examples

The plan supports two execution modes (see [Migration Modes](../prompts/migration/java-ee-migration-plan.md#migration-modes)):

- **Mode A — Git:** source and target are GitHub repositories; changes are committed and pushed.
- **Mode B — Local paths:** source and target are folders already on the local machine; nothing is
  cloned and nothing is pushed.

---

## Mode A — Git (remote repositories)

### Migrate a Specific Functionality

Scope the migration to a single source class by providing `sourceFunctionality`:

```text
Execute the java-ee-migration-plan.md for sourceGitHubUrlRepo: git@github.com:CompanyTI/SomeLegacyProject.git ; sourceGitHubBranchRepo: master-branch ; sourceFunctionality: SomeLegacyProject/src/main/java/co/company/accountservices/ejb/controller/SomeLegacyServicesBean.java ; targetGitHubUrlRepo: git@github.com:CompanyTI/some_project_ms.git ; targetGitHubBranchRepo: functionality-migration
```

```text
Execute the java-ee-migration-plan.md for sourceGitHubUrlRepo: git@github.com:CompanyTI/SomeLegacyProject.git ; sourceGitHubBranchRepo: master-branch ; sourceFunctionality: SomeLegacyProject/src/main/java/co/company/accountservices/ejb/controller/SomeLegacyAnotherServicesBean.java ; targetGitHubUrlRepo: git@github.com:CompanyTI/some_project_ms.git ; targetGitHubBranchRepo: functionality-migration
```

### Migrate a Full Project

Omit `sourceFunctionality` to migrate the entire project instead of a single functionality:

```text
Execute the java-ee-migration-plan.md for sourceGitHubUrlRepo: git@github.com:CompanyTI/SomeLegacyProject.git ; sourceGitHubBranchRepo: master-branch ; targetGitHubUrlRepo: git@github.com:CompanyTI/some_project_ms.git ; targetGitHubBranchRepo: functionality-migration
```

---

## Mode B — Local paths (no clone, no push)

Use this mode when both the legacy source project and the target microservice project already exist
on your machine. Provide `sourceLocalPath` and `targetLocalPath` instead of the GitHub repo/branch
parameters. Nothing is cloned and nothing is pushed — all migrated code is written into
`targetLocalPath` for you to review and version manually.

### Migrate a Specific Functionality

Scope the migration to a single source class by providing `sourceFunctionality` (relative to
`sourceLocalPath`):

```text
Execute the java-ee-migration-plan.md for sourceLocalPath: /Users/me/projects/SomeLegacyProject ; sourceFunctionality: SomeLegacyProject/src/main/java/co/company/accountservices/ejb/controller/SomeLegacyServicesBean.java ; targetLocalPath: /Users/me/projects/some_project_ms
```

```text
Execute the java-ee-migration-plan.md for sourceLocalPath: /Users/me/projects/SomeLegacyProject ; sourceFunctionality: SomeLegacyProject/src/main/java/co/company/accountservices/ejb/controller/SomeLegacyAnotherServicesBean.java ; targetLocalPath: /Users/me/projects/some_project_ms
```

### Migrate a Full Project

Omit `sourceFunctionality` to migrate the entire project instead of a single functionality:

```text
Execute the java-ee-migration-plan.md for sourceLocalPath: /Users/me/projects/SomeLegacyProject ; targetLocalPath: /Users/me/projects/some_project_ms
```
