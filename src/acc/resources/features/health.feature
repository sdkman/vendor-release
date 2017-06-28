Feature: Health Check endpoint

	Scenario: Check if the service is alive
		Given an alive OK entry in the application collection
		When a GET request on the /alive endpoint
		Then the status received is 200 OK
		And the message containing "OK" is received