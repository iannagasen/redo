param([string]$HostName = "shopbuddy.com")

$ip = minikube ip
$entry = "$ip $HostName"
$file = "C:\Windows\System32\drivers\etc\hosts"

# Escape entry for regex safely and check if it exists
if (-not (Select-String -Path $file -Pattern ([regex]::Escape($entry)) -Quiet)) {
    Add-Content -Path $file -Value "`n$entry"
    Write-Host "Added: $entry"
} else {
    Write-Host "Entry already exists."
}