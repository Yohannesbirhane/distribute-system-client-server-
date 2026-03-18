-- Database Initialization Script

-- 1. Create database (Run this first, then connect to it to run the rest)
CREATE DATABASE university_da;

-- 2. Connect to the university_da before running the following commands
\c university_da

-- 3. Create the students table
DROP TABLE IF EXISTS students;
CREATE TABLE students (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sex VARCHAR(10) NOT NULL,
    age INT NOT NULL,
    department VARCHAR(100) NOT NULL
);

-- 4. Insert sample data
INSERT INTO students (name, sex, age, department) VALUES 
('Alice Smith', 'Female', 20, 'Computer Science'),
('Bob Johnson', 'Male', 22, 'Mechanical Engineering'),
('Charlie Brown', 'Male', 21, 'Electrical Engineering'),
('Diana Prince', 'Female', 23, 'Information Systems'),
('Eve Davis', 'Female', 19, 'Software Engineering'),
('Frank Miller', 'Male', 24, 'Civil Engineering'),
('Grace Hopper', 'Female', 21, 'Computer Science'),
('Hank Pym', 'Male', 22, 'Physics'),
('Iris West', 'Female', 20, 'Journalism'),
('John Stewart', 'Male', 25, 'Architecture');
