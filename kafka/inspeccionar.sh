#!/usr/bin/env bash
# Lista tópicos y consumer groups, y describe cada uno.
# Uso: BASE=http://localhost:8089 TOKEN=<jwt> ./kafka/inspeccionar.sh
set -euo pipefail

AUTH="Authorization: Bearer $TOKEN"

echo "== Tópicos =="
curl -sS "$BASE/kafka/admin/topics" -H "$AUTH"; echo
for topic in $(curl -sS "$BASE/kafka/admin/topics" -H "$AUTH" | tr -d '[]"' | tr ',' ' '); do
  echo "-- $topic"
  curl -sS "$BASE/kafka/admin/topics/$topic" -H "$AUTH"; echo
done

echo "== Consumer groups =="
curl -sS "$BASE/kafka/admin/consumer-groups" -H "$AUTH"; echo
for grupo in $(curl -sS "$BASE/kafka/admin/consumer-groups" -H "$AUTH" | tr -d '[]"' | tr ',' ' '); do
  echo "-- $grupo"
  curl -sS "$BASE/kafka/admin/consumer-groups/$grupo" -H "$AUTH"; echo
done