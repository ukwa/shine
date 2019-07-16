
// The search fields and filterable facets you want
const fields = [
        {label: "All text fields", field: "text", type: "text"}, // Using field:text rather than field:* as this matches our setup.
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

maxFacets = 100;

