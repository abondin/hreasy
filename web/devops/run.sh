#!/bin/sh

echo "Nginx user Limits"
ulimit -a
echo "----";

if [ -z "$HREASY_API_HOST" ]
then
  UPSTREAM_HOST=hreasy-api-facade
else
  UPSTREAM_HOST=$HREASY_API_HOST
fi
echo "-- Configure API upstream to $UPSTREAM_HOST"
(echo "upstream hreasyplatform { server $UPSTREAM_HOST; }" && cat /etc/nginx/conf.d/full_proxy.conf.template) > front-templated.conf

mv front-templated.conf /etc/nginx/conf.d/default.conf
nginx -g 'daemon off;'
