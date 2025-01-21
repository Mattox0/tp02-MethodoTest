Feature: store and get data
  Scenario: the user creates books and retrieves them
    When the user creates the book "1984" written by "George Orwell"
    And the user creates the book "Animal Farm" written by "George Orwell"
    And the user get all books
    Then the list should contains the following books in the same order
      | name        | author        | reserved |
      | 1984        | George Orwell | false    |
      | Animal Farm | George Orwell | false    |

  Scenario: the user gets a specific book
    When the user creates the book "The Catcher in the Rye" written by "J.D. Salinger"
    And the user gets the book with id 3
    Then the book should have the following properties
      | name                  | author        | reserved |
      | The Catcher in the Rye | J.D. Salinger | false    |

  Scenario: the user reserves a book
    When the user creates the book "Brave New World" written by "Aldous Huxley"
    And the user reserves the book with id 4
    And the user gets the book with id 4
    Then the book should have the following properties
      | name           | author        | reserved |
      | Brave New World | Aldous Huxley | true     |

  Scenario: the user tries to reserve an already reserved book
    When the user creates the book "Fahrenheit 451" written by "Ray Bradbury"
    And the user reserves the book with id 5
    And the user reserves the book with id 5
    Then the reservation should fail with conflict status

  Scenario: the user tries to get a non-existing book
    When the user gets the book with id 999
    Then the book should not be found