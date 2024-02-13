# DSA-Routenplaner

Mit diesem kleinen Programmchen kann der geneigte DSA-Fan (oder sonstige Nutzer von Routenplanern) sein eigenes Streckennetz auf einer bestehenden Karte zeichnen und eine einfache Navigation durchführen.

## Starten des Programms

Die Java-Datei kann ohne besondere Abhängigkeiten kompiliert werden:
```javac Routenplaner.java```

Gestartet wird sie so:
```java Routenplaner```

## Tastaturbelegung

* **F1** - Zeichnen der Karte ein/aus
* **F2** - Zeichnen der Punkte ein/aus
* **F3** - Zeichnen der Linien ein/aus
* **F4** - Zeichnen der Beschriftungen ein/aus
* **F5** - Speichern der aktuellen Karte
* **F6** - Laden der aktuellen Karte
* **F9** - zeige Editor für Geländetypen
* **F10** - Maßstab festlegen
* **LINKS** - zur vorherigen Karte (Änderungen werden nicht gespeichert!)
* **RECHTS** - zur nächsten Karte (Änderungen werden nicht gespeichert!)
* **LEERTASTE** - Umschalten zwischen Navigations- und Editiermodus
* **0-9** - Geländetyp festlegen

## Hinzufügen eigener Karten

Eigene Karten von Städten, Ländern, Dungeons etc. müssen als .png-Datei vorliegen und ins Arbeitsverzeichnis des Java-Programms (neben die Beispieldatei **pergament.png**) kopiert werden.
Beim ersten Speichern mit **F5** wird dann eine gleichnamige .json-Datei angelegt, die die Streckeninformationen des Netzes enthält.

## Mehr Informationen

Ein kleine Demo der Fähigkeiten des Editors kann in folgender Ausgabe der SchelmSchau bewundert werden:

[![SchelmSchau 160](https://img.youtube.com/vi/1FJq-mWnLy8/0.jpg)](https://www.youtube.com/watch?v=1FJq-mWnLy8)
