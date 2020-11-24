import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.staticFileLocation;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import org.bson.Document;
import org.eclipse.jetty.http.HttpStatus;
import spark.ModelAndView;
import spark.Request;
import spark.template.freemarker.FreeMarkerEngine;

public class Movies {

    private static Map<Integer, Movie> movies = new HashMap<>();

    public static void main(String[] args) {
        // Let's add some Movies to the HashMap
        // You shoud read them from the MongoDB
        movies.put(1, new Movie("Django Unchained", "Quentin Tarantino", 2012, "https://cdn.shopify.com/s/files/1/0969/9128/products/Django_Unchained_-_Fan_Art_-_Quentin_Tarantino_-_Hollywood_Movie_Poster_Collection_c888b077-cc07-40be-9601-6f720cd5633d.jpg?v=1573214217"));
        movies.put(2, new Movie("Kill Bill", "Quentin Tarantino", 2003, "https://images-na.ssl-images-amazon.com/images/I/61%2BA2IymvWL._AC_SL1055_.jpg"));
        movies.put(3, new Movie("Inglorious Bastards", "Quentin Tarantino", 2009, "https://images-na.ssl-images-amazon.com/images/I/61PXdxTJGPL._AC_SL1022_.jpg"));

        final Gson gson = new Gson();
        final Random random = new Random();

        /////////////////////////////// Initialize DB  ///////////////////////////////////
        MongoExecutor executor = new MongoExecutor();
        executor.execDelAllMovies();

        for (int i = 1; i <= movies.size(); i++) {
            String json = new Gson().toJson(movies.get(i));
            Document doc = Document.parse(json);
            executor.execStoreMovie(doc);
        }

        staticFileLocation("public");

        post("/movies", (request, response) -> {
            String director = request.queryParams("director");
            String title = request.queryParams("title");
            Integer year = Integer.valueOf(request.queryParams("year"));
            String posterUrl = request.queryParams("posterUrl");
            Movie movie = new Movie(title, director, year, posterUrl);

            int id = random.nextInt(Integer.MAX_VALUE);

            // Store new movie in DB
            String json = new Gson().toJson(movie);
            Document doc = Document.parse(json);
            executor.execStoreMovie(doc);

            movies.put(id, movie);

            response.status(HttpStatus.CREATED_201);
            return id;
        });

        // Gets the movie resource for the provided id
        get("/movies/:id", (request, response) -> {

            String id = request.params(":id");
            Function<Document, Movie> handler = doc -> gson.fromJson(doc.toJson(), Movie.class);
            Movie movie = executor.findMovieByObjId(id, handler);

            if (movie == null) {
                response.status(HttpStatus.NOT_FOUND_404);
                return "Movie not found";
            }
            if (clientAcceptsHtml(request)) {
                Map<String, Object> moviesMap = new HashMap<>();
                moviesMap.put("movie", movie);
                return render(moviesMap, "movie.ftl");
            } else if (clientAcceptsJson(request)) {
                return gson.toJson(movie);
            }
            return null;
        });


        put("/movies/:id", (request, response) -> {
            String id = request.params(":id");
            Function<Document, Movie> handler = doc -> gson.fromJson(doc.toJson(), Movie.class);
            Movie movie = executor.findMovieByObjId(id, handler);
            BasicDBObject newData = new BasicDBObject();
            /////
            ////
            if (movie == null) {
                response.status(HttpStatus.NOT_FOUND_404);
                return "Movie not found";
            }
            String newDirector = request.queryParams("director");
            if (newDirector != null) {
                newData.put("director", newDirector);
            }
            String newTitle = request.queryParams("title");
            if (newTitle != null) {
                newData.put("title", newTitle);
            }
            String newYear = request.queryParams("year");
            if (newYear != null) {
                newData.put("year", newYear);
            }
            String newPosterUrl = request.queryParams("posterUrl");
            if (newPosterUrl != null) {
                newData.put("posterUrl", newPosterUrl);
            }
            executor.execUpdateMovieById(id, newData, handler);
            return "Movie with id '" + id + "' updated";
        });

        // Deletes the movie resource for the provided id
        delete("/movies/:id", (request, response) -> {
            String id = request.params(":id");
            Function<Document, Movie> handler = doc -> gson.fromJson(doc.toJson(), Movie.class);
            Movie movie = executor.findMovieByObjId(id, handler);
            executor.execDelMovieByid(id);
            if (movie == null) {
                response.status(HttpStatus.NOT_FOUND_404);
                return "Movie not found";
            }
            return "Movie with id '" + id + "' deleted";
        });

        // Gets all available Movie resources
        get("/movies", (request, response) -> {
            if (clientAcceptsHtml(request)) {
                Function<List<Movie>, List<Movie>> handler = doc -> doc;
                List result = executor.execGetMovies(handler);
                Map<String, Object> moviesMap = new HashMap<>();
                moviesMap.put("movies", result);
                return render(moviesMap, "movies.ftl");
            } else if (clientAcceptsJson(request)) {
                return gson.toJson(movies);
            }
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
        return accept != null && (accept.contains("application/json") || accept.contains("*/*"));
    }

    public static class Movie {

        public String director;
        public String title;
        public Integer year;
        public String posterUrl;

        public Movie(String title, String director, Integer year, String posterUrl) {
            this.director = director;
            this.title = title;
            this.year = year;
            this.posterUrl = posterUrl;
        }

        public String getDirector() {
            return director;
        }

        public void setDirector(String director) {
            this.director = director;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public String getPosterUrl() {
            return posterUrl;
        }

        public void setPosterUrl(String posterUrl) {
            this.posterUrl = posterUrl;
        }


    }
}