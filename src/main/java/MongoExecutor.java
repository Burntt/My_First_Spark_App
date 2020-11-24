import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.bson.Document;
import org.bson.types.ObjectId;

public class MongoExecutor {
    MongoClient client;
    MongoDatabase database;
    MongoClientURI uri;

    public MongoExecutor() {
        uri = new MongoClientURI(
                "mongodb+srv://userHW10:VA1z0Oi1owEi4TJh@cluster0"
                        + ".ygemx.mongodb.net/<dbname>?retryWrites=true&w=majority");
        client = new MongoClient(uri);
        database = client.getDatabase("moviedb");
    }

    public void execStoreMovie(Document document) {
        MongoCollection<Document> mongoCollection = database.getCollection("movies");
        mongoCollection.insertOne(document);
        System.out.println("Succesfully stored movie");
    }

    public Movies.Movie findMovieByObjId(String id, Function<Document, Movies.Movie> handler) {
        MongoCollection<Document> mongoCollection = database.getCollection("movies");
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", new ObjectId(id));
        FindIterable<Document> result = mongoCollection.find(searchQuery);
        return handler.apply(result.first());
    }

    public void execDelMovieByid(String id) {
        MongoCollection<Document> mongoCollection = database.getCollection("movies");
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", new ObjectId(id));
        mongoCollection.findOneAndDelete(searchQuery);
    }

    public void execDelAllMovies() {
        MongoCollection<Document> mongoCollection = database.getCollection("movies");
        BasicDBObject document = new BasicDBObject();
        mongoCollection.deleteMany(document);
        System.out.println("WARNING: deleted all movies");
    }

    public Movies.Movie execUpdateMovieById(
            String id, BasicDBObject newData, Function<Document, Movies.Movie> handler) {
        MongoCollection<Document> mongoCollection
                = database.getCollection("movies");
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", new ObjectId(id));
        BasicDBObject updateObject = new BasicDBObject();
        updateObject.put("$set", newData);
        mongoCollection.updateOne(searchQuery, updateObject);
        return handler.apply(mongoCollection.find(searchQuery).first());
    }

    public List<Movies.Movie> execGetMovies(
            Function<List<Movies.Movie>, List<Movies.Movie>> handler) {
        MongoCollection<Document> mongoCollection = database.getCollection("movies");
        FindIterable<Document> result = mongoCollection.find();
        List<Movies.Movie> movies = new ArrayList<>();
        for (Document document : result) {
            movies.add(new Gson().fromJson(document.toJson(), Movies.Movie.class));
        }
        return handler.apply(movies);
    }
}



