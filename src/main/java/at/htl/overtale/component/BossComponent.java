package at.htl.overtale.component;

import com.almasb.fxgl.entity.component.Component;

public class BossComponent extends Component {

    private final int _bossIndex;
    private boolean _defeated = false;

    public BossComponent(int index) {
        _bossIndex = index;
    }

    public int getBossIndex() { return _bossIndex; }
    public boolean isDefeated() { return _defeated; }
    public void setDefeated() { _defeated = true; }
}
