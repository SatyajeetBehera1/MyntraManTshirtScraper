# MyntraManTshirtScraper

## Overview
MyntraManTshirtScraper is a Playwright-based web scraping project designed to extract discounted T-shirt data for men from the Myntra website. The extracted data includes original prices, discounted prices, discounts in percentage, and links to the products. This project also sorts the extracted data based on the highest discount percentage using the Bubble Sort algorithm.

> **Assignment Provided By**: GoComet

## Features
- Automated navigation and interaction with the Myntra website.
- Scrapes data for discounted T-shirts, including details like original price, discounted price, and discount percentage.
- Handles pagination to scrape data from multiple pages.
- Filters products by type and brand.
- Sorts the T-shirts by the highest discount percentage using Bubble Sort.
- Displays the sorted data in the console.

## Prerequisites
- Java Development Kit (JDK) installed (version 8 or above).
- Maven installed for dependency management.
- Basic understanding of Java, Playwright, and Cucumber BDD.

## Dependencies
- **Playwright**: For browser automation.
- **Cucumber**: For behavior-driven development (BDD).

Dependencies are managed via Maven and specified in the `pom.xml` file.

## Setup Instructions

### 1. Clone the Repository
```bash
$ git clone https://github.com/your-repo/MyntraManTshirtScraper.git
$ cd MyntraManTshirtScraper
```

### 2. Install Dependencies
Ensure Maven is installed and run:
```bash
$ mvn install
```

### 3. Run the Tests
Execute the following command to run the project:
```bash
$ mvn test
```

### 4. View Results
After execution, the console will display the sorted T-shirt data with details including:
- Discounted Price
- Original Price
- Discount Percentage
- Product Link

## Code Workflow

### Step Definitions

#### Navigation to Myntra
```java
@Given("I navigate to {string}")
```
Initializes Playwright, launches a browser, and navigates to the specified URL.

#### Category Selection
```java
@When("I select the {string} category")
```
Hovers over the desired category (e.g., "Men").

#### Filter by Type
```java
@And("I filter by type {string}")
```
Filters products by a specified type (e.g., "T-Shirts").

#### Filter by Brand
```java
@And("I filter by brand {string}")
```
Filters products by the specified brand using the search and checkbox feature.

#### Extract Data
```java
@Then("I extract the discounted T-shirts data")
```
Scrapes the data from the current page, iterates through pagination, and collects data until all pages are processed.

#### Sort Data
```java
@Then("I sort the tshirts by highest discount")
```
Sorts the extracted T-shirts by discount percentage using the Bubble Sort algorithm.

#### Display Data
```java
@Then("I print the sorted data to the console")
```
Prints the sorted T-shirt data in a user-friendly format in the console.

## Error Handling
The project includes robust error handling mechanisms for changes in website structure or failures during scraping. Errors are caught and logged with meaningful messages for debugging purposes.
