public class NoteTest {



    static int passed = 0;
    static int failed = 0;
    
    //lofi 

    static void test(String name, boolean result) {
        if (result) {
            System.out.println("pass: " + name);
            passed++;
        } else {
            System.out.println("fail: " + name);
            failed++;
        }
    }

    public static void main (String[] args) {
        Note n = new Note("My Title", "Joseph", "some content");
        test("Title is set", n.title.equals("My Title"));

        test("author is set " , n.author.equals("Joseph"));

        test("content is set ", n.content.equals("some content"));

        test("crated is not null", n.created != null);

        System.out.println("\n" + passed + "passed, " + failed + "failed");
    }
}
