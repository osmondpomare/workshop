#Requires -Version 5.1
<#
.SYNOPSIS
  Install the WAS Migration Developer Kit (Windows version of install-toolkit.sh).

.DESCRIPTION
  Installs skills and steering rules for Kiro CLI, Claude Code CLI, or AWS Transform CLI (atx).

.PARAMETER Target
  Installation target: kiro | claude | atx

.EXAMPLE
  .\install-toolkit.ps1 kiro
#>

[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [string]$Target
)

$ErrorActionPreference = 'Stop'

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

function Show-Usage {
    Write-Host "Usage: .\install-toolkit.ps1 [kiro|claude|atx]"
    Write-Host "  kiro   - Install to %USERPROFILE%\.kiro\skills\ and %USERPROFILE%\.kiro\steering\"
    Write-Host "  claude - Install to %USERPROFILE%\.claude\ (CLAUDE.md + commands)"
    Write-Host "  atx    - Install to %USERPROFILE%\.aws\atx\skills\ (AWS Transform CLI)"
    exit 1
}

function Install-Kiro {
    $kiroDir     = Join-Path $env:USERPROFILE ".kiro"
    $skillsDir   = Join-Path $kiroDir "skills"
    $steeringDir = Join-Path $kiroDir "steering"

    New-Item -ItemType Directory -Force -Path $skillsDir   | Out-Null
    New-Item -ItemType Directory -Force -Path $steeringDir | Out-Null

    Copy-Item -Path (Join-Path $ScriptDir "skills\*")   -Destination $skillsDir   -Recurse -Force
    Copy-Item -Path (Join-Path $ScriptDir "steering\*") -Destination $steeringDir -Recurse -Force

    Write-Host "[OK] Installed to $kiroDir (skills + steering)"
}

function Install-Claude {
    $claudeDir   = Join-Path $env:USERPROFILE ".claude"
    $commandsDir = Join-Path $claudeDir "commands"
    New-Item -ItemType Directory -Force -Path $commandsDir | Out-Null

    $claudeMd = Join-Path $claudeDir "CLAUDE.md"
    $marker    = "# === WAS Migration Developer Kit - Steering Rules ==="
    $endMarker = "# === END WAS Migration Developer Kit ==="

    # Remove previous installation if present
    if (Test-Path $claudeMd) {
        $content = Get-Content -Path $claudeMd -Raw
        if ($content -match [regex]::Escape($marker)) {
            $pattern = "(?s)" + [regex]::Escape($marker) + ".*?" + [regex]::Escape($endMarker) + "\r?\n?"
            $content = [regex]::Replace($content, $pattern, "")
            Set-Content -Path $claudeMd -Value $content -NoNewline
        }
    }

    # Append steering rules
    $sb = New-Object System.Text.StringBuilder
    [void]$sb.AppendLine("")
    [void]$sb.AppendLine($marker)
    foreach ($file in Get-ChildItem -Path (Join-Path $ScriptDir "steering") -Filter "*.md" -File) {
        [void]$sb.AppendLine("")
        [void]$sb.AppendLine((Get-Content -Path $file.FullName -Raw))
    }
    [void]$sb.AppendLine("")
    [void]$sb.AppendLine($endMarker)
    Add-Content -Path $claudeMd -Value $sb.ToString()
    Write-Host "[OK] Steering rules appended to $claudeMd"

    # Copy skills as custom slash commands
    foreach ($skillDir in Get-ChildItem -Path (Join-Path $ScriptDir "skills") -Directory) {
        $skillFile = Join-Path $skillDir.FullName "SKILL.md"
        if (Test-Path $skillFile) {
            Copy-Item -Path $skillFile -Destination (Join-Path $commandsDir "$($skillDir.Name).md") -Force
        }
    }
    Write-Host "[OK] Skills installed as commands in $commandsDir"
}

function Install-Atx {
    # AWS Transform CLI (atx) discovers user-level skills from ~/.aws/atx/skills/
    # (same SKILL.md format as Kiro). It has no separate "steering" concept, so
    # the steering rules are bundled into a single always-relevant skill.
    $atxSkillsDir = Join-Path $env:USERPROFILE ".aws\atx\skills"
    New-Item -ItemType Directory -Force -Path $atxSkillsDir | Out-Null

    # Copy migration skills
    Copy-Item -Path (Join-Path $ScriptDir "skills\*") -Destination $atxSkillsDir -Recurse -Force
    Write-Host "[OK] Skills installed to $atxSkillsDir"

    # Bundle steering rules into a single skill so atx injects them as context
    $steeringSkillDir = Join-Path $atxSkillsDir "was-migration-steering-rules"
    $referencesDir    = Join-Path $steeringSkillDir "references"
    New-Item -ItemType Directory -Force -Path $referencesDir | Out-Null

    $steeringFiles = @(Get-ChildItem -Path (Join-Path $ScriptDir "steering") -Filter "*.md" -File)
    foreach ($file in $steeringFiles) {
        Copy-Item -Path $file.FullName -Destination (Join-Path $referencesDir $file.Name) -Force
    }

    # Generate the SKILL.md that points the agent at the steering references
    $sb = New-Object System.Text.StringBuilder
    [void]$sb.AppendLine("---")
    [void]$sb.AppendLine("name: was-migration-steering-rules")
    [void]$sb.AppendLine("description: Java 17+ and Spring Boot coding standards for WAS migrations - best practices, naming conventions, project structure, and error handling. Consult when writing or migrating any Java code.")
    [void]$sb.AppendLine("---")
    [void]$sb.AppendLine("# WAS Migration Steering Rules")
    [void]$sb.AppendLine("")
    [void]$sb.AppendLine("When writing, generating, or migrating Java or Spring Boot code, follow the coding standards in this skill's ``references/`` directory:")
    [void]$sb.AppendLine("")
    foreach ($file in $steeringFiles) {
        [void]$sb.AppendLine("- ``references/$($file.Name)``")
    }
    [void]$sb.AppendLine("")
    [void]$sb.AppendLine("Apply these rules to all Java (``**/*.java``) files: constructor injection, Records for DTOs, ``@RestControllerAdvice`` error handling, domain-driven package structure, and the naming conventions defined in the references.")
    Set-Content -Path (Join-Path $steeringSkillDir "SKILL.md") -Value $sb.ToString()
    Write-Host "[OK] Steering rules installed as skill in $steeringSkillDir"
}

switch ($Target) {
    'kiro'   { Install-Kiro }
    'claude' { Install-Claude }
    'atx'    { Install-Atx }
    default  { Show-Usage }
}

Write-Host "Installation complete!"
