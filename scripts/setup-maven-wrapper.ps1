Param()
Write-Host "Creating .mvn\wrapper directory and downloading maven-wrapper.jar..."
$target = Join-Path -Path $PSScriptRoot -ChildPath "..\.mvn\wrapper"
New-Item -ItemType Directory -Force -Path $target | Out-Null
$jarUrl = 'https://repo1.maven.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar'
$outFile = Join-Path -Path $target -ChildPath 'maven-wrapper.jar'
try {
    Invoke-WebRequest -Uri $jarUrl -OutFile $outFile -UseBasicParsing -ErrorAction Stop
    Write-Host "Downloaded -> $outFile"
    Write-Host "You can now run .\mvnw.cmd clean package or ./mvnw package"
} catch {
    Write-Error "Failed to download wrapper jar: $_"
    Write-Host "You can also download manually: $jarUrl -> .mvn\wrapper\maven-wrapper.jar"
}
