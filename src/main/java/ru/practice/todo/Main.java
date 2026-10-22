package ru.practice.todo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {

    private final Scanner scanner;
    private final TaskManager taskManager;

    private Main(Scanner scanner, TaskManager taskManager) {
        this.scanner = scanner;
        this.taskManager = taskManager;
    }

    public static void main(String[] args) {
        Path dataFile = args.length > 0 ? Path.of(args[0]) : Path.of("data", "tasks.json");
        try (Scanner scanner = new Scanner(System.in, "UTF-8")) {
            TaskManager taskManager = new TaskManager(dataFile);
            new Main(scanner, taskManager).run();
        } catch (IOException exception) {
            System.err.println("Ошибка работы с файлом задач: " + exception.getMessage());
        }
    }

    private void run() {
        System.out.println("Добро пожаловать в ToDo List!");
        while (true) {
            printMenu();
            String choice = readLine("Выберите пункт: ");
            try {
                switch (choice) {
                    case "1" -> showTasks();
                    case "2" -> addTask();
                    case "3" -> editTask();
                    case "4" -> deleteTask();
                    case "0" -> {
                        System.out.println("Задачи сохранены. До свидания!");
                        return;
                    }
                    default -> System.out.println("Некорректный пункт меню. Введите число от 0 до 4.");
                }
            } catch (IOException exception) {
                System.out.println("Не удалось сохранить задачи: " + exception.getMessage());
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("1. Показать задачи");
        System.out.println("2. Добавить задачу");
        System.out.println("3. Редактировать задачу");
        System.out.println("4. Удалить задачу");
        System.out.println("0. Выход");
    }

    private void showTasks() {
        List<Task> tasks = taskManager.getAll();
        if (tasks.isEmpty()) {
            System.out.println("Список задач пуст.");
            return;
        }
        System.out.println("Список задач:");
        for (Task task : tasks) {
            System.out.printf("%d. %s%n", task.getId(), task.getTitle());
        }
    }

    private void addTask() throws IOException {
        String title = readLine("Введите название задачи: ");
        try {
            Task task = taskManager.add(title);
            System.out.println("Задача добавлена: " + task.getTitle());
        } catch (IllegalArgumentException exception) {
            System.out.println("Некорректные данные: " + exception.getMessage());
        }
    }

    private void editTask() throws IOException {
        Integer id = readId("Введите номер задачи для редактирования: ");
        if (id == null) {
            return;
        }
        if (taskManager.findById(id).isEmpty()) {
            System.out.println("Задача с таким номером не найдена.");
            return;
        }
        String title = readLine("Введите новое название: ");
        try {
            taskManager.edit(id, title);
            System.out.println("Задача обновлена.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Некорректные данные: " + exception.getMessage());
        }
    }

    private void deleteTask() throws IOException {
        Integer id = readId("Введите номер задачи для удаления: ");
        if (id == null) {
            return;
        }
        if (taskManager.delete(id)) {
            System.out.println("Задача удалена.");
        } else {
            System.out.println("Задача с таким номером не найдена.");
        }
    }

    private Integer readId(String prompt) {
        String value = readLine(prompt);
        try {
            int id = Integer.parseInt(value);
            if (id <= 0) {
                System.out.println("Номер задачи должен быть положительным числом.");
                return null;
            }
            return id;
        } catch (NumberFormatException exception) {
            System.out.println("Некорректный номер. Введите целое положительное число.");
            return null;
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}
