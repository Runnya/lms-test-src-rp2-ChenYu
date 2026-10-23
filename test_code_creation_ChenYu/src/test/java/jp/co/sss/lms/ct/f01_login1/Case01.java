package jp.co.sss.lms.ct.f01_login1;

import static jp.co.sss.lms.ct.util.WebDriverUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;

/**
 * 結合テスト ログイン機能①
 * ケース01
 * @author holy
 */
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("ケース01 ログイン画面への遷移")
public class Case01 {

	/** 前処理 */
	@BeforeAll
	static void before() {
		createDriver();
	}

	/** 後処理 */
	@AfterAll
	static void after() {
		closeDriver();
	}

	@Test
	@Order(1)
	@DisplayName("テスト01 トップページURLでアクセス")
	void test01() {
		goTo("http://localhost:8080/lms");
		visibilityTimeout(By.id("loginId"), 10);

		assertTrue(webDriver.getTitle().contains("ログイン"));
		assertEquals("ログイン", webDriver.findElement(By.tagName("h2")).getText());
		assertTrue(webDriver.findElement(By.id("loginId")).isDisplayed());
		assertTrue(webDriver.findElement(By.id("password")).isDisplayed());
		assertEquals("ログイン", webDriver.findElement(By.cssSelector("input[type='submit']")).getAttribute("value"));
		assertTrue(webDriver.findElement(By.linkText("パスワードを忘れた方はこちら")).isDisplayed());
		assertTrue(webDriver.findElement(By.linkText("よくあるご質問")).isDisplayed());

		getEvidence(new Object() {
		});
	}

}
