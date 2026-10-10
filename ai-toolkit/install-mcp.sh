#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MCP_TEMPLATE="$SCRIPT_DIR/settings/mcp.json"

# Config file locations
KIRO_MCP_FILE="$HOME/.kiro/settings/mcp.json"
CLAUDE_MCP_FILE="$HOME/.claude.json"
ATX_MCP_FILE="$HOME/.aws/atx/mcp.json"

# Defaults
AZURE_ORG="YourOrganizacion"
AZURE_PROJECT="YourProject"

# ── Helpers ──────────────────────────────────────────────────────────────────

log()  { echo "  $1"; }
ok()   { echo "✅ $1"; }
warn() { echo "⚠️  $1"; }
err()  { echo "❌ $1" >&2; exit 1; }
sep()  { echo "──────────────────────────────────────────────"; }

usage() {
  echo ""
  echo "Usage: ./setup-mcp.sh [kiro|claude|atx|all]"
  echo ""
  echo "  kiro   — Install MCP config to ~/.kiro/settings/mcp.json"
  echo "  claude — Install MCP config to ~/.claude.json (Claude Code CLI)"
  echo "  atx    — Install MCP config to ~/.aws/atx/mcp.json (AWS Transform CLI)"
  echo "  all    — Install to all of the above"
  echo ""
  exit 1
}

# Detect absolute path to npx (desktop apps don't inherit shell PATH)
detect_npx() {
  local npx_path
  npx_path="$(which npx 2>/dev/null || true)"
  if [[ -z "$npx_path" ]]; then
    err "npx not found. Install Node.js 20+ (preferably via nvm) and try again."
  fi
  echo "$npx_path"
}

# Derive the node bin directory from the npx path
node_bin_dir() {
  dirname "$1"
}

# Verify jq is available
check_jq() {
  if ! command -v jq &>/dev/null; then
    err "jq is required but not found. Install it with: brew install jq"
  fi
}

# Verify required PATs are exported based on what servers are in the template
check_pat() {
  local has_azure has_github
  has_azure="$(jq -r 'if .mcpServers["azure-devops"] then "yes" else "no" end' "$MCP_TEMPLATE")"
  has_github="$(jq -r 'if .mcpServers["github"] then "yes" else "no" end' "$MCP_TEMPLATE")"

  if [[ "$has_azure" == "yes" ]] && [[ -z "${AZURE_DEVOPS_PAT:-}" ]]; then
    echo ""
    warn "AZURE_DEVOPS_PAT is not set in the current environment."
    echo "   Add the following to your ~/.zshrc or ~/.bashrc and reload:"
    echo ""
    echo '   export AZURE_DEVOPS_PAT="your-token-here"'
    echo ""
    read -r -p "   Continue anyway? [y/N] " confirm
    [[ "$confirm" =~ ^[Yy]$ ]] || exit 0
  fi

  if [[ "$has_github" == "yes" ]] && [[ -z "${GITHUB_PERSONAL_ACCESS_TOKEN:-}" ]]; then
    echo ""
    warn "GITHUB_PERSONAL_ACCESS_TOKEN is not set in the current environment."
    echo "   Add the following to your ~/.zshrc or ~/.bashrc and reload:"
    echo ""
    echo '   export GITHUB_PERSONAL_ACCESS_TOKEN="your-token-here"'
    echo ""
    read -r -p "   Continue anyway? [y/N] " confirm
    [[ "$confirm" =~ ^[Yy]$ ]] || exit 0
  fi
}

# Read .mcpServers from the template, patching azure-devops with runtime values
build_servers() {
  local npx_path="$1"
  local node_bin="$2"

  jq \
    --arg cmd "$npx_path" \
    --arg org "$AZURE_ORG" \
    --arg project "$AZURE_PROJECT" \
    --arg path "$node_bin:/usr/local/bin:/usr/bin:/bin" \
    '
    .mcpServers |
    if .["azure-devops"] then
      .["azure-devops"].command                           = $cmd |
      .["azure-devops"].args                             = ["-y", "@azure-devops/mcp", $org] |
      .["azure-devops"].env.PATH                         = $path |
      .["azure-devops"].env.AZURE_DEVOPS_DEFAULT_PROJECT = $project
    else . end
    ' "$MCP_TEMPLATE"
}

# Print which servers will be added vs skipped (reads target file BEFORE merge)
report_servers() {
  local target_file="$1"
  local new_servers="$2"
  local label="$3"

  local existing
  existing="$(<"$target_file")"

  echo "$new_servers" | jq -r 'to_entries[] | .key' | while IFS= read -r key; do
    if echo "$existing" | jq -e ".mcpServers[\"$key\"] // empty" &>/dev/null; then
      warn "$label: '$key' already exists — skipped"
    else
      ok "$label: '$key' will be added"
    fi
  done
}

# Merge new servers into target JSON — only adds keys that are missing
merge_servers() {
  local target_file="$1"
  local new_servers="$2"

  jq --argjson new "$new_servers" '
    .mcpServers //= {} |
    reduce ($new | to_entries[]) as $e (
      .;
      if .mcpServers[$e.key] == null then
        .mcpServers[$e.key] = $e.value
      else
        .
      end
    )
  ' "$target_file"
}

# ── Installers ────────────────────────────────────────────────────────────────

install_kiro() {
  local servers="$1"

  mkdir -p "$(dirname "$KIRO_MCP_FILE")"

  if [[ -f "$KIRO_MCP_FILE" ]]; then
    report_servers "$KIRO_MCP_FILE" "$servers" "Kiro"
    local updated
    updated="$(merge_servers "$KIRO_MCP_FILE" "$servers")"
    echo "$updated" > "$KIRO_MCP_FILE"
    ok "Kiro config saved: $KIRO_MCP_FILE"
  else
    jq -n --argjson servers "$servers" '{"mcpServers": $servers}' > "$KIRO_MCP_FILE"
    echo "$servers" | jq -r 'to_entries[] | .key' | while read -r key; do
      ok "Kiro: '$key' added"
    done
    ok "Kiro config created: $KIRO_MCP_FILE"
  fi

  log "Restart Kiro and open the MCP Servers panel to connect."
}

install_claude() {
  local servers="$1"

  if [[ -f "$CLAUDE_MCP_FILE" ]]; then
    report_servers "$CLAUDE_MCP_FILE" "$servers" "Claude"
    local updated
    updated="$(merge_servers "$CLAUDE_MCP_FILE" "$servers")"
    echo "$updated" > "$CLAUDE_MCP_FILE"
    ok "Claude config saved: $CLAUDE_MCP_FILE"
  else
    jq -n --argjson servers "$servers" '{"mcpServers": $servers}' > "$CLAUDE_MCP_FILE"
    echo "$servers" | jq -r 'to_entries[] | .key' | while read -r key; do
      ok "Claude: '$key' added"
    done
    ok "Claude config created: $CLAUDE_MCP_FILE"
  fi

  log "Run 'claude mcp list' to verify the server is registered."
}

install_atx() {
  local servers="$1"

  mkdir -p "$(dirname "$ATX_MCP_FILE")"

  if [[ -f "$ATX_MCP_FILE" ]]; then
    report_servers "$ATX_MCP_FILE" "$servers" "atx"
    local updated
    updated="$(merge_servers "$ATX_MCP_FILE" "$servers")"
    echo "$updated" > "$ATX_MCP_FILE"
    ok "atx config saved: $ATX_MCP_FILE"
  else
    jq -n --argjson servers "$servers" '{"mcpServers": $servers}' > "$ATX_MCP_FILE"
    echo "$servers" | jq -r 'to_entries[] | .key' | while read -r key; do
      ok "atx: '$key' added"
    done
    ok "atx config created: $ATX_MCP_FILE"
  fi

  log "Run 'atx mcp tools' to verify the server is registered."
}

# ── Main ──────────────────────────────────────────────────────────────────────

sep
echo "  WAS Migration Developer Kit — MCP Setup"
sep

check_jq

if [[ ! -f "$MCP_TEMPLATE" ]]; then
  err "MCP template not found: $MCP_TEMPLATE"
fi

check_pat

NPX_PATH="$(detect_npx)"
NODE_BIN="$(node_bin_dir "$NPX_PATH")"

log "Template  : $MCP_TEMPLATE"
log "npx path  : $NPX_PATH"
log "Node bin  : $NODE_BIN"
log "Org       : $AZURE_ORG"
log "Project   : $AZURE_PROJECT"
sep

SERVERS="$(build_servers "$NPX_PATH" "$NODE_BIN")"

case "${1:-}" in
  kiro)
    install_kiro "$SERVERS"
    ;;
  claude)
    install_claude "$SERVERS"
    ;;
  atx)
    install_atx "$SERVERS"
    ;;
  all)
    install_kiro   "$SERVERS"
    install_claude "$SERVERS"
    install_atx    "$SERVERS"
    ;;
  *)
    usage
    ;;
esac

sep
echo "🎉 MCP setup complete!"
sep
