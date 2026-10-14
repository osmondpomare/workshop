#Requires -Version 5.1
<#
.SYNOPSIS
  WAS Migration Developer Kit - MCP Setup (Windows version of install-mcp.sh).

.DESCRIPTION
  Installs the MCP server configuration into Kiro, Claude Code CLI and/or AWS Transform CLI (atx)
  config files. Uses native PowerShell JSON handling (no jq required). Only adds servers that are
  missing; existing entries are left untouched.

.PARAMETER Target
  kiro | claude | atx | all

.EXAMPLE
  .\install-mcp.ps1 all
#>

[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [string]$Target
)

$ErrorActionPreference = 'Stop'

$ScriptDir    = Split-Path -Parent $MyInvocation.MyCommand.Path
$McpTemplate  = Join-Path $ScriptDir "settings\mcp.json"

# Config file locations
$KiroMcpFile   = Join-Path $env:USERPROFILE ".kiro\settings\mcp.json"
$ClaudeMcpFile = Join-Path $env:USERPROFILE ".claude.json"
$AtxMcpFile    = Join-Path $env:USERPROFILE ".aws\atx\mcp.json"

# Defaults
$AzureOrg     = "YourOrganizacion"
$AzureProject = "YourProject"

# -- Helpers -------------------------------------------------------------------

function Write-Log  { param($m) Write-Host "  $m" }
function Write-Ok   { param($m) Write-Host "[OK] $m" }
function Write-Warn { param($m) Write-Host "[WARN] $m" }
function Write-Err  { param($m) Write-Error $m; exit 1 }
function Write-Sep  { Write-Host "----------------------------------------------" }

function Show-Usage {
    Write-Host ""
    Write-Host "Usage: .\install-mcp.ps1 [kiro|claude|atx|all]"
    Write-Host ""
    Write-Host "  kiro   - Install MCP config to %USERPROFILE%\.kiro\settings\mcp.json"
    Write-Host "  claude - Install MCP config to %USERPROFILE%\.claude.json (Claude Code CLI)"
    Write-Host "  atx    - Install MCP config to %USERPROFILE%\.aws\atx\mcp.json (AWS Transform CLI)"
    Write-Host "  all    - Install to all of the above"
    Write-Host ""
    exit 1
}

# Detect absolute path to npx (desktop apps don't inherit shell PATH)
function Get-NpxPath {
    $cmd = Get-Command npx -ErrorAction SilentlyContinue
    if (-not $cmd) {
        Write-Err "npx not found. Install Node.js 20+ and try again."
    }
    return $cmd.Source
}

# Derive the node bin directory from the npx path
function Get-NodeBinDir { param($npxPath) return (Split-Path -Parent $npxPath) }

# Verify required PATs are exported based on what servers are in the template
function Test-Pat {
    param($template)

    $hasAzure  = $null -ne $template.mcpServers.'azure-devops'
    $hasGithub = $null -ne $template.mcpServers.'github'

    if ($hasAzure -and [string]::IsNullOrEmpty($env:AZURE_DEVOPS_PAT)) {
        Write-Host ""
        Write-Warn "AZURE_DEVOPS_PAT is not set in the current environment."
        Write-Host "   Set it (PowerShell) and reload:"
        Write-Host ""
        Write-Host '   $env:AZURE_DEVOPS_PAT = "your-token-here"'
        Write-Host '   [Environment]::SetEnvironmentVariable("AZURE_DEVOPS_PAT", "your-token-here", "User")'
        Write-Host ""
        $confirm = Read-Host "   Continue anyway? [y/N]"
        if ($confirm -notmatch '^[Yy]$') { exit 0 }
    }

    if ($hasGithub -and [string]::IsNullOrEmpty($env:GITHUB_PERSONAL_ACCESS_TOKEN)) {
        Write-Host ""
        Write-Warn "GITHUB_PERSONAL_ACCESS_TOKEN is not set in the current environment."
        Write-Host "   Set it (PowerShell) and reload:"
        Write-Host ""
        Write-Host '   $env:GITHUB_PERSONAL_ACCESS_TOKEN = "your-token-here"'
        Write-Host '   [Environment]::SetEnvironmentVariable("GITHUB_PERSONAL_ACCESS_TOKEN", "your-token-here", "User")'
        Write-Host ""
        $confirm = Read-Host "   Continue anyway? [y/N]"
        if ($confirm -notmatch '^[Yy]$') { exit 0 }
    }
}

# Read .mcpServers from the template, patching azure-devops with runtime values.
# Returns a PSCustomObject whose properties are the server definitions.
function Build-Servers {
    param($template, $npxPath, $nodeBin)

    # Deep-clone the template's mcpServers via JSON round-trip
    $servers = $template.mcpServers | ConvertTo-Json -Depth 100 | ConvertFrom-Json

    if ($null -ne $servers.'azure-devops') {
        $azure = $servers.'azure-devops'
        $azure.command = $npxPath
        $azure.args    = @("-y", "@azure-devops/mcp", $AzureOrg)

        $winRoot = if ($env:SystemRoot) { $env:SystemRoot } else { "C:\Windows" }
        $pathValue = "$nodeBin;$winRoot\System32;$winRoot"
        if ($null -eq $azure.env) {
            $azure | Add-Member -NotePropertyName env -NotePropertyValue ([PSCustomObject]@{}) -Force
        }
        $azure.env | Add-Member -NotePropertyName PATH -NotePropertyValue $pathValue -Force
        $azure.env | Add-Member -NotePropertyName AZURE_DEVOPS_DEFAULT_PROJECT -NotePropertyValue $AzureProject -Force
    }

    return $servers
}

# Convert a PSCustomObject to an ordered list of its property names
function Get-ServerKeys { param($servers) return @($servers.PSObject.Properties.Name) }

# Report which servers will be added vs skipped (reads target file BEFORE merge)
function Report-Servers {
    param($targetFile, $servers, $label)

    $existing = $null
    if (Test-Path $targetFile) {
        $raw = Get-Content -Path $targetFile -Raw
        if (-not [string]::IsNullOrWhiteSpace($raw)) {
            $existing = $raw | ConvertFrom-Json
        }
    }

    foreach ($key in (Get-ServerKeys $servers)) {
        $alreadyThere = $existing -and $existing.mcpServers -and ($null -ne $existing.mcpServers.$key)
        if ($alreadyThere) {
            Write-Warn "${label}: '$key' already exists - skipped"
        } else {
            Write-Ok "${label}: '$key' will be added"
        }
    }
}

# Merge new servers into target JSON file - only adds keys that are missing
function Merge-Servers {
    param($targetFile, $servers)

    if (Test-Path $targetFile) {
        $raw = Get-Content -Path $targetFile -Raw
        if ([string]::IsNullOrWhiteSpace($raw)) {
            $root = [PSCustomObject]@{ mcpServers = [PSCustomObject]@{} }
        } else {
            $root = $raw | ConvertFrom-Json
        }
    } else {
        $root = [PSCustomObject]@{ mcpServers = [PSCustomObject]@{} }
    }

    if ($null -eq $root.mcpServers) {
        $root | Add-Member -NotePropertyName mcpServers -NotePropertyValue ([PSCustomObject]@{}) -Force
    }

    foreach ($key in (Get-ServerKeys $servers)) {
        if ($null -eq $root.mcpServers.$key) {
            $root.mcpServers | Add-Member -NotePropertyName $key -NotePropertyValue $servers.$key -Force
        }
    }

    return $root
}

function Save-Json {
    param($object, $targetFile)
    $dir = Split-Path -Parent $targetFile
    if ($dir) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }
    $object | ConvertTo-Json -Depth 100 | Set-Content -Path $targetFile -Encoding UTF8
}

# -- Installers ----------------------------------------------------------------

function Install-Target {
    param($targetFile, $servers, $label, $verifyHint)

    $existed = Test-Path $targetFile
    if ($existed) { Report-Servers $targetFile $servers $label }

    $merged = Merge-Servers $targetFile $servers
    Save-Json $merged $targetFile

    if ($existed) {
        Write-Ok "$label config saved: $targetFile"
    } else {
        foreach ($key in (Get-ServerKeys $servers)) { Write-Ok "${label}: '$key' added" }
        Write-Ok "$label config created: $targetFile"
    }
    Write-Log $verifyHint
}

# -- Main ----------------------------------------------------------------------

Write-Sep
Write-Host "  WAS Migration Developer Kit - MCP Setup"
Write-Sep

if (-not (Test-Path $McpTemplate)) {
    Write-Err "MCP template not found: $McpTemplate"
}

$Template = Get-Content -Path $McpTemplate -Raw | ConvertFrom-Json

Test-Pat $Template

$NpxPath = Get-NpxPath
$NodeBin = Get-NodeBinDir $NpxPath

Write-Log "Template  : $McpTemplate"
Write-Log "npx path  : $NpxPath"
Write-Log "Node bin  : $NodeBin"
Write-Log "Org       : $AzureOrg"
Write-Log "Project   : $AzureProject"
Write-Sep

$Servers = Build-Servers $Template $NpxPath $NodeBin

switch ($Target) {
    'kiro' {
        Install-Target $KiroMcpFile $Servers "Kiro" "Restart Kiro and open the MCP Servers panel to connect."
    }
    'claude' {
        Install-Target $ClaudeMcpFile $Servers "Claude" "Run 'claude mcp list' to verify the server is registered."
    }
    'atx' {
        Install-Target $AtxMcpFile $Servers "atx" "Run 'atx mcp tools' to verify the server is registered."
    }
    'all' {
        Install-Target $KiroMcpFile   $Servers "Kiro"   "Restart Kiro and open the MCP Servers panel to connect."
        Install-Target $ClaudeMcpFile $Servers "Claude" "Run 'claude mcp list' to verify the server is registered."
        Install-Target $AtxMcpFile    $Servers "atx"    "Run 'atx mcp tools' to verify the server is registered."
    }
    default { Show-Usage }
}

Write-Sep
Write-Host "MCP setup complete!"
Write-Sep
