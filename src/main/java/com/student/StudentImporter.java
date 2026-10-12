package com.student;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReplaceOptions;

import org.bson.Document;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Indexes.ascending;

public class StudentImporter {

  public static void main(String[] args) throws Exception {

    String json = Files.readString(
        Path.of("data/students.json"));

    BsonArray records = BsonArray.parse(json);

    if (records.size() != 200) {
      throw new IllegalStateException(
          "Expected exactly 200 records.");
    }

    Set<String> studentIds = new HashSet<>();

    for (BsonValue value : records) {

      BsonDocument student = value.asDocument();

      String studentId = student.getString("student_id").getValue();

      if (!studentIds.add(studentId)) {
        throw new IllegalStateException(
            "Duplicate student ID: " + studentId);
      }

      BsonDocument scores = student.getDocument("scores");

      int total = 0;

      String[] subjects = {
          "mathematics", "english", "programming",
          "database_systems", "statistics"
      };

      for (String subject : subjects) {
        int score = scores.getInt32(subject).getValue();

        if (score < 0 || score > 100) {
          throw new IllegalStateException(
              "Invalid score: " + studentId);
        }

        total += score;
      }

      double average = total / 5.0;

      if (student.getInt32("total_score").getValue() != total ||
          Math.abs(
              student.getDouble("average_score").getValue()
                  - average) > 0.000001) {

        throw new IllegalStateException(
            "Score calculation mismatch: " + studentId);
      }
    }

    try (MongoClient client = MongoClients.create("mongodb://127.0.0.1:27017")) {

      MongoDatabase database = client.getDatabase("student_db");

      MongoCollection<Document> students = database.getCollection("students");

      // Prevent duplicate student IDs at database level.
      students.createIndex(
          ascending("student_id"),
          new IndexOptions().unique(true));

      ReplaceOptions options = new ReplaceOptions().upsert(true);

      for (BsonValue value : records) {

        Document student = Document.parse(value.asDocument().toJson());

        students.replaceOne(
            eq("student_id", student.getString("student_id")),
            student,
            options);
      }

      long count = students.countDocuments();

      System.out.println("Import completed.");
      System.out.println("Total records in collection: " + count);

      if (count != 200) {
        System.out.println(
            "WARNING: Collection contains extra records. " +
                "Check previously inserted CRUD test students.");
      }
    }
  }
}