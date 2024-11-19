package org.example;

import java.io.*;
import java.net.Socket;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;
    private static final int BLOCK_SIZE = 512;

    public static void main(String[] args) {
        String filePath = "C:\\Users\\Админ\\Desktop\\Новий Текстовий документ.txt";

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            System.out.println("Підключено до сервера.");

            // Читання файлу
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("Файл не знайдено.");
                return;
            }

            int fileSize = (int) file.length();
            byte[] fileData = new byte[fileSize];

            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(fileData);
            }

            // Отримання останнього блоку, з якого потрібно продовжити
            int lastBlockSent = in.readInt();
            System.out.println("Передача починається з блоку: " + lastBlockSent);

            // Відправка блоків
            int totalBlocks = (int) Math.ceil((double) fileSize / BLOCK_SIZE);
            for (int blockNumber = lastBlockSent; blockNumber < totalBlocks; blockNumber++) {
                int start = blockNumber * BLOCK_SIZE;
                int end = Math.min(start + BLOCK_SIZE, fileSize);
                byte[] block = new byte[end - start];
                System.arraycopy(fileData, start, block, 0, end - start);

                out.writeInt(blockNumber); // Номер блоку
                out.writeInt(block.length); // Розмір блоку
                out.write(block); // Дані блоку
                System.out.println("Відправлено блок " + blockNumber);

                // Чекаємо підтвердження від сервера
                String response = in.readUTF();
                if (!response.equals("BLOCK_RECEIVED:" + blockNumber)) {
                    System.err.println("Помилка підтвердження для блоку " + blockNumber);
                    blockNumber--; // Повторно відправити блок
                }
            }

            System.out.println("Передача завершена.");
        } catch (IOException e) {
            System.err.println("Помилка клієнта: " + e.getMessage());
        }
    }
}
