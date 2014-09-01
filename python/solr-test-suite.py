from __future__ import print_function
import json, sys, codecs, hashlib
import urllib, datetime, re, random
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
# - Add group.true&group.field=domain etc. to see how that affects performance.
# - More Like This?
# - Carrot Clustering?

# The URL Opener:
#urlo=urllib.URLopener()
urlo = urllib.FancyURLopener({"http":"http://explorer.bl.uk:3127"})

word_list = open('long-dictionary-word-list.txt').read().splitlines()
num_words = 200
run_no_facet_queries = False
run_single_facet_queries = False
run_all_facet_queries = True

def elapsed_ms( start_time, end_time ):
	dt = end_time - start_time
	return (dt.days*24*60*60 + dt.seconds) * 1000 + dt.microseconds/1000.0

def doQuery(tag, facet, url):
	print("URL: "+url)
	start_time = datetime.datetime.now()
	response = urlo.open(url)
	rj = json.load(response)
	end_time = datetime.datetime.now()
	print(tag + ".QTime.[ms] %s numFound %s wallclock.[ms] %s" % (rj['responseHeader']['QTime'], rj['response']['numFound'], elapsed_ms(start_time,end_time) ) )
	# And output the first facet:
	if rj.has_key('facet_counts'):
		if len(rj['facet_counts']['facet_fields'][facet]) > 0:
		    print(tag + ".facetValues[0] " + rj['facet_counts']['facet_fields'][facet][0])
	sys.stdout.flush()


def runQueries(endpoint):
	print("Running queries against: "+endpoint)
	start_time = datetime.datetime.now()
	# Config:
	base_ndq = "%s&sort=crawl_date+asc" % endpoint
	# "&cache=false"

	# Query for *:*:
	doQuery("NO-FACETS-ALL", None, base_ndq + "&q={!cache=false}*:*")

	# Pseudo-random word queries:
	words = []
	for x in range(1,num_words):
  		words.append(random.choice(word_list)) 

	# Optionally skip the no-facet queries
	if run_no_facet_queries:
		for word in words:
			doQuery("NO-FACETS-"+word, None, base_ndq + ("&q={!cache=false}\"%s\"" % word ) )

	# Fields
	general_fields = [ "wayback_date", "url", "text", "title", "source_file_s", "id", "hash", "description", "crawl_dates", "content_length", "content_text_length" ]
	location_fields = [ "postcode", "location" ]

	# Facets
	facet_method = "&facet=true&facet.mincount=1&facet.sort=count" #&facet.method=fcs&facet.threads=100&facet.enum.cache.minDf=100"
	# This are the core facets we really need to support:
	facets = ["crawl_years", "content_language", "public_suffix", "links_public_suffixes", "domain", "links_domains", 
	          "content_type_norm", "author", "postcode_district"]
	# These are additional facets that would be nice to have:
	additional_facets = ['licence_url', 'last_modified', "last_modified_year", "keywords", "author"]
	additional_link_facets = [ "links_hosts" ]
	additional_url_facets =[ 'host', 'url_type' ]

	# Nice-to-have format-related fields/facets:
	format_facets = ["generator", "content_type", "content_type_full", "content_type_tika", "content_type_droid", "content_ffb", "content_type_ext",
	          "elements_used", "content_encoding", "content_type_served", "content_type_version", "content_first_bytes", "content_metadata_ss",
	          "parse_error", "pdf_pdfa_is_valid", "pdf_pdfa_errors", "xml_root_ns", "server" ]
	# And also the experimental ssdeep_hash_bs_* ssdeep_hash_ngram_bs_* fields.

	# Generator appears to bump RAM (bumped to 18GB during building) quite a bit - perhaps many of these should be DocValues? 
	# Added the elements_used pushed to 19GB.
	# But fast once cached.

	# Date Range Faceting (harder work I think):
	facet_date_range_1y = "&facet.date=crawl_dates&facet.date.start=1994-01-01T00:00:00Z&facet.date.end=NOW/YEAR%2B1YEAR&facet.date.gap=%2B1YEAR"
	facet_date_range_6m = "&facet.date=crawl_dates&facet.date.start=1994-01-01T00:00:00Z&facet.date.end=NOW/YEAR%2B1YEAR&facet.date.gap=%2B6MONTHS"
	# Hook it in
	#facet_method = facet_method + facet_date_range_1y


	# Optionally skip the single-facet queries
	if run_single_facet_queries:

		# Each facet seperately:
		for facet in facets:
			q = base_ndq + facet_method + "&q={!cache=false}*:*" + ("&facet.field=%s" % facet)
			doQuery("ONE-FACET-"+facet, facet, q )	

		# All word-facet combinations:
		for word in words:
		    for facet in facets:
			    q = base_ndq + facet_method + ("&q={!cache=false}\"%s\"" % word ) + ("&facet.field=%s" % facet)
			    doQuery("ONE-FACET-"+facet+"-"+word, facet, q )


	# Optionally skip the all-facet queries
	if run_all_facet_queries:

		# All the facets at once:
		for word in words:
		    q = base_ndq + facet_method + ("&q={!cache=false}\"%s\"" % word )
		    for facet in facets:
		    	q = q  + ("&facet.field=%s" % facet)
		    doQuery("ALL-FACETS-"+word, "public_suffix", q )

	end_time = datetime.datetime.now()
	elapsed = end_time - start_time
	print("TIMING %s [ms] for %s " %(elapsed_ms(start_time,end_time), endpoint))

# Automatic distributed mode:
#runQueries("http://192.168.1.181:8983/solr/jisc5/select?wt=json&indent=true")

# non-distributed mode:
#runQueries("http://192.168.1.181:8983/solr/jisc5/select?wt=json&indent=true&distrib=false")
#runQueries("http://192.168.1.182:8983/solr/jisc5/select?wt=json&indent=true&distrib=false")
#runQueries("http://192.168.1.203:8983/solr/jisc5/select?wt=json&indent=true&distrib=false")
#runQueries("http://192.168.1.215:8983/solr/jisc5/select?wt=json&indent=true&distrib=false")


# Attempt to controlled over 3 on each server separately:
#runQueries("http://192.168.1.181:8983/solr/jisc5/select?wt=json&indent=true&shards=192.168.1.181:8983/solr/jisc5,192.168.1.181:8984/solr/jisc5,192.168.1.181:8985/solr/jisc5")
runQueries("http://192.168.1.182:8983/solr/jisc5/select?wt=json&indent=true&shards=192.168.1.182:8983/solr/jisc5,192.168.1.182:8984/solr/jisc5,192.168.1.182:8985/solr/jisc5")

# Attempt to controlled over 6 on each server separately:
#runQueries("http://192.168.1.181:8983/solr/jisc5/select?wt=json&indent=true&shards=192.168.1.181:8983/solr/jisc5,192.168.1.181:8984/solr/jisc5,192.168.1.181:8985/solr/jisc5,192.168.1.181:8986/solr/jisc5,192.168.1.181:8987/solr/jisc5,192.168.1.181:8988/solr/jisc5")
#runQueries("http://192.168.1.182:8983/solr/jisc5/select?wt=json&indent=true&shards=192.168.1.182:8983/solr/jisc5,192.168.1.182:8984/solr/jisc5,192.168.1.182:8985/solr/jisc5,192.168.1.182:8986/solr/jisc5,192.168.1.182:8987/solr/jisc5,192.168.1.182:8988/solr/jisc5")
#runQueries("http://192.168.1.203:8983/solr/jisc5/select?wt=json&indent=true&shards=192.168.1.203:8983/solr/jisc5,192.168.1.203:8984/solr/jisc5,192.168.1.203:8985/solr/jisc5,192.168.1.203:8986/solr/jisc5,192.168.1.203:8987/solr/jisc5,192.168.1.203:8988/solr/jisc5")
#runQueries("http://192.168.1.215:8983/solr/jisc5/select?wt=json&indent=true&shards=192.168.1.215:8983/solr/jisc5,192.168.1.215:8984/solr/jisc5,192.168.1.215:8985/solr/jisc5,192.168.1.215:8986/solr/jisc5,192.168.1.215:8987/solr/jisc5,192.168.1.215:8988/solr/jisc5")

# Attempt to control allocation of distrib mode across all four servers:
# runQueries("http://192.168.1.181:8983/solr/jisc5/select?wt=json&indent=true&shards=192.168.1.181:8983/solr/jisc5,192.168.1.181:8984/solr/jisc5,192.168.1.181:8985/solr/jisc5,192.168.1.181:8986/solr/jisc5,192.168.1.181:8987/solr/jisc5,192.168.1.181:8988/solr/jisc5,192.168.1.182:8983/solr/jisc5,192.168.1.182:8984/solr/jisc5,192.168.1.182:8985/solr/jisc5,192.168.1.182:8986/solr/jisc5,192.168.1.182:8987/solr/jisc5,192.168.1.182:8988/solr/jisc5,192.168.1.203:8983/solr/jisc5,192.168.1.203:8984/solr/jisc5,192.168.1.203:8985/solr/jisc5,192.168.1.203:8986/solr/jisc5,192.168.1.203:8987/solr/jisc5,192.168.1.203:8988/solr/jisc5,192.168.1.215:8983/solr/jisc5,192.168.1.215:8984/solr/jisc5,192.168.1.215:8985/solr/jisc5,192.168.1.215:8986/solr/jisc5,192.168.1.215:8987/solr/jisc5,192.168.1.215:8988/solr/jisc5")

# Attempt to control allocation of distrib mode across just the two dedicated servers (181,182):
# 
#runQueries("http://192.168.1.181:8983/solr/jisc5/select?wt=json&indent=true&shards=192.168.1.181:8983/solr/jisc5,192.168.1.181:8984/solr/jisc5,192.168.1.181:8985/solr/jisc5,192.168.1.181:8986/solr/jisc5,192.168.1.181:8987/solr/jisc5,192.168.1.181:8988/solr/jisc5,192.168.1.181:8989/solr/jisc5,192.168.1.181:8990/solr/jisc5,192.168.1.181:8991/solr/jisc5,192.168.1.181:8992/solr/jisc5,192.168.1.181:8993/solr/jisc5,192.168.1.181:8994/solr/jisc5,192.168.1.182:8983/solr/jisc5,192.168.1.182:8984/solr/jisc5,192.168.1.182:8985/solr/jisc5,192.168.1.182:8986/solr/jisc5,192.168.1.182:8987/solr/jisc5,192.168.1.182:8988/solr/jisc5,192.168.1.182:8989/solr/jisc5,192.168.1.182:8990/solr/jisc5,192.168.1.182:8991/solr/jisc5,192.168.1.182:8992/solr/jisc5,192.168.1.182:8993/solr/jisc5,192.168.1.182:8994/solr/jisc5")

#
# Build shard lists:
#
# Not needed presently as the necessary shard lists have been generated and used above
# Loop over endpoints:
endpoint_template = "http://%s:%s/solr/jisc5/select?distrib=false&wt=json&indent=true"
#hosts = [ "192.168.1.181", "192.168.1.182", "192.168.1.203", "192.168.1.215" ]
hosts = [ "192.168.1.181", "192.168.1.182" ]
#hosts = [ "192.168.1.203", "192.168.1.215" ]
#ports = ["8983", "8984", "8985", "8986", "8987", "8988"]
ports = ["8983", "8984", "8985", "8986", "8987", "8988", "8989", "8990", "8991", "8992", "8993", "8994"]
endpoints = []
for host in hosts:
    for port in ports:
    	pass
    	endpoints.append("%s:%s/solr/jisc5" % (host, port) )
        #runQueries(endpoint_template % (host, port))

#print("&shards=" + ",".join(endpoints))
