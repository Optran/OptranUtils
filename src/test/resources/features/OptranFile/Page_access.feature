Feature: Enable users of the OptranFile to have adhoc access to fixed sized pages based on page id.
  The OptranFile will serve as an alternative to the RandomAccessFile in that it allows the developer 
  to interact with any OptranFile as if it is a numbered sequence of pages starting at 0.

	Background:
	  Given The user creates a new OptranFile with page size as 512

  Scenario: Fetching the metadata page as soon as the file is created.
    When The user attempts to fetch a page with page id 0
    Then The page must be retrieved successfully
     And the length of the retrieved page must be 0
     
	Scenario: Attempting to free the metadata page.
    When The user attempts to free a page with page id 0
    Then The response from the call to free must be false as the metadata page cannot be freed

	Scenario Outline: Writing data to the metadata page.
    When The user writes <Data> to the metadata page
     And The user saves the page
    Then when the page is retrieved a second time, it must contain <Data> with length <Length>
  
  Examples: 
	  |Data       |Length|
	  |Cat        |3     |
	  |Optran     |6     |
	  |XYZZYSPOON!|11    |

  Scenario: Attempting to access a page that has not yet been alocated.
    When The user attempts to read a page with page id 3
    Then The response from read must be null as the page has not yet been alocated.
    
  Scenario: Allocating a new page.
    When The user attempts to allocate a new page
    Then The user should get a valid page from the OptranFile

  Scenario Outline: Saving data to pages.
    When The user creates a test string with <Test char> having occurence <Occurence> and final character <Final Char>
     And saves the test string to a <Page type> page
     And the user reads the test data back from the page 
    Then The user finds that the length of the data retrieved is <Length>
     And the size of the page is <Size>
     And the capacity of the page is <Capacity>
     And the final characters presense is <isFinalCharPresent>

  Examples: 
		|Page type|Test char|Occurence|Final Char|isFinalCharPresent|Size|Capacity|Length|
	  |Metadata |a        |397      |b         |Y                 |412 |399     |398   |
	  |Metadata |a        |398      |b         |Y                 |412 |399     |399   |
	  |Metadata |a        |399      |b         |N                 |412 |399     |399   |
	  |Metadata |a        |400      |b         |N                 |412 |399     |399   |
	  |Standard |a        |497      |b         |Y                 |512 |499     |498   |
	  |Standard |a        |498      |b         |Y                 |512 |499     |499   |
	  |Standard |a        |499      |b         |N                 |512 |499     |499   |
	  |Standard |a        |500      |b         |N                 |512 |499     |499   |
