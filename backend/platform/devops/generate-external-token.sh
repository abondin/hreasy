#!/bin/sh

set -eu

command -v openssl >/dev/null || {
  echo "openssl is required" >&2
  exit 1
}
command -v sha256sum >/dev/null || {
  echo "sha256sum is required" >&2
  exit 1
}

printf 'External system name: ' >&2
IFS= read -r SYSTEM
printf 'HR Easy subject (employee email): ' >&2
IFS= read -r SUBJECT

case "$SYSTEM" in
  ''|*[!A-Za-z0-9._-]*)
    echo "System name may contain only letters, digits, dot, underscore, and hyphen" >&2
    exit 1
    ;;
esac
case "$SUBJECT" in
  ''|*[!A-Za-z0-9@._+-]*)
    echo "Subject may contain only email-safe characters" >&2
    exit 1
    ;;
esac

TOKEN=$(openssl rand -hex 32)
TOKEN_SHA256=$(printf '%s' "$TOKEN" | sha256sum)
TOKEN_SHA256=${TOKEN_SHA256%% *}

printf 'Bearer token (give to the external system; shown once):\n%s\n\n' "$TOKEN"
printf 'Docker Compose environment for hreasyplatform:\n'
printf 'environment:\n'
printf '  HREASY_EXTERNAL_API_TOKENS_0_SYSTEM: %s\n' "$SYSTEM"
printf '  HREASY_EXTERNAL_API_TOKENS_0_SUBJECT: %s\n' "$SUBJECT"
printf '  HREASY_EXTERNAL_API_TOKENS_0_SHA256: %s\n' "$TOKEN_SHA256"
