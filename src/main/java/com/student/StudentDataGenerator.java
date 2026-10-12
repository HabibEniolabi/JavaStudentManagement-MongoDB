package com.student;

import org.bson.Document;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StudentDataGenerator {

  private static final String[] FIRST_NAMES = {
      "Adebayo", "Mary", "Ibrahim", "Chinedu", "Fatima",
      "Grace", "Samuel", "Zainab", "Tunde", "Blessing"
  };

  private static final String[] LAST_NAMES = {
      "Johnson", "Okafor", "Musa", "Adeyemi", "Bello",
      "Eze", "Lawal", "Ahmed", "Obi", "Yusuf"
  };

  private static final String[] DEPARTMENTS = {
      "Computer Science", "Accounting", "Economics",
      "Engineering", "Business Administration"
  };

  private static final String[] STATES = {
      "Lagos", "Ogun", "Oyo", "Kano", "Kaduna",
      "Anambra", "Enugu", "Rivers", "Osun", "Kwara"
  };

  private static final int[] LEVELS = {
      100, 200, 300, 400
  };

  public static void main(String[] args) throws Exception {

    Random random = new Random(42);
    List<String> records = new ArrayList<>();

    for (int i = 1; i <= 200; i++) {

      int mathematics = generateScore(random);
      int english = generateScore(random);
      int programming = generateScore(random);
      int databaseSystems = generateScore(random);
      int statistics = generateScore(random);

      int total = mathematics + english + programming
          + databaseSystems + statistics;

      double average = total / 5.0;

      Document scores = new Document()
          .append("mathematics", mathematics)
          .append("english", english)
          .append("programming", programming)
          .append("database_systems", databaseSystems)
          .append("statistics", statistics);

      Document student = new Document()
          .append("student_id", String.format("STU%03d", i))
          .append("first_name", choose(random, FIRST_NAMES))
          .append("last_name", choose(random, LAST_NAMES))
          .append("age", 17 + random.nextInt(12))
          .append("gender", random.nextBoolean() ? "Male" : "Female")
          .append("department", choose(random, DEPARTMENTS))
          .append("level", LEVELS[random.nextInt(LEVELS.length)])
          .append("state_of_origin", choose(random, STATES))
          .append("scores", scores)
          .append("total_score", total)
          .append("average_score", average);

      records.add(student.toJson());
    }

    Files.createDirectories(Path.of("data"));

    String json = "[\n" + String.join(",\n", records) + "\n]";

    Files.writeString(Path.of("data/students.json"), json);

    System.out.println("Successfully generated 200 student records.");
    System.out.println("File saved to data/students.json");
  }

  private static int generateScore(Random random) {
    return 35 + random.nextInt(66);
  }

  private static String choose(Random random, String[] values) {
    return values[random.nextInt(values.length)];
  }
}