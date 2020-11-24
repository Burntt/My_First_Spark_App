import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.staticFileLocation;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.eclipse.jetty.http.HttpStatus;
import spark.ModelAndView;
import spark.Request;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * A simple CRUD example showing how to create, get, update and delete book resources.
 */
public class Books {

    /**
     * Map holding the books
     */
    private static Map<Integer, Book> books = new HashMap<>();

    public static void main(String[] args) {
        // Let's add some books to the HashMap
        // You shoud read them from the MongoDB
        books.put(1, new Book("The Little Prince", "Antouan De Saint Exupery", 100));
        books.put(2, new Book("The Oxford Dictionary", "The British Crown", 2000));
        books.put(3, new Book("Intro to Java + OOP", "Harbour.Space", 15));

        final Gson gson = new Gson();
        final Random random = new Random();

        staticFileLocation("public");

        // Creates a new book resource, will return the ID to the created resource
        // author and title are sent in the post body as x-www-urlencoded values e.g. author=Foo&title=Bar
        // you get them by using request.queryParams("valuename")
        post("/books", (request, response) -> {
            String author = request.queryParams("author");
            String title = request.queryParams("title");
            Integer pages = Integer.valueOf(request.queryParams("pages"));
            Book book = new Book(author, title, pages);

            int id = random.nextInt(Integer.MAX_VALUE);
            books.put(id, book);

            response.status(HttpStatus.CREATED_201);
            return id;
        });

        // Gets the book resource for the provided id
        get("/books/:id", (request, response) -> {
            Integer id = Integer.valueOf(request.params(":id"));
            Book book = books.get(id);
            if (book == null) {
                response.status(HttpStatus.NOT_FOUND_404);
                return "Book not found";
            }
            if (clientAcceptsHtml(request)) {
                Map<String, Object> bookMap = new HashMap<>();
                bookMap.put("book", book);
                return render(bookMap, "book.ftl");
            } else if (clientAcceptsJson(request))
                return gson.toJson(book);

            return null;
        });

        // Updates the book resource for the provided id with new information
        // author and title are sent in the request body as x-www-urlencoded values e.g. author=Foo&title=Bar
        // you get them by using request.queryParams("valuename")
        put("/books/:id", (request, response) -> {
            Integer id = Integer.valueOf(request.params(":id"));
            Book book = books.get(id);
            if (book == null) {
                response.status(HttpStatus.NOT_FOUND_404);
                return "Book not found";
            }
            String newAuthor = request.queryParams("author");
            String newTitle = request.queryParams("title");
            String newPages = request.queryParams("pages");
            if (newAuthor != null) {
                book.setAuthor(newAuthor);
            }
            if (newTitle != null) {
                book.setTitle(newTitle);
            }
            if (newPages != null) {
                book.setPages(Integer.valueOf(newPages));
            }
            return "Book with id '" + id + "' updated";
        });

        // Deletes the book resource for the provided id
        delete("/books/:id", (request, response) -> {
            Integer id = Integer.valueOf(request.params(":id"));
            Book book = books.remove(id);
            if (book == null) {
                response.status(HttpStatus.NOT_FOUND_404);
                return "Book not found";
            }
            return "Book with id '" + id + "' deleted";
        });

        // Gets all available book resources
        get("/books", (request, response) -> {
            if (clientAcceptsHtml(request)) {
                Map<String, Object> booksMap = new HashMap<>();
                booksMap.put("books", books);
                return render(booksMap, "books.ftl");
            } else if (clientAcceptsJson(request))
                return gson.toJson(books);

            return null;
        });
    }

    public static String render(Map values, String template) {
        return new FreeMarkerEngine().render(new ModelAndView(values, template));
    }

    public static boolean clientAcceptsHtml(Request request) {
        String accept = request.headers("Accept");
        return accept != null && accept.contains("text/html");
    }

    public static boolean clientAcceptsJson(Request request) {
        String accept = request.headers("Accept");
        return accept != null && accept.contains("application/json");
    }

    public static class Book {

        public String author, title;
        public Integer pages;

        public Book(String author, String title, Integer pages) {
            this.author = author;
            this.title = title;
            this.pages = pages;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getPages() {
            return pages;
        }

        public void setPages(Integer pages) {
            this.pages = pages;
        }
    }
}