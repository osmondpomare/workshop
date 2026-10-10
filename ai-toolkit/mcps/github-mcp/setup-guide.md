# GitHub MCP Server — Setup Guide

## Overview

The GitHub MCP server runs as a Docker container and exposes GitHub capabilities (repositories, issues, PRs, code search, etc.) to Claude Code CLI and Kiro CLI via the Model Context Protocol (MCP).

---

## Prerequisites

| Requirement | How to verify |
|---|---|
| Docker Desktop running | `docker info` |
| Claude Code CLI installed | `claude --version` |
| Kiro CLI installed | `kiro --version` |
| GitHub account | — |

---

## Step 1 — Create a GitHub Personal Access Token (PAT)

1. Go to **GitHub → Settings → Developer settings → Personal access tokens → Fine-grained tokens**
2. Click **Generate new token**
3. Configure the token:
   - **Token name**: `mcp-local` (or any name)
   - **Expiration**: choose a suitable window (e.g. 90 days)
   - **Repository access**: Select the repos the AI assistant should access, or **All repositories**
   - **Permissions** (minimum required):

     | Permission | Level |
     |---|---|
     | Contents | Read |
     | Issues | Read & Write |
     | Pull requests | Read & Write |
     | Metadata | Read (mandatory) |

4. Click **Generate token** and copy the value — you will not see it again.

---

## Step 2 — Set the Environment Variable

Export the token so that Docker can pick it up at runtime.

### macOS / zsh (temporary — current session only)

```bash
export GITHUB_PERSONAL_ACCESS_TOKEN="github_pat_XXXXXXXXXXXX"
```

### macOS / zsh (permanent)

Add the following line to `~/.zshrc` (or `~/.zprofile`):

```bash
export GITHUB_PERSONAL_ACCESS_TOKEN="github_pat_XXXXXXXXXXXX"
```

Then reload the shell:

```bash
source ~/.zshrc
```

### Verify the variable is set

```bash
echo $GITHUB_PERSONAL_ACCESS_TOKEN
```

---

## Step 3 — Pull the Docker Image

Pull the image once before first use to avoid startup delays:

```bash
docker pull ghcr.io/github/github-mcp-server
```

---

## Step 4 — Add the MCP Config to Your AI CLI

### Claude Code CLI

**Config file location:**

```
~/.claude.json          # Global (all projects)
.claude.json            # Project-level (current project only)
```

Add the `github` block to the `mcpServers` section of your chosen file:

```json
{
  "mcpServers": {
    "github": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e",
        "GITHUB_PERSONAL_ACCESS_TOKEN",
        "ghcr.io/github/github-mcp-server"
      ],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "${env:GITHUB_PERSONAL_ACCESS_TOKEN}"
      }
    }
  }
}
```

> `${env:GITHUB_PERSONAL_ACCESS_TOKEN}` reads the value from your shell environment at startup — no hardcoded secrets in config files.

#### Add via Claude Code CLI command

Alternatively, run this command in your terminal:

```bash
claude mcp add-json github '{
  "command": "docker",
  "args": ["run", "-i", "--rm", "-e", "GITHUB_PERSONAL_ACCESS_TOKEN", "ghcr.io/github/github-mcp-server"],
  "env": { "GITHUB_PERSONAL_ACCESS_TOKEN": "${env:GITHUB_PERSONAL_ACCESS_TOKEN}" }
}'
```

Or with the add shorthand:

```bash
claude mcp add github -e GITHUB_PERSONAL_ACCESS_TOKEN -- \
  docker run -i --rm -e GITHUB_PERSONAL_ACCESS_TOKEN ghcr.io/github/github-mcp-server
```

List configured servers to confirm:

```bash
claude mcp list
```

---

### Kiro CLI

**Config file location:**

```
~/.kiro/settings/mcp.json        # Global (all projects)
```

Add the `github` block to the `mcpServers` section:

```json
{
  "mcpServers": {
    "github": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e",
        "GITHUB_PERSONAL_ACCESS_TOKEN",
        "ghcr.io/github/github-mcp-server"
      ],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "${env:GITHUB_PERSONAL_ACCESS_TOKEN}"
      }
    }
  }
}
```

Kiro reads the config automatically at startup — no CLI command required. Restart Kiro after editing the file.

---

## Step 5 — Verify the Connection

### Claude Code CLI

Start a new Claude Code session and run:

```
/mcp
```

The `github` server should appear with status **connected**.

Or from the terminal:

```bash
claude mcp get github
```

### Kiro CLI

Open Kiro and check the MCP panel (bottom status bar or **Settings → MCP Servers**). The `github` entry should show as **connected**.

---

Once connected, you can ask your AI assistant things like:

- _"List open PRs in repo owner/repo-name"_
- _"Search for issues labeled bug in owner/repo-name"_
- _"Show me the README of owner/repo-name"_

---

## Troubleshooting

### Server shows as disconnected

1. Confirm Docker is running: `docker ps`
2. Confirm the token variable is exported: `echo $GITHUB_PERSONAL_ACCESS_TOKEN`
3. Test the container manually:
   ```bash
   docker run -i --rm -e GITHUB_PERSONAL_ACCESS_TOKEN ghcr.io/github/github-mcp-server
   ```
   It should start without errors.

### Permission denied on GitHub API calls

- Check that the token has the required permissions listed in Step 1.
- Confirm the token has not expired (GitHub → Settings → Developer settings → Personal access tokens).

### Image not found

```bash
docker pull ghcr.io/github/github-mcp-server
```

If you are behind a corporate proxy, configure Docker's proxy settings in Docker Desktop → Settings → Resources → Proxies.

---

## Security Notes

- **Never** hardcode the token value directly in `mcp.json` or commit it to version control.
- Use `${env:GITHUB_PERSONAL_ACCESS_TOKEN}` so the secret stays in your shell environment only.
- Rotate the token periodically and update the environment variable.
- Use fine-grained tokens scoped to specific repositories where possible.
