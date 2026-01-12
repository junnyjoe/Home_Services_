Param(
    [string]$RepoUrl = "https://github.com/bellaangemickael2006/abidjan_prices.git",
    [string]$Username = "bellaangemickael2006",
    [string]$Email = "bellaangemickael2006@gmail.com"
)

Write-Host "Configuring Git identity..."
git config --global user.name $Username
git config --global user.email $Email

Write-Host "Setting remote origin to $RepoUrl"
git remote remove origin 2>$null || Write-Host "No existing origin to remove"
git remote add origin $RepoUrl

Write-Host "Attempting to push branch 'main' to origin..."
$output = & git push -u origin main 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "Push succeeded. Repository is on GitHub."
    exit 0
}

Write-Host "\nPush failed. Git output follows:\n"
Write-Host $output

if ($output -match "403|Permission to") {
    Write-Host "\nDetected permission error (403). Typical fixes:"
    Write-Host "  1) Authenticate with GitHub CLI: 'gh auth login' (install gh first)"
    Write-Host "  2) Use a Personal Access Token (PAT) for HTTPS: generate one at https://github.com/settings/tokens (scope: repo)"
    Write-Host "  3) Or switch to SSH: generate an SSH key and add it to GitHub, then set remote to git@github.com:<user>/<repo>.git"

    $answer = Read-Host "Open PAT creation page in your browser now? (Y/N)"
    if ($answer -match '^[Yy]') {
        Start-Process "https://github.com/settings/tokens/new?scopes=repo&description=abidjan_prices_push"
    }
}

Write-Host "If you need a helper script to create a PAT or configure SSH, tell me and I'll add it."
exit 1
