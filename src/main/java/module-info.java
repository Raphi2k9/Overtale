open module at.htl.overtale {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires com.google.gson;

    exports at.htl.overtale;
    exports at.htl.overtale.entity;
    exports at.htl.overtale.hud;
    exports at.htl.overtale.component;
    exports at.htl.overtale.data;
}
