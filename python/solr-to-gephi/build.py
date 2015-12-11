#!/usr/bin/env python

import json
from pprint import pprint

src_host = "www.bl.uk"

links = {}
links[src_host] = {}
hosts = set()
hosts.add(src_host)

with open('from-bl.json') as data_file:    
    data = json.load(data_file)
    for doc in data['response']['docs']:
        for host in doc['links_hosts']:
            if not links[src_host].has_key(host):
                links[src_host][host] = 0
            links[src_host][host] += 1
            hosts.add(host)

with open('to-bl.json') as data_file:    
    data = json.load(data_file)
    for doc in data['response']['docs']:
        if True:
            host = doc['host']
            hosts.add(host)
            if not links.has_key(host):
                links[host] = {}
                links[host][src_host] = 0
            links[host][src_host] += 1

hi = 0
hids = {}
for h in hosts:
    hids[h] = h #"h%s" % hi
    hi += 1

print "nodedef>name VARCHAR,label VARCHAR"
for h in hosts:
    print "%s,%s" % (hids[h],h)

print "edgedef>node1 VARCHAR,node2 VARCHAR,weight DOUBLE,directed BOOLEAN"
for src in links:
    for dest in links[src]:
        print "%s,%s,%s,true" % (hids[src], hids[dest], links[src][dest])
