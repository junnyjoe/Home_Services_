<#
Script: clean-choco.ps1
But: retire les fichiers hoco/ChocolateyScratch du suivi Git local et commit la suppression.
Usage: Exécuter depuis la racine du projet PowerShell (ex: .\scripts\clean-choco.ps1)
#>
Param()
if (-not (Test-Path .git)) {
    Write-Error "Ce dossier ne semble pas être un dépôt Git. Exécutez ce script depuis la racine du dépôt."
    exit 1
}

$path = 'hoco/ChocolateyScratch'
if (-not (Test-Path $path)) {
    Write-Host "Aucun dossier '$path' trouvé — rien à faire."
    exit 0
}

Write-Host "Removing $path from Git tracking (git rm --cached -r)..."
git rm -r --cached $path
if ($LASTEXITCODE -ne 0) {
    Write-Error "Git command failed (exit code: $LASTEXITCODE). Run manually: git rm -r --cached $path"
    exit $LASTEXITCODE
}

Write-Host "Adding entry to .gitignore"
Add-Content -Path .gitignore -Value "`n$path/"

git add .gitignore
git commit -m "Remove Chocolatey scratch files from repo" || Write-Host "No commit needed"

Write-Host "Done. Push changes: git push origin main"
