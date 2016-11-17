# Shine - Visualising Web Archive via Solr

This is a front-end for Solr indicies created using the warc-discovery package. 

## Setting up the application

### Installing the repository

1. Install Typesafe Activator [from the website](https://www.lightbend.com/activator/download) or using Homebrew if on a Mac.
2. Clone the Shine repository to your local machine including submodules with ```git clone --recursive```

### Configuration

Copy ```conf/application.example.conf``` to ```conf/application.conf```. 

This file will not be commit to git. Paste in the missing fields for Solr and Postgres servers here.


## Running the application in development

Run the application in development from the console:

    activator run

This will run Shine on localhost:9000. If another port is desired use:

    activator "run 8080" 

To run the application using another configuration file use:

    activator run -Dconfig.file=conf/application-localhost-solr.conf

Note that ```application.conf``` is the main configuration file and ```application-test.conf``` will inherit the main config and override specific
values. See ```application-test.conf``` for an example.

To run the application in the Scala REPL, use:

    activator console

To do a clean compilation and then running the app, use:

    activator clean run


## Testing the application

Running all tests:

    activator test

Integration tests only:

    activator "test-only integration.*"


## Ideas

Indexer:

* Ensure DROID is up to date in the indexer.

Improvements:

* Show snippets/fragments instead of highlights when there are no text matches?

Good Trends?

* Politicians:
* Social Media: myspace,facebook,twitter
* Outbreaks: "Foot and mouth","Bird flu"
* News: Terrorist, Iraq, Iraq war, Jubilee,
* Science: Genome, Exome
    
### Trend Analysis ###
* Basic idea is to have some parameterisable queries and axes, so that we can plot features 
like term freqency, element usage, licenses etc. over time, with a range of normalisation options:
    * As absolute numbers.
    * Percentage of resources.
    * Percentage of HTML resources.
    * ???

Here's the raw data for an example, like <blink>:

http://192.168.1.151:8984/solr/jisc/select?q=elements_used%3A%22blink%22&rows=0&wt=json&indent=true&facet=true&facet.field=crawl_year


{
  "responseHeader":{
    "status":0,
    "QTime":128,
    "params":{
      "facet":"true",
      "indent":"true",
      "q":"elements_used:\"blink\"",
      "facet.field":"crawl_year",
      "wt":"json",
      "rows":"0"}},
  "response":{"numFound":682216,"start":0,"maxScore":7.2878804,"docs":[]
  },
  "facet_counts":{
    "facet_queries":{},
    "facet_fields":{
      "crawl_year":[
        "2005",471750,
        "2003",43360,
        "2004",36009,
        "2002",30581,
        "2001",22149,
        "2007",18977,
        "2006",15046,
        "2008",14943,
        "1999",7878,
        "1997",6959,
        "2000",5282,
        "1998",4734,
        "2009",4273,
        "1996",275,
        "1980",0,
        "1994",0,
        "1995",0,
        "2010",0]},
    "facet_dates":{},
    "facet_ranges":{}}}

http://192.168.1.151:8984/solr/jisc/select?q=*%3A*&rows=0&wt=json&indent=true&facet=true&facet.field=crawl_year

{
  "responseHeader":{
    "status":0,
    "QTime":3295,
    "params":{
      "facet":"true",
      "indent":"true",
      "q":"*:*",
      "facet.field":"crawl_year",
      "wt":"json",
      "rows":"0"}},
  "response":{"numFound":295991293,"start":0,"maxScore":1.0,"docs":[]
  },
  "facet_counts":{
    "facet_queries":{},
    "facet_fields":{
      "crawl_year":[
        "2007",43999842,
        "2004",43510947,
        "2008",42256620,
        "2006",38525483,
        "2003",35358287,
        "2005",34498771,
        "2002",22592924,
        "2009",11163001,
        "2000",10576943,
        "2001",9278413,
        "1999",2348534,
        "1997",1065680,
        "1998",743960,
        "1996",71500,
        "1980",148,
        "1994",131,
        "1995",99,
        "2010",10]},
    "facet_dates":{},
    "facet_ranges":{}}}

### Variations ###
To see if we can leverage the ssdeep hashes to indicate how much a page has changed over time, and spot large changes.

c.f. warc-discovery/analysis-tools/ssdiff.py

Example of note: http://web.archive.org/web/20131113083636/http://www.conservatives.com/robots.txt

    
### Snowflakes ###

Host/Domain Snowflake:
- Pick a random slash page, perhaps from a given year, and keep going until we
get a domain with an significant number of pages. e.g. >50.
"fq=url_type:"slashpage"&sort=random_xxx+desc" get domain/host.
"fq=domain:"selected"" until you get a decent count.
- Then, get the links_private_suffixes for the random first up-to 1000 pages.
- Then turn that into a connectivity graph file.
- Then plot that, with weights/size/colour e.g. by number of links, making most linked-to domain clear.
- Lots of additions could be made, like back links on the same graph as an outer layer.
- Links links_hosts and links_private_suffixes as a tree might work well.

NB Can look up number of facet values via:

https://cwiki.apache.org/confluence/display/solr/The+Stats+Component

(use this to make faceted search saner)

Perhaps a simple force-directed graph display, like [this one](http://bl.ocks.org/mbostock/1153292)?


### Domain Index ###

Domain index, i.e. all distinct slash pages ordered alphabetically.

URL/host summary page, including proxy-rendered archival shot.


### YOLO-15 ###

Use the Solr index to find pages from 15 years ago, and publish as a twitter feed (e.g. daily). Could be basic search with screenshots, or perhaps a bit more meaningful if we hook in a search for the test 'you only live once'.

* http://solr1.bl.uk:8080/solr/select?sort=harvest_date+asc&indent=on&version=2.2&q=harvest_date%3A%5BNOW-17YEAR+TO+NOW-17YEAR%2B1MONTH%5D&fq=&start=0&rows=10&fl=*%2Cscore&wt=&explainOther=&hl.fl=


### Old News ###

Pull tweets from BBC news, look for old news articles that 'match'.
domain:bbc.co.uk AND news AND -weather ("Jeremy Hunt")


Notes
-----



http://stackoverflow.com/questions/17270393/call-solr-asynchronous-from-play-framework/17315047#17315047

val itselfNodeFuture = Statix.doParams( Statix.SolrSelectWSReq, 
    List(
    "wt"     -> "json", 
    "q"      -> "*:*",
    "fq"     -> "node_type:collection",
    "fq"     -> "id:%d".format( nodeId),
    "indent" -> "true",
    "rows"   -> "1",
    "fl"     -> "id,parent_id,title",
    "fl"     -> "date_created,date_about,date_modified")
).get()

//Use the first Await after the last future
val itselfJson = Await.result(
    itselfNodeFuture, Duration("2 sec")).json

val mainRow = (itselfJson \ "response" \ "docs").as[ Seq[JsValue]]
val mainNodeParent = (mainRow(0) \ "parent_id").as[Long]
val mainNodeTitle = (mainRow(0) \ "title").as[String]


object Statix { //Noder must extend this
    def SolrSelectWSReq = WS.url("http://127.0.0.1:8080/solr-store/collection1/select/")
    def SolrUpdateWSReq = WS.url("http://127.0.0.1:8080/solr-store/collection1/update/json/")

    def doParams(request: WS.WSRequestHolder, params: List[(String, String)]) = {
        params.foldLeft( request){
            (wsReq, tuple) => wsReq.withQueryString( tuple)}}
}