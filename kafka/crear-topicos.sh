#!/usr/bin/env bash
# Crea los tópicos definidos en kafka/topics/*.json usando el kafka-admin-service.
# Uso: BASE=http://localhost:8089 TOKEN=<jwt> ./kafka/crear-topicos.sh
set -euo pipefail

for archivo in "$(dirname "$0")"/topics/*.json; do
  echo "Creando tópico desde $archivo"
  curl -sS -o /dev/null -w "HTTP %{http_code}\n" -X POST "$BASE/kafka/admin/topics" \
    -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
    -d @"$archivo"
done