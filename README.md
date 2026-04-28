# Overtale

Ein rundenbasiertes 2D-RPG für den POS-Unterricht an der HTL, inspiriert von **Undertale**. Gebaut mit Java und dem FXGL-Framework.

---

## Technologie

| Komponente | Version |
|---|---|
| Java | 25 |
| FXGL | 17.3 |
| JavaFX | 21.0.6 |
| Maven | Wrapper (mvnw) |


---



## Spielprinzip

Der Spieler bewegt sich auf einer scrolling Map und kann mit NPCs, Gegnern und Loot-Truhen interagieren. Kommt es zum Kampf, wechselt das Spiel in ein Undertale-typisches **Battle-HUD** mit Ausweichphase und Angriffsmenü.

---

## Steuerung

### Weltbewegung

| Taste | Aktion |
|---|---|
| `W / A / S / D` | Spieler bewegen |
| `E` | Interagieren (NPC ansprechen, Kampf starten, Truhe öffnen) |

### Dialog

| Taste | Aktion |
|---|---|
| `Z` | Dialog weiterschalten / Typewriter überspringen |

### Kampfmenü

| Taste | Aktion |
|---|---|
| `← / →` | Button auswählen (FIGHT / ACT / ITEM / MERCY) |
| `Z` oder `E` | Auswahl bestätigen |

### Ausweichphase (nach FIGHT)

| Taste | Aktion |
|---|---|
| `W / A / S / D` | Herz (Spieler-Seele) bewegen |

### Inventar

| Taste | Aktion |
|---|---|
| `↑ / ↓` | Zeile navigieren |
| `← / →` | Spalte wechseln |
| `Z` oder `E` | Item benutzen |
| `Q` | Item wegwerfen |
| `X` | Inventar schließen |

---

## Kampfsystem

1. Spieler nähert sich einem Gegner und drückt `E` → **Battle-HUD** öffnet sich.
2. Im Menü stehen vier Optionen zur Verfügung:
   - **FIGHT** → Startet die Ausweichphase. Gegnerische Kugeln fliegen auf das rote Herz zu. Nach 6 Sekunden endet die Phase und der Gegner erleidet Schaden.
   - **ACT** → Stand jetzt noch keine Belegung
   - **ITEM** → Öffnet das Inventar; Items können vor dem Angriff eingesetzt werden.
   - **MERCY** → Kampf beenden ohne Schaden.
3. Trifft eine Kugel das Herz, verliert der Spieler **2 HP**.
4. Nach jeder Runde wird der Gegnerschaden berechnet: `2 + Bonus-Schaden` (Items können den Bonus erhöhen).

---

## Inventar

Das Inventar hat **8 Slots** in einem 4×2-Raster. Items können genutzt oder weggeworfen werden. Heilungs- und Schadenseffekte werden sofort beim Benutzen angewendet.

---

## Items

| Name | Heilung | Angriffsbonus | Beschreibung |
|---|---|---|---|
| Engelssegen | — | +10 | Segen eines Engels, verstärkt den Angriff |
| Engelsträne | +10 HP | — | Träne eines Engels |
| Goldener Nektar | +20 HP | — | Getränk der Götter |
| Sonnenessenz | +5 HP | — | Frisch von der Sonne |
| Himmelsrelikt | +5 HP | +4 | Uraltes Relikt aus vergangenen Zeiten |
| Heilige Schriftrolle | +10 HP | +7 | Aus den Ruinen der Götter |
| Heiliges Schwert | — | +3 | Schwert eines Götterschmieds |
| Göttlicher Speer | — | +10 | Waffe der Götter |

---

## Loot-Truhen

In der Spielwelt befinden sich **3 Truhen** (goldene `?`-Kisten). Durch Drücken von `E` in der Nähe einer Truhe werden alle enthaltenen Items ins Inventar gelegt. Eine geöffnete Truhe wird grau und kann nicht erneut geöffnet werden.


---

## Projektstruktur

```
src/main/java/at/htl/overtale/
├── GameApp.java                        # Einstiegspunkt, Game-Loop, Input, Physics
├── entity/
│   ├── EntityType.java                 # Enum: PLAYER, ENEMY, BULLET, NPC, LOOT_CHEST
│   └── GameEntityFactory.java          # Spawn-Methoden für alle Entitäten
├── component/
│   ├── BulletComponent.java            # Bewegungslogik für Kugeln
│   ├── LootChestComponent.java         # Truhen-Zustand und Item-Verwaltung
│   └── items/
│       ├── Item.java                   # Abstrakte Basisklasse für alle Items
│       ├── Inventory.java              # 8-Slot-Inventar
│       ├── Engelssegen.java
│       ├── Engelstraene.java
│       ├── GoldenerNektar.java
│       ├── Sonnenessenz.java
│       ├── Himmelsrelikt.java
│       ├── HeiliigeSchriftrolle.java
│       ├── HeiligesSchwert.java
│       └── GöttlicherSpeer.java
└── hud/
    ├── OvertaleHud.java                # Battle-HUD, HP-Leisten, Buttons, Ausweichphase
    ├── InventoryHud.java               # Inventar-Overlay
    └── DialogManager.java              # Seitenbasierter Dialog mit Typewriter-Effekt

src/main/resources/assets/
├── levels/                             # TMX-Kartendateien (TestMap1.tmx)
└── textures/                           # Grafiken
```

---

## Architektur-Überblick

- **FXGL** übernimmt Game-Loop, Entity-System, Physics und Rendering.
- Das **Battle-HUD** läuft komplett auf der JavaFX-UI-Ebene (kein separates Scene-Switching).
- Items sind reine Datenobjekte; ihre Effekte (Heilung, Schadensbonus) werden in `GameApp.handleItemUse()` ausgewertet.
- Der `DialogManager` steuert mehrseitige Dialoge und ruft nach dem letzten Satz optional einen `Runnable`-Callback auf (z. B. um das HUD wieder anzuzeigen).
