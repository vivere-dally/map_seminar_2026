package course;

import course.controller.MovieController;
import course.domain.Movie;
import course.domain.MovieSortField;
import course.domain.validators.MovieValidator;
import course.repository.PagingRepository;
import course.repository.Repository;
import course.repository.factory.RepositoryFactory;
import course.service.MovieService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Seminar 9: interfata grafica JavaFX pentru filmele din baza de date cinema.
 */
public class MovieApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MovieApp.class.getResource("movie-view.fxml"));
        stage.setScene(new Scene(loader.load()));
        MovieController controller = loader.getController();
        controller.setService(new MovieService(createRepository()));
        stage.setTitle("Cinema");
        stage.getIcons().add(new Image(MovieApp.class.getResourceAsStream("cinema-logo.png")));
        stage.show();
    }

    /**
     *
     * @return the paging movie repository created by the factory, from config.properties
     * @throws IOException           if config.properties cannot be read
     * @throws IllegalStateException if the configured repository has no pagination
     */
    @SuppressWarnings("unchecked")
    private static PagingRepository<Long, Movie, MovieSortField> createRepository() throws IOException {
        Properties props = new Properties();
        try (InputStream in = MovieApp.class.getResourceAsStream("/config.properties")) {
            props.load(in);
        }
        Repository<Long, Movie> repo = new RepositoryFactory(props).createMovieRepository(new MovieValidator());
        // la executie tipurile generice sunt sterse, deci instanceof verifica doar PagingRepository;
        // singurul PagingRepository de filme este MovieDbRepository, sortat dupa MovieSortField
        if (!(repo instanceof PagingRepository<?, ?, ?> pagingRepo))
            throw new IllegalStateException("Paginarea cere repository.type=DATABASE");
        return (PagingRepository<Long, Movie, MovieSortField>) pagingRepo;
    }
}
