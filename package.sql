set serveroutput on

Drop trigger enroll_student_log;
Drop table prereq_helper_table;
Create table prereq_helper_table(dept_code varchar2(4) not null, course_no number(3) not null);

drop sequence log_seq;
create sequence log_seq
increment by 1
start with 1000;

-- SPECIFICATIONS
create or replace package registrations as
type ref_cursor is ref cursor;

-- Q2: Show Tables
function show_students
  return ref_cursor;

function show_courses
  return ref_cursor;

function show_prerequisites
  return ref_cursor;

function show_classes
  return ref_cursor;

function show_enrollments
  return ref_cursor;
  
function show_logs
  return ref_cursor;
  
-- Q3: Insert Student
procedure insert_student(v_SID in Students.SID%type, v_firstname in Students.firstname%type, v_lastname in Students.lastname%type, 
                         v_status in Students.status%type, v_GPA in Students.GPA%type, v_email in Students.email%type);
 
-- Q4: Student Info
procedure student_info(v_SID in Students.SID%type, msg out varchar2, ref_cursor out sys_refcursor);

-- Q5: Prerequisites
procedure get_prereqs(v_dept_code in Prerequisites.dept_code%type, v_course_no in Prerequisites.course_no%type, ref_cursor out sys_refcursor);
  
-- Q6: Get Class Students
function get_class_students(v_classid in enrollments.classid%type, msg out varchar2, ref_cursor out sys_refcursor)
return number;

-- Q7: Enroll Students
procedure enroll_student(v_sid in Students.sid%type, v_classid in Enrollments.classid%type, msg out varchar2);

-- Q8: Unenroll Students
procedure unenroll_student(v_sid in Students.sid%type, v_classid in Enrollments.classid%type, msg out varchar2);

-- Q9: Delete Students
function delete_student(v_sid in Students.sid%type, msg out varchar2) 
return number;

end;
/
show errors

-- BODY
create or replace package body registrations as

-- Q2: Show Tables
function show_students
return ref_cursor as
rc ref_cursor;
begin
  open rc for
  select * from students;
  return rc;
end;

function show_courses
return ref_cursor as
rc ref_cursor;
begin
  open rc for
  select * from courses;
  return rc;
end;

function show_prerequisites
return ref_cursor as
rc ref_cursor;
begin
  open rc for
  select * from prerequisites;
  return rc;
end;

function show_classes
return ref_cursor as
rc ref_cursor;
begin
  open rc for
  select * from classes;
  return rc;
end;

function show_enrollments
return ref_cursor as
rc ref_cursor;
begin
  open rc for
  select * from enrollments;
  return rc;
end;

function show_logs
return ref_cursor as
rc ref_cursor;
begin
  open rc for
  select * from logs;
  return rc;
end;

-- Q3: Insert Student
procedure insert_student(v_SID in Students.SID%type, v_firstname in Students.firstname%type, v_lastname in Students.lastname%type, 
                         v_status in Students.status%type, v_GPA in Students.GPA%type, v_email in Students.email%type) as
begin
  insert into students(SID, firstname, lastname, status, GPA, email) values (v_SID, v_firstname, v_lastname, v_status, v_GPA, v_email);
end; 

-- Q4: Student Info
procedure student_info(v_SID in Students.SID%type, msg out varchar2, ref_cursor out sys_refcursor) is 
  v_SID_count Number; 
  v_class_count Number;
begin
  select count(*) into v_SID_count from Students where v_SID = Students.SID;
  select count(*) into v_class_count from Students, Enrollments where Students.SID = Enrollments.SID AND v_SID = Enrollments.SID;
  if v_SID_count = 0 then
     msg := 'invalid sid';
  else
    if v_class_count = 0 then
      msg := 'has not taken any course';
    else open ref_cursor for 
      select students.sid, students.lastname, students.status, classes.classid, concat(classes.dept_code, classes.course_no) as course_id from Students 
      join Enrollments on students.sid = enrollments.sid 
      join Classes on Enrollments.classid = Classes.classid
      where students.sid = v_sid;
    end if;
  end if;
end;

-- Q5: Prerequisites
/*
-make a prereq cursor that contains all the prereqs of the course we selected
	ex. CS and 442 are inputed and its prereqs are CS 240 and Math 314
		the cursor now contains 
		CS 240
		Math 314
-save these values from the cursor to a table prereq_helper_table(dept_code, course_no) for output
-make a record cursor to store each line for recursion
-start a loop
	-fetch first line of prereq_cursor into prereq_rec
	-exit loop if nothing was fetched
	-recursively call the function on the prereqs to get its prereqs
	-store values into the table
-store table values onto ref_cursor
*/
procedure get_prereqs(v_dept_code in Prerequisites.dept_code%type, v_course_no in Prerequisites.course_no%type, ref_cursor out sys_refcursor) is
cursor prereq_cursor is
select pre_dept_code, pre_course_no from prerequisites
where dept_code = v_dept_code and course_no = v_course_no;
prereq_rec prereq_cursor%rowtype;


begin
  insert into prereq_helper_table select pre_dept_code, pre_course_no
  from prerequisites where v_dept_code = dept_code and v_course_no = course_no;
  open prereq_cursor;
  loop
    fetch prereq_cursor into prereq_rec;
    exit when prereq_cursor%notfound;
    get_prereqs(prereq_rec.pre_dept_code, prereq_rec.pre_course_no, ref_cursor);
  end loop;
  open ref_cursor for select * from prereq_helper_table;
  close prereq_cursor;
end;


-- Q6: Function that takes classid and prints classid, course title, sid, lastname, and email of all students taking/taken that class
function get_class_students(v_classid in enrollments.classid%type, msg out varchar2, ref_cursor out sys_refcursor)
return number is
v_classid_count Number;
v_student_count Number;
begin
  select count(*) into v_classid_count from Classes where v_classid = Classes.classid;
  select count(*) into v_student_count from Enrollments where v_classid = Enrollments.classid;
  if v_classid_count = 0 then
    msg := 'invalid cid';
    return 1;
  elsif v_student_count = 0 then
    msg := 'empty class';
    return 1;
  else
    open ref_cursor for
    select enrollments.classid, courses.title, students.sid, students.lastname, students.email from enrollments 
      join Classes on Enrollments.classid = Classes.classid
      join Courses on Classes.course_no = Courses.course_no
      join Students on students.sid = enrollments.sid
      where enrollments.classid = v_classid;
  end if;
  return 0;
end;


-- Q7: Procedure to enroll student into a class. Takes in student.sid and enrollment.classid.
procedure enroll_student(v_sid in Students.sid%type, v_classid in Enrollments.classid%type, msg out varchar2) is
  v_sid_count Number; 
  v_classid_count Number;
  v_open_seats Number;
  v_already_in Number;
  v_class_count Number;
  v_prereq_count Number;

begin
  begin
  -- Use counts to validate sid/cid
  select count(*) into v_sid_count from Students where v_sid = sid;
  
  select count(*) into v_classid_count from Classes where classid = v_classid; 
    
  -- Limit - class_size = Number of available seats 
  select limit-class_size into v_open_seats from Classes where v_classid = classid;
  
  select count(*) into v_already_in from Enrollments where v_sid = sid and v_classid = classid;
  
  -- Keep a count of the enrolled classes in the current semester and year of inputted SID
  select count(*) into v_class_count from enrollments, classes
  where enrollments.sid = v_sid and enrollments.classid = classes.classid and classes.semester = 'Spring' and classes.year = 2022 ;
  
  -- Select (prereqs of inputted SID and CID) minus (all classes taken by inputted SID with lgrade of at least C+)
    -- If count > 0, then prerequisite requirements are not met
  select count(*) into v_prereq_count from 
    (select prerequisites.pre_dept_code, prerequisites.pre_course_no from Prerequisites
	  join Classes on Prerequisites.dept_code = Classes.dept_code and Prerequisites.course_no = Classes.course_no
	  join Enrollments on Enrollments.classid = Classes.classid
	  where Enrollments.sid = v_sid and Enrollments.classid = v_classid
  minus
    select classes.dept_code, classes.course_no from Classes
	  join Enrollments on Enrollments.classid = Classes.classid
	  where Enrollments.sid = v_sid and Enrollments.lgrade < 'C') count;
  exception
    when no_data_found then
      msg := 'invalid classid';
  end;
  

  if v_sid_count = 0 then
    msg := 'invalid sid';
  elsif v_classid_count = 0 then
    msg := 'invalid classid';
  elsif v_open_seats = 0 then
    msg := 'class full';
  elsif v_already_in > 0 then
    msg := 'already in this class';
  elsif v_class_count > 4 then
    msg := 'overloaded!';
  elsif v_prereq_count > 0 then
    msg := 'prerequisite courses have not been completed';
  else
    insert into Enrollments(sid, classid) values (v_sid, v_classid);
  end if;
end;


-- Q8: Procedure to unenroll student from class
procedure unenroll_student(v_sid in Students.sid%type, v_classid in Enrollments.classid%type, msg out varchar2) is
  v_sid_count Number; 
  v_classid_count Number;
  v_already_in Number;
  v_class_count Number;
  v_class_size Number;
  
begin
  select count(*) into v_sid_count from Students where v_sid = sid;
  select count(*) into v_classid_count from Classes where classid = v_classid; 
  select count(*) into v_already_in from Enrollments where v_sid = sid and v_classid = classid;
  select count(*) into v_class_count from Enrollments where v_sid = sid;
  select count(*) into v_class_size from Classes where classid = v_classid;
  
  if v_sid_count = 0 then
    msg := 'invalid sid';
  elsif v_classid_count = 0 then
    msg := 'invalid classid';
  elsif v_already_in = 0 then
    msg := 'student not enrolled';
  elsif v_class_count = 1 then
    msg := 'drop request rejected; must be enrolled in at least one class.';
  elsif v_class_size = 1 then
    msg := 'no student in this class';
    delete from enrollments where v_sid = sid and v_classid = classid;
  else
    delete from enrollments where v_sid = sid and v_classid = classid;
  end if;
end;

-- Q9: Procedure to delete student
function delete_student(v_sid in Students.sid%type, msg out varchar2) 
  return number is
  v_sid_count Number;

begin
  select count(*) into v_sid_count from Students where v_sid = sid;
  if v_sid_count = 0 then
    msg := 'sid not found';
    return 1;
  end if;
  delete from students where v_sid = sid;
  return 0;
end;

end;
/
show errors

-- Triggers
create or replace trigger enroll_student_log
after insert on enrollments
for each row
declare
  v_logid Number;
  v_who varchar2(10);
  v_table_name varchar2(20);
  v_operation_name varchar2(6);
  v_sid enrollments.sid%type;
  v_classid enrollments.classid%type;
  v_key_value varchar2(14);
begin
  v_logid := log_seq.nextVal;
  select user into v_who from dual;
  v_table_name := 'Enrollments';
  v_operation_name := 'Insert';
  v_sid := :new.sid;
  v_classid := :new.classid;
  v_key_value := (v_sid || ', ' || v_classid);
  insert into logs values(v_logid, v_who, sysdate, v_table_name, v_operation_name, v_key_value);
  update classes
  set class_size = class_size + 1
  where classid = v_classid;
end;
/
/*
create or replace trigger delete_student
after delete on students
begin
  delete from enrollments where sid = students.sid
end;
/
*/
show errors;
  