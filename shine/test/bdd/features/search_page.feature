Feature: View Search Page
  As a user of Shine
  I want to be able to view the basic search page
  So that I can view my options

Scenario: Search Page
  Given I am on the Main Page
  When I click the "Search" menu
  Then I should see search page title "Search"
  And first tab option is "Search"
  And second tab option is "Advanced Search"
  And mode button of "Sample Mode"