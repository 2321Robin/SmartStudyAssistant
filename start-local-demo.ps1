$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

function Start-DemoProcess {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Title,

        [Parameter(Mandatory = $true)]
        [string]$Command
    )

    Start-Process pwsh -WorkingDirectory $root -ArgumentList @(
        '-NoExit',
        '-Command',
        "& { `$Host.UI.RawUI.WindowTitle = '$Title'; $Command }"
    )
}

Start-DemoProcess -Title 'user-service' -Command '$env:SPRING_PROFILES_ACTIVE=''local''; mvn -f backend/user-service/pom.xml spring-boot:run'
Start-DemoProcess -Title 'plan-service' -Command 'mvn -f backend/plan-service/pom.xml spring-boot:run'
Start-DemoProcess -Title 'resource-service' -Command 'mvn -f backend/resource-service/pom.xml spring-boot:run'
Start-DemoProcess -Title 'note-service' -Command 'mvn -f backend/note-service/pom.xml spring-boot:run'
Start-DemoProcess -Title 'gateway-service' -Command '$env:SPRING_PROFILES_ACTIVE=''local''; mvn -f backend/gateway-service/pom.xml spring-boot:run'

Start-Sleep -Seconds 15

Start-DemoProcess -Title 'web-client' -Command 'Set-Location web-client; npm install; npm run dev'

Write-Host 'Local demo startup commands launched.'
Write-Host 'Frontend: http://localhost:5173'
Write-Host 'Gateway:  http://localhost:8088'
