package steps;

/* 
Importing the playwright library which provides tools for automating web browsers 
and interacting with web pages in a programmatic way.
*/
import com.microsoft.playwright.*;

/*
Importing Cucumber's @Given, @When, @And, and @Then annotations.
These annotations are used to define step definitions in Cucumber's Behavior-Driven Development (BDD) framework.
Each annotation corresponds to a step in the Gherkin feature file.
*/
import io.cucumber.java.en.*;

import java.util.*;

public class MyntraScraperSteps {

    public Browser browser;
    public Page page;
    public String brand;
    List<Map<String, String>> tshirts = new ArrayList<>();

    /*
    Navigate to the provided URL using the Playwright library.
    Added try-catch to handle navigation errors.
    */
    @Given("I navigate to {string}")
    public void NavigateToUrl(String url) {
        try{
            Playwright playwright = Playwright.create();
            BrowserType browserType = playwright.chromium();
            browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(false));
            page = browser.newPage();
            page.navigate(url);
        }catch (Exception e) {
            System.err.println("Error navigating to URL: " + e.getMessage());
        }
    }

    /*
    Hover over the specified category to reveal subcategories.
    Added try-catch to handle interaction errors.
    */
    @When("I select the {string} category")
    public void SelectCategory(String category) {
        try {
            page.hover("text=" + category);
        } catch (Exception e) {
            System.err.println("Error selecting category: " + e.getMessage());
        }
    }

    /*
    Filter items by product type (e.g., "T-shirts").
    Enhanced selector to dynamically handle different categories.
    */
    @And("I filter by type {string}")
    public void FilterByType(String Type) {
        try {
            page.click("a[href='/men-" + Type.toLowerCase() + "']");
        } catch (Exception e) {
            System.err.println("Error filtering by type: " + e.getMessage());
        }
    }

    /*
    Apply a filter for the specified brand using the search input.
    Enhanced to include event dispatching for consistent interaction.
    */
    @And("I filter by brand {string}")
    public void FilterByBrand(String shirt_brand) {
        brand = shirt_brand;
        try {
            page.click(".filter-search-iconSearch");
            page.fill(".filter-search-inputBox", brand);
            page.press(".filter-search-inputBox", "Enter");
            page.locator("input[type='checkbox'][value='" + brand + "']").dispatchEvent("click");
        } catch (Exception e) {
            System.err.println("Error filtering by brand: " + e.getMessage());
        }
    }

    /*
    Extract discounted T-shirts data, navigating through all pagination.
    Added dynamic pagination handling and error handling for missing elements.
    */
    @Then("I extract the discounted T-shirts data")
    public void ExtractDiscountedTshirts() {
        try {
            while (true) {
                extractProductsFromPage();

                Locator nextButton = page.locator(".pagination-next");
                if (nextButton.count() == 0 || nextButton.getAttribute("class").contains("pagination-disabled")) {
                    break;
                }

                nextButton.click();
                page.waitForLoadState();
            }
        } catch (Exception e) {
            System.err.println("Error extracting discounted T-shirts: " + e.getMessage());
        }
    }

    /*
    Extract data for all products on the current page.
    Enhanced to handle cases where elements might not be visible.
    */
    private void extractProductsFromPage() {
        try {
            page.waitForSelector(".product-base");
            Locator products = page.locator(".product-base");
            int count = products.count();

            for (int i = 0; i < count; i++) {
                try {
                    // Adding retry mechanism for handling intermittent issues
                    int retries = 3;
                    while (retries > 0) {
                        try {
                            Locator discountedProducts = products.nth(i).locator(".product-strike");

                            // Check if the product has a discount element and if it is visible
                            if (discountedProducts.count() > 0 && discountedProducts.isVisible()) {
                                String discountedPrice = products.nth(i).locator(".product-discountedPrice").textContent().trim();
                                String originalPrice = discountedProducts.textContent().trim();
                                String discount = products.nth(i).locator(".product-discountPercentage").textContent().trim();
                                String link = "https://www.myntra.com/" + products.nth(i).locator("a").getAttribute("href");

                                Map<String, String> tshirt = new HashMap<>();
                                tshirt.put("originalPrice", originalPrice);
                                tshirt.put("discountedPrice", discountedPrice);
                                tshirt.put("discount", discount);
                                tshirt.put("link", link);

                                tshirts.add(tshirt);
                            }
                            break; // Exit retry loop if successful
                        } catch (Exception e) {
                            retries--;
                            if (retries == 0) {
                                System.err.println("Failed to extract product at index " + i + " after retries");
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error extracting product at index " + i + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting products from page: " + e.getMessage());
        }
    }

    /*
    Sort the T-shirts by discount percentage using bubble sort.
    This ensures a stable and simple sorting algorithm is used.
    */
    @Then("I sort the tshirts by highest discount")
    public void SortDiscountedTshirts() {
        int n = tshirts.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                int discount1 = extractNumericValue(tshirts.get(j).get("discount"));
                int discount2 = extractNumericValue(tshirts.get(j + 1).get("discount"));

                if (discount1 < discount2) {
                    Map<String, String> temp = tshirts.get(j);
                    tshirts.set(j, tshirts.get(j + 1));
                    tshirts.set(j + 1, temp);
                }
            }
        }
    }

    /*
    Extract numeric values from discount strings.
    Handles cases where input might not be purely numeric.
    */
    private int extractNumericValue(String discountString) {
        try {
            return Integer.parseInt(discountString.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            System.err.println("Error extracting numeric value from discount: " + e.getMessage());
            return 0;
        }
    }

    /*
    Print the sorted T-shirt data to the console.
    Handles errors during display and ensures browser closure.
    */
    @Then("I print the sorted data to the console")
    public void DisplaySortedData() {
        if(tshirts.isEmpty()){
            System.out.println("No T-shirt found on "+brand+" brand");
        }else{
            System.out.println("DISCOUNTS FOR BRAND: " + brand);
            System.out.println("*****************************");
            tshirts.forEach(tshirt -> {
                System.out.println("Discounted Price: " + tshirt.get("discountedPrice"));
                System.out.println("Original Price: " + tshirt.get("originalPrice"));
                System.out.println("Discount: " + tshirt.get("discount"));
                System.out.println("Link: " + tshirt.get("link"));
                System.out.println("*****************************");
            });
            System.out.println("Total number of T-shirts: " + tshirts.size());
            browser.close();
        }
        
    }
}
