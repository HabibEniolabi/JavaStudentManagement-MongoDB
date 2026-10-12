package com.student;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class StudentController {

  @FXML
  private TableView<Document> studentTable;

  @FXML
  private TableColumn<Document, String> idColumn;
  @FXML
  private TableColumn<Document, String> nameColumn;
  @FXML
  private TableColumn<Document, String> departmentColumn;
  @FXML
  private TableColumn<Document, String> averageColumn;

  @FXML
  private TextField studentIdField;
  @FXML
  private TextField firstNameField;
  @FXML
  private TextField lastNameField;
  @FXML
  private TextField departmentField;

  @FXML
  private Label statusLabel;

  private MongoClient client;
  private MongoCollection<Document> students;

  @FXML
  public void initialize() {

    idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
        data.getValue().getString("student_id")));

    nameColumn.setCellValueFactory(data -> {
      Document student = data.getValue();

      return new javafx.beans.property.SimpleStringProperty(
          student.getString("first_name") + " " +
              student.getString("last_name"));
    });

    departmentColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
        data.getValue().getString("department")));

    averageColumn.setCellValueFactory(data -> {
      Number average = data.getValue().get("average_score", Number.class);

      return new javafx.beans.property.SimpleStringProperty(
          average == null ? "N/A" : average.toString());
    });

    try {
      client = MongoClients.create(
          "mongodb://127.0.0.1:27017");

      MongoDatabase database = client.getDatabase("student_db");

      database.runCommand(new Document("ping", 1));

      // Separate collection for CRUD demonstrations.
      students = database.getCollection("students");

      students.createIndex(
          ascending("student_id"),
          new IndexOptions().unique(true));

      studentTable.getSelectionModel()
          .selectedItemProperty()
          .addListener((obs, oldValue, student) -> {
            if (student != null) {
              studentIdField.setText(
                  student.getString("student_id"));
              firstNameField.setText(
                  student.getString("first_name"));
              lastNameField.setText(
                  student.getString("last_name"));
              departmentField.setText(
                  student.getString("department"));
            }
          });
      studentTable.setColumnResizePolicy(
          TableView.CONSTRAINED_RESIZE_POLICY);
      loadStudents();

    } catch (Exception ex) {
      showStatus("Connection error: " + ex.getMessage());
    }
  }

  @FXML
  public void loadStudents() {

    if (students == null)
      return;

    try {
      studentTable.setItems(
          FXCollections.observableArrayList(
              students.find().into(
                  new java.util.ArrayList<Document>())));

      showStatus("Student records loaded successfully.");

    } catch (Exception ex) {
      showStatus("Read error: " + ex.getMessage());
    }
  }

  @FXML
  public void createStudent() {

    if (students == null)
      return;

    String id = studentIdField.getText().trim();
    String firstName = firstNameField.getText().trim();
    String lastName = lastNameField.getText().trim();
    String department = departmentField.getText().trim();

    if (id.isEmpty() || firstName.isEmpty()
        || lastName.isEmpty() || department.isEmpty()) {

      showStatus("Please complete all fields.");
      return;
    }

    try {
      // Default scores are for the CRUD demo only.
      Document scores = new Document()
          .append("mathematics", 0)
          .append("english", 0)
          .append("programming", 0)
          .append("database_systems", 0)
          .append("statistics", 0);

      Document student = new Document()
          .append("student_id", id)
          .append("first_name", firstName)
          .append("last_name", lastName)
          .append("department", department)
          .append("age", 18)
          .append("gender", "Not specified")
          .append("level", 100)
          .append("state_of_origin", "Not specified")
          .append("scores", scores)
          .append("total_score", 0)
          .append("average_score", 0.0);

      students.insertOne(student);

      loadStudents();
      showStatus("Student created successfully.");

    } catch (Exception ex) {
      showStatus("Create error: " + ex.getMessage());
    }
  }

  @FXML
  public void updateStudent() {

    if (students == null)
      return;

    String id = studentIdField.getText().trim();

    if (id.isEmpty() ||
        firstNameField.getText().trim().isEmpty() ||
        lastNameField.getText().trim().isEmpty() ||
        departmentField.getText().trim().isEmpty()) {

      showStatus("Enter student ID, name and department.");
      return;
    }

    try {
      UpdateResult result = students.updateOne(
          eq("student_id", id),
          combine(
              set("first_name", firstNameField.getText().trim()),
              set("last_name", lastNameField.getText().trim()),
              set("department", departmentField.getText().trim())));

      loadStudents();

      showStatus(
          result.getMatchedCount() > 0
              ? "Student update processed successfully."
              : "Student not found.");

    } catch (Exception ex) {
      showStatus("Update error: " + ex.getMessage());
    }
  }

  @FXML
  public void deleteStudent() {

    if (students == null)
      return;

    String id = studentIdField.getText().trim();

    if (id.isEmpty()) {
      showStatus("Select or enter a student ID.");
      return;
    }

    Alert confirmation = new Alert(
        Alert.AlertType.CONFIRMATION,
        "Delete student " + id + "?",
        ButtonType.YES,
        ButtonType.NO);

    if (confirmation.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
      return;
    }

    try {
      DeleteResult result = students.deleteOne(
          eq("student_id", id));

      loadStudents();

      if (result.getDeletedCount() > 0) {
        studentIdField.clear();
        firstNameField.clear();
        lastNameField.clear();
        departmentField.clear();
        showStatus("Student deleted successfully.");
      } else {
        showStatus("Student not found.");
      }

    } catch (Exception ex) {
      showStatus("Delete error: " + ex.getMessage());
    }
  }

  private void showStatus(String message) {
    statusLabel.setText(message);
    System.out.println(message);
  }
}