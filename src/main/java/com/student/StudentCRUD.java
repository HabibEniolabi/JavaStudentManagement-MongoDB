
package com.student;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;

import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.result.DeleteResult;

import org.bson.Document;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Updates.set;

public class StudentCRUD {

  private static final String CONNECTION_STRING = "mongodb://127.0.0.1:27017";

  private static final String DATABASE_NAME = "student_db";
  private static final String COLLECTION_NAME = "students";

  private static final String JSON_FILE = "data/students.json";

  public static void main(String[] args) {

    try (MongoClient mongoClient = MongoClients.create(CONNECTION_STRING)) {

      MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);

      database.runCommand(new Document("ping", 1));

      MongoCollection<Document> students = database.getCollection(COLLECTION_NAME);

      students.createIndex(
          ascending("student_id"),
          new IndexOptions().unique(true));

      if (args.length == 0) {
        showUsage();
        return;
      }

      String command = args[0].toLowerCase();

      switch (command) {

        case "create":
          requireArguments(args, 2);
          createStudent(students, args[1]);
          break;

        case "read":
          requireArguments(args, 2);
          getStudentById(students, args[1]);
          break;

        case "list":
          getAllStudents(students);
          break;

        case "department":
          requireArguments(args, 2);
          getStudentsByDepartment(students, args[1]);
          break;

        case "update":
          requireArguments(args, 3);
          updateStudent(students, args[1], args[2]);
          break;

        case "delete":
          requireArguments(args, 2);
          deleteStudent(students, args[1]);
          break;

        default:
          showUsage();
      }

    } catch (Exception exception) {
      System.err.println(
          "Error: " + exception.getMessage());
    }
  }

  // CREATE: Read a student from JSON, then insert into MongoDB.

  public static void createStudent(
      MongoCollection<Document> students,
      String studentId) throws Exception {

    if (students.find(
        eq("student_id", studentId)).first() != null) {

      System.out.println(
          "Student already exists: " + studentId);
      return;
    }

    String json = Files.readString(
        Path.of(JSON_FILE));

    // Read the JSON array containing 200 students.
    List<Document> studentRecords = Document.parse("{\"records\":" + json + "}")
        .getList("records", Document.class);

    Document selectedStudent = null;

    for (Document student : studentRecords) {

      if (studentId.equals(
          student.getString("student_id"))) {

        selectedStudent = student;
        break;
      }
    }

    if (selectedStudent == null) {
      System.out.println(
          "Student ID not found in JSON: " + studentId);
      return;
    }

    InsertOneResult result = students.insertOne(selectedStudent);

    System.out.println("Student inserted successfully!");
    System.out.println(
        "MongoDB ID: " + result.getInsertedId());
  }

  // READ: Retrieve every student.

  public static void getAllStudents(
      MongoCollection<Document> students) {

    FindIterable<Document> records = students.find().sort(ascending("student_id"));

    int count = 0;

    for (Document student : records) {
      printStudent(student);
      count++;
    }

    System.out.println("Total students displayed: " + count);
  }

  // READ: Retrieve a student by student ID.

  public static void getStudentById(
      MongoCollection<Document> students,
      String studentId) {

    Document student = students
        .find(eq("student_id", studentId))
        .first();

    if (student == null) {
      System.out.println("Student not found: " + studentId);
      return;
    }

    printStudent(student);
  }

  // READ: Retrieve students belonging to one department.

  public static void getStudentsByDepartment(
      MongoCollection<Document> students,
      String department) {

    FindIterable<Document> records = students
        .find(eq("department", department))
        .sort(ascending("student_id"));

    int count = 0;

    for (Document student : records) {
      printStudent(student);
      count++;
    }

    System.out.println(
        "Students in " + department + ": " + count);
  }

  // UPDATE: Modify an existing student's department.

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
      System.out.println(
          "Student already has this department.");
    } else {
      System.out.println("Student updated successfully!");
    }
  }

  // DELETE: Remove a student by student ID.

  public static void deleteStudent(
      MongoCollection<Document> students,
      String studentId) {

    DeleteResult result = students.deleteOne(
        eq("student_id", studentId));

    if (result.getDeletedCount() > 0) {
      System.out.println(
          "Student deleted successfully: " + studentId);
    } else {
      System.out.println("Student not found.");
    }
  }

  // Display a student in a readable format.

  private static void printStudent(Document student) {

    System.out.println("------------------------------");
    System.out.println(
        "ID: " + student.getString("student_id"));
    System.out.println(
        "Name: " + student.getString("first_name")
            + " " + student.getString("last_name"));
    System.out.println(
        "Department: " + student.getString("department"));
    System.out.println(
        "Average Score: " + student.get("average_score"));
  }

  // Ensure a command receives enough arguments.

  private static void requireArguments(
      String[] args,
      int expected) {

    if (args.length < expected) {
      throw new IllegalArgumentException(
          "Missing arguments. See available commands.");
    }
  }

  // Display supported commands.

  private static void showUsage() {

    System.out.println("Student CRUD Commands:");
    System.out.println("create <student_id>");
    System.out.println("read <student_id>");
    System.out.println("list");
    System.out.println("department <department>");
    System.out.println("update <student_id> <department>");
    System.out.println("delete <student_id>");
  }
}
