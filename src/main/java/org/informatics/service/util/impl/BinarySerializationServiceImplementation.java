package org.informatics.service.util.impl;

import org.informatics.service.util.BinarySerializationService;

import java.io.*;

public class BinarySerializationServiceImplementation implements BinarySerializationService {

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Serializable> void serialize(String filename, T object) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(filename);
             ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream)) {
            outputStream.writeObject(object);
        } catch (IOException e) {
            throw new RuntimeException("Error during serialization of object: " + object, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Serializable> T deserialize(String filename, Class<T> targetClass) {
        try (FileInputStream fileInputStream = new FileInputStream(filename);
             ObjectInputStream inputStream = new ObjectInputStream(fileInputStream)) {

            Object obj = inputStream.readObject();
            // Dynamically verifies the deserialized object matches the target class safely
            return targetClass.cast(obj);

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error during deserialization from file: " + filename, e);
        } catch (ClassCastException e) {
            throw new RuntimeException("Data format corruption: File content does not match " + targetClass.getSimpleName(), e);
        }
    }
}
