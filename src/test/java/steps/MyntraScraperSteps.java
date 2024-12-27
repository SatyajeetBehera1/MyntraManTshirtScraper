package steps;

/* 
Importing the Playwright library, which provides tools for automating web browsers 
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

    // Browser and page objects for interacting with the browser.
    public Browser browser;
    public Page page;

    // Store the brand being filtered and a list of T-shirts data.
    public String brand;
    List<Map<String, String>> tshirts = new ArrayList<>();

    /*
    Navigate to the provided URL using the Playwright library.
    A browser instance is launched, and the page navigates to the specified URL.
    */
    @Given("I navigate to {string}")
    public void openWebsite(String url) {
        try {
            Playwright playwright = Playwright.create();
            BrowserType browserType = playwright.chromium();
            browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(false));
            page = browser.newPage();
            page.navigate(url);
        } catch (Exception e) {
            System.err.println("Error navigating to URL: " + e.getMessage());
        }
    }

    /*
    Hover over the specified category on the website to reveal subcategories.
    Useful for accessing deeper levels of navigation.
    */
    @When("I select the {string} category")
    public void hoverOverCategory(String category) {
        try {
            page.hover("text=" + category);
        } catch (Exception e) {
            System.err.println("Error selecting category: " + e.getMessage());
        }
    }

    /*
    Filter items by the specified product type, such as T-shirts.
    Uses dynamic selectors to handle different types of products.
    */
    @And("I filter by type {string}")
    public void clickProductType(String type) {
        try {
            page.click("a[href='/men-" + type.toLowerCase() + "']");
        } catch (Exception e) {
            System.err.println("Error filtering by type: " + e.getMessage());
        }
    }

    /*
    Apply a filter for the specified brand using the website's search functionality.
    Ensures the brand filter is dynamically applied and interacts with the UI.
    */
    @And("I filter by brand {string}")
    public void applyBrandFilter(String shirtBrand) {
        brand = shirtBrand;
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
    Extract information about discounted T-shirts by navigating through all pages.
    Handles dynamic pagination and ensures all products are collected.
    */
    @Then("I extract the discounted T-shirts data")
    public void collectDiscountedTshirts() {
        try {
            while (true) {
                scrapePageProducts();

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
    Scrape product information, such as original price, discounted price, discount percentage, and link,
    from all visible products on the current page.
    */
    private void scrapePageProducts() {
        try {
            page.waitForSelector(".product-base");
            Locator products = page.locator(".product-base");
            int count = products.count();

            for (int i = 0; i < count; i++) {
                try {
                    Locator discountedProducts = products.nth(i).locator(".product-strike");

                    if (discountedProducts.isVisible()) {
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
                } catch (Exception e) {
                    System.err.println("Error extracting product at index " + i + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting products from page: " + e.getMessage());
        }
    }

    /*
    Sort the collected T-shirts by their discount percentage in descending order.
    Bubble sort is used for simplicity, as the dataset is expected to be small.
    */
    @Then("I sort the tshirts by highest discount")
    public void sortTshirtsByDiscount() {
        int n = tshirts.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                int discountA = parseDiscountValue(tshirts.get(j).get("discount"));
                int discountB = parseDiscountValue(tshirts.get(j + 1).get("discount"));

                if (discountA < discountB) {
                    Map<String, String> temp = tshirts.get(j);
                    tshirts.set(j, tshirts.get(j + 1));
                    tshirts.set(j + 1, temp);
                }
            }
        }
    }

    /*
    Convert a discount percentage string to an integer.
    Handles potential errors in parsing numeric values from text.
    */
    private int parseDiscountValue(String discountText) {
        try {
            return Integer.parseInt(discountText.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            System.err.println("Error extracting numeric value from discount: " + e.getMessage());
            return 0;
        }
    }

    /*
    Print the sorted T-shirts data, including discount details and links, to the console.
    Ensures the browser is closed properly after data display.
    */
    @Then("I print the sorted data to the console")
    public void displaySortedTshirts() {
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
