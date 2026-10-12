
package com.student;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

public class StudentAnalytics {

  private static final String URI = "mongodb://127.0.0.1:27017";

  // Converts MongoDB pipeline stages into Java Document objects.
  private static List<Document> pipeline(String... stages) {
    return Arrays.stream(stages)
        .map(Document::parse)
        .toList();
  }

  // Executes an aggregation pipeline and prints its results.
  private static void run(
      MongoCollection<Document> students,
      int number,
      String title,
      List<Document> stages) {

    System.out.println("\n==================================");
    System.out.println(number + ". " + title);
    System.out.println("==================================");

    students.aggregate(stages).forEach(
        result -> System.out.println(result.toJson()));
  }

  public static void main(String[] args) {

    try (MongoClient client = MongoClients.create(URI)) {

      MongoCollection<Document> students = client
          .getDatabase("student_db")
          .getCollection("students");

      long count = students.countDocuments();

      System.out.println("Connected to student_db");
      System.out.println("Student records: " + count);

      if (count != 200) {
        System.err.println(
            "Warning: Expected 200 student records.");
      }

      // 1. Total number of students
      run(students, 1, "Total Students", pipeline(
          "{ $count: 'total_students' }"));

      // 2. Department analysis
      run(students, 2, "Students by Department", pipeline(
          "{ $group: { _id: '$department', count: { $sum: 1 } } }",
          "{ $sort: { count: -1, _id: 1 } }"));

      // 3. Gender analysis
      run(students, 3, "Gender Distribution", pipeline(
          "{ $group: { _id: '$gender', count: { $sum: 1 } } }",
          "{ $sort: { _id: 1 } }"));

      // 4. Average score by department
      run(students, 4, "Department Average Scores", pipeline(
          "{ $group: { _id: '$department', average_score: { $avg: '$average_score' } } }",
          "{ $sort: { average_score: -1 } }"));

      // 5. Top 10 students
      run(students, 5, "Highest Performing Students", pipeline(
          "{ $sort: { average_score: -1, student_id: 1 } }",
          "{ $limit: 10 }",
          "{ $project: { _id: 0, student_id: 1, first_name: 1, last_name: 1, department: 1, average_score: 1 } }"));

      // 6. Bottom 10 students
      run(students, 6, "Lowest Performing Students", pipeline(
          "{ $sort: { average_score: 1, student_id: 1 } }",
          "{ $limit: 10 }",
          "{ $project: { _id: 0, student_id: 1, first_name: 1, last_name: 1, department: 1, average_score: 1 } }"));

      // 7. Age distribution (each individual age)
      run(students, 7, "Age Distribution", pipeline(
          "{ $group: { _id: '$age', count: { $sum: 1 } } }",
          "{ $sort: { _id: 1 } }"));

      // 8. Academic level distribution
      run(students, 8, "Level Distribution", pipeline(
          "{ $group: { _id: '$level', count: { $sum: 1 } } }",
          "{ $sort: { _id: 1 } }"));

      // 9. Performance classification
      run(students, 9, "Performance Classification", pipeline(
          """
              { $project: {
                  category: {
                      $switch: {
                          branches: [
                              { case: { $gte: ['$average_score', 70] }, then: 'Excellent' },
                              { case: { $gte: ['$average_score', 60] }, then: 'Good' },
                              { case: { $gte: ['$average_score', 50] }, then: 'Average' }
                          ],
                          default: 'Poor'
                      }
                  }
              } }
              """,
          "{ $group: { _id: '$category', count: { $sum: 1 } } }",
          "{ $sort: { count: -1 } }"));

      // 10. Average performance in each subject
      run(students, 10, "Subject Performance", pipeline(
          """
              { $group: {
                  _id: null,
                  mathematics: { $avg: '$scores.mathematics' },
                  english: { $avg: '$scores.english' },
                  programming: { $avg: '$scores.programming' },
                  database_systems: { $avg: '$scores.database_systems' },
                  statistics: { $avg: '$scores.statistics' }
              } }
              """,
          "{ $project: { _id: 0 } }"));

      // 11. Department ranking
      run(students, 11, "Department Ranking", pipeline(
          "{ $group: { _id: '$department', average_score: { $avg: '$average_score' } } }",
          "{ $sort: { average_score: -1, _id: 1 } }",
          """
              { $setWindowFields: {
                  sortBy: { average_score: -1, _id: 1 },
                  output: { rank: { $documentNumber: {} } }
              } }
              """,
          "{ $project: { _id: 0, department: '$_id', average_score: 1, rank: 1 } }",
          "{ $sort: { rank: 1 } }"));

      // 12. State of origin analysis
      run(students, 12, "State of Origin Distribution", pipeline(
          "{ $group: { _id: '$state_of_origin', count: { $sum: 1 } } }",
          "{ $sort: { count: -1, _id: 1 } }"));

      // 13. Pass/fail counts and percentages
      run(students, 13, "Pass and Fail Analysis", pipeline(
          """
              { $group: {
                  _id: null,
                  total: { $sum: 1 },
                  passed: {
                      $sum: { $cond: [{ $gte: ['$average_score', 50] }, 1, 0] }
                  },
                  failed: {
                      $sum: { $cond: [{ $lt: ['$average_score', 50] }, 1, 0] }
                  }
              } }
              """,
          """
              { $project: {
                  _id: 0,
                  total: 1,
                  passed: 1,
                  failed: 1,
                  pass_percentage: {
                      $round: [{ $multiply: [{ $divide: ['$passed', '$total'] }, 100] }, 2]
                  },
                  fail_percentage: {
                      $round: [{ $multiply: [{ $divide: ['$failed', '$total'] }, 100] }, 2]
                  }
              } }
              """));

      // 14. Students above the overall average
      run(students, 14, "Above-Average Students", pipeline(
          """
              { $group: {
                  _id: null,
                  overall_average: { $avg: '$average_score' },
                  records: { $push: '$$ROOT' }
              } }
              """,
          "{ $unwind: '$records' }",
          "{ $match: { $expr: { $gt: ['$records.average_score', '$overall_average'] } } }",
          """
              { $project: {
                  _id: 0,
                  student_id: '$records.student_id',
                  first_name: '$records.first_name',
                  last_name: '$records.last_name',
                  average_score: '$records.average_score',
                  overall_average: 1
              } }
              """,
          "{ $sort: { average_score: -1, student_id: 1 } }"));

      // 15. Department and academic level analysis
      run(students, 15, "Department and Level Analysis", pipeline(
          """
              { $group: {
                  _id: { department: '$department', level: '$level' },
                  student_count: { $sum: 1 },
                  average_score: { $avg: '$average_score' }
              } }
              """,
          "{ $sort: { '_id.department': 1, '_id.level': 1 } }"));

      System.out.println("\nAll 15 analytics completed.");

    } catch (Exception exception) {
      System.err.println(
          "Analytics error: " + exception.getMessage());
      exception.printStackTrace();
    }
  }
}
