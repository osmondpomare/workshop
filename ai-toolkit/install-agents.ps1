#Requires -Version 5.1
<#
.SYNOPSIS
  Install agent configurations for Kiro CLI (Windows version of install-agents.sh).

.DESCRIPTION
  Copies every *.json agent configuration from ./agents/ into %USERPROFILE%\.kiro\agents\.

.PARAMETER Target
  Installation target. Currently only 'kiro' is supported.

.EXAMPLE
  .\install-agents.ps1 kiro
#>

[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [string]$Target
)

$ErrorActionPreference = 'Stop'

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

function Show-Usage {
    Write-Host "Usage: .\install-agents.ps1 [kiro]"
    Write-Host "  kiro   - Install agent configurations to %USERPROFILE%\.kiro\agents\"
    exit 1
}

function Install-Kiro {
    $kiroAgentsDir = Join-Path $env:USERPROFILE ".kiro\agents"
    New-Item -ItemType Directory -Force -Path $kiroAgentsDir | Out-Null

    $agentsSource = Join-Path $ScriptDir "agents"
    $agentFiles = @(Get-ChildItem -Path $agentsSource -Filter "*.json" -File -ErrorAction SilentlyContinue)

    if ($agentFiles.Count -eq 0) {
        Write-Host "[ERROR] No agent configuration files (*.json) found in $agentsSource"
        exit 1
    }

    foreach ($agentFile in $agentFiles) {
        $agentName = [System.IO.Path]::GetFileNameWithoutExtension($agentFile.Name)
        Copy-Item -Path $agentFile.FullName -Destination (Join-Path $kiroAgentsDir "$agentName.json") -Force
        Write-Host "  * Installed agent: $agentName"
    }

    Write-Host "[OK] Agents installed to $kiroAgentsDir"
    Write-Host "[INFO] Run 'kiro-cli agent list' to see them, or start a session with 'kiro-cli chat --agent <name>'."
}

switch ($Target) {
    'kiro'  { Install-Kiro }
    default { Show-Usage }
}

Write-Host "Agent installation complete!"
