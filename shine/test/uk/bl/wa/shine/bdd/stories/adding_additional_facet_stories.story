Narrative:
In order to see more facets  
As a user  
I want to use a function to add more facets
  
Scenario: Add additional facet

Given a list of current facets  
When I add a new <facet>
Then the outcome should show <result> with new facet
  
Examples:  
|facet|result|  
|'domain'|['crawl_year', 'domain']| 