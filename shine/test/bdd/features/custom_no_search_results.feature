Feature: Custom number of search results
  As a user
  I want to be able to specify a number of results per page
  So that I can view the results

Scenario: Entering a number to display search results
  Given I want to display 10 per page
  When I choose to search
  Then I should see the results per page as 10

  Scenario Outline: Entering a number to display search results
  	Given I want to display "<input>" per page
 	When I choose to search
  	Then I should see the results per page as  "<output>"
    Examples:
      | input 	| output  	| 
      | 10   	| 10		| 
      | 20   	| 20		| 
      