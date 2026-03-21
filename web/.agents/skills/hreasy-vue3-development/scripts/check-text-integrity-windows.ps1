param(
  [string[]]$Paths = @(
    "src",
    ".agents",
    "AGENTS.md",
    "README.md"
  ),
  [string[]]$Include = @("*.vue", "*.ts", "*.md", "*.json"),
  [switch]$SkipUnicodeEscapes,
  [switch]$SkipBom,
  [switch]$SkipUtf8Validation,
  [switch]$SkipReplacementChar
)

$utf8Strict = [System.Text.UTF8Encoding]::new($false, $true)
$issues = New-Object System.Collections.Generic.List[string]

function Get-TargetFiles {
  param(
    [string[]]$InputPaths,
    [string[]]$Patterns
  )

  $seen = New-Object System.Collections.Generic.HashSet[string]([System.StringComparer]::OrdinalIgnoreCase)
  $result = New-Object System.Collections.Generic.List[string]

  foreach ($inputPath in $InputPaths) {
    if (-not (Test-Path $inputPath)) {
      continue
    }

    $item = Get-Item $inputPath
    if ($item.PSIsContainer) {
      foreach ($file in Get-ChildItem -Path $item.FullName -Recurse -File -Include $Patterns) {
        if ($seen.Add($file.FullName)) {
          $result.Add($file.FullName)
        }
      }
      continue
    }

    if ($seen.Add($item.FullName)) {
      $result.Add($item.FullName)
    }
  }

  return $result
}

$files = Get-TargetFiles -InputPaths $Paths -Patterns $Include

foreach ($file in $files) {
  $bytes = [System.IO.File]::ReadAllBytes($file)

  if (-not $SkipBom -and $bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
    $issues.Add("BOM: $file")
  }

  $text = $null
  if (-not $SkipUtf8Validation) {
    try {
      $text = $utf8Strict.GetString($bytes)
    } catch {
      $issues.Add("Invalid UTF-8: $file")
      continue
    }
  } else {
    $text = [System.Text.Encoding]::UTF8.GetString($bytes)
  }

  if (-not $SkipUnicodeEscapes -and $text -match "\\u[0-9A-Fa-f]{4}") {
    $issues.Add("Unicode escape sequence: $file")
  }

  if (-not $SkipReplacementChar -and $text.Contains([string][char]0xFFFD)) {
    $issues.Add("Replacement character found: $file")
  }
}

if ($issues.Count -gt 0) {
  Write-Host "Text integrity check failed:" -ForegroundColor Red
  foreach ($issue in $issues) {
    Write-Host " - $issue"
  }
  exit 1
}

Write-Host "Text integrity check passed. UTF-8 without BOM, no Unicode escapes, no replacement chars found." -ForegroundColor Green
