# ☁️ PROJECT OVERTALE: THE CELESTIAL RIFT
**Genre:** RPG / Bullet-Hell / PvE Hybrid  
**Engine:** JavaFXGL (FXGL 17+)  
**Status:** Sequel-Konzept (Post-Undertale Pacifist Route)

---

## 1. Die Story-Prämisse
Nachdem Frisk die Barriere im Untergrund zerstört hat, ist das magische Gleichgewicht der Welt instabil. Die gewaltige Menge an freigesetzter "Entschlossenheit" hat die Aufmerksamkeit der **Archonen** erregt – mächtige Lichtwesen, die im "Overground" (dem Himmel) über die kosmische Ordnung wachen.

Sie betrachten die Freiheit der Monster als "Systemfehler". Ihre Lösung: Die **Extraktion**. Sie entführen die Monster in den Himmel, um ihre Seelen zu "bleichen" (alle Erinnerungen und Emotionen zu entfernen), damit sie als reines, willenloses Licht existieren.

**Deine Mission:** Als Frisk steigst du in den Himmel auf, um deine Freunde aus dem "Archiv der Ewigkeit" zu retten, bevor ihre Identität gelöscht wird.

---

## 2. Die Spielwelt (Der Overground)
Der Himmel ist nicht nur Wolken; er ist eine hochtechnologische, sakrale Welt.

* **The Gilded Gates:** Der Einstiegspunkt. Goldene Architektur und schwebende Gärten.
* **The Prism Forest:** Ein Wald aus Glaskonstrukten, die das Sonnenlicht in tödliche Laser-Rätsel brechen.
* **The Binary Cathedral:** Das Zentrum der Macht, in dem die Archonen den "Code der Welt" verwalten.

---

## 3. Charaktere & Rollen

| Charakter | Zustand im Himmel | Rolle / Interaktion |
| :--- | :--- | :--- |
| **Sans** | "Gefangener Beobachter" | Er hat sich in das himmlische Sicherheitssystem gehackt. Er verkauft dir "Heiligenschein-Donuts", die deine HP heilen. |
| **Papyrus** | "Lehrling der Ordnung" | Er glaubt ernsthaft, er macht eine Ausbildung zum Engel. Seine Rätsel im Himmel nutzen Lichtstrahlen statt Knochen. |
| **Undyne** | "Die standhafte Dissonanz" | Sie weigert sich, vergessen zu werden. Ihr Kampf ist ein intensives PvE-Event, bei dem du sie vor den Reinigungslasern schützen musst. |
| **Alphys** | "System-Glitched" | Sie kommuniziert mit dir über dein Handy, während sie versucht, die Firewall des Himmels zu stürzen. |
| **Metatron Prime** | **Der Antagonist** | Nicht der Roboter aus Undertale, sondern das Original-Wesen, nach dem Mettaton geformt wurde. Kalt, perfekt und gnadenlos. |

---

## 4. Gameplay-Mechaniken (JavaFXGL Fokus)

### A. Das Kampfsystem (Hybrid PvE)
In Overtale kämpfst du nicht nur gegen Gegner, sondern gegen die Umgebung (**Player vs. Environment**):
* **Die Seele:** Dein Herz ist **Cyan (Reinheit)**. 
* **Aktive Abwehr:** Mit der Leertaste löst du einen `Pulse` aus. Dieser zerstört kleinere Licht-Projektile im Umkreis (FXGL Collision Handling).
* **Combo-System:** Wenn du Projektile mit deinem Puls abwehrst, lädt sich deine "Gnade" schneller auf.

### B. Erkundung & Rätsel
* **Vertikalität:** Da es der Himmel ist, bewegst du dich mit JavaFXGL-Physik oft nach oben (Low-Gravity-Zonen).
* **Licht-Puzzles:** Du musst Spiegel-Entities verschieben, um Lichtstrahlen auf Sensoren zu lenken, die Türen öffnen.

---

## 5. Technische Design-Elemente

* **Grafik-Stil:** Minimalistisches Pixel-Art, aber mit starken **JavaFX-Effekten**. Viel `Bloom`, `Glow` und Transparenz-Effekte, um die göttliche Atmosphäre zu betonen.
* **Soundscape:** Sphärische, orchestrale Remixe der Undertale-Klassiker (z.B. eine Harfen-Version von "Megalovania").
* **UI-System:** Ein sauberes, weiß-goldenes Menü-Interface, das im Kontrast zum schwarzen UI des Originals steht.

---

## 6. Die Enden (The Moral Choice)

1.  **Reunion (Pacifist):** Du rettest alle Freunde und überzeugst die Archonen, dass Individualität kein Fehler ist. Der Riss schließt sich friedlich.
2.  **Shattered Light (Neutral):** Du rettest deine Freunde, zerstörst aber den Himmel. Die Welt verliert ihre spirituelle Führung.
3.  **Eternal Night (Genocide):** Du korrumpierst das Licht des Himmels mit deiner Entschlossenheit. Die Sonne erlischt, und du wirst zum neuen, dunklen Gott über eine leere Welt.

---

## Aufgabenteilung:

+ **Raphi:** Mapper, Entities(Player, Enemys, usw.)
+ **Daniel:** Designer, Story macher
+ **Andere:** Game mechanics 



*Erstellt für die Entwicklung mit FXGL – "Strahle heller als die Angst."*# ☁️ PROJECT OVERTALE: THE CELESTIAL RIFT
**Genre:** RPG / Bullet-Hell / PvE Hybrid  
**Engine:** JavaFXGL (FXGL 17+)  
**Status:** Sequel-Konzept (Post-Undertale Pacifist Route)

---

## 1. Die Story-Prämisse
Nachdem Frisk die Barriere im Untergrund zerstört hat, ist das magische Gleichgewicht der Welt instabil. Die gewaltige Menge an freigesetzter "Entschlossenheit" hat die Aufmerksamkeit der **Archonen** erregt – mächtige Lichtwesen, die im "Overground" (dem Himmel) über die kosmische Ordnung wachen.

Sie betrachten die Freiheit der Monster als "Systemfehler". Ihre Lösung: Die **Extraktion**. Sie entführen die Monster in den Himmel, um ihre Seelen zu "bleichen" (alle Erinnerungen und Emotionen zu entfernen), damit sie als reines, willenloses Licht existieren.

**Deine Mission:** Als Frisk steigst du in den Himmel auf, um deine Freunde aus dem "Archiv der Ewigkeit" zu retten, bevor ihre Identität gelöscht wird.

---

## 2. Die Spielwelt (Der Overground)
Der Himmel ist nicht nur Wolken; er ist eine hochtechnologische, sakrale Welt.

* **The Gilded Gates:** Der Einstiegspunkt. Goldene Architektur und schwebende Gärten.
* **The Prism Forest:** Ein Wald aus Glaskonstrukten, die das Sonnenlicht in tödliche Laser-Rätsel brechen.
* **The Binary Cathedral:** Das Zentrum der Macht, in dem die Archonen den "Code der Welt" verwalten.

---

## 3. Charaktere & Rollen

| Charakter | Zustand im Himmel | Rolle / Interaktion |
| :--- | :--- | :--- |
| **Sans** | "Gefangener Beobachter" | Er hat sich in das himmlische Sicherheitssystem gehackt. Er verkauft dir "Heiligenschein-Donuts", die deine HP heilen. |
| **Papyrus** | "Lehrling der Ordnung" | Er glaubt ernsthaft, er macht eine Ausbildung zum Engel. Seine Rätsel im Himmel nutzen Lichtstrahlen statt Knochen. |
| **Undyne** | "Die standhafte Dissonanz" | Sie weigert sich, vergessen zu werden. Ihr Kampf ist ein intensives PvE-Event, bei dem du sie vor den Reinigungslasern schützen musst. |
| **Alphys** | "System-Glitched" | Sie kommuniziert mit dir über dein Handy, während sie versucht, die Firewall des Himmels zu stürzen. |
| **Metatron Prime** | **Der Antagonist** | Nicht der Roboter aus Undertale, sondern das Original-Wesen, nach dem Mettaton geformt wurde. Kalt, perfekt und gnadenlos. |

---

## 4. Gameplay-Mechaniken (JavaFXGL Fokus)

### A. Das Kampfsystem (Hybrid PvE)
In Overtale kämpfst du nicht nur gegen Gegner, sondern gegen die Umgebung (**Player vs. Environment**):
* **Die Seele:** Dein Herz ist **Cyan (Reinheit)**. 
* **Aktive Abwehr:** Mit der Leertaste löst du einen `Pulse` aus. Dieser zerstört kleinere Licht-Projektile im Umkreis (FXGL Collision Handling).
* **Combo-System:** Wenn du Projektile mit deinem Puls abwehrst, lädt sich deine "Gnade" schneller auf.

### B. Erkundung & Rätsel
* **Vertikalität:** Da es der Himmel ist, bewegst du dich mit JavaFXGL-Physik oft nach oben (Low-Gravity-Zonen).
* **Licht-Puzzles:** Du musst Spiegel-Entities verschieben, um Lichtstrahlen auf Sensoren zu lenken, die Türen öffnen.

---

## 5. Technische Design-Elemente

* **Grafik-Stil:** Minimalistisches Pixel-Art, aber mit starken **JavaFX-Effekten**. Viel `Bloom`, `Glow` und Transparenz-Effekte, um die göttliche Atmosphäre zu betonen.
* **Soundscape:** Sphärische, orchestrale Remixe der Undertale-Klassiker (z.B. eine Harfen-Version von "Megalovania").
* **UI-System:** Ein sauberes, weiß-goldenes Menü-Interface, das im Kontrast zum schwarzen UI des Originals steht.

---

## 6. Die Enden (The Moral Choice)

1.  **Reunion (Pacifist):** Du rettest alle Freunde und überzeugst die Archonen, dass Individualität kein Fehler ist. Der Riss schließt sich friedlich.
2.  **Shattered Light (Neutral):** Du rettest deine Freunde, zerstörst aber den Himmel. Die Welt verliert ihre spirituelle Führung.
3.  **Eternal Night (Genocide):** Du korrumpierst das Licht des Himmels mit deiner Entschlossenheit. Die Sonne erlischt, und du wirst zum neuen, dunklen Gott über eine leere Welt.

---

## Aufgabenteilung:

+ **Raphi:** Mapper, Entities(Player, Enemys, usw.)
+ **Daniel:** Designer, Story macher
+ **Andere:** Game mechanics 



*Erstellt für die Entwicklung mit FXGL – "Strahle heller als die Angst."*
