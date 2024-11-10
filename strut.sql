create database college;
show databases;
use college;
create table BTAIML(S_no integer, Name varchar(50) NOT NULL, Admission_no varchar(15) PRIMARY KEY, Enrollment_no BIGINT UNIQUE, Mobile_no BIGINT, emailid varchar(25), DOB date);
create table BTDS(S_no integer, Name varchar(50) NOT NULL, Admission_no varchar(15) PRIMARY KEY, Enrollment_no BIGINT UNIQUE, Mobile_no BIGINT, emailid varchar(25), DOB date);
create table BTCYBERS(S_no integer, Name varchar(50) NOT NULL, Admission_no varchar(15) PRIMARY KEY, Enrollment_no BIGINT UNIQUE, Mobile_no BIGINT, emailid varchar(25), DOB date);

desc BTAIML;
desc BTDS;
desc BTCYBERS;
create table admin(name varchar(20), password varchar(15));
show tables;

CREATE TABLE books (
    name VARCHAR(255) PRIMARY KEY,
    quantity INT
);

INSERT INTO books (name, quantity) VALUES
('The Great Gatsby', 10),
('Moby Dick', 10),
('Pride and Prejudice', 10),
('To Kill a Mockingbird', 10),
('1984', 10),
('The Catcher in the Rye', 10),
('The Hobbit', 10),
('Little Women', 10),
('Jane Eyre', 10),
('The Odyssey', 10);
Select * from books;
CREATE TABLE attendance_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    admission_no VARCHAR(15),
    student_name VARCHAR(50),
    date DATE,
    status ENUM('Present', 'Absent'),
    FOREIGN KEY (admission_no) REFERENCES BTAIML(Admission_no) ON DELETE CASCADE
);
select *  from attendance_records;
CREATE TABLE attendance_metadata (
    id INT AUTO_INCREMENT PRIMARY KEY,
    total_classes INT DEFAULT 0
);
