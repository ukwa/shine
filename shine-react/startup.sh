#!/usr/bin/env bash
envsubst '${SOLR_URL}' < ./nginx.conf.template > /etc/nginx/conf.d/default.conf
/usr/local/openresty/bin/openresty -g "daemon off;"
