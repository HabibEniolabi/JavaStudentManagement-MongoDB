package com.student;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

import static com.mongodb.client.model.Indexes.ascending;

//Read
import com.mongodb.client.FindIterable;
import static com.mongodb.client.model.Filters.eq;

//Update
import com.mongodb.client.result.UpdateResult;
import static com.mongodb.client.model.Updates.set;

//Delete
import com.mongodb.client.result.DeleteResult;

public class StudentCRUD {

  private static final String CONNECTION_STRING = "mongodb://127.0.0.1:27017";

  private static final String DATABASE_NAME = "student_db";
  private static final String COLLECTION_NAME = "students";

  public static void main(String[] args) {

    try (MongoClient mongoClient = MongoClients.create(CONNECTION_STRING)) {

      MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);

      MongoCollection<Document> students = database.getCollection(COLLECTION_NAME);

      // Ensure each student ID is unique.
      students.createIndex(
          ascending("student_id"),
          new IndexOptions().unique(true));

      getAllStudents(students);
      getStudentById(students, "STU001");
      getStudentsByDepartment(students, "Computer Science");
      updateStudent(students, "STU002", "Economics");
      getStudentById(students, "STU002");
      deleteStudent(students, "STU003");
      getAllStudents(students);

    } catch (Exception exception) {
      System.err.println(
          "Error: " + exception.getMessage());
    }
  }

  public static void createStudent(
      MongoCollection<Document> students) {

    Document scores = new Document()
        .append("mathematics", 80)
        .append("english", 75)
        .append("programming", 90)
        .append("database_systems", 85)
        .append("statistics", 70);

    int totalScore = 80 + 75 + 90 + 85 + 70;
    double averageScore = totalScore / 5.0;

    Document student = new Document()
        .append("student_id", "STU001")
        .append("first_name", "Adebayo")
        .append("last_name", "Johnson")
        .append("age", 21)
        .append("gender", "Male")
        .append("department", "Computer Science")
        .append("level", 300)
        .append("state_of_origin", "Lagos")
        .append("scores", scores)
        .append("total_score", totalScore)
        .append("average_score", averageScore);

    students.insertOne(student);

    System.out.println("Student created successfully!");
    System.out.println(student.toJson());
  }

  public static void createMoreStudents(
      MongoCollection<Document> students) {

    Document student2 = new Document()
        .append("student_id", "STU002")
        .append("first_name", "Mary")
        .append("last_name", "Okafor")
        .append("age", 22)
        .append("gender", "Female")
        .append("department", "Accounting")
        .append("level", 200)
        .append("state_of_origin", "Anambra")
        .append("scores", new Document()
            .append("mathematics", 70)
            .append("english", 80)
            .append("programming", 65)
            .append("database_systems", 75)
            .append("statistics", 60))
        .append("total_score", 350)
        .append("average_score", 70.0);

    Document student3 = new Document()
        .append("student_id", "STU003")
        .append("first_name", "Ibrahim")
        .append("last_name", "Musa")
        .append("age", 20)
        .append("gender", "Male")
        .append("department", "Computer Science")
        .append("level", 300)
        .append("state_of_origin", "Kano")
        .append("scores", new Document()
            .append("mathematics", 85)
            .append("english", 75)
            .append("programming", 95)
            .append("database_systems", 90)
            .append("statistics", 80))
        .append("total_score", 425)
        .append("average_score", 85.0);

    students.insertMany(
        java.util.Arrays.asList(student2, student3));

    System.out.println("Two additional students inserted!");
  }

  public static void getAllStudents(
      MongoCollection<Document> students) {

    System.out.println("All Student Records:");

    FindIterable<Document> studentRecords = students.find();

    for (Document student : studentRecords) {
      System.out.println(student.toJson());
    }
  }

  public static void getStudentById(
      MongoCollection<Document> students,
      String studentId) {

    Document student = students
        .find(eq("student_id", studentId))
        .first();

    if (student != null) {
      System.out.println("Student Found:");
      System.out.println(student.toJson());
    } else {
      System.out.println("Student not found: " + studentId);
    }
  }

  public static void getStudentsByDepartment(
      MongoCollection<Document> students,
      String department) {

    System.out.println("Department: " + department);

    FindIterable<Document> studentRecords = students.find(
        eq("department", department));

    for (Document student : studentRecords) {
      System.out.println(student.toJson());
    }
  }

  public static void updateStudent(
      MongoCollection<Document> students,
      String studentId,
      String newDepartment) {

    UpdateResult result = students.updateOne(
        eq("student_id", studentId),
        set("department", newDepartment));

    if (result.getMatchedCount() == 0) {
      System.out.println("Student not found.");
    } else if (result.getModifiedCount() == 0) {
      System.out.println("Student already has this department.");
    } else {
      System.out.println("Student updated successfully!");
    }
  }

  public static void deleteStudent(
      MongoCollection<Document> students,
      String studentId) {

    DeleteResult result = students.deleteOne(
        eq("student_id", studentId));

    if (result.getDeletedCount() > 0) {
      System.out.println("Student deleted successfully!");
    } else {
      System.out.println("Student not found.");
    }
  }
}