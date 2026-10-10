#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

usage() {
  echo "Usage: ./install-agents.sh [kiro]"
  echo "  kiro   - Install agent configurations to ~/.kiro/agents/"
  exit 1
}

install_kiro() {
  KIRO_AGENTS_DIR="$HOME/.kiro/agents"
  mkdir -p "$KIRO_AGENTS_DIR"

  # Verify there is at least one agent to install
  if ! ls "$SCRIPT_DIR/agents/"*.json >/dev/null 2>&1; then
    echo "❌ No agent configuration files (*.json) found in $SCRIPT_DIR/agents/"
    exit 1
  fi

  # Copy each agent configuration (filename without .json becomes the agent name)
  for agent_file in "$SCRIPT_DIR/agents/"*.json; do
    agent_name="$(basename "$agent_file" .json)"
    cp "$agent_file" "$KIRO_AGENTS_DIR/$agent_name.json"
    echo "  • Installed agent: $agent_name"
  done
  echo "✅ Agents installed to $KIRO_AGENTS_DIR"
  echo "ℹ️  Run 'kiro-cli agent list' to see them, or start a session with 'kiro-cli chat --agent <name>'."
}

# Main
case "${1:-}" in
  kiro) install_kiro ;;
  *)    usage ;;
esac

echo "🎉 Agent installation complete!"
