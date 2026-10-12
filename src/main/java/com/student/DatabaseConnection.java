package com.student;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class DatabaseConnection {

    private static final String CONNECTION_STRING = "mongodb://127.0.0.1:27017";

    private static final String DATABASE_NAME = "student_db";

    public static void main(String[] args) {

        try (MongoClient mongoClient = MongoClients.create(CONNECTION_STRING)) {

            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);

            database.runCommand(new Document("ping", 1));

            System.out.println("MongoDB connected successfully!");
            System.out.println("Database: " + database.getName());

        } catch (Exception exception) {

            System.err.println(
                    "MongoDB connection failed: "
                            + exception.getMessage());
        }
    }
}