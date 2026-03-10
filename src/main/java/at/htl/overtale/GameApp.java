package at.htl.overtale;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import javafx.scene.input.KeyCode;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameApp extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Overtale");
        settings.setVersion("0.1");
        settings.setWidth(800);
        settings.setHeight(600);
    }

    @Override
    protected void initInput() {
        onKey(KeyCode.W, () -> getGameWorld().getSingleton(EntityType.PLAYER).translateY(-3));
        onKey(KeyCode.S, () -> getGameWorld().getSingleton(EntityType.PLAYER).translateY(3));
        onKey(KeyCode.D, () -> getGameWorld().getSingleton(EntityType.PLAYER).translateX(3));
        onKey(KeyCode.A, () -> getGameWorld().getSingleton(EntityType.PLAYER).translateX(-3));

    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new GameEntityFactory());

        spawn("player", 100, 100);
    }

    @Override
    protected void initPhysics() {
        // Collision handlers go here
    }

    @Override
    protected void initUI() {
        // HUD, score text, health bar, etc.
    }

    @Override
    protected void onUpdate(double tpf) {
        // Called every frame — tpf = time per frame
    }

    public static void main(String[] args) {
        launch(args);
    }
}