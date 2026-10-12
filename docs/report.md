# Java and MongoDB Student Management and Data Analytics

## 1. Introduction

This project implements a student management and data analytics application using Java and MongoDB. The system provides CRUD operations, imports a dataset containing 200 student records, and performs 15 analytical queries using MongoDB aggregation pipelines.

A JavaFX graphical interface was also developed using FXML and CSS to demonstrate student management operations.

## 2. Technologies Used

- **Java 17:** Application development and data processing.
- **MongoDB:** NoSQL document database for storing student records.
- **MongoDB Java Driver:** Communication between the Java application and MongoDB.
- **Apache Maven:** Dependency management and project compilation.
- **JavaFX, FXML and CSS:** Graphical user interface.
- **MongoDB Compass and mongosh:** Database inspection and verification.
- **Git and GitHub:** Version control.

## 3. System Implementation

The application connects to the local MongoDB server through `mongodb://127.0.0.1:27017`.

The primary database is `student_db`, containing the `students` collection.

Student records include identification details, demographic information, academic information, five subject scores, total scores and average scores.

CRUD operations were implemented using `insertOne()`, `find()`, `updateOne()` and `deleteOne()`.

A Java data generator creates 200 synthetic student records and saves them in `students.json`. The importer validates uniqueness and score calculations before importing the records into MongoDB.

## 4. Data Analytics

Fifteen aggregation pipelines were implemented to analyze academic performance and student demographics.

### Finding 1 — Department Distribution

[Insert the department with the highest student population and its count.]

### Finding 2 — Average Score by Department

[Insert the highest-ranking department and its average score.]

### Finding 3 — Student Performance Classification

[Insert the number of Excellent, Good, Average and Poor students.]

### Finding 4 — Pass and Fail Rates

[Insert the number and percentage of students who passed and failed using the pass mark of 50.]

### Finding 5 — Subject Performance

[Insert the subject with the highest average score and the subject with the lowest average score.]

## 5. Testing and Verification

The project was tested through Java compilation, database connectivity checks, CRUD operation tests, JSON import validation and aggregation pipeline execution.

[Record the confirmed number of imported students, unique ID validation results and calculation consistency checks.]

Screenshots are included as evidence.

## 6. Conclusion

This project demonstrates how Java can connect to MongoDB to manage structured student records and perform data analytics.

MongoDB aggregation pipelines enabled statistical operations such as grouping, filtering, sorting, ranking and calculating averages directly within the database.

The JavaFX interface provides a visual demonstration of student management operations.
