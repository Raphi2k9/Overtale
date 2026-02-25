module at.htl.overtale {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;

    opens at.htl.overtale to javafx.fxml;
    exports at.htl.overtale;
}