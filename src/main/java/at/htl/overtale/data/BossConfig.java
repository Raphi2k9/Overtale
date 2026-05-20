package at.htl.overtale.data;

import java.util.List;

public class BossConfig {
    public String name;
    public int    maxHp;
    public String pattern;
    public double fightDuration;
    public double bulletInterval;
    public double spawnX, spawnY;
    public String color, stroke;
    public String reward;
    public List<String>      preDialog;
    public List<String>      actDialog;
    public List<String>      postDialog;
    public List<PhaseConfig> phases;     // null for single-phase bosses
}
