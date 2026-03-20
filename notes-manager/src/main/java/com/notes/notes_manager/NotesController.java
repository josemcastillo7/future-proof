package com.notes.notes_manager;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

public String createNote(@RequestBody java.util.Map<String, String> body) throws Exception {
    String title = body.get("title");
    String content = body.get("content");
    
    // Create filename from title
    String filename = title.toLowerCase()
        .replaceAll(" ", "-")
        .replaceAll("[^a-z0-9-]", "") + ".note";

    // Build note content with YAML header
    String noteContent = String.format("""
            ---
            title: %s
            author: web
            created: %s
            modified: %s
            tags: []
            ---

            %s
            """, title, java.time.Instant.now(), java.time.Instant.now(), content);

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
