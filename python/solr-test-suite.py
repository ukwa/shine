from __future__ import print_function
import json, sys, codecs, hashlib
import urllib, datetime, re
from pprint import pprint


# Variations & Option
# 
# This code currently has examples of
# - different facet methods (enum commented out below)
# - using a date range query instead of/as well as the crawl years facet (commented out right now)
# - using multiple threads to count facets (currently set to 20)
# - preventing the results from being cached (see cache=false commented out below, but it's not clear that it clears/ignored existing caches)
#
# Things to try:
# - Either change the words and add cache=false, or use random words from a larger selection (are these really equivalent?)
# - Allow it to repeat over all shards. See if any are slower/breakier.
# - Allow distributed queries, but control the shards to keep the queries apart.
# - Allow distributed queries, but deliberately query two cores in the same JVM/Jetty instance.
# - Try doing it in full auto-distributed mode and watch if/how it fails.

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

words = [ "jam", "bananas", "gary barlow", "doctor who", "sherlock holmes", "lossless" ]
for word in words:
	doQuery("NO-FACETS-"+word, None, base_ndq + ("&q=\"%s\"" % word ) )


# Facets
facet_method = "&facet=true&facet.mincount=1&facet.sort=count&facet.threads=20" #&facet.method=enum&facet.enum.cache.minDf=100"
facets = ["public_suffix", "content_type_norm", "crawl_years", "content_language", "links_public_suffixes", "author", "postcode_district", "domain", "links_domains", 
          "generator", "content_type", "content_type_full", "content_type_tika", "content_type_droid", "content_ffb", "content_type_ext" ]

# Date Range Faceting (harder work I think):
facet_date_range_1y = "&facet.date=crawl_dates&facet.date.start=1994-01-01T00:00:00Z&facet.date.end=NOW/YEAR%2B1YEAR&facet.date.gap=%2B1YEAR"
facet_date_range_6m = "&facet.date=crawl_dates&facet.date.start=1994-01-01T00:00:00Z&facet.date.end=NOW/YEAR%2B1YEAR&facet.date.gap=%2B6MONTHS"
# Hook it in
#facet_method = facet_method + facet_date_range_1y

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

