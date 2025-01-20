Feature: store and get data
  Scenario: the user creates books and retrieves them
    When the user creates the book "Les Misérables" written by "Victor Hugo"
    And the user creates the book "Notre-Dame de Paris" written by "Victor Hugo"
    And the user get all books
    Then the list should contains the following books in the same order
      | name                | author      | reserved |
      | Les Misérables      | Victor Hugo | false    |
      | Notre-Dame de Paris | Victor Hugo | false    |

  Scenario: the user gets a specific book
    When the user creates the book "Les Misérables" written by "Victor Hugo"
    And the user gets the book with id 1
    Then the book should have the following properties
      | name           | author      | reserved |
      | Les Misérables | Victor Hugo | false    |

  Scenario: the user reserves a book
    When the user creates the book "Les Misérables" written by "Victor Hugo"
    And the user reserves the book with id 1
    And the user gets the book with id 1
    Then the book should have the following properties
      | name           | author      | reserved |
      | Les Misérables | Victor Hugo | true     |

  Scenario: the user tries to reserve an already reserved book
    When the user creates the book "Les Misérables" written by "Victor Hugo"
    And the user reserves the book with id 1
    And the user reserves the book with id 1
    Then the reservation should fail with conflict status

  Scenario: the user tries to get a non-existing book
    When the user gets the book with id 999
    Then the book should not be found