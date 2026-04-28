package at.htl.overtale.entity;

import at.htl.overtale.component.BulletComponent;
import at.htl.overtale.component.LootChestComponent;
import at.htl.overtale.component.items.Item;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.*;
import com.almasb.fxgl.entity.components.CollidableComponent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

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
    @Spawns("npc")
    public Entity newNpc(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.NPC)
                .viewWithBBox(new Rectangle(40, 40, Color.YELLOW))
                .zIndex(1)
                .build();
    }

    @Spawns("enemy")
    public Entity newEnemy(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.ENEMY)
                .viewWithBBox(new Rectangle(40, 40, Color.RED))
                .with(new CollidableComponent(true))
                .zIndex(1)
                .build();
    }

    @Spawns("lootChest")
    public Entity newLootChest(SpawnData data) {
        Item[] items = data.get("items");

        // Optik: goldene Truhe mit "?" Symbol
        Rectangle box = new Rectangle(36, 32, Color.web("#8B6914"));
        box.setStroke(Color.web("#FFD700"));
        box.setStrokeWidth(2);
        Text label = new Text("?");
        label.setFont(Font.font("Monospaced", 16));
        label.setFill(Color.YELLOW);
        StackPane view = new StackPane(box, label);

        return FXGL.entityBuilder(data)
                .type(EntityType.LOOT_CHEST)
                .viewWithBBox(view)
                .with(new CollidableComponent(true))
                .with(new LootChestComponent(items))
                .zIndex(1)
                .build();
    }

    @Spawns("bullet")
    public Entity newBullet(SpawnData data) {
        double vx = data.get("vx");
        double vy = data.get("vy");

        return FXGL.entityBuilder(data)
                .type(EntityType.BULLET)
                .viewWithBBox(new Rectangle(10, 10, Color.BLACK))
                .with(new CollidableComponent(true))
                .with(new BulletComponent(vx, vy))  // ← Velocity direkt mitgeben
                .build();
    }
}
