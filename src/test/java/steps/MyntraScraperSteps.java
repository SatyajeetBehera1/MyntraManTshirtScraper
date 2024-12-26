package steps;

/* 
Importing the playwright library which provides tools for automating web browsers 
and interacting with web pages in a programmatic way
*/
import com.microsoft.playwright.*;

/*
Importing Cucumber's @Given, @When, @And, and @Then annotations
These annotations are used to define step definitions in Cucumber's Behavior-Driven Development(BDD) framework
Each annotation corresponds to a step in the Gherkin feature file
*/
import io.cucumber.java.en.*;

import java.util.*;

public class MyntraScraperSteps {

    public Browser browser;
    public Page page;
    public String brand;
    List<Map<String, String>> tshirts = new ArrayList<>();

    @Given("I navigate to {string}")
    public void NavigateToUrl(String url) {
        try {
            // Initializing Playwright and opening a browser session
            Playwright playwright = Playwright.create();
            BrowserType browserType = playwright.chromium();
            browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(false));
            page = browser.newPage();
            page.navigate(url);
        } catch (Exception e) {
            // Catch any error during navigation and print it to the console
            System.err.println("Error navigating to URL: " + e.getMessage());
        }
    }

    @When("I select the {string} category")
    public void SelectCategory(String category) {
        try {
            // Hovering over the specified category text (e.g., "MENS")
            page.hover("text=" + category);
        } catch (Exception e) {
            // Catch any error while selecting the category and print it
            System.err.println("Error selecting category: " + e.getMessage());
        }
    }

    @And("I filter by type {string}")
    public void FilterByType(String Type) {
        try {
            // Clicking the link for the given product type (e.g., "t-shirts")
            page.click("a[href='/men-" + Type.toLowerCase() + "']");
        } catch (Exception e) {
            // Catch any error while applying type filter and print it
            System.err.println("Error filtering by type: " + e.getMessage());
        }
    }

    @And("I filter by brand {string}")
    public void FilterByBrand(String shirt_brand) {
        brand = shirt_brand;
        try {
            // Clicking the search icon for brand filter
            page.click(".filter-search-iconSearch");
            // Typing the brand name into the search box
            page.fill(".filter-search-inputBox", brand);
            // Simulating pressing the "Enter" key to apply the filter
            page.press(".filter-search-inputBox", "Enter");
            // Clicking the checkbox for the brand (handling pseudo-element issue)
            page.locator("input[type='checkbox'][value='" + brand + "']").dispatchEvent("click");
        } catch (Exception e) {
            // Catch any error while applying brand filter and print it
            System.err.println("Error filtering by brand: " + e.getMessage());
        }
    }

    @Then("I extract the discounted T-shirts data")
    public void ExtractDiscountedTshirts() {
        try {
            // Loop through pages until the "Next" button is disabled
            while (true) {
                // Locate all product elements on the current page
                Locator products = page.locator(".product-base");
                int count = products.count();

                // Loop through each product to extract data
                for (int i = 0; i < count; i++) {
                    Locator discountedProducts = products.nth(i).locator(".product-strike");

                    if (discountedProducts.isVisible()) {
                        String discountedPrice = products.nth(i).locator(".product-discountedPrice").textContent().trim();
                        String originalPrice = discountedProducts.textContent().trim();
                        String discount = products.nth(i).locator(".product-discountPercentage").textContent().trim();
                        String link = "https://www.myntra.com/" + products.nth(i).locator("a").getAttribute("href");

                        if (discount != null && discount.contains("%")) {
                            Map<String, String> tshirt = new HashMap<>();
                            tshirt.put("originalPrice", originalPrice);
                            tshirt.put("discountedPrice", discountedPrice);
                            tshirt.put("discount", discount);
                            tshirt.put("link", link);

                            tshirts.add(tshirt);
                        }
                    }
                }

                // Check if the "Next" button is disabled (end of pagination)
                Locator nextButton = page.locator(".pagination-next");
                if (nextButton.getAttribute("class").contains("pagination-disabled")) {
                    break; // Exit loop if no more pages
                }

                // Click the "Next" button to navigate to the next page
                nextButton.click();

                // Wait for the next page to load fully before scraping
                page.waitForLoadState();
            }
        } catch (Exception e) {
            System.err.println("Error extracting discounted T-shirts: " + e.getMessage());
        }
    }

    @Then("I sort the tshirts by highest discount")
    public void SortDiscountedTshirts() {
        int n = tshirts.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                int discountA = extractNumericValue(tshirts.get(j).get("discount"));
                int discountB = extractNumericValue(tshirts.get(j + 1).get("discount"));

                if (discountA < discountB) {
                    Map<String, String> temp = tshirts.get(j);
                    tshirts.set(j, tshirts.get(j + 1));
                    tshirts.set(j + 1, temp);
                }
            }
        }
    }

    private int extractNumericValue(String discountString) {
        try { 
            return Integer.parseInt(discountString.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            System.err.println("Error extracting numeric value from discount: " + e.getMessage());
            return 0; // Default value if extraction fails
        }
    }

    @Then("I print the sorted data to the console")
    public void DisplaySortedData() {
        try {
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
        } catch (Exception e) {
            System.err.println("Error displaying sorted data: " + e.getMessage());
        }
    }
}
