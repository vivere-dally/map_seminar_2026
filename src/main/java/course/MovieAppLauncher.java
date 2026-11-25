package course;

import javafx.application.Application;

// O clasa main care nu extinde Application: asa aplicatia porneste din IDE fara --module-path pentru JavaFX.
public class MovieAppLauncher {
    public static void main(String[] args) {
        Application.launch(MovieApp.class, args);
    }
}
