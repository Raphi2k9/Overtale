module at.htl.overtale {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;
    requires java.desktop;

    opens at.htl.overtale to javafx.fxml;
    opens at.htl.overtale.entity to com.almasb.fxgl.all;
    opens at.htl.overtale.component to com.almasb.fxgl.all;

    exports at.htl.overtale;
    exports at.htl.overtale.entity;
    exports at.htl.overtale.hud;
    exports at.htl.overtale.component;
}