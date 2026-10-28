package jp.co.sss.lms.ct.f02_faq;

import static jp.co.sss.lms.ct.util.WebDriverUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * 結合テスト よくある質問機能
 * ケース05
 * @author holy
 */
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("ケース05 キーワード検索 正常系")
public class Case05 {

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

	@Test
	@Order(2)
	@DisplayName("テスト02 初回ログイン済みの受講生ユーザーでログイン")
	void test02() {
		visibilityTimeout(By.id("loginId"), 10);
		webDriver.findElement(By.id("loginId")).clear();
		webDriver.findElement(By.id("loginId")).sendKeys("StudentAA01");
		webDriver.findElement(By.id("password")).clear();
		webDriver.findElement(By.id("password")).sendKeys("StudentAA01");
		webDriver.findElement(By.cssSelector("input[type='submit']")).click();

		visibilityTimeout(By.linkText("マイコース"), 10);
		assertTrue(webDriver.getTitle().contains("コース詳細"));
		assertEquals("コース詳細", webDriver.findElement(By.cssSelector("ol.breadcrumb li.active")).getText());
		WebElement heading = webDriver.findElement(By.tagName("h2"));
		assertTrue(heading.isDisplayed());
		assertTrue(heading.getText().contains("年"));
		assertTrue(heading.getText().contains("～"));

		getEvidence(new Object() {
		});
	}

	@Test
	@Order(3)
	@DisplayName("テスト03 上部メニューの「ヘルプ」リンクからヘルプ画面に遷移")
	void test03() {
		webDriver.manage().window().maximize();
		WebElement functionMenu = webDriver.findElements(By.cssSelector("a.dropdown-toggle")).stream()
				.filter(element -> element.getText().contains("機能")).findFirst().orElseThrow();
		new Actions(webDriver).moveToElement(functionMenu).click().perform();
		((JavascriptExecutor) webDriver).executeScript(
				"arguments[0].setAttribute('aria-expanded','true'); arguments[0].parentElement.classList.add('open');",
				functionMenu);
		By helpLink = By.cssSelector("ul.dropdown-menu a[href*='/help']");
		visibilityTimeout(helpLink, 10);
		((JavascriptExecutor) webDriver).executeScript("arguments[0].click();",
				webDriver.findElement(helpLink));
		new WebDriverWait(webDriver, Duration.ofSeconds(10))
				.until(ExpectedConditions.urlContains("/help"));

		visibilityTimeout(By.tagName("h2"), 10);
		assertTrue(webDriver.getTitle().contains("ヘルプ"));
		assertEquals("ヘルプ", webDriver.findElement(By.tagName("h2")).getText());
		assertTrue(webDriver.findElement(By.linkText("よくある質問")).isDisplayed());

		getEvidence(new Object() {
		});
	}

	@Test
	@Order(4)
	@DisplayName("テスト04 「よくある質問」リンクからよくある質問画面を別タブに開く")
	void test04() {
		String currentWindow = webDriver.getWindowHandle();
		WebElement faqLink = webDriver.findElement(By.cssSelector("a[target='_blank'][href*='/faq']"));
		((JavascriptExecutor) webDriver).executeScript("window.open(arguments[0].href, '_blank');", faqLink);
		new WebDriverWait(webDriver, Duration.ofSeconds(10))
				.until(ExpectedConditions.numberOfWindowsToBe(2));
		for (String handle : webDriver.getWindowHandles()) {
			if (!handle.equals(currentWindow)) {
				webDriver.switchTo().window(handle);
			}
		}

		visibilityTimeout(By.id("form"), 10);
		assertTrue(webDriver.getTitle().contains("よくある質問"));
		assertEquals("よくある質問", webDriver.findElement(By.tagName("h2")).getText());
		assertTrue(webDriver.findElement(By.id("form")).isDisplayed());
		boolean categorySearch = webDriver.findElements(By.tagName("legend")).stream()
				.anyMatch(legend -> legend.getText().contains("カテゴリ検索"));
		assertTrue(categorySearch);

		getEvidence(new Object() {
		});
	}

	@Test
	@Order(5)
	@DisplayName("テスト05 キーワード検索で該当キーワードを含む検索結果だけ表示")
	void test05() {
		webDriver.findElement(By.id("form")).clear();
		webDriver.findElement(By.id("form")).sendKeys("キャンセル");
		webDriver.findElement(By.cssSelector("input[type='submit'][value='検索']")).click();

		visibilityTimeout(By.cssSelector("[id^='question-h']"), 10);
		String resultText = webDriver.findElement(By.xpath("//table[.//th[contains(.,'検索結果')]]")).getText();
		assertTrue(resultText.contains("キャンセル"));
		assertFalse(resultText.contains("助成金"));
		assertFalse(resultText.contains("遠隔研修"));

		getEvidence(new Object() {
		});
	}

	@Test
	@Order(6)
	@DisplayName("テスト06 「クリア」ボタン押下で入力したキーワードを消去")
	void test06() {
		webDriver.findElement(By.cssSelector("input[type='button'][value='クリア']")).click();
		assertEquals("", webDriver.findElement(By.id("form")).getAttribute("value"));

		getEvidence(new Object() {
		});
	}

}
