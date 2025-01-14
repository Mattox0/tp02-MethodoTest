

Feature: store and get data
    Scenario: the user creates two entries and retrieves both
        When the user creates the book "Les Misérables" written by "Victor Hugo"
        And the user creates the book
        And the user get all book
        Then the list should contains the following data
            | data |
            | Toto |
            | Tata |