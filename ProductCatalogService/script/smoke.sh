#!/usr/bin/env bash
# Smoke test: set AUTH_URL and CATALOG_URL in environment, then run this script.
# this script assumes  database  to be empty
# this sciprt is created by opencode as tool  after reading requests.md
# command  used is -> opencode run --agent build --auto -f requests.md "Write smoke.sh: a bash script UNDER 40 LINES that runs the attached requests in order with curl against AUTH_URL and CATALOG_URL (environment variables). Use only curl and sed. Print PASS or FAIL with the request number and the status codes. Keep it simple. Do not run it."
call() {
  R=$(curl -s -w '\n%{http_code}' "$@")
  C=$(printf '%s\n' "$R" | sed -n '$p')
  B=$(printf '%s\n' "$R" | sed '$d')
}
report() {
  if [ "$2" = "$3" ]; then printf 'PASS request %s: status=%s\n' "$1" "$3"
  else printf 'FAIL request %s: status=%s expected=%s\n' "$1" "$3" "$2"; fi
}
CRED='{"email":"admin@shopnest.com","password":"Pass@12345"}'
PROD='{"name":"Handmade Candle","price":199.00,"category":{"name":"home-decoration"}}'
call -X POST -H 'Content-Type: application/json' -d "$CRED" "$AUTH_URL/auth/signup"
report 1 201 "$C"
call -X POST -H 'Content-Type: application/json' -d "$CRED" "$AUTH_URL/auth/login"
report 2 200 "$C"
TOKEN=$(printf '%s' "$B" | sed -n 's/"[A-Za-z]*[Tt]oken"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
[ -n "$TOKEN" ] && printf 'PASS request 2: token received\n' || printf 'FAIL request 2: token missing\n'
call "$CATALOG_URL/products/1"
report 3 404 "$C"
call -X POST -H 'Content-Type: application/json' -d "$PROD" "$CATALOG_URL/products"
report 4 401 "$C"
call -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d "$PROD" "$CATALOG_URL/products"
report 5 201 "$C"
ID=$(printf '%s' "$B" | sed -n 's/"id"[[:space:]]*:[[:space:]]*\([0-9][0-9]*\).*/\1/p')
call -X DELETE -H "Authorization: Bearer $TOKEN" "$CATALOG_URL/products/$ID"
report 6 403 "$C"
