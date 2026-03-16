public class Note {
    String title;
    String author;
    String created;
    String modified;
    String tags;
    String content;

    public Note(String title, String author, String content) {
        this.title = title;
        this.author = author;
        this.content = content;
        this.created = java.time.Instant.now().toString();
        this.modified = this.created;
        this.tags = "";

        }

        public String toString() {
        return String.format("""
                ---
                title: %s
                author: %s
                created: %s
                modified: %s
                tags: [%s]
                ---

                %s
                """, title, author, created, modified, tags, content);
    }
    public void saveToFile() {
    try {
        // Create the notes directory if it doesn't exist
        java.nio.file.Path dir = java.nio.file.Path.of(
            System.getProperty("user.home"), ".notes", "notes"
        );
        java.nio.file.Files.createDirectories(dir);

        // Create filename from title
        String filename = title.toLowerCase()
            .replaceAll(" ", "-")
            .replaceAll("[^a-z0-9-]", "") + ".note";

        // Save the file
        java.nio.file.Path filePath = dir.resolve(filename);
        java.nio.file.Files.writeString(filePath, this.toString());

        System.out.println("Note saved to: " + filePath);

    } catch (java.io.IOException e) {
        System.out.println("Error saving note: " + e.getMessage());
    }
}
       
    }

