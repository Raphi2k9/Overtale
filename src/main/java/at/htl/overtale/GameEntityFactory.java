package at.htl.overtale;

// GameEntityFactory.java
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.*;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameEntityFactory implements EntityFactory {

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.PLAYER)
                .viewWithBBox(new Rectangle(40, 40, Color.BLUE)) // Größe & Farbe
                .with(new CollidableComponent(true))
                .zIndex(1)
                .build();
    }
    @Spawns("bullet")
    public Entity newBullet(SpawnData data) {
        double vx = data.get("vx");
        double vy = data.get("vy");

        return FXGL.entityBuilder(data)
                .type(EntityType.BULLET)
                .viewWithBBox(new Rectangle(10, 10, Color.WHITE))
                .with(new CollidableComponent(true))
                .with(new BulletComponent(vx, vy))  // ← Velocity direkt mitgeben
                .build();
    }
}
