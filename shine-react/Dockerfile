FROM openresty/openresty:alpine-fat

COPY index.html config.js web.js /usr/share/nginx/html/
COPY nginx/shine.conf nginx.conf.template
COPY startup.sh .

ENV SOLR_URL http://localhost:8983/solr/discovery

CMD ./startup.sh


