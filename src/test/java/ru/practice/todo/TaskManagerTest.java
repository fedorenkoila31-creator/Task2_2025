package ru.practice.todo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskManagerTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void savesAndLoadsTasksBetweenInstances() throws IOException {
        Path file = temporaryDirectory.resolve("tasks.json");
        TaskManager first = new TaskManager(file);
        first.add("  Купить хлеб  ");

        TaskManager second = new TaskManager(file);

        assertEquals(1, second.getAll().size());
        assertEquals("Купить хлеб", second.getAll().get(0).getTitle());
        assertEquals(1, second.getAll().get(0).getId());
    }

    @Test
    void editsAndDeletesTasks() throws IOException {
        TaskManager manager = new TaskManager(temporaryDirectory.resolve("tasks.json"));
        Task task = manager.add("Первая задача");

        assertTrue(manager.edit(task.getId(), "Обновлённая задача"));
        assertEquals("Обновлённая задача", manager.findById(task.getId()).orElseThrow().getTitle());
        assertTrue(manager.delete(task.getId()));
        assertTrue(manager.getAll().isEmpty());
        assertFalse(manager.delete(task.getId()));
    }

    @Test
    void rejectsEmptyTaskTitle() throws IOException {
        TaskManager manager = new TaskManager(temporaryDirectory.resolve("tasks.json"));

        assertThrows(IllegalArgumentException.class, () -> manager.add("   "));
    }

    @Test
    void reportsMalformedJsonInsteadOfStartingWithEmptyList() throws IOException {
        Path file = temporaryDirectory.resolve("tasks.json");
        java.nio.file.Files.writeString(file, "not json");

        assertThrows(IOException.class, () -> new TaskManager(file));
    }
}
