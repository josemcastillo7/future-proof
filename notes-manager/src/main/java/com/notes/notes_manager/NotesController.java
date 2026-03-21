package com.notes.notes_manager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/api/auth/login")
public org.springframework.http.ResponseEntity<String> login(
        @RequestBody java.util.Map<String, String> body) throws Exception {
    Path credPath = Path.of(
        System.getProperty("user.home"), ".notes", "credentials.txt"
    );

    // Create default credentials if file doesn't exist
    if (!Files.exists(credPath)) {
        Files.createDirectories(credPath.getParent());
        Files.writeString(credPath, "admin:nightnote");
    }

    String stored = Files.readString(credPath).trim();
    String[] parts = stored.split(":");
    String storedUser = parts[0];
    String storedPass = parts[1];

    String username = body.get("username");
    String password = body.get("password");

    if (username.equals(storedUser) && password.equals(storedPass)) {
        return org.springframework.http.ResponseEntity.ok("Login successful");
    } else {
        return org.springframework.http.ResponseEntity
            .status(401).body("Invalid credentials");
    }
}
@PostMapping("/api/auth/logout")
public String logout() {
    return "Logged out";
}

    @GetMapping("/api/notes/meta")
    public List<Map<String, String>> getNotesMeta() throws Exception {
        try (Stream<Path> paths = Files.walk(NOTES_DIR, 1)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(p -> {
                    String name = p.getFileName().toString();
                    return name.endsWith(".md") || name.endsWith(".note") || name.endsWith(".txt");
                })
                .sorted()
                .map(p -> {
                    Map<String, String> meta = new HashMap<>();
                    meta.put("file", p.getFileName().toString());
                    meta.put("title", p.getFileName().toString());
                    meta.put("priority", "none");
                    try {
                        List<String> lines = Files.readAllLines(p);
                        for (String line : lines) {
                            if (line.startsWith("title:")) meta.put("title", line.substring(6).trim());
                            if (line.startsWith("priority:")) meta.put("priority", line.substring(9).trim());
                            if (line.startsWith("created:")) meta.put("created", line.substring(8).trim());
                            if (line.startsWith("due:")) meta.put("due", line.substring(4).trim());
                        }
                    } catch (Exception e) {}
                    return meta;
                })
                .toList();
        }
    }

    @GetMapping("/api/search")
    public List<Map<String, String>> searchNotes(@RequestParam String q) throws Exception {
        try (Stream<Path> paths = Files.walk(NOTES_DIR, 1)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(p -> {
                    try {
                        return Files.readString(p).toLowerCase()
                            .contains(q.toLowerCase());
                    } catch (Exception e) { return false; }
                })
                .sorted()
                .map(p -> {
                    Map<String, String> meta = new HashMap<>();
                    meta.put("file", p.getFileName().toString());
                    meta.put("title", p.getFileName().toString());
                    meta.put("priority", "none");
                    try {
                        List<String> lines = Files.readAllLines(p);
                        for (String line : lines) {
                            if (line.startsWith("title:")) meta.put("title", line.substring(6).trim());
                            if (line.startsWith("priority:")) meta.put("priority", line.substring(9).trim());
                            if (line.startsWith("created:")) meta.put("created", line.substring(8).trim());
                        }
                    } catch (Exception e) {}
                    return meta;
                })
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
    public String createNote(@RequestBody Map<String, String> body) throws Exception {
        String title = body.get("title");
        String content = body.get("content");
        String priority = body.getOrDefault("priority", "none");
        String due = body.getOrDefault("due", "");

        String filename = title.toLowerCase()
            .replaceAll(" ", "-")
            .replaceAll("[^a-z0-9-]", "") + ".note";

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

        Path filePath = NOTES_DIR.resolve(filename);
        Files.writeString(filePath, noteContent);
        return "Note created: " + filename;
    }

    @PutMapping("/api/notes/{id}")
    public String updateNote(@PathVariable String id,
            @RequestBody Map<String, String> body) throws Exception {
        Path filePath = NOTES_DIR.resolve(id);
        if (!Files.exists(filePath)) {
            return "Note not found: " + id;
        }
        String content = body.get("content");
        String now = java.time.Instant.now().toString();

        List<String> lines = Files.readAllLines(filePath);
        StringBuilder newFile = new StringBuilder();
        boolean inYaml = false;
        boolean pastYaml = false;
        for (String line : lines) {
            if (line.equals("---") && !inYaml) { inYaml = true; newFile.append(line).append("\n"); continue; }
            if (line.equals("---") && inYaml) { pastYaml = true; inYaml = false; newFile.append(line).append("\n"); continue; }
            if (inYaml && line.startsWith("modified:")) { newFile.append("modified: ").append(now).append("\n"); continue; }
            if (!pastYaml) { newFile.append(line).append("\n"); }
        }
        newFile.append("\n").append(content);
        Files.writeString(filePath, newFile.toString());
        return "Note updated: " + id;
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

    @PostMapping("/api/notes/{id}/images")
    public String uploadImage(@PathVariable String id,
            @RequestParam("file") MultipartFile file) throws Exception {
        Path imagesDir = Path.of(
            System.getProperty("user.home"), ".notes", "images"
        );
        Files.createDirectories(imagesDir);
        String filename = id.replace(".note", "").replace(".md", "") + "-" +
            file.getOriginalFilename()
                .replaceAll(" ", "-")
                .replaceAll("[^a-zA-Z0-9._-]", "-");
        Path imagePath = imagesDir.resolve(filename);
        Files.write(imagePath, file.getBytes());
        return "/api/images/" + filename;
    }

    @GetMapping("/api/images/{filename}")
    public org.springframework.http.ResponseEntity<byte[]> getImage(
            @PathVariable String filename) throws Exception {
        Path imagePath = Path.of(
            System.getProperty("user.home"), ".notes", "images", filename
        );
        if (!Files.exists(imagePath)) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        byte[] imageBytes = Files.readAllBytes(imagePath);
        String contentType = filename.endsWith(".png") ? "image/png" : "image/jpeg";
        return org.springframework.http.ResponseEntity.ok()
            .header("Content-Type", contentType)
            .body(imageBytes);
    }

    @GetMapping("/api/images/list/{prefix}")
    public List<String> listImages(@PathVariable String prefix) throws Exception {
        Path imagesDir = Path.of(
            System.getProperty("user.home"), ".notes", "images"
        );
        if (!Files.exists(imagesDir)) return java.util.Collections.emptyList();
        try (Stream<Path> paths = Files.walk(imagesDir, 1)) {
            return paths
                .filter(Files::isRegularFile)
                .map(p -> p.getFileName().toString())
                .filter(f -> f.startsWith(prefix))
                .toList();
        }
    }

    // ⬇️ NEW QUIZ METHODS
    @GetMapping("/api/notes/{id}/quiz")
    public String getQuiz(@PathVariable String id) throws Exception {
        Path quizPath = NOTES_DIR.resolve(
            id.replace(".note", "").replace(".md", "") + ".quiz"
        );
        if (!Files.exists(quizPath)) return "[]";
        return Files.readString(quizPath);
    }

    @PostMapping("/api/notes/{id}/quiz")
    public String saveQuiz(@PathVariable String id,
            @RequestBody String quizJson) throws Exception {
        Path quizPath = NOTES_DIR.resolve(
            id.replace(".note", "").replace(".md", "") + ".quiz"
        );
        Files.writeString(quizPath, quizJson);
        return "Quiz saved!";
    }
}
