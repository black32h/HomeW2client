package org.example;

import java.io.*;
import java.net.Socket;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        // Вкажіть шлях до файлу
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

            if (fileSize > 1024) {
                System.out.println("Файл занадто великий для відправки.");
                saveTransferStatus(fileName, fileSize, "Файл не задовольняє умовам");
                return;
            }

            byte[] fileData = new byte[fileSize];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(fileData);
            }

            // Відправлення метаданих і файлу
            out.writeUTF(fileName);
            out.writeInt(fileSize);
            out.write(fileData);

            // Отримання відповіді від сервера.
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
                saveTransferStatus(fileName, fileSize, "Успішно скачано та збережено");
            } else {
                saveTransferStatus(fileName, fileSize, "Файл не задовольняє умовам");
            }
        } catch (IOException e) {
            System.err.println("Помилка клієнта: " + e.getMessage());
        }
    }

    // Метод для збереження статусу передачі
    private static void saveTransferStatus(String fileName, int fileSize, String status) {
        String log = "Файл: " + fileName + ", Розмір: " + fileSize + " байт, Статус: " + status;
        System.out.println("Статус передачі: " + log);

        try (FileWriter writer = new FileWriter("transfer_status.txt", true)) {
            writer.write(log + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Помилка запису статусу: " + e.getMessage());
        }
    }
}
