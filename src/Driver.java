import java.sql.*;
import oracle.jdbc.*;
import java.math.*;
import java.io.*;
import java.awt.*;
import oracle.jdbc.pool.OracleDataSource;

public class Driver {
	public static void main(String args[]) throws SQLException {
		try {
			OracleDataSource ds = new oracle.jdbc.pool.OracleDataSource();
			ds.setURL("jdbc:oracle:thin:@localhost:1521:orcl");
			Connection conn = ds.getConnection("sys as sysdba", "pass");

			while (true) {
				System.out.println("\n----------MENU----------");
				System.out.println("1: View Table");
				System.out.println("2: Insert Student");
				System.out.println("3: Student's Classes");
				System.out.println("4: Class Prequisites");
				System.out.println("5: Show Class Students");
				System.out.println("6: Enroll Students");
				System.out.println("7: Unenroll Students");
				System.out.println("8: Delete Students");
				System.out.println("9: Exit");

				BufferedReader readKeyBoard;
				int i = 0;
				readKeyBoard = new BufferedReader(new InputStreamReader(System.in));
				System.out.print("Select an option: ");
				i = Integer.parseInt(readKeyBoard.readLine());

				switch (i) {
					
					// Q2 - Show Six Tables
					case 1: {
						System.out.println("\n-----SELECT A TABLE-----");
						System.out.println("1: Students");
						System.out.println("2: Courses");
						System.out.println("3: Prerequisites");
						System.out.println("4: Classes");
						System.out.println("5: Enrollments");
						System.out.println("6: Logs");
		
						BufferedReader input;
						int j = 0;
						input = new BufferedReader(new InputStreamReader(System.in));
						System.out.print("Select an option: ");
						j = Integer.parseInt(input.readLine());
						System.out.println();
						showTable(j, conn);
						break;
					}
					
					// Q3 - Insert Student
					case 2: {
						BufferedReader input;
						input = new BufferedReader(new InputStreamReader(System.in));
						System.out.println("\nStudent SID: ");
						String sid = input.readLine();
						System.out.println("\nStudent First Name: ");
						String firstname = input.readLine();
						System.out.println("\nStudent Last Name: ");
						String lastname = input.readLine();
						System.out.println("\nStudent Status: ");
						String status = input.readLine();
						System.out.println("\nStudent GPA: ");
						Double gpa = Double.parseDouble(input.readLine());
						System.out.println("\nStudent Email: ");
						String email = input.readLine();
						insertStudent(conn, sid, firstname, lastname, status, gpa, email);
						break;
					}
					
					/* Q4:
					 *  Lists sid, lastname, status of a student
					 *  + Lists classid, dept_code, and course_no (concatenate dept_code and course_no)
					 * 
					 *  Exceptions:
					 * 	 If a student is not in the table, print 'invalid sid'
					 *   If a student is in the table but has not taken any courses, print 'has not taken any course'
					 */ 
					case 3: {
						BufferedReader input;
						input = new BufferedReader(new InputStreamReader(System.in));
						System.out.print("\nStudent SID: ");
						String sid = input.readLine();
						studentInfo(conn, sid);
						break;
					}
					
					/* Q5:
					 *  Given dept_code and course_no as inputs, show all prerequisites (including indirect)
					 *  For each prerequisite show concatenated dept_code and course_no
					 * */
					case 4: {
						BufferedReader input;
						input = new BufferedReader(new InputStreamReader(System.in));
						System.out.print("\nClass deptcode: ");
						String deptcode = input.readLine();
						System.out.print("\nClass course_no: ");
						String course_no = input.readLine();
						getPrereqs(conn, deptcode, course_no);
						break;
					}
					
					/* Q6: Prints all students taking a class
					 *  
					 */
					case 5: {
						BufferedReader input;
						input = new BufferedReader(new InputStreamReader(System.in));
						System.out.print("\nClassid: ");
						String classid = input.readLine();
						getClassStudents(conn, classid);
						break;
					}
					
					/* Q7: Enroll Student
					 * 
					 * */
					case 6: {
						BufferedReader input;
						input = new BufferedReader(new InputStreamReader(System.in));
						System.out.print("\nStudent Sid: ");
						String sid = input.readLine();
						System.out.print("\nClassid: ");
						String classid = input.readLine();
						enrollStudents(conn, sid, classid);
						break;
					}
					
					/* Q8: Unenroll Student
					 * 
					 */
					case 7: {
						BufferedReader input;
						input = new BufferedReader(new InputStreamReader(System.in));
						System.out.print("\nStudent Sid: ");
						String sid = input.readLine();
						System.out.print("\nClassid: ");
						String classid = input.readLine();
						unenrollStudents(conn, sid, classid);
						break;
					}
					
					/* Q9: Delete Student
					 * 
					 */
					case 8: {
						BufferedReader input;
						input = new BufferedReader(new InputStreamReader(System.in));
						System.out.print("\nStudent Sid: ");
						String sid = input.readLine();
						deleteStudent(conn, sid);
						break;
					}
					
					// Exit Program
					case 9: {
						System.exit(1);
					}
				}

			}

		} catch (SQLException ex) {
			System.out.println("\n*** SQLException caught ***\n" + ex.getMessage());
		} catch (Exception e) {
			System.out.println("\n*** other Exception caught ***\n");
		}
	}
	
	// Q2 - Show 6 Tables
	public static void showTable(int selection, Connection conn) {
		switch (selection) {
			// 1: Show Students
			case 1: {
				try {
					CallableStatement cs = conn.prepareCall("begin ? := registrations.show_students(); end;");
					cs.registerOutParameter(1, OracleTypes.CURSOR);
					cs.execute();
					ResultSet rs = (ResultSet) cs.getObject(1);
					while (rs.next()) {
						System.out.println(rs.getString(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3) + "\t"
								+ rs.getString(4) + "\t" + rs.getDouble(5) + "\t" + rs.getString(6));
					}
					rs.close();
					cs.close();
				} catch (SQLException ex) {
					System.out.println("\n*** SQLException caught ***\n" + ex.getMessage());
				}
				break;
			}
			
			// 2: Show Courses
			case 2: {
				try {
					CallableStatement cs = conn.prepareCall("begin ? := registrations.show_courses(); end;");
					cs.registerOutParameter(1, OracleTypes.CURSOR);
					cs.execute();
					ResultSet rs = (ResultSet) cs.getObject(1);
					while (rs.next()) {
						System.out.println(rs.getString(1) + "\t" + rs.getInt(2) + "\t" + rs.getString(3));
					}
					rs.close();
					cs.close();
				} catch (SQLException ex) {
					System.out.println("\n*** SQLException caught ***\n" + ex.getMessage());
				}
				break;
			}
			
			// 3: Show Prerequisites
			case 3: {
				try {
					CallableStatement cs = conn.prepareCall("begin ? := registrations.show_prerequisites(); end;");
					cs.registerOutParameter(1, OracleTypes.CURSOR);
					cs.execute();
					ResultSet rs = (ResultSet) cs.getObject(1);
					while (rs.next()) {
						System.out.println(
								rs.getString(1) + "\t" + rs.getInt(2) + "\t" + rs.getString(3) + "\t" + rs.getInt(4));
					}
					rs.close();
					cs.close();
				} catch (SQLException ex) {
					System.out.println("\n*** SQLException caught ***\n" + ex.getMessage());
				}
				break;
			}
			
			// 4: Show Classes
			case 4: {
				try {
					CallableStatement cs = conn.prepareCall("begin ? := registrations.show_classes(); end;");
					cs.registerOutParameter(1, OracleTypes.CURSOR);
					cs.execute();
					ResultSet rs = (ResultSet) cs.getObject(1);
					while (rs.next()) {
						System.out.println(rs.getString(1) + "\t" + rs.getString(2) + "\t" + rs.getInt(3) + "\t"
								+ rs.getInt(4) + "\t" + rs.getInt(5) + "\t" + rs.getString(6) + "\t" + rs.getInt(7) + "\t"
								+ rs.getInt(8));
					}
					rs.close();
					cs.close();
				} catch (SQLException ex) {
					System.out.println("\n*** SQLException caught ***\n" + ex.getMessage());
				}
				break;
			}
			
			// 5: Show Enrollments
			case 5: {
				try {
					CallableStatement cs = conn.prepareCall("begin ? := registrations.show_enrollments(); end;");
					cs.registerOutParameter(1, OracleTypes.CURSOR);
					cs.execute();
					ResultSet rs = (ResultSet) cs.getObject(1);
					while (rs.next()) {
						System.out.println(rs.getString(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3));
					}
					rs.close();
					cs.close();
				} catch (SQLException ex) {
					System.out.println("\n*** SQLException caught ***\n" + ex.getMessage());
				}
				break;
			}
			
			// 6: Show Logs
			case 6: {
				try {
					CallableStatement cs = conn.prepareCall("begin ? := registrations.show_logs(); end;");
					cs.registerOutParameter(1, OracleTypes.CURSOR);
					cs.execute();
					ResultSet rs = (ResultSet) cs.getObject(1);
					while (rs.next()) {
						System.out.println(rs.getInt(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3) + "\t"
								+ rs.getString(4) + "\t" + rs.getString(5) + "\t" + rs.getString(6));
					}
					rs.close();
					cs.close();
				} catch (SQLException ex) {
					System.out.println("\n*** SQLException caught ***\n" + ex.getMessage());
				}
				break;
			}
		}
	}

	// Q3 - Insert a Student
	public static void insertStudent(Connection conn, String sid, String firstname, String lastname, String status,
			Double gpa, String email) {
		try {
			CallableStatement cs = conn.prepareCall("begin registrations.insert_student(?,?,?,?,?,?); end;");
			cs.setString(1, sid);
			cs.setString(2, firstname);
			cs.setString(3, lastname);
			cs.setString(4, status);
			cs.setDouble(5, gpa);
			cs.setString(6, email);
			cs.execute();
			cs.close();

		} catch (SQLException ex) {
			System.out.println("\n*** SQLException caught ***\n" + ex.getMessage());
		}
	}

	/* Q4 - Show Student's sid, lastname, status,
	 *                      classid, concatenate(dept_code, course_no)
	 *      (Only prints sid if student is not taking any courses)
	 */
	public static void studentInfo(Connection conn, String sid) {
		try {
			CallableStatement cs = conn.prepareCall("begin registrations.student_info(?,?,?); end;");
			cs.setString(1, sid);
			cs.registerOutParameter(2, java.sql.Types.VARCHAR);
			cs.registerOutParameter(3, OracleTypes.CURSOR);
			cs.execute();
			String msg = cs.getString(2);
			// If v_SID_count = 0, then 'invalid sid' is printed
			// If v_class_count = 0, then 'has not taken any course' is printed
			// Otherwise, Cursor results are printed
			if (msg != null) {
				System.out.println(msg);
			} else {
				ResultSet rs = (ResultSet) cs.getObject(3);
				while (rs.next()) {
					System.out.println(rs.getString(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3) + "\t"
							+ rs.getString(4) + "\t" + rs.getString(5));
				}
				rs.close();
				cs.close();
			}
		} catch (SQLException ex) {
			System.out.println("\n*** SQLException caught ***\n" + ex.getMessage());
		} 
	}

	// Q5
	public static void getPrereqs(Connection conn, String deptcode, String course_no) {
		try {
			CallableStatement cs = conn.prepareCall("begin registrations.get_prereqs(?,?,?); end;");
			cs.setString(1, deptcode);
			cs.setString(2, course_no);
			cs.registerOutParameter(3, OracleTypes.CURSOR);
			cs.execute();
			
			ResultSet rs = (ResultSet) cs.getObject(3);
			while (rs.next()) {
				System.out.println(rs.getString(1) + rs.getString(2));
			}
			Statement truncate = conn.createStatement();
			truncate.executeQuery("truncate table prereq_helper_table");
			rs.close();
			cs.close();
			truncate.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Exception in getPrereqs()");
		}
	}
	
	/* Q6 - Given a classid, prints the classid, course title, sid, lastname, and email
	 * 		Function get_class_students(classid, msg, cursor) returns 0 if at least one student is printed
	 * 		Returns 1 and prints "invalid cid" if classid is not in class table
	 * 		Returns 1 and prints "empty class" if classid is not in enrollments table
	 * */
	public static void getClassStudents(Connection conn, String classid) {
		try {
			CallableStatement cs = conn.prepareCall("begin ? := registrations.get_class_students(?, ?, ?); end;");
			cs.registerOutParameter(1, java.sql.Types.NUMERIC);
			cs.setString(2, classid);
			cs.registerOutParameter(3, java.sql.Types.VARCHAR);
			cs.registerOutParameter(4, OracleTypes.CURSOR);
			cs.execute();
			String msg = cs.getString(3);
			if (msg != null) {
				System.out.println(msg);
			} else {
				ResultSet rs = (ResultSet) cs.getObject(4);
				while (rs.next()) {
					System.out.println(rs.getString(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3) + "\t"
							+ rs.getString(4) + "\t" + rs.getString(5));
				}
				rs.close();
				cs.close();
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Exception in getClassStudents()");
		}
	}

	/* Q7: Enroll Student
	 * */
	public static void enrollStudents(Connection conn, String sid, String classid) {
		try {
			CallableStatement cs = conn.prepareCall("begin registrations.enroll_student(?,?,?); end;");
			cs.setString(1, sid);
			cs.setString(2, classid);
			cs.registerOutParameter(3, java.sql.Types.VARCHAR);
			cs.execute();
			String msg = cs.getString(3);
			if (msg != null) {
				System.out.println(msg);
			} else {
				System.out.println("student enrolled");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Exception in enrollStudents()");
		}
	}
	
	/* Q8: Unenroll Student
	 * */
	public static void unenrollStudents(Connection conn, String sid, String classid) {
		try {
			CallableStatement cs = conn.prepareCall("begin registrations.unenroll_student(?,?,?); end;");
			cs.setString(1, sid);
			cs.setString(2, classid);
			cs.registerOutParameter(3, java.sql.Types.VARCHAR);
			cs.execute();
			String msg = cs.getString(3);
			if (msg != null) {
				System.out.println(msg);
			} else {
				System.out.println("student enrolled");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Exception in unenrollStudents()");
		}
	}
	
	/* Q9: Delete Student
	 * */
	public static void deleteStudent(Connection conn, String sid) {
		try {
			CallableStatement cs = conn.prepareCall("begin ? := registrations.delete_student(?, ?); end;");
			cs.registerOutParameter(1, java.sql.Types.NUMERIC);
			cs.setString(2, sid);
			cs.registerOutParameter(3, java.sql.Types.VARCHAR);
			cs.execute();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Exception in deleteStudent()");
		}
	}
}
