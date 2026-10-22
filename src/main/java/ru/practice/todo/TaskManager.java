package ru.practice.todo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TaskManager {

    private final Path dataFile;
    private final ObjectMapper objectMapper;
    private final List<Task> tasks = new ArrayList<>();
    private int nextId = 1;

    public TaskManager(Path dataFile) throws IOException {
        this.dataFile = dataFile;
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        load();
    }

    public List<Task> getAll() {
        return List.copyOf(tasks);
    }

    public Task add(String title) throws IOException {
        String validTitle = validateTitle(title);
        Task task = new Task(nextId++, validTitle);
        tasks.add(task);
        save();
        return task;
    }

    public boolean edit(int id, String title) throws IOException {
        String validTitle = validateTitle(title);
        Optional<Task> task = findById(id);
        if (task.isEmpty()) {
            return false;
        }
        task.get().setTitle(validTitle);
        save();
        return true;
    }

    public boolean delete(int id) throws IOException {
        boolean removed = tasks.removeIf(task -> task.getId() == id);
        if (removed) {
            save();
        }
        return removed;
    }

    public Optional<Task> findById(int id) {
        return tasks.stream().filter(task -> task.getId() == id).findFirst();
    }

    private String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Название задачи не может быть пустым.");
        }
        String normalized = title.trim();
        if (normalized.length() > 200) {
            throw new IllegalArgumentException("Название задачи не должно превышать 200 символов.");
        }
        return normalized;
    }

    private void load() throws IOException {
        if (!Files.exists(dataFile)) {
            return;
        }
        if (Files.size(dataFile) == 0) {
            return;
        }
        try {
            List<Task> loaded = objectMapper.readValue(dataFile.toFile(), new TypeReference<>() { });
            if (loaded == null) {
                return;
            }
            for (Task task : loaded) {
                if (task != null && task.getId() > 0 && task.getTitle() != null && !task.getTitle().isBlank()) {
                    tasks.add(new Task(task.getId(), task.getTitle().trim()));
                }
            }
            nextId = tasks.stream().map(Task::getId).max(Comparator.naturalOrder()).orElse(0) + 1;
        } catch (IOException exception) {
            throw new IOException("Не удалось прочитать файл задач " + dataFile.toAbsolutePath()
                    + ". Проверьте, что это корректный JSON.", exception);
        }
    }

    private void save() throws IOException {
        Path absolutePath = dataFile.toAbsolutePath();
        Path parent = absolutePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temporaryFile = Files.createTempFile(parent, "tasks-", ".json.tmp");
        try {
            objectMapper.writeValue(temporaryFile.toFile(), tasks);
            try {
                Files.move(temporaryFile, absolutePath, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, absolutePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }
}
