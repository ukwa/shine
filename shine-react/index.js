// index.js
import React from "react";
import ReactDOM from "react-dom";
import cx from "classnames";
import {
	SolrFacetedSearch,
	SolrClient,
        defaultComponentPack
} from "solr-faceted-search-react";

// Custom class for the result component
class MyResult extends React.Component {

	renderValue(field, doc) {
		const value = [].concat(doc[field] || null).filter((v) => v !== null);

                if( field == 'content') return "-";

		return value.join(", ");
	}

	render() {
		const { bootstrapCss, doc, fields } = this.props;

		return (
			<li className={cx({"list-group-item": bootstrapCss})} onClick={() => this.props.onSelect(doc)}>
				<ul>
                                        {Object.keys(this.props.doc).map( key => 
                			        <li><label>{key}</label> {this.renderValue(key, doc)}</li>
					)}
				</ul>
			</li>
		);
	}
}

// Create a custom component pack from the default component pack
const myComponentPack = {
	...defaultComponentPack,
	results: {
		...defaultComponentPack.results,
		result: MyResult
	}
}

document.addEventListener("DOMContentLoaded", () => {
	// The client class
	new SolrClient({
		// The solr index url to be queried by the client
		url: "/solr/discovery/select",
		searchFields: fields,
		sortFields: sortFields,
                facetLimit: maxFacets,
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
					customComponents={myComponentPack}
					onSelectDoc={(doc) => console.log(doc)}
				/>,
				document.getElementById("app")
			)
	}).initialize(); // this will send an initial search, fetching all results from solr
});
