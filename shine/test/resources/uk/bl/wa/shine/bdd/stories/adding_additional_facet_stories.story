Narrative:
In order to see more facets  
As a user  
I want to use a service to add more facets
  
Scenario: Add additional facet

Given a facet service  
When I add a new <facet>
Then the outcome should show <result> with new facet
  
Examples:  
|facet|result|  
|'domain'|['crawl_year', 'domain']| 