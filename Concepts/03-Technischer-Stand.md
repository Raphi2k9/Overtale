# 🛠️ Technischer Stand — Level 1

**Zuletzt aktualisiert:** 2026-05-19  
**Branch:** main  
**Status:** Spielbar, Level 1 fast fertig

---

## Karte (TestMap1.tmx)

- **Größe:** 30 × 50 Tiles → 960 × 1600 px  
- **Tile-Größe:** 32 × 32 px  
- **Kamera:** folgt dem Spieler, Viewport-Bounds (0, 0, 960, 1600)

### Struktur
| Bereich | Cols | Rows | Beschreibung |
|---|---|---|---|
| Eingangs-Korridor | 11–14 | 32–49 | Spieler startet hier unten |
| Horizontaler Korridor | 1–25 | 25–31 | NPCs + Truhen hier |
| Verbindungsstück / Kurve | 10–16 | 14–24 | Vertikaler Aufstieg |
| Boss-Arena (oben) | 10–16 | 4–13 | „Der Richter" wartet hier |

---

## Entitäten — Spawn-Positionen (col × 32, row × 32)

| Entity | Pixel X | Pixel Y | Notizen |
|---|---|---|---|
| Spieler | 416 | 1504 | col 13, row 47 — Mitte Eingangs-Korridor |
| Sans (NPC) | 160 | 800 | col 5, row 25 — Links im Horizontalkorridor |
| Papyrus (NPC) | 672 | 800 | col 21, row 25 — Rechts im Horizontalkorridor |
| Finsternisgeist (Boss 0) | 416 | 1376 | col 13, row 43 — Eingangs-Korridor |
| Schattenwächter (Boss 1) | 416 | 576 | col 13, row 18 — Mittlerer Aufstieg |
| Der Richter (Boss 2) | 416 | 192 | col 13, row 6 — Obere Plattform |
| Truhe 1 — Sonnenessenz | 416 | 1280 | col 13, row 40 — Erster Reward |
| Truhe 2 — Engelstraene | 96 | 832 | col 3, row 26 — Links im Korridor |
| Truhe 3 — GoldenerNektar | 704 | 832 | col 22, row 26 — Rechts im Korridor |

---

## Kampfsystem

### Ablauf
1. Spieler drückt **E** in der Nähe eines Bosses → Pre-Dialog  
2. Battle-HUD öffnet sich: **FIGHT / ACT / ITEM / MERCY**  
3. FIGHT → **(nur Der Richter)** 1,5s Phasen-Ankündigung im Dialog-Feld, dann Ausweichphase  
4. Ausweichphase endet → Spieler nimmt Schaden → Boss verliert HP → HUD wieder sichtbar  
5. Boss besiegt → Belohnungs-Truhe spawnt, Post-Dialog, ggf. Victory-Screen

### Battle Box
- **Größe:** 400 × 300 px  
- **Position:** x=200, y=150 (zentriert auf 800×600)

### Schaden
- Normale Kugel trifft Herz → **−2 HP**  
- Richter-Blitz (gold, 20×20) trifft Herz → **−5 HP**  
- HP kann nicht unter 0 fallen  
- **Tod:** GAME OVER Overlay → `R` zum Neustart

---

## Der Richter — Phasen-System

HP: **30**. Jede überlebte Ausweich-Runde → `_dodgeRound++`.  
Runde 0 → Phase 0, dann Runden 1–4 → Phasen 1–4, ab Runde 5 → Phasen 1–4 im Loop.

| Runde | Phase | Name | Dauer | Interval | Beschreibung |
|---|---|---|---|---|---|
| 1 | 0 | Urteil | 5 s | 0,6 s | 4 Kugeln von allen Seiten, auf Herz gerichtet, Speed 120 |
| 2 | 1 | Kreuzgericht | 4 s | 0,4 s | Zwei diagonale Ströme (45°), Speed 160 |
| 3 | 2 | Heiliger Regen | 5 s | 0,15 s | Dichter Regen von oben, Speed 200 |
| 4 | 3 | Mauern des Himmels | 6 s | 0,5 s | Wände von links+rechts, 80px-Korridor verschiebt sich bei t=2s+4s |
| 5+ | 4→1 | Göttliches Gericht | 6 s | 0,4 s | 8 Kugeln/Tick + Richter-Blitz alle 1,5s (gold, −5 HP) |

---

## Inventar & Items

**8 Slots** in einem 4×2-Raster.  
Navigation: UP/DOWN = Zeile, LEFT/RIGHT = Spalte, Z/E = Benutzen, Q = Wegwerfen, X = Schließen.

| Item | Heilung | Schaden-Bonus | Fundort |
|---|---|---|---|
| Sonnenessenz | +5 HP | — | Truhe 1 (Eingang) |
| Engelstraene | +10 HP | — | Truhe 2 (Korridor links) |
| Goldener Nektar | +20 HP | — | Truhe 3 (Korridor rechts) |
| Engelssegen | — | +10 | Drop: Finsternisgeist |
| Heiliges Schwert | — | +3 | Drop: Schattenwächter |
| Heilige Schriftrolle | +10 HP | +7 | Drop: Der Richter |
| Himmelsrelikt | +5 HP | +4 | (noch kein Fundort in Level 1) |
| Göttlicher Speer | — | +10 | (noch kein Fundort in Level 1) |

---

## NPCs

| Name | Position | Dialog |
|---|---|---|
| **Sans** | Horizontalkorridor links | „Heya. Ich bin Sans..." — warnt vor den Archons |
| **Papyrus** | Horizontalkorridor rechts | „NYYYYEH HEH HEH!..." — hält sich für Engel-Trainee |

---

## Was noch fehlt (offene Punkte)

- [ ] Tile-Kollision (Spieler kann durch Wände laufen)
- [ ] Sprite-Texturen für Spieler, Gegner, NPCs, Items
- [ ] Audio / Musik
- [ ] Win-Screen nach Sieg über Der Richter (Victory-Dialog existiert, aber kein dedizierter Screen)
- [ ] Level 2 (The Prism Forest / Flashing Forest)
- [ ] Undyne & Alphys als spielbare NPCs/Events
