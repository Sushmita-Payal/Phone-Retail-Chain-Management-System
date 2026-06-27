#!/usr/bin/env bash
# End-to-end test: create store -> add phones (Kafka) -> verify inventory
# Usage: ./scripts/test-flow.sh
# Prerequisite: docker compose up --build

set -euo pipefail

CHAIN_STORE_URL="${CHAIN_STORE_URL:-http://localhost:8082}"
INVENTORY_URL="${INVENTORY_URL:-http://localhost:8081}"
STORE_ID="${STORE_ID:-Store1}"
MODEL="iPhone 15"

wait_for_service() {
  local name="$1"
  local url="$2"
  local max_attempts="${3:-30}"

  echo "Waiting for ${name} at ${url} ..."
  for ((i = 1; i <= max_attempts; i++)); do
    status="$(curl -s -o /dev/null -w "%{http_code}" "${url}" || true)"
    if [[ "${status}" =~ ^(200|404|405)$ ]]; then
      echo "  ${name} is ready."
      return 0
    fi
    sleep 3
  done

  echo "${name} did not become ready in time." >&2
  exit 1
}

echo
echo "=== Phone Retail Chain - E2E Test ==="
echo

wait_for_service "inventory-service" "${INVENTORY_URL}/phone-inventory/inventory/model/test"
wait_for_service "chain-store" "${CHAIN_STORE_URL}/phone-store/store/${STORE_ID}"

echo
echo "[1/4] Creating store ..."
create_status="$(curl -s -o /tmp/create-store.json -w "%{http_code}" \
  -X POST "${CHAIN_STORE_URL}/phone-store/store/createStore" \
  -H "Content-Type: application/json" \
  -d '[{
    "storeName": "Downtown Phone Hub",
    "address": "123 Main Street NYC",
    "managerName": "John Smith"
  }]')"

if [[ "${create_status}" == "201" ]]; then
  echo "  Response: $(cat /tmp/create-store.json)"
else
  echo "  Store may already exist (HTTP ${create_status}), continuing ..."
fi

echo
echo "[2/4] Fetching store ${STORE_ID} ..."
curl -s "${CHAIN_STORE_URL}/phone-store/store/${STORE_ID}" | tee /tmp/store.json
echo

echo
echo "[3/4] Adding phones via chain-store (Kafka) ..."
curl -s -o /dev/null -X POST "${CHAIN_STORE_URL}/phone-store/store/addPhones" \
  -H "Content-Type: application/json" \
  -d "{
    \"action\": \"ADD_PHONES\",
    \"storeId\": \"${STORE_ID}\",
    \"payload\": [{
      \"type\": \"Smartphone\",
      \"model\": \"${MODEL}\",
      \"quantity\": 10,
      \"price\": 999.99,
      \"available\": true,
      \"dateAdded\": \"$(date -u +"%Y-%m-%dT%H:%M:%S")\"
    }]
  }"

echo "  Event published to Kafka. Waiting for inventory-service to consume ..."
sleep 6

echo
echo "[4/4] Verifying inventory on inventory-service ..."
encoded_model="$(python3 -c "import urllib.parse; print(urllib.parse.quote('${MODEL}'))")"
inventory="$(curl -s "${INVENTORY_URL}/phone-inventory/inventory/model/${encoded_model}")"

if ! echo "${inventory}" | grep -q "\"storeId\"[[:space:]]*:[[:space:]]*\"${STORE_ID}\""; then
  echo
  echo "FAILED: No inventory found for model '${MODEL}' in store '${STORE_ID}'."
  echo "Check logs: docker compose logs inventory-service chain-store kafka"
  exit 1
fi

echo
echo "SUCCESS: End-to-end flow completed."
echo "${inventory}" | python3 -m json.tool 2>/dev/null || echo "${inventory}"
echo
