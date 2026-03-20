package com.notes.notes_manager;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotesController {

    private static final Path NOTES_DIR = Path.of(
        System.getProperty("user.home"), ".notes", "notes"
    );

    @GetMapping("/api/notes")
    public List<String> listNotes() throws Exception {
        try (Stream<Path> paths = Files.walk(NOTES_DIR, 1)) {
            return paths
                .filter(Files::isRegularFile)
                .map(p -> p.getFileName().toString())
                .sorted()
                .toList();
        }

    }

    @GetMapping("/api/notes/meta")
public List<java.util.Map<String, String>> getNotesMeta() throws Exception {
    try (Stream<Path> paths = Files.walk(NOTES_DIR, 1)) {
        return paths
            .filter(Files::isRegularFile)
            .filter(p -> {
                String name = p.getFileName().toString();
                return name.endsWith(".md") || name.endsWith(".note") || name.endsWith(".txt");
            })
            .sorted()
            .map(p -> {
                java.util.Map<String, String> meta = new java.util.HashMap<>();
                meta.put("file", p.getFileName().toString());
                meta.put("title", p.getFileName().toString());
                meta.put("priority", "none");
                try {
                    List<String> lines = Files.readAllLines(p);
                    for (String line : lines) {
                        if (line.startsWith("title:")) meta.put("title", line.substring(6).trim());
                        if (line.startsWith("priority:")) meta.put("priority", line.substring(9).trim());
                    }
                } catch (Exception e) {}
                return meta;
            })
            .toList();
    }
}

    

    @GetMapping("/api/search")
public List<String> searchNotes(@RequestParam String q) throws Exception {
    try (Stream<Path> paths = Files.walk(NOTES_DIR, 1)) {
        return paths
            .filter(Files::isRegularFile)
            .filter(p -> {
                try {
                    return Files.readString(p).toLowerCase()
                        .contains(q.toLowerCase());
                } catch (Exception e) {
                    return false;
                }
            })
            .map(p -> p.getFileName().toString())
            .sorted()
            .toList();
    }
}

  @GetMapping("/api/notes/{id}")
public String readNote(@PathVariable String id) throws Exception {
    Path filePath = NOTES_DIR.resolve(id);
    if (!Files.exists(filePath)) {
        return "Note not found: " + id;
    }
    return Files.readString(filePath);
}

  @PostMapping("/api/notes")
public String createNote(@RequestBody java.util.Map<String, String> body) throws Exception {
    String title = body.get("title");
    String content = body.get("content");
    String priority = body.getOrDefault("priority", "none");

    // Create filename from title
    String filename = title.toLowerCase()
        .replaceAll(" ", "-")
        .replaceAll("[^a-z0-9-]", "") + ".note";

    // Build note content
    String now = java.time.Instant.now().toString();
    String noteContent = "---\n" +
        "title: " + title + "\n" +
        "author: web\n" +
        "created: " + now + "\n" +
        "modified: " + now + "\n" +
        "priority: " + priority + "\n" +
        "tags: []\n" +
        "---\n\n" +
        content + "\n";

    // Save the file
    Path filePath = NOTES_DIR.resolve(filename);
    Files.writeString(filePath, noteContent);

    return "Note created: " + filename;
}

@DeleteMapping("/api/notes/{id}")
public String deleteNote(@PathVariable String id) throws Exception {
    Path filePath = NOTES_DIR.resolve(id);
    if (!Files.exists(filePath)) {
        return "Note not found: " + id;
    }
    Files.delete(filePath);
    return "Note deleted: " + id;
}
}
