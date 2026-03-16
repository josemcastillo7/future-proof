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
        this.created = created;
        this.modified = modified;
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

