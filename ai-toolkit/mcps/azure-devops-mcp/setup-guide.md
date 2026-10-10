# Azure DevOps MCP Server — Setup Guide

Step-by-step guide to connect the Azure DevOps MCP server in Claude Code CLI and Kiro CLI.

## Prerequisites

| Requirement | How to verify |
|---|---|
| Node.js 20+ installed (via nvm) | `node --version` |
| Claude Code CLI installed | `claude --version` |
| Kiro CLI installed | `kiro --version` |
| An Azure DevOps Personal Access Token (PAT) | — |

---

## Step 1 — Generate the Personal Access Token (PAT)

1. Go to `https://dev.azure.com/[YourCompany]/_usersSettings/tokens`
2. Click **New Token**
3. Select the required scopes:
   - **Work Items**: Read & Write
   - **Code**: Read
   - **Build**: Read & Execute
   - **Project and Team**: Read
4. Copy the generated token.

---

## Step 2 — Export the PAT as an Environment Variable

Add to your `~/.zshrc` or `~/.zprofile`:

```bash
export AZURE_DEVOPS_PAT="your-token-here"
```

Then reload:

```bash
source ~/.zshrc
```

Verify:

```bash
echo $AZURE_DEVOPS_PAT
```

---

## Step 3 — Find the Absolute Path to npx

> **Kiro only.** Claude Code CLI inherits the shell PATH and does not need this step.

Kiro (as a desktop app on macOS) does not inherit the full shell PATH. You need the absolute path to `npx`:

```bash
which npx
# Example: /Users/[YourUser]/.nvm/versions/node/v20.19.5/bin/npx
```

---

## Step 4 — Add the MCP Config to Your AI CLI

### Claude Code CLI

**Config file location:**

```
~/.claude.json          # Global (all projects)
.claude.json            # Project-level (current project only)
```

Add the `azure-devops` block to the `mcpServers` section of your chosen file:

```json
{
  "mcpServers": {
    "azure-devops": {
      "command": "npx",
      "args": ["-y", "@azure-devops/mcp", "YourCompany"],
      "env": {
        "AZURE_DEVOPS_AUTH_METHOD": "pat",
        "AZURE_DEVOPS_PAT": "${env:AZURE_DEVOPS_PAT}",
        "AZURE_DEVOPS_DEFAULT_PROJECT": "YourCompany"
      }
    }
  }
}
```

> Claude Code CLI inherits the shell PATH so `npx` resolves without an absolute path.

#### Add via Claude Code CLI command

Alternatively, run this command in your terminal:

```bash
claude mcp add-json azure-devops '{
  "command": "npx",
  "args": ["-y", "@azure-devops/mcp", "YourCompany"],
  "env": {
    "AZURE_DEVOPS_AUTH_METHOD": "pat",
    "AZURE_DEVOPS_PAT": "${env:AZURE_DEVOPS_PAT}",
    "AZURE_DEVOPS_DEFAULT_PROJECT": "YourCompany"
  }
}'
```

List configured servers to confirm:

```bash
claude mcp list
```

---

### Kiro CLI

**Config file location:**

```
~/.kiro/settings/mcp.json          # Global (all projects)
.kiro/settings/mcp.json            # Workspace-level (current project only)
```

Add the `azure-devops` block to the `mcpServers` section:

```json
{
  "mcpServers": {
    "azure-devops": {
      "command": "/Users/[YourCompany]/.nvm/versions/node/v20.19.5/bin/npx",
      "args": ["-y", "@azure-devops/mcp", "YourCompany"],
      "env": {
        "PATH": "/Users/[YourUser]/.nvm/versions/node/v20.19.5/bin:/usr/local/bin:/usr/bin:/bin",
        "AZURE_DEVOPS_AUTH_METHOD": "pat",
        "AZURE_DEVOPS_PAT": "${env:AZURE_DEVOPS_PAT}",
        "AZURE_DEVOPS_DEFAULT_PROJECT": "YourCompany"
      },
      "disabled": false,
      "autoApprove": []
    }
  }
}
```

> Replace the `npx` path and `PATH` prefix with the output of `which npx` from Step 3.

---

## Step 5 — Approve Environment Variables (Kiro only)

When you save the Kiro config file, Kiro will show a security warning asking you to approve `env:AZURE_DEVOPS_PAT`. This is expected — click **Approve** to allow the variable expansion.

---

## Step 6 — Verify the Connection

### Claude Code CLI

Start a new Claude Code session and run:

```
/mcp
```

The `azure-devops` server should appear with status **connected**.

Or from the terminal:

```bash
claude mcp get azure-devops
```

### Kiro CLI

1. Open the MCP Servers panel (command palette → **MCP**)
2. The `azure-devops` server should appear listed
3. Click the **Start / Restart** button if needed

---

## Troubleshooting

### `spawn npx ENOENT`

Kiro cannot find `npx`. Use the absolute path in `command` (see Step 3).

### `env: node: No such file or directory`

`npx` runs but cannot find `node`. Add the node bin directory to `PATH` inside `env` in the Kiro config (see Step 4 — Kiro section).

### `Incompatible auth server: does not support dynamic client registration`

Occurs when using the remote server URL (`https://mcp.dev.azure.com/...`) — Kiro does not support that OAuth flow. Use the local server with PAT instead (this guide).

### `Unauthorized`

The PAT is invalid or not exported. Verify:

```bash
echo $AZURE_DEVOPS_PAT
```

---

## Usage

Once connected, you can use prompts like:

> **Note:** Available tools depend on the PAT scopes configured in Step 1.

- "List my work items for the Company project"
- "Show the project repos"
- "List the team iterations"
- "Create a Task work item"
