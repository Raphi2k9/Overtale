package at.htl.overtale;

import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

public class BulletComponent extends Component {

    private final Point2D velocity;

    public BulletComponent(double vx, double vy) {
        this.velocity = new Point2D(vx, vy);
    }

    @Override
    public void onUpdate(double tpf) {
        entity.translate(velocity.getX() * tpf, velocity.getY() * tpf);

        if (entity.getX() < -20 || entity.getX() > 400 ||
                entity.getY() < -20 || entity.getY() > 400) {
            entity.removeFromWorld();
        }
    }
}