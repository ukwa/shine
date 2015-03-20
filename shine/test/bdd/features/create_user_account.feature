Feature: Create a user account
  As an admin user
  I want to be able a user account
  So that the user can use the system

Scenario Outline: Creating a new user account
  Given I have an email address "<input>"
  When I choose to save
  Then a valid account should be created with "<output>"
  Examples:
      | input 				| output  	| 
      | kinman.li@bl.uk  	| success	| 
      
Scenario Outline: Creating a new user account with existing email
  Given I have an email address "<input>"
  When I choose to save
  Then it should alert with a "<output>"
  Examples:
      | input 				| output  	| 
      | kinman.li@bl.uk  	| error		| 
      