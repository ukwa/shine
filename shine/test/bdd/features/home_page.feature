Feature: View Home Page
  As a user of Shine
  I want to be able to view the home landing page
  So that I can view my options

Scenario: Home Page
  Given I am on the Home Page
  When the home page loads
  Then I should see home page title "Welcome :: SHINE"
  And first menu option is "Search"
  And second menu option is "Trends"
