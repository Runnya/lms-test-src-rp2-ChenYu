package jp.co.sss.lms.ct.f04_attendance;

import static jp.co.sss.lms.ct.util.WebDriverUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebElement;

/**
 * 結合テスト 勤怠管理機能
 * ケース10
 * @author holy
 */
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("ケース10 受講生 勤怠登録 正常系")
public class Case10 {

	/** 前処理 */
	@BeforeAll
	static void before() throws Exception {
		prepareTodayAttendance();
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
	@DisplayName("テスト03 上部メニューの「勤怠」リンクから勤怠管理画面に遷移")
	void test03() {
		By attendanceLink = By.cssSelector("a[href*='/attendance/detail']");
		visibilityTimeout(attendanceLink, 10);
		((JavascriptExecutor) webDriver).executeScript("arguments[0].click();",
				webDriver.findElement(attendanceLink));
		acceptAlerts();

		visibilityTimeout(By.cssSelector("input[name='punchIn']"), 10);
		assertTrue(webDriver.getTitle().contains("勤怠"));
		assertEquals("勤怠管理", webDriver.findElement(By.tagName("h2")).getText());
		assertEquals("出勤", webDriver.findElement(By.cssSelector("input[name='punchIn']")).getAttribute("value"));
		assertEquals("退勤", webDriver.findElement(By.cssSelector("input[name='punchOut']")).getAttribute("value"));
		assertTrue(webDriver.findElement(By.linkText("勤怠情報を直接編集する")).isDisplayed());
		WebElement today = todayRow();
		((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView({block:'center'});", today);
		assertTrue(today.getText().contains(
				LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.JAPAN))));
		assertTrue(today.getText().contains("勤怠登録確認"));

		getEvidence(new Object() {
		});
	}

	@Test
	@Order(4)
	@DisplayName("テスト04 「出勤」ボタンを押下し出勤時間を登録")
	void test04() {
		punch("input[name='punchIn']");

		assertTrue(webDriver.findElement(By.cssSelector(".alert-info")).getText().contains("勤怠情報の登録が完了しました。"));
		List<WebElement> cells = todayRow().findElements(By.tagName("td"));
		assertTrue(cells.get(2).getText().matches("\\d{2}:\\d{2}"));
		assertEquals("", cells.get(3).getText().trim());

		getEvidence(new Object() {
		});
	}

	@Test
	@Order(5)
	@DisplayName("テスト05 「退勤」ボタンを押下し退勤時間を登録")
	void test05() {
		punch("input[name='punchOut']");

		assertTrue(webDriver.findElement(By.cssSelector(".alert-info")).getText().contains("勤怠情報の登録が完了しました。"));
		List<WebElement> cells = todayRow().findElements(By.tagName("td"));
		assertTrue(cells.get(2).getText().matches("\\d{2}:\\d{2}"));
		assertTrue(cells.get(3).getText().matches("\\d{2}:\\d{2}"));

		getEvidence(new Object() {
		});
	}

	private void punch(String selector) {
		WebElement button = webDriver.findElement(By.cssSelector(selector));
		((JavascriptExecutor) webDriver).executeScript(
				"window.confirm=function(){return true;}; arguments[0].click();", button);
		acceptAlerts();
		visibilityTimeout(By.cssSelector(".alert-info"), 10);
		((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView({block:'center'});", todayRow());
	}

	private WebElement todayRow() {
		return webDriver.findElement(By.cssSelector("tbody tr.info"));
	}

	private void acceptAlerts() {
		for (int i = 0; i < 3; i++) {
			try {
				webDriver.switchTo().alert().accept();
			} catch (NoAlertPresentException e) {
				return;
			}
		}
	}

	/** 本日が研修日でない、または出退勤済みでも打刻できるよう、当日の区間と勤怠を整える */
	private static void prepareTodayAttendance() throws Exception {
		Class.forName("org.postgresql.Driver");
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/tisdb", "tisuser",
				"systemsss")) {
			int lmsUserId;
			int courseId;
			try (PreparedStatement statement = connection.prepareStatement(
					"SELECT l.lms_user_id, cu.course_id FROM m_user u "
							+ "JOIN m_lms_user l ON l.user_id = u.user_id "
							+ "JOIN t_course_user cu ON cu.lms_user_id = l.lms_user_id AND cu.delete_flg = 0 "
							+ "WHERE u.login_id = 'StudentAA01'")) {
				try (ResultSet resultSet = statement.executeQuery()) {
					if (!resultSet.next()) {
						return;
					}
					lmsUserId = resultSet.getInt(1);
					courseId = resultSet.getInt(2);
				}
			}
			int sectionCount;
			try (PreparedStatement statement = connection.prepareStatement(
					"SELECT COUNT(*) FROM m_section WHERE course_id = ? AND CAST(date AS DATE) = CURRENT_DATE")) {
				statement.setInt(1, courseId);
				try (ResultSet resultSet = statement.executeQuery()) {
					resultSet.next();
					sectionCount = resultSet.getInt(1);
				}
			}
			if (sectionCount == 0) {
				try (PreparedStatement insert = connection.prepareStatement(
						"INSERT INTO m_section (section_name, section_description, course_id, category_id, date, account_id, "
								+ "delete_flg, first_create_user, first_create_date, last_modified_user, last_modified_date) "
								+ "SELECT '勤怠登録確認', section_description, course_id, category_id, CURRENT_DATE, account_id, "
								+ "0, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP FROM m_section "
								+ "WHERE course_id = ? AND delete_flg = 0 ORDER BY date LIMIT 1")) {
					insert.setInt(1, lmsUserId);
					insert.setInt(2, lmsUserId);
					insert.setInt(3, courseId);
					insert.executeUpdate();
				}
			}
			try (PreparedStatement delete = connection.prepareStatement(
					"DELETE FROM t_student_attendance WHERE lms_user_id = ? AND CAST(training_date AS DATE) = CURRENT_DATE")) {
				delete.setInt(1, lmsUserId);
				delete.executeUpdate();
			}
		}
	}

}
