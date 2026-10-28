package jp.co.sss.lms.ct.f03_report;

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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

/**
 * 結合テスト レポート機能
 * ケース07
 * @author holy
 */
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("ケース07 受講生 レポート新規登録(日報) 正常系")
public class Case07 {

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
	@DisplayName("テスト03 未提出の研修日の「詳細」ボタンを押下しセクション詳細画面に遷移")
	void test03() {
		By detailButton = By.xpath(
				"//tr[td[contains(.,'ハードウェア、ソフトウェア、WWW')] and td[contains(.,'未提出')]]//input[@value='詳細']");
		visibilityTimeout(detailButton, 10);
		((JavascriptExecutor) webDriver).executeScript("arguments[0].click();",
				webDriver.findElement(detailButton));

		visibilityTimeout(By.cssSelector("ol.breadcrumb li.active"), 10);
		assertTrue(webDriver.getTitle().contains("セクション詳細"));
		assertEquals("セクション詳細", webDriver.findElement(By.cssSelector("ol.breadcrumb li.active")).getText());
		assertTrue(webDriver.findElement(By.cssSelector("input[value='日報【デモ】を提出する']")).isDisplayed());

		getEvidence(new Object() {
		});
	}

	@Test
	@Order(4)
	@DisplayName("テスト04 「提出する」ボタンを押下しレポート登録画面に遷移")
	void test04() {
		((JavascriptExecutor) webDriver).executeScript("arguments[0].click();",
				webDriver.findElement(By.cssSelector("input[value='日報【デモ】を提出する']")));

		visibilityTimeout(By.id("content_0"), 10);
		assertTrue(webDriver.getTitle().contains("レポート登録"));
		assertTrue(webDriver.findElement(By.id("content_0")).isDisplayed());
		assertTrue(webDriver.findElement(By.cssSelector("button[type='submit']")).getText().contains("提出する"));

		getEvidence(new Object() {
		});
	}

	@Test
	@Order(5)
	@DisplayName("テスト05 報告内容を入力し「提出する」ボタンを押下しセクション詳細画面に遷移")
	void test05() {
		WebElement content = webDriver.findElement(By.id("content_0"));
		content.clear();
		content.sendKeys("本日の研修内容を報告します。");
		((JavascriptExecutor) webDriver).executeScript("arguments[0].click();",
				webDriver.findElement(By.cssSelector("button[type='submit']")));

		By submitted = By.cssSelector("input[value='提出済み日報【デモ】を確認する']");
		visibilityTimeout(submitted, 10);
		assertTrue(webDriver.getTitle().contains("セクション詳細"));
		assertTrue(webDriver.findElement(submitted).isDisplayed());

		getEvidence(new Object() {
		});
	}

}
