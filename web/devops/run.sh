#!/bin/sh

set -eu
set -f

echo "Nginx user Limits"
ulimit -a
echo "----";

if [ -z "${HREASY_API_HOST:-}" ]
then
  UPSTREAM_HOST=hreasyplatform
else
  UPSTREAM_HOST=$HREASY_API_HOST
fi
echo "-- Configure API upstream to $UPSTREAM_HOST"

EXTERNAL_ALLOWED_IPS=${HREASY_EXTERNAL_ALLOWED_IPS:-"127.0.0.1,::1"}
EXTERNAL_ALLOWLIST_FILE=/etc/nginx/external_allowlist.conf
ORIGINAL_IFS=$IFS
IFS=','
: > "$EXTERNAL_ALLOWLIST_FILE"
for ALLOWED_IP in $EXTERNAL_ALLOWED_IPS
do
  IFS=$ORIGINAL_IFS
  ALLOWED_IP=$(printf '%s' "$ALLOWED_IP" | tr -d '[:space:]')
  case "$ALLOWED_IP" in
    ''|*[!0-9A-Fa-f:./]*)
      echo "Invalid IP or CIDR in HREASY_EXTERNAL_ALLOWED_IPS: $ALLOWED_IP" >&2
      exit 1
      ;;
  esac
  printf 'allow %s;\n' "$ALLOWED_IP" >> "$EXTERNAL_ALLOWLIST_FILE"
  IFS=','
done
IFS=$ORIGINAL_IFS
printf 'deny all;\n' >> "$EXTERNAL_ALLOWLIST_FILE"

(echo "upstream hreasyplatform { server $UPSTREAM_HOST; }" && cat /etc/nginx/conf.d/full_proxy.conf.template) > front-templated.conf

mv front-templated.conf /etc/nginx/conf.d/default.conf
nginx -g 'daemon off;'
