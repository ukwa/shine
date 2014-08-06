from __future__ import print_function
import json, sys, codecs, hashlib
import urllib, datetime, re
from pprint import pprint

# Config:
endpoint = "http://192.168.1.215:8983/solr/jisc5/select"
base_ndq = "%s?wt=json&indent=true&distrib=false&sort=crawl_date+asc" % endpoint
# "&cache=false"

def doQuery(tag, facet, url):
	print("URL: "+url)
	response = urlo.open(url)
	rj = json.load(response)
	print(tag + ".QTime " + str(rj['responseHeader']['QTime']))
	print(tag + ".numFound " + str(rj['response']['numFound']))
	if rj.has_key('facet_counts'):
		pprint(rj['facet_counts']['facet_fields'][facet][0])

# The URL Opener:
#urlo=urllib.URLopener()
urlo = urllib.FancyURLopener({"http":"http://explorer.bl.uk:3127"})

# Query for *:*:
doQuery("NO-FACETS-ALL", None, base_ndq + "&q=*:*")

# Pseudo-random word queries:
words = [ "jam", "bananas", "gary barlow", "doctor who", "sherlock holmes" ]
for word in words:
	doQuery("NO-FACETS-"+word, None, base_ndq + ("&q=\"%s\"" % word ) )


# Facets
facet_method = "&facet=true&facet.mincount=1&facet.sort=count&facet.threads=5" #&facet.method=enum&facet.enum.cache.minDf=0"
facets = ["public_suffix", "content_type_norm", "crawl_years", "content_language", "links_public_suffixes", "author", "postcode_district", "domain", "links_domains" ]

# Each facet seperately:
for facet in facets:
	q = base_ndq + facet_method + "&q=*:*" + ("&facet.field=%s" % facet)
	doQuery("ONE-FACET-"+facet, facet, q )

# All word-facet combinations:
for word in words:
    for facet in facets:
	    q = base_ndq + facet_method + ("&q=\"%s\"" % word ) + ("&facet.field=%s" % facet)
	    doQuery("ONE-FACET-"+facet+"-"+word, facet, q )


# All the facets at once:
for word in words:
    q = base_ndq + facet_method + ("&q=\"%s\"" % word )
    for facet in facets:
    	q = q  + ("&facet.field=%s" % facet)
    doQuery("ALL-FACETS-"+word, "public_suffix", q )

