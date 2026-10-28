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
 * ケース08
 * @author holy
 */
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("ケース08 受講生 レポート修正(週報) 正常系")
public class Case08 {

	private static final String UPDATED_CONTENT = "修正後の所感です。";
	private static String weeklyReportDateLabel = "2025年7月9日";

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
	@DisplayName("テスト03 提出済の研修日の「詳細」ボタンを押下しセクション詳細画面に遷移")
	void test03() {
		By detailButton = By.xpath(
				"//tr[td[contains(.,'アルゴリズム、フローチャート')] and td[contains(.,'提出済み')]]//input[@value='詳細']");
		visibilityTimeout(detailButton, 10);
		((JavascriptExecutor) webDriver).executeScript("arguments[0].click();",
				webDriver.findElement(detailButton));

		By confirmButton = By.cssSelector("input[value='提出済み週報【デモ】を確認する']");
		visibilityTimeout(confirmButton, 10);
		assertTrue(webDriver.getTitle().contains("セクション詳細"));
		assertEquals("セクション詳細", webDriver.findElement(By.cssSelector("ol.breadcrumb li.active")).getText());
		assertTrue(webDriver.findElement(confirmButton).isDisplayed());

		getEvidence(new Object() {
		});
	}

	@Test
	@Order(4)
	@DisplayName("テスト04 「確認する」ボタンを押下しレポート登録画面に遷移")
	void test04() {
		((JavascriptExecutor) webDriver).executeScript("arguments[0].click();",
				webDriver.findElement(By.cssSelector("input[value='提出済み週報【デモ】を確認する']")));

		visibilityTimeout(By.id("content_1"), 10);
		assertTrue(webDriver.getTitle().contains("レポート登録"));
		assertEquals("修正前の所感です。", webDriver.findElement(By.id("content_1")).getAttribute("value"));
		assertTrue(webDriver.findElement(By.cssSelector("button[type='submit']")).getText().contains("提出する"));

		getEvidence(new Object() {
		});
	}

	@Test
	@Order(5)
	@DisplayName("テスト05 報告内容を修正して「提出する」ボタンを押下しセクション詳細画面に遷移")
	void test05() {
		WebElement content = webDriver.findElement(By.id("content_1"));
		((JavascriptExecutor) webDriver).executeScript("arguments[0].value = arguments[1];", content,
				UPDATED_CONTENT);
		((JavascriptExecutor) webDriver).executeScript("arguments[0].value = arguments[1];",
				webDriver.findElement(By.id("intFieldName_0")), "Java");
		new Select(webDriver.findElement(By.id("intFieldValue_0"))).selectByValue("1");
		((JavascriptExecutor) webDriver).executeScript("arguments[0].submit();",
				webDriver.findElement(By.cssSelector("form[action*='/report/complete']")));

		visibilityTimeout(By.cssSelector("ol.breadcrumb li.active"), 10);
		assertTrue(webDriver.getTitle().contains("セクション詳細"));
		assertEquals("セクション詳細", webDriver.findElement(By.cssSelector("ol.breadcrumb li.active")).getText());

		getEvidence(new Object() {
		});
	}

	@Test
	@Order(6)
	@DisplayName("テスト06 上部メニューの「ようこそ○○さん」リンクからユーザー詳細画面に遷移")
	void test06() {
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
	@Order(7)
	@DisplayName("テスト07 該当レポートの「詳細」ボタンを押下しレポート詳細画面で修正内容が反映される")
	void test07() {
		By reportDetail = By.xpath(
				"//tr[td[contains(.,'" + weeklyReportDateLabel
						+ "')] and td[contains(.,'週報【デモ】')]]//form[contains(@action,'/report/detail')]//input[@value='詳細']");
		visibilityTimeout(reportDetail, 10);
		((JavascriptExecutor) webDriver).executeScript("arguments[0].click();",
				webDriver.findElement(reportDetail));

		visibilityTimeout(By.tagName("h2"), 10);
		assertTrue(webDriver.getTitle().contains("レポート詳細"));
		assertTrue(webDriver.findElement(By.tagName("body")).getText().contains(UPDATED_CONTENT));

		getEvidence(new Object() {
		});
	}

	/** 提出済み週報が無い環境でもケース08を実行できるよう、対象研修日の週報を用意する */
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
			String[] dateParts = sectionDate.toString().split("-");
			weeklyReportDateLabel = Integer.parseInt(dateParts[0]) + "年" + Integer.parseInt(dateParts[1]) + "月"
					+ Integer.parseInt(dateParts[2]) + "日";

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
			saveDetail(connection, submitId, 2, "修正前の所感です。");
			saveDetail(connection, submitId, 3, "一週間の振り返りです。");
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
