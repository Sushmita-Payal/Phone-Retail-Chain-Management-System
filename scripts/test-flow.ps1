# End-to-end test: create store -> add phones (Kafka) -> verify inventory
# Usage: .\scripts\test-flow.ps1
# Prerequisite: docker compose up --build

$ChainStoreUrl = if ($env:CHAIN_STORE_URL) { $env:CHAIN_STORE_URL } else { "http://localhost:8082" }
$InventoryUrl  = if ($env:INVENTORY_URL)  { $env:INVENTORY_URL }  else { "http://localhost:8081" }
$StoreId       = if ($env:STORE_ID)       { $env:STORE_ID }       else { "Store1" }
$Model         = "iPhone 15"

function Wait-ForService {
    param(
        [string]$Name,
        [string]$Url,
        [int]$MaxAttempts = 30
    )

    Write-Host "Waiting for $Name at $Url ..."
    for ($i = 1; $i -le $MaxAttempts; $i++) {
        try {
            Invoke-WebRequest -Uri $Url -Method Get -TimeoutSec 5 -ErrorAction Stop | Out-Null
            Write-Host "  $Name is ready."
            return
        } catch {
            if ($_.Exception.Response.StatusCode.value__ -in 404, 405) {
                Write-Host "  $Name is ready."
                return
            }
            Start-Sleep -Seconds 3
        }
    }

    throw "$Name did not become ready in time."
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Url,
        $Body = $null
    )

    $params = @{
        Method      = $Method
        Uri         = $Url
        ContentType = "application/json"
    }

    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Depth 10)
    }

    return Invoke-RestMethod @params
}

Write-Host "`n=== Phone Retail Chain - E2E Test ===`n"

Wait-ForService -Name "inventory-service" -Url "$InventoryUrl/phone-inventory/inventory/model/test"
Wait-ForService -Name "chain-store" -Url "$ChainStoreUrl/phone-store/store/$StoreId"

Write-Host "`n[1/4] Creating store ..."
$createStoreBody = @(
    @{
        storeName   = "Downtown Phone Hub"
        address     = "123 Main Street NYC"
        managerName = "John Smith"
    }
)
try {
    $createResult = Invoke-Api -Method Post -Url "$ChainStoreUrl/phone-store/store/createStore" -Body $createStoreBody
    Write-Host "  Response: $($createResult.message)"
} catch {
    Write-Host "  Store may already exist, continuing ..."
}

Write-Host "`n[2/4] Fetching store $StoreId ..."
$store = Invoke-Api -Method Get -Url "$ChainStoreUrl/phone-store/store/$StoreId"
Write-Host "  Store: $($store.storeName) | Manager: $($store.managerName)"

Write-Host "`n[3/4] Adding phones via chain-store (Kafka) ..."
$addPhonesBody = @{
    action  = "ADD_PHONES"
    storeId = $StoreId
    payload = @(
        @{
            type       = "Smartphone"
            model      = $Model
            quantity   = 10
            price      = 999.99
            available  = $true
            dateAdded  = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
        }
    )
}
Invoke-Api -Method Post -Url "$ChainStoreUrl/phone-store/store/addPhones" -Body $addPhonesBody | Out-Null
Write-Host "  Event published to Kafka. Waiting for inventory-service to consume ..."
Start-Sleep -Seconds 6

Write-Host "`n[4/4] Verifying inventory on inventory-service ..."
$encodedModel = [uri]::EscapeDataString($Model)
$inventory = Invoke-Api -Method Get -Url "$InventoryUrl/phone-inventory/inventory/model/$encodedModel"

$storeItems = @($inventory | Where-Object { $_.storeId -eq $StoreId })
if ($storeItems.Count -eq 0) {
    Write-Host "`nFAILED: No inventory found for model '$Model' in store '$StoreId'." -ForegroundColor Red
    Write-Host "Check logs: docker compose logs inventory-service chain-store kafka"
    exit 1
}

$item = $storeItems[0]
Write-Host "`nSUCCESS: End-to-end flow completed." -ForegroundColor Green
Write-Host "  Store ID   : $($item.storeId)"
Write-Host "  Model      : $($item.model)"
Write-Host "  Quantity   : $($item.quantity)"
Write-Host "  Price      : $($item.price)"
Write-Host "  Available  : $($item.isAvailable)"
Write-Host ""
