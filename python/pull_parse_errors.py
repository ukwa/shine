#!/bin/python

import urllib

#http://192.168.1.181:8983/solr/jisc5/select?distrib=false&rows=100000&q=parse_error%3Aorg.xml.sax.SAXParseException*&fl=wayback_date%2Curl%2Ccontent_length%2Ccontent_type_tika%2Cparse_error&wt=csv&indent=true&sort=random_10 asc


# Loop over endpoints:
#hosts = [ "192.168.1.181", "192.168.1.182", "192.168.1.203", "192.168.1.215" ]
hosts = [ "192.168.1.181", "192.168.1.182" ]
#hosts = [ "192.168.1.203", "192.168.1.215" ]
#ports = ["8983", "8984", "8985", "8986", "8987", "8988"]
ports = ["8983", "8984", "8985", "8986", "8987", "8988", "8989", "8990", "8991", "8992", "8993", "8994"]
count = 0
for host in hosts:
    for port in ports:
        endpoint = "http://%s:%s/solr/jisc5" % (host, port)
        query = "/select?distrib=false&rows=100000&q=parse_error%3A[*+TO+*]+-parse_error%3Aorg.apache.tika.sax.WriteOutContentHandler*+-parse_error%3Aorg.xml.sax.SAXParseException*&fl=wayback_date%2Curl%2Ccontent_length%2Ccontent_type_tika%2Cparse_error&wt=csv&indent=true"
        url = endpoint+query
        outfile = "parse_errors_%s.csv" % count
        print("Quering "+url)
        print("Writing to "+outfile)
        #(filename, headers) = urllib.urlretrieve(url,outfile)
        print("Wrote to "+filename)
        count += 1

