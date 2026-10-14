# AI Toolkit — Java EE → Spring Boot Migration Kit

A collection of **skills, steering rules, playbooks, agents, and MCP configurations** that drive an
AI coding agent (Kiro CLI, Claude Code, or AWS Transform CLI) through the migration of legacy
**Java EE (7/8)** applications to modern **reactive Spring Boot** microservices built on the
**Scaffold Clean Architecture** (Spring WebFlux + Project Reactor).

> **Target stack:** Spring Boot 4.1.x · Java 25 · Scaffold Clean Architecture · Gradle · Spring
> WebFlux / Project Reactor.

---

## Table of Contents

- [What's in this toolkit](#whats-in-this-toolkit)
- [Repository structure](#repository-structure)
- [Getting started](#-getting-started)
- [MCP settings](#-mcp-settings)
- [Installing agents](#installing-agents)
- [The migration playbook](#the-migration-playbook)
- [Skills reference](#skills-reference)
- [Steering rules](#steering-rules)
- [Requirements](#requirements)

---

## What's in this toolkit

| Component | Location | Purpose |
|-----------|----------|---------|
| **Skills** | [`skills/`](skills/) | On-demand `/workshop-*` slash commands that perform each migration/implementation task |
| **Steering rules** | [`steering/`](steering/) | Always-on Java/Spring coding standards loaded into the agent's context |
| **Playbook** | [`prompts/migration/`](prompts/migration/) | The step-by-step migration plan plus its usage guide |
| **Doc prompts** | [`prompts/documentation/`](prompts/documentation/) | Reusable prompts (e.g. README generation) |
| **Agents** | [`agents/`](agents/) | Pre-built Kiro agent configurations |
| **MCP settings** | [`settings/`](settings/), [`mcps/`](mcps/) | MCP server template and provider setup guides |
| **Examples** | [`examples/`](examples/) | Copy-paste prompts to execute the playbook |
| **Install scripts** | `install-toolkit.sh`, `install-mcp.sh`, `install-agents.sh` | Install the toolkit, MCP servers, and agents |

---

## Repository structure

```
ai-toolkit/
├── install-toolkit.sh / .ps1           # Install skills + steering (kiro | claude | atx)
├── install-mcp.sh     / .ps1           # Install MCP servers (kiro | claude | atx | all)
├── install-agents.sh  / .ps1           # Install agent configs (kiro)
├── agents/
│   └── java-ee-migration-specialist-agent.json
├── skills/                             # 13 workshop-* skills (SKILL.md each)
│   ├── workshop-get-java-ee-project-context/
│   ├── workshop-scaffold-clean-architecture/
│   ├── workshop-migrate-java-ee-dto-clases-reactive/
│   ├── workshop-migrate-java-ee-db-entity-clases-reactive/
│   ├── workshop-migrate-java-ee-business-logic-classes-reactive/
│   ├── workshop-migrate-java-ee-business-utils-classes-reactive/
│   ├── workshop-migrate-java-ee-controller-clases-reactive/
│   ├── workshop-migrate-java-ee-jms-clases-reactive/
│   ├── workshop-spring-boot-implement-logback-reactive/
│   ├── workshop-spring-boot-implement-actuator-reactive/
│   ├── workshop-spring-boot-implement-openapi-reactive/
│   ├── workshop-spring-boot-implement-unit-testing-reactive/
│   └── workshop-spring-boot-generate-readme/
├── steering/
│   ├── java-best-practices.md
│   ├── java-naming-conventions.md
│   ├── java-project-structure.md
│   └── spring-error-handling.md
├── prompts/
│   ├── migration/
│   │   ├── java-ee-migration-plan.md   # The playbook (14 steps, 2 modes)
│   │   └── usage-guide.md
│   └── documentation/
│       └── readme-generator.md
├── settings/
│   └── mcp.json                        # MCP server template (github + azure-devops)
├── mcps/
│   ├── github-mcp/setup-guide.md
│   └── azure-devops-mcp/setup-guide.md
└── examples/
    └── execute-migration-plan.md
```

---

## 🚀 Getting started

### 1. Install the toolkit (skills + steering)

`install-toolkit.sh` copies the skills and steering rules into your CLI's configuration directory.

```bash
./install-toolkit.sh kiro     # → ~/.kiro/skills/ and ~/.kiro/steering/
./install-toolkit.sh claude   # → ~/.claude/ (CLAUDE.md + commands)
./install-toolkit.sh atx      # → ~/.aws/atx/skills/ (AWS Transform CLI)
```

On **Windows**, use the PowerShell equivalent (`install-toolkit.ps1`). You may first need to allow script execution for the current session:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\install-toolkit.ps1 kiro     # → %USERPROFILE%\.kiro\skills\ and %USERPROFILE%\.kiro\steering\
.\install-toolkit.ps1 claude   # → %USERPROFILE%\.claude\ (CLAUDE.md + commands)
.\install-toolkit.ps1 atx      # → %USERPROFILE%\.aws\atx\skills\ (AWS Transform CLI)
```

Running it with no argument (or an unknown one) prints the usage help.

### 2. Verify

- **Kiro:** start `kiro-cli chat` and type `/` — the `workshop-*` skills appear as slash commands.
- **Claude:** the skills are installed as commands under `~/.claude/commands/`.

---

## 🔌 MCP settings

Some workflows use MCP servers (GitHub for repos/issues/PRs, Azure DevOps for work items). The
server template lives in [`settings/mcp.json`](settings/mcp.json).

### Install the MCP servers

`install-mcp.sh` merges the template into your CLI's MCP config (only adding servers that aren't
already present):

```bash
./install-mcp.sh kiro     # → ~/.kiro/settings/mcp.json
./install-mcp.sh claude   # → ~/.claude.json
./install-mcp.sh atx      # → ~/.aws/atx/mcp.json
./install-mcp.sh all      # → all of the above
```

On **Windows**, use `install-mcp.ps1` (native PowerShell JSON handling — `jq` is **not** required):

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\install-mcp.ps1 kiro     # → %USERPROFILE%\.kiro\settings\mcp.json
.\install-mcp.ps1 claude   # → %USERPROFILE%\.claude.json
.\install-mcp.ps1 atx      # → %USERPROFILE%\.aws\atx\mcp.json
.\install-mcp.ps1 all      # → all of the above
```

**Requirements:** `jq` (bash only — the PowerShell version uses built-in JSON support), and `npx`
(Node.js 20+, preferably via nvm). Docker Desktop is required for the GitHub MCP server (it runs as
a container).

### Required tokens

Export the Personal Access Tokens the servers need before running the script:

```bash
export GITHUB_PERSONAL_ACCESS_TOKEN="your-token-here"
export AZURE_DEVOPS_PAT="your-token-here"
```

On **Windows PowerShell**, set them for the current session (and optionally persist them at user scope):

```powershell
$env:GITHUB_PERSONAL_ACCESS_TOKEN = "your-token-here"
$env:AZURE_DEVOPS_PAT = "your-token-here"
# Persist across sessions (optional):
[Environment]::SetEnvironmentVariable("GITHUB_PERSONAL_ACCESS_TOKEN", "your-token-here", "User")
[Environment]::SetEnvironmentVariable("AZURE_DEVOPS_PAT", "your-token-here", "User")
```

Detailed provider setup (token scopes, verification) is in:
- [`mcps/github-mcp/setup-guide.md`](mcps/github-mcp/setup-guide.md)
- [`mcps/azure-devops-mcp/setup-guide.md`](mcps/azure-devops-mcp/setup-guide.md)

---

## Installing agents

`install-agents.sh` copies the pre-built agent configurations from [`agents/`](agents/) into Kiro's
global agents directory:

```bash
./install-agents.sh kiro      # → ~/.kiro/agents/
```

On **Windows**, use `install-agents.ps1`:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\install-agents.ps1 kiro     # → %USERPROFILE%\.kiro\agents\
```

Then list or use them:

```bash
kiro-cli agent list
kiro-cli chat --agent java-ee-migration-specialist-agent
```

The included **`java-ee-migration-specialist-agent`** is a Java EE → Spring Boot migration
specialist pre-wired with the GitHub and Azure DevOps MCP servers and the toolkit's skills/steering
resources.

---

## The migration playbook

The core of the toolkit is [`prompts/migration/java-ee-migration-plan.md`](prompts/migration/java-ee-migration-plan.md)
— a **playbook**, not a slash command. An AI agent reads it and executes its 14 steps in order,
each invoking one of the `workshop-*` skills.

### Two execution modes

| Mode | Source & target | Version control |
|------|-----------------|-----------------|
| **A — Git** | Cloned from GitHub repositories | Commits and pushes to the target branch after each step |
| **B — Local paths** | Folders already on the local machine | Nothing is cloned or pushed; changes stay in the local target folder |

If the target destination is an **empty** folder/repo, the agent scaffolds a new Scaffold Clean
Architecture project into it (Step 3.1) before migrating.

### How to run it

Open a chat session and tell the agent to execute the plan with the parameters for your chosen mode:

```text
Execute the java-ee-migration-plan.md for sourceLocalPath: /Users/me/projects/SomeLegacyProject ; targetLocalPath: /Users/me/projects/some_project_ms
```

See [`prompts/migration/usage-guide.md`](prompts/migration/usage-guide.md) for the full walkthrough
(parameters, expectations, troubleshooting) and [`examples/execute-migration-plan.md`](examples/execute-migration-plan.md)
for ready-made prompts in both modes.

### Playbook steps at a glance

| Step | Action | Skill |
|------|--------|-------|
| 1 | Provide source Java EE project | — (clone / validate path) |
| 2 | Analyze the project | `/workshop-get-java-ee-project-context` |
| 3 | Provide target project (scaffold if empty) | `/workshop-scaffold-clean-architecture` |
| 4 | Migrate DTO classes | `/workshop-migrate-java-ee-dto-clases-reactive` |
| 5 | Migrate JPA entities | `/workshop-migrate-java-ee-db-entity-clases-reactive` |
| 6 | Migrate business logic | `/workshop-migrate-java-ee-business-logic-classes-reactive` |
| 7 | Migrate business utils | `/workshop-migrate-java-ee-business-utils-classes-reactive` |
| 8 | Migrate REST controllers | `/workshop-migrate-java-ee-controller-clases-reactive` |
| 9 | Migrate JMS entry-points | `/workshop-migrate-java-ee-jms-clases-reactive` |
| 10 | Implement logging (Logback) | `/workshop-spring-boot-implement-logback-reactive` |
| 11 | Enable Actuator | `/workshop-spring-boot-implement-actuator-reactive` |
| 12 | Enable OpenAPI | `/workshop-spring-boot-implement-openapi-reactive` |
| 13 | Implement unit testing | `/workshop-spring-boot-implement-unit-testing-reactive` |
| 14 | Generate README | `/workshop-spring-boot-generate-readme` |

---

## Skills reference

All skills live under [`skills/`](skills/) as `workshop-*` directories, each with a `SKILL.md`.
They can be invoked individually as slash commands or orchestrated by the playbook.

**Analysis & scaffolding**
- `workshop-get-java-ee-project-context` — analyze a Java EE 7/8 project and produce a migration
  analysis report.
- `workshop-scaffold-clean-architecture` — generate / inspect a Scaffold Clean Architecture project
  (reactive).

**Migration (Java EE → reactive Spring Boot)**
- `workshop-migrate-java-ee-dto-clases-reactive` — DTOs.
- `workshop-migrate-java-ee-db-entity-clases-reactive` — JPA entities, repositories, persistence.
- `workshop-migrate-java-ee-business-logic-classes-reactive` — `@Stateless` EJBs → reactive use cases.
- `workshop-migrate-java-ee-business-utils-classes-reactive` — business-utils dependencies.
- `workshop-migrate-java-ee-controller-clases-reactive` — REST entry-points.
- `workshop-migrate-java-ee-jms-clases-reactive` — JMS entry-points.

**Cross-cutting concerns**
- `workshop-spring-boot-implement-logback-reactive` — SLF4J + Logback logging.
- `workshop-spring-boot-implement-actuator-reactive` — Actuator health/metrics.
- `workshop-spring-boot-implement-openapi-reactive` — SpringDoc OpenAPI.
- `workshop-spring-boot-implement-unit-testing-reactive` — JUnit 5 + StepVerifier tests.
- `workshop-spring-boot-generate-readme` — generate/refresh the project README.

---

## Steering rules

The files in [`steering/`](steering/) are loaded into the agent's context on every turn (via the
default agent, or explicitly in a custom agent's `resources`). They enforce consistent Java 17+ /
Spring Boot practices across all generated code:

- [`java-best-practices.md`](steering/java-best-practices.md) — constructor injection, Records,
  modern Java features, Spring Boot conventions.
- [`java-naming-conventions.md`](steering/java-naming-conventions.md) — class/method/package naming.
- [`java-project-structure.md`](steering/java-project-structure.md) — domain-driven package layout.
- [`spring-error-handling.md`](steering/spring-error-handling.md) — exception hierarchy and
  `@RestControllerAdvice` + RFC 7807 Problem Details.

---

## Requirements

| Tool | Needed for |
|------|-----------|
| **Kiro CLI**, **Claude Code**, or **AWS Transform CLI** | Running the skills and playbook |
| **Java 25**, **Gradle** | The target Spring Boot project |
| **Docker Desktop** | GitHub MCP server (runs as a container) |
| **Node.js 20+** (`npx`) and **`jq`** | `install-mcp.sh` (Azure DevOps MCP + config merging) |
| **SSH access** to your GitHub org | Mode A of the playbook (clone/push) |
