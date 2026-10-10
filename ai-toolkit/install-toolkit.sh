#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

usage() {
  echo "Usage: ./install.sh [kiro|claude|atx]"
  echo "  kiro   - Install to ~/.kiro/skills/ and ~/.kiro/steering/"
  echo "  claude - Install to ~/.claude/ (CLAUDE.md + commands)"
  echo "  atx    - Install to ~/.aws/atx/skills/ (AWS Transform CLI)"
  exit 1
}

install_kiro() {
  KIRO_DIR="$HOME/.kiro"
  mkdir -p "$KIRO_DIR/skills" "$KIRO_DIR/steering"
  cp -r "$SCRIPT_DIR/skills/"* "$KIRO_DIR/skills/"
  cp -r "$SCRIPT_DIR/steering/"* "$KIRO_DIR/steering/"
  echo "✅ Installed to $KIRO_DIR (skills + steering)"
}

install_claude() {
  CLAUDE_DIR="$HOME/.claude"
  COMMANDS_DIR="$CLAUDE_DIR/commands"
  mkdir -p "$COMMANDS_DIR"

  # Merge steering rules into CLAUDE.md
  CLAUDE_MD="$CLAUDE_DIR/CLAUDE.md"
  MARKER="# === WAS Migration Developer Kit - Steering Rules ==="

  # Remove previous installation if exists
  if [ -f "$CLAUDE_MD" ] && grep -q "$MARKER" "$CLAUDE_MD"; then
    sed -i.bak "/$MARKER/,/# === END WAS Migration Developer Kit ===/d" "$CLAUDE_MD"
    rm -f "$CLAUDE_MD.bak"
  fi

  # Append steering rules
  {
    echo ""
    echo "$MARKER"
    for file in "$SCRIPT_DIR/steering/"*.md; do
      echo ""
      cat "$file"
    done
    echo ""
    echo "# === END WAS Migration Developer Kit ==="
  } >> "$CLAUDE_MD"
  echo "✅ Steering rules appended to $CLAUDE_MD"

  # Copy skills as custom slash commands
  for skill_dir in "$SCRIPT_DIR/skills/"*/; do
    skill_name="$(basename "$skill_dir")"
    if [ -f "$skill_dir/SKILL.md" ]; then
      cp "$skill_dir/SKILL.md" "$COMMANDS_DIR/$skill_name.md"
    fi
  done
  echo "✅ Skills installed as commands in $COMMANDS_DIR"
}

install_atx() {
  # AWS Transform CLI (atx) discovers user-level skills from ~/.aws/atx/skills/
  # (same SKILL.md format as Kiro). It has no separate "steering" concept, so
  # the steering rules are bundled into a single always-relevant skill.
  ATX_SKILLS_DIR="$HOME/.aws/atx/skills"
  mkdir -p "$ATX_SKILLS_DIR"

  # Copy migration skills
  cp -r "$SCRIPT_DIR/skills/"* "$ATX_SKILLS_DIR/"
  echo "✅ Skills installed to $ATX_SKILLS_DIR"

  # Bundle steering rules into a single skill so atx injects them as context
  STEERING_SKILL_DIR="$ATX_SKILLS_DIR/was-migration-steering-rules"
  mkdir -p "$STEERING_SKILL_DIR/references"

  # Copy each steering rule as a reference doc
  for file in "$SCRIPT_DIR/steering/"*.md; do
    cp "$file" "$STEERING_SKILL_DIR/references/$(basename "$file")"
  done

  # Generate the SKILL.md that points the agent at the steering references
  {
    echo "---"
    echo "name: was-migration-steering-rules"
    echo "description: Java 17+ and Spring Boot coding standards for WAS migrations — best practices, naming conventions, project structure, and error handling. Consult when writing or migrating any Java code."
    echo "---"
    echo "# WAS Migration Steering Rules"
    echo ""
    echo "When writing, generating, or migrating Java or Spring Boot code, follow the coding standards in this skill's \`references/\` directory:"
    echo ""
    for file in "$SCRIPT_DIR/steering/"*.md; do
      echo "- \`references/$(basename "$file")\`"
    done
    echo ""
    echo "Apply these rules to all Java (\`**/*.java\`) files: constructor injection, Records for DTOs, \`@RestControllerAdvice\` error handling, domain-driven package structure, and the naming conventions defined in the references."
  } > "$STEERING_SKILL_DIR/SKILL.md"
  echo "✅ Steering rules installed as skill in $STEERING_SKILL_DIR"
}

# Main
case "${1:-}" in
  kiro)  install_kiro ;;
  claude) install_claude ;;
  atx)   install_atx ;;
  *)     usage ;;
esac

echo "🎉 Installation complete!"