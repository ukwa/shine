// index.js
import React from "react";
import ReactDOM from "react-dom";
import {
	SolrFacetedSearch,
	SolrClient
} from "solr-faceted-search-react";

/* TODO?
 *  - Actually understand React etc. properly?
 *  - use react-router and https://www.npmjs.com/package/query-string etc. to set up separate pages and record them in the URL properly.
 *  - use redux to manage state cleanly?
 *  - improve the UI, to somewhere between Shine, Blacklight and UKWA-UI?
 *
 */


// The search fields and filterable facets you want
const fields = [
	{label: "All text fields", field: "*", type: "text"},
	{label: "Title", field: "title", type: "text"},
	{label: "Host", field: "host", type: "list-facet"},
	{label: "Year of capture", field: "crawl_year", type: "range-facet"},
	{label: "Content type", field: "content_type_norm", type: "list-facet"}
];

// The sortable fields you want
const sortFields = [
	{label: "Title", field: "title"},
	{label: "Date of crawl", field: "crawl_date"},
	{label: "Relevance", field: "score"}
];

document.addEventListener("DOMContentLoaded", () => {
	// The client class
	new SolrClient({
		// The solr index url to be queried by the client
		url: "/solr/discovery/select",
		searchFields: fields,
		sortFields: sortFields,
                facetLimit: 10,
                facetSort: "count",

		// The change handler passes the current query- and result state for render
		// as well as the default handlers for interaction with the search component
		onChange: (state, handlers) =>
			// Render the faceted search component
			ReactDOM.render(
				<SolrFacetedSearch 
					{...state}
					{...handlers}
					bootstrapCss={true}
					onSelectDoc={(doc) => console.log(doc)}
				/>,
				document.getElementById("app")
			)
	}).initialize(); // this will send an initial search, fetching all results from solr
});
