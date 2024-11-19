package org.example;

import java.io.*;
import java.net.Socket;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        // Вкажіть правильний шлях до файлу, який потрібно відправити
        String filePath = "C:\\Users\\Админ\\Desktop\\Новий Текстовий документ.txt";

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            System.out.println("Підключення до сервера...");

            // Читання файлу
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("Файл не знайдено: " + filePath);
                return;
            }

            String fileName = file.getName();
            int fileSize = (int) file.length();
            byte[] fileData = new byte[fileSize];

            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(fileData);
            }

            // Відправлення метаданих і файлу на сервер
            out.writeUTF(fileName);
            out.writeInt(fileSize);
            out.write(fileData);
            System.out.println("Файл відправлено на сервер.");

            // Отримання відповіді від сервера
            String serverResponse = in.readUTF();
            System.out.println("Відповідь сервера: " + serverResponse);

            if (serverResponse.contains("успішно")) {
                int receivedSize = in.readInt();
                byte[] receivedData = new byte[receivedSize];
                in.readFully(receivedData);

                // Збереження отриманого файлу
                try (FileOutputStream fos = new FileOutputStream("received_" + fileName)) {
                    fos.write(receivedData);
                    System.out.println("Файл отримано та збережено як 'received_" + fileName + "'.");
                }
            }
        } catch (IOException e) {
            System.err.println("Помилка клієнта: " + e.getMessage());
        }
    }
}
