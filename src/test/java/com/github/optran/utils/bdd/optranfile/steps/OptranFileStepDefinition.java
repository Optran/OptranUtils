package com.github.optran.utils.bdd.optranfile.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import com.github.optran.utils.disk.OptranFile;
import com.github.optran.utils.disk.model.OptranFilePage;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class OptranFileStepDefinition {
	private static final String FILE_NAME = "E:\\testfile.optran";
	private OptranFile optranFile;
	private long lastReadPageId;
	private OptranFilePage lastReadPage;
	private boolean freePageResult;
	private String testString;
	private char finalChar;

	@Before
	public void before() {
		File file = new File(FILE_NAME);
		if (file.exists()) {
			assertTrue(file.delete());
			;
		}
	}

	@Given("The user creates a new OptranFile with page size as {int}")
	public void the_user_creates_a_new_optran_file_with_page_size_as(Integer pageSize) {
		optranFile = new OptranFile(FILE_NAME, pageSize, 1);
	}

	@When("^The user attempts to (?:read|fetch) a page with page id ([0-9]+)$")
	public void the_user_attempts_to_fetch_a_page_with_page_id(Integer pageId) {
		lastReadPageId = pageId;
		lastReadPage = optranFile.readPage(pageId.longValue());
	}

	@Then("The page must be retrieved successfully")
	public void the_page_must_be_retrieved_successfully() {
		assertNotNull(lastReadPage);
		assertEquals(lastReadPageId, lastReadPage.getPageId());
	}

	@Then("the length of the retrieved page must be {int}")
	public void the_length_of_the_retrieved_page_must_be(Integer pageLength) {
		assertEquals(lastReadPage.getLength(), pageLength.longValue());
	}

	@When("The user attempts to free a page with page id {int}")
	public void the_user_attempts_to_free_a_page_with_page_id(Integer pageId) {
		lastReadPageId = pageId;
		lastReadPage = optranFile.readPage(pageId.longValue());
		freePageResult = optranFile.free(lastReadPage);
	}

	@Then("The response from the call to free must be false as the metadata page cannot be freed")
	public void the_response_from_the_call_to_free_must_be_false_as_the_metadata_page_cannot_be_freed() {
		assertFalse(freePageResult);
	}

	@When("^The user writes ([A-Za-z0-9! ]+) to the metadata page$")
	public void the_user_writes_cat_to_the_metadata_page(String dataToWrite) {
		lastReadPageId = 0;
		lastReadPage = optranFile.readPage(0);
		lastReadPage.setLength(0);
		lastReadPage.write(dataToWrite.getBytes());
	}

	@When("The user saves the page")
	public void the_user_saves_the_page() {
		optranFile.writePage(lastReadPage);
		optranFile.close();
	}

	@Then("^when the page is retrieved a second time, it must contain ([A-Za-z0-9! ]+) with length ([0-9]+)$")
	public void when_the_page_is_retrieved_a_second_time_it_must_contain_cat_with_length(String dataWritten,
			Integer pageLength) {
		lastReadPage = optranFile.readPage(lastReadPageId);
		assertEquals(pageLength.longValue(), lastReadPage.getLength());
		lastReadPage.setHead(0);
		byte[] data = new byte[lastReadPage.getLength()];
		assertEquals(pageLength.intValue(), lastReadPage.read(data));
		assertEquals(dataWritten, new String(data));
	}

	@Then("The response from read must be null as the page has not yet been alocated.")
	public void the_response_from_read_must_be_null_as_the_page_has_not_yet_been_alocated() {
		assertNull(lastReadPage);
	}

	@When("The user attempts to allocate a new page")
	public void the_user_attempts_to_allocate_a_new_page() {
		lastReadPage = optranFile.malloc();
		lastReadPageId = lastReadPage.getPageId();
	}

	@Then("The user should get a valid page from the OptranFile")
	public void the_user_should_get_a_valid_page_from_the_optran_file() {
		assertNotNull(lastReadPage);
	}

	@When("The user creates a test string with {word} having occurence {int} and final character {word}")
	public void the_user_creates_a_test_string_with_testChar_having_occurence_and_final_character_finalChar(String testChar, int occurence, String finalChar) {
		assertEquals(1, testChar.length());
		assertEquals(1, finalChar.length());
		assertTrue(occurence>0);
		StringBuilder sb = new StringBuilder(occurence+1);
		for (int i = 0; i < occurence; i++) {
			sb.append(testChar);
		}
		sb.append(finalChar);
		this.finalChar = finalChar.charAt(0);
		testString = sb.toString();
	}

	@When("saves the test string to a {word} page")
	public void saves_the_test_string_to_a_pageType_page(String pageType) {
		if("Metadata".equalsIgnoreCase(pageType)) {
			lastReadPage = optranFile.readPage(0);
			lastReadPageId = lastReadPage.getPageId();
		} else if("Standard".equalsIgnoreCase(pageType)) {
			lastReadPage = optranFile.malloc();
			lastReadPageId = lastReadPage.getPageId();
		} else {
			throw new io.cucumber.java.PendingException();
		}
		lastReadPage.setHead(0);
		lastReadPage.write(testString.getBytes());
		optranFile.writePage(lastReadPage);
		optranFile.close();
	}

	@When("the user reads the test data back from the page")
	public void the_user_reads_the_test_data_back_from_the_page() {
		lastReadPage = optranFile.readPage(lastReadPageId);
		byte[]data = new byte[lastReadPage.getLength()];
		assertEquals(data.length, lastReadPage.read(data));
		testString = new String(data);
	}

	@Then("The user finds that the length of the data retrieved is {int}")
	public void the_user_finds_that_the_length_of_the_data_retrieved_is(Integer length) {
		assertEquals(testString.length(), length.intValue());
	}

	@Then("the size of the page is {int}")
	public void the_size_of_the_page_is(Integer pageSize) {
		assertEquals(lastReadPage.size(), pageSize.intValue());
	}

	@Then("the capacity of the page is {int}")
	public void the_capacity_of_the_page_is(Integer capicity) {
		assertEquals(lastReadPage.capicity(), capicity.intValue());
	}

	@Then("the final characters presense is {word}")
	public void the_final_characters_presense_is(String isFinalCharPresent) {
		if("Y".equalsIgnoreCase(isFinalCharPresent)) {
			assertEquals(finalChar, testString.charAt(testString.length()-1));
		} else if("N".equalsIgnoreCase(isFinalCharPresent)) {
			assertNotEquals(finalChar, testString.charAt(testString.length()-1));
		} else {
			throw new io.cucumber.java.PendingException();
		}
	}

	@After
	public void cleanup() {
		if (null != optranFile) {
			optranFile.close();
			assertTrue(optranFile.delete());
		}
	}
}
