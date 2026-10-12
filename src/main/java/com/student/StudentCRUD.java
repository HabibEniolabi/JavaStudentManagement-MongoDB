package com.student;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class StudentCRUD {

    private static final String CONNECTION_STRING =
            "mongodb://127.0.0.1:27017";

    private static final String DATABASE_NAME = "student_db";
    private static final String COLLECTION_NAME = "students";

    public static void main(String[] args) {

        try (MongoClient mongoClient =
                     MongoClients.create(CONNECTION_STRING)) {

            MongoDatabase database =
                    mongoClient.getDatabase(DATABASE_NAME);

            boolean collectionExists = false;

            for (String collectionName : database.listCollectionNames()) {
                if (collectionName.equals(COLLECTION_NAME)) {
                    collectionExists = true;
                    break;
                }
            }

            if (!collectionExists) {
                database.createCollection(COLLECTION_NAME);
                System.out.println("Students collection created successfully!");
            } else {
                System.out.println("Students collection already exists.");
            }

        } catch (Exception exception) {
            System.err.println("Error: " + exception.getMessage());
        }
    }
}