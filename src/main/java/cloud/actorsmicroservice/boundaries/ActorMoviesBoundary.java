package cloud.actorsmicroservice.boundaries;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ActorMoviesBoundary {

    private String id;
    private String name;
    private String birthdate;
    private Set<Map<String, Object>> movies;

    public ActorMoviesBoundary() {
        this.movies = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public Set<Map<String, Object>> getMovies() {
        return movies;
    }

    public void setMovies(Set<Map<String, Object>> movies) {
        this.movies = movies;
    }
}
