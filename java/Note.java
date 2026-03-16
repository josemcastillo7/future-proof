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
       
    }

