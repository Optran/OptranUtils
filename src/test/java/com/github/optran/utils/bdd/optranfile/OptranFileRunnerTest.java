package com.github.optran.utils.bdd.optranfile;

import org.junit.runner.RunWith;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
		features = "classpath:features/OptranFile", 
				glue = "com.github.optran.utils.bdd.optranfile.steps", 
				plugin = { 
							"pretty"
							,"html:target/test-report.html"
							,"json:target/test-report.json"
						}, 
		monochrome = true, 
		publish = false
		)
public class OptranFileRunnerTest {
}
