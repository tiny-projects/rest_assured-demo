package demo.rest.assured;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.Method;

public class IMDbSearchTest {
	@BeforeTest
	public void init() {
		RestAssured.baseURI = "https://v2.sg.media-imdb.com/suggestion";
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
	}

	@SuppressWarnings("unchecked")
	@Test(dataProvider = "readDataForQuery")
	public void testSearchSuggestion(String query, String queryString) {
		given().log().uri()
		.request(Method.GET, queryString)
		.then().statusCode(200)
		.and().assertThat().body("d[0].l",containsStringIgnoringCase(query))
		.and().extract()
		.jsonPath().getList("d")
		.forEach(item->{
					String suggestions = ((LinkedHashMap<String, String>) item).get("l");
					String timeStamp = new SimpleDateFormat("[HH:mm:ss SSS]").format(Calendar.getInstance().getTime());
					String errorMessage = "\n" + timeStamp + ": expected suggestion [" + suggestions
							+ "] to contain the typed query : '" + query + "'";
					assertTrue(StringUtils.containsIgnoreCase(suggestions, query), errorMessage);
				});
	}

	
	@DataProvider(name = "readDataForQuery", parallel = true)
	public Object[][] readDataForQuery() throws Exception {
		String csv = "src/test/resources/data.csv";
		return readDataFromCSV(csv);
	}

	private Object[][] readDataFromCSV(String csvFileName) throws Exception {
		try {
			Scanner scanner = new Scanner(new File(csvFileName));
			scanner.nextLine(); // skip the header columns
			List<String[]> params = new ArrayList<String[]>();
			while (scanner.hasNextLine()) {
				String[] param = scanner.nextLine().split(",");
				params.add(new String[] { param[0], param[1] });
			}
			String[][] csv = new String[params.size()][];
			return params.toArray(csv);
		} catch (Exception e) {
			System.err.println("Error reading test data form source file " + e.getMessage());
			throw e;
		}
	}
}
