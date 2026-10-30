package jp.co.sss.lms.ct.f03_report;

import static jp.co.sss.lms.ct.util.WebDriverUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
import org.openqa.selenium.support.ui.Select;

/**
 * 結合テスト レポート機能
 * ケース09
 * @author holy
 */
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("ケース09 受講生 レポート登録 入力チェック")
public class Case09 {

	private static final String IMPRESSION = "修正前の所感です。";
	private static final String REVIEW = "一週間の振り返りです。";
	private static String goalId;
	private static String impressionId;
	private static String reviewId;

	/** 前処理 */
	@BeforeAll
	static void before() throws Exception {
		prepareSubmittedWeeklyReport();
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
	@DisplayName("テスト03 上部メニューの「ようこそ○○さん」リンクからユーザー詳細画面に遷移")
	void test03() {
		By userLink = By.cssSelector("a[href*='/user/detail']");
		visibilityTimeout(userLink, 10);
		assertTrue(webDriver.findElement(userLink).getText().contains("ようこそ"));
		((JavascriptExecutor) webDriver).executeScript("arguments[0].click();",
				webDriver.findElement(userLink));

		visibilityTimeout(By.tagName("h2"), 10);
		assertTrue(webDriver.getTitle().contains("ユーザー詳細"));
		assertEquals("ユーザー詳細", webDriver.findElement(By.tagName("h2")).getText());

		getEvidence(new Object() {
		});
	}

	@Test
	@Order(4)
	@DisplayName("テスト04 該当レポートの「修正する」ボタンを押下しレポート登録画面に遷移")
	void test04() {
		By editButton = By.xpath(
				"//tr[td[contains(.,'週報【デモ】')]]//form[contains(@action,'/report/regist')]//input[@value='修正する']");
		visibilityTimeout(editButton, 10);
		((JavascriptExecutor) webDriver).executeScript("arguments[0].click();",
				webDriver.findElement(editButton));

		visibilityTimeout(By.id("content_0"), 10);
		goalId = contentIdOf("目標の達成度");
		impressionId = contentIdOf("所感");
		reviewId = contentIdOf("一週間の振り返り");

		assertTrue(webDriver.getTitle().contains("レポート登録"));
		assertTrue(webDriver.findElement(By.tagName("h2")).getText().contains("週報【デモ】"));
		assertEquals(IMPRESSION, webDriver.findElement(By.id(impressionId)).getAttribute("value"));
		assertTrue(webDriver.findElement(By.cssSelector("button[type='submit']")).getText().contains("提出する"));
		assertTrue(webDriver.findElement(By.id("intFieldName_0")).isDisplayed());
		assertTrue(webDriver.findElement(By.id("intFieldValue_0")).isDisplayed());

		getEvidence(new Object() {
		});
	}

	@Test
	@Order(5)
	@DisplayName("テスト05 報告内容を修正して「提出する」ボタンを押下しエラー表示：学習項目が未入力")
	void test05() {
		clearLearning();
		setValue(By.id("intFieldValue_0"), "1");
		fillReport(inRangeGoal(), IMPRESSION, REVIEW);
		submitReport();

		assertError("理解度を入力した場合は、学習項目は必須です。");
		getEvidence(new Object() {
		});
	}

	@Test
	@Order(6)
	@DisplayName("テスト06 不適切な内容で修正して「提出する」ボタンを押下しエラー表示：理解度が未入力")
	void test06() {
		clearLearning();
		setValue(By.id("intFieldName_0"), "Java");
		fillReport(inRangeGoal(), IMPRESSION, REVIEW);
		submitReport();

		assertError("学習項目を入力した場合は、理解度は必須です。");
		getEvidence(new Object() {
		});
	}

	@Test
	@Order(7)
	@DisplayName("テスト07 不適切な内容で修正して「提出する」ボタンを押下しエラー表示：目標の達成度が数値以外")
	void test07() {
		fillLearning();
		fillReport("abc", IMPRESSION, REVIEW);
		submitReport();

		assertError("目標の達成度は半角数字で入力してください。");
		getEvidence(new Object() {
		});
	}

	@Test
	@Order(8)
	@DisplayName("テスト08 不適切な内容で修正して「提出する」ボタンを押下しエラー表示：目標の達成度が範囲外")
	void test08() {
		fillLearning();
		String from = rangeValue("rangeFrom");
		String to = rangeValue("rangeTo");
		fillReport(String.valueOf(Integer.parseInt(to) + 1), IMPRESSION, REVIEW);
		submitReport();

		assertError("目標の達成度は、半角数字で、" + from + "～" + to + "の範囲内で入力してください。");
		getEvidence(new Object() {
		});
	}

	@Test
	@Order(9)
	@DisplayName("テスト09 不適切な内容で修正して「提出する」ボタンを押下しエラー表示：目標の達成度・所感が未入力")
	void test09() {
		fillLearning();
		fillReport("", "", REVIEW);
		submitReport();

		assertError("目標の達成度は半角数字で入力してください。");
		assertError("所感は必須です。");
		getEvidence(new Object() {
		});
	}

	@Test
	@Order(10)
	@DisplayName("テスト10 不適切な内容で修正して「提出する」ボタンを押下しエラー表示：所感・一週間の振り返りが2000文字超")
	void test10() {
		fillLearning();
		String over = "あ".repeat(2001);
		fillReport(inRangeGoal(), over, over);
		submitReport();

		assertError("所感の長さが最大値(2000)を超えています。");
		assertError("一週間の振り返りの長さが最大値(2000)を超えています。");
		getEvidence(new Object() {
		});
	}

	private void fillLearning() {
		clearLearning();
		setValue(By.id("intFieldName_0"), "Java");
		setValue(By.id("intFieldValue_0"), "1");
	}

	private void clearLearning() {
		for (WebElement name : webDriver.findElements(By.cssSelector("input[id^='intFieldName_']"))) {
			setValue(name, "");
		}
		for (WebElement value : webDriver.findElements(By.cssSelector("select[id^='intFieldValue_']"))) {
			new Select(value).selectByValue("");
		}
	}

	private void fillReport(String goal, String impression, String review) {
		setValue(By.id(goalId), goal);
		setValue(By.id(impressionId), impression);
		setValue(By.id(reviewId), review);
	}

	private void submitReport() {
		((JavascriptExecutor) webDriver).executeScript("arguments[0].submit();",
				webDriver.findElement(By.cssSelector("form[action*='/report/complete']")));
		visibilityTimeout(By.cssSelector("form[action*='/report/complete']"), 10);
	}

	private void assertError(String message) {
		assertTrue(webDriver.getTitle().contains("レポート登録"));
		WebElement error = webDriver.findElement(By.xpath("//span[contains(@class,'error') and contains(.,'" + message + "')]"));
		assertTrue(error.isDisplayed());
		((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView({block:'center'});", error);
	}

	private String inRangeGoal() {
		return rangeValue("rangeFrom");
	}

	private String rangeValue(String prefix) {
		String index = goalId.substring("content_".length());
		return webDriver.findElement(By.id(prefix + "_" + index)).getAttribute("value");
	}

	private void setValue(By locator, String value) {
		setValue(webDriver.findElement(locator), value);
	}

	private void setValue(WebElement element, String value) {
		if ("select".equals(element.getTagName())) {
			new Select(element).selectByValue(value);
			return;
		}
		((JavascriptExecutor) webDriver).executeScript("arguments[0].value = arguments[1];", element, value);
	}

	private String contentIdOf(String fieldName) {
		return webDriver.findElement(By.xpath(
				"//label[contains(.,'" + fieldName + "')]/following::textarea[1]")).getAttribute("id");
	}

	/** 提出済み週報が無い環境でもケース09を実行できるよう、対象研修日の週報を用意する */
	private static void prepareSubmittedWeeklyReport() throws Exception {
		Class.forName("org.postgresql.Driver");
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/tisdb", "tisuser",
				"systemsss")) {
			int lmsUserId;
			Date sectionDate;
			int reportId;
			try (PreparedStatement statement = connection.prepareStatement(
					"SELECT l.lms_user_id, s.date, r.daily_report_id FROM m_user u "
							+ "JOIN m_lms_user l ON l.user_id = u.user_id "
							+ "JOIN t_course_user cu ON cu.lms_user_id = l.lms_user_id AND cu.delete_flg = 0 "
							+ "JOIN m_section s ON s.course_id = cu.course_id AND s.delete_flg = 0 "
							+ "JOIN m_daily_report r ON r.report_name = '週報【デモ】' AND r.delete_flg = 0 "
							+ "WHERE u.login_id = 'StudentAA01' AND s.section_name = 'アルゴリズム、フローチャート'")) {
				try (ResultSet resultSet = statement.executeQuery()) {
					if (!resultSet.next()) {
						return;
					}
					lmsUserId = resultSet.getInt(1);
					sectionDate = resultSet.getDate(2);
					reportId = resultSet.getInt(3);
				}
			}
			int submitId;
			try (PreparedStatement statement = connection.prepareStatement(
					"SELECT daily_report_submit_id FROM t_daily_report_submit "
							+ "WHERE daily_report_id = ? AND date = ? AND lms_user_id = ? AND delete_flg = 0")) {
				statement.setInt(1, reportId);
				statement.setDate(2, sectionDate);
				statement.setInt(3, lmsUserId);
				try (ResultSet resultSet = statement.executeQuery()) {
					if (resultSet.next()) {
						submitId = resultSet.getInt(1);
					} else {
						try (PreparedStatement insert = connection.prepareStatement(
								"INSERT INTO t_daily_report_submit (daily_report_id, date, lms_user_id, account_id, delete_flg, "
										+ "first_create_user, first_create_date, last_modified_user, last_modified_date) "
										+ "VALUES (?, ?, ?, 1, 0, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP) "
										+ "RETURNING daily_report_submit_id")) {
							insert.setInt(1, reportId);
							insert.setDate(2, sectionDate);
							insert.setInt(3, lmsUserId);
							insert.setInt(4, lmsUserId);
							insert.setInt(5, lmsUserId);
							try (ResultSet inserted = insert.executeQuery()) {
								inserted.next();
								submitId = inserted.getInt(1);
							}
						}
					}
				}
			}
			saveDetail(connection, submitId, 1, "5");
			saveDetail(connection, submitId, 2, IMPRESSION);
			saveDetail(connection, submitId, 3, REVIEW);
		}
	}

	private static void saveDetail(Connection connection, int submitId, int fieldNum, String content) throws Exception {
		try (PreparedStatement update = connection.prepareStatement(
				"UPDATE t_daily_report_submit_detail SET content = ? "
						+ "WHERE daily_report_submit_id = ? AND field_num = ? AND delete_flg = 0")) {
			update.setString(1, content);
			update.setInt(2, submitId);
			update.setInt(3, fieldNum);
			if (update.executeUpdate() > 0) {
				return;
			}
		}
		try (PreparedStatement insert = connection.prepareStatement(
				"INSERT INTO t_daily_report_submit_detail (daily_report_submit_id, field_num, content, account_id, delete_flg, "
						+ "first_create_user, first_create_date, last_modified_user, last_modified_date) "
						+ "VALUES (?, ?, ?, 1, 0, 16, CURRENT_TIMESTAMP, 16, CURRENT_TIMESTAMP)")) {
			insert.setInt(1, submitId);
			insert.setInt(2, fieldNum);
			insert.setString(3, content);
			insert.executeUpdate();
		}
	}

}
