package at.htl.overtale;

import javazoom.jl.player.Player;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class GameMusicPlayer {

    private volatile Player _player;
    private volatile boolean _running;
    private volatile int _generation;

    public void loop(String resourceName) {
        stop();
        _running = true;
        int gen = ++_generation;
        Thread t = new Thread(() -> {
            while (_running && _generation == gen) {
                try {
                    InputStream is = GameMusicPlayer.class.getResourceAsStream("/assets/music/" + resourceName);
                    if (is == null) return;
                    _player = new Player(new BufferedInputStream(is));
                    _player.play();
                    _player.close();
                } catch (Exception e) {
                    if (_running && _generation == gen) e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void stop() {
        _running = false;
        _generation++;
        Player p = _player;
        if (p != null) p.close();
        _player = null;
    }
}
