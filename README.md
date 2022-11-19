<p align="center">
    <img alt="GuiApi" src=".assets/GuiApi-logo.svg" width="250" />
</p>

<h3 align="center"> A library for creating component-based animated Minecraft inventory GUIs.</h3>

---

Das Ziel dieses Projektes ist es, GUIs möglichst einfach erstellen zu können.
Dabei wird großer Wert darauf gelegt, dass auch komplexe GUIs (z.B. mit Animationen)
unterstützt werden.
Die grundlegende Komponenten-Struktur ermöglicht es, einzelne Komponenten
in verschiedenen GUIs wiederzuverwenden und aus externen Quellen zu beziehen.
Dies führt zu einem stark reduzierten Aufwand, da häufig verwendete GUI-Strukturen
(z.B. Listen mit mehreren Seiten) nicht immer erneut programmiert werden müssen.

## Oberflächen-Typen

Die GUI kann auf verschiedenen Oberflächen angezeigt werden.
Momentan werden drei Oberflächen in Minecraft-Inventaren unterstützt.

### ChestInventory

Dies ist die wahrscheinlich populärste Oberfläche.
Sie besteht aus dem Inventar einer Kiste
und kann eine bis sechs Zeilen groß sein.
Unter ihr wird das aktuelle Spieler-Inventar angezeigt.
Dieses gehört allerdings nicht zu der Oberfläche
und die Nutzung davon kann deaktiviert werden.

![](.assets/readme/ChestInv_dp.png)

### MergedInventory

Diese Oberfläche verbindet das [ChestInventory](#chestinventory) mit dem
darunterliegenden [PlayerInventory](#playerinventory).
Alle zuvor im Spieler-Inventar vorhandenen Items werden beim Öffnen des GUI entfernt
und beim Schließen wiederhergestellt.

![](.assets/readme/MergedInv_dp.png)

### PlayerInventory

Diese Oberfläche besteht aus dem Spieler-Inventar des Spielers und besitzt vier Zeilen.
Alle zuvor im Inventar vorhandenen Items werden beim Öffnen des GUI entfernt
und beim Schließen wiederhergestellt.

![](.assets/readme/Playerinv_dp.png)

## Komponenten

Alle Elemente des GUI bestehen aus Komponenten.
Komponenten als Ganzes können wiederum als Unterkomponenten in
andere Komponenten eingefügt werden.
Komponenten können jegliche Form annehmen
und auch nicht miteinander zusammenhängende Slots besitzen.
Der Index einer Komponente verläuft immer von der obersten Zeile nach unten
und jeweils pro Zeile von links nach rechts.

### ReservedSlots

ReservedSlots legen fest, wie Komponenten im Zweidimensionalen aufgebaut sind.
Dafür wird ein zweidimensionales Array an booleans benutzt.
Jeder boolean, welcher true ist, markiert einen Slot, welcher der Komponente zugehörig ist.
Jeder boolean, welcher auf false gesetzt wurde, gehört wiederum nicht zu
der Komponente und dient als Abstandshalter für einen zugehörigen Slot.

Dieses Beispiel zeigt die ReservedSlots der roten Komponente aus dem Beispiel
von [Verschachtelung](#verschachtelung).

![](.assets/readme/Component_str.png)

![](.assets/readme/green.png) ReservedSlots -> true <br>
![](.assets/readme/red.png) ReservedSlots -> false <br>
![](.assets/readme/number.png) Index der Komponente

Die ReservedSlots dieser Komponente:

```
[
    [false, true],
    [true, false, true],
    [false, true]
]
```

### Verschachtelung

Komponenten können Unterkomponenten jeglicher Form enthalten,
solange alle ReservedSlots der Unterkomponente auch in der eigentlichen Komponente reserviert sind.
Jede Komponente besitzt dabei einen eigenen Index,
welcher sich über alle in der Komponente reservierten Slots erstreckt.

Dieses Beispiel zeigt eine Komponente (grün), welche einige Unterkomponenten besitzt.
Dazu werden in den jeweiligen Farben der Komponenten die Indizes aller Komponenten angegeben.

![](.assets/readme/Components_ex.png)

![](.assets/readme/comp_border.png) Abgrenzung der Komponente einer Farbe<br>
![](.assets/readme/number.png) Index der Komponente einer Farbe<br>
![](.assets/readme/startIndex.png) [Start-Index](#start-index) der Komponente einer Farbe

#### Start-Index

Um eine Unterkomponente einzufügen, wird ein Start-Index benötigt,
welcher als Ankerpunkt der Unterkomponente auf der übergeordneten Komponente dient.
Auf dem angegebenen Start-Index liegt der erste Eintrag der [ReservedSlots](#reservedslots).
Es macht dabei keinen Unterschied, ob der erste Eintrag reserviert ist, oder nicht.

Generell kann sich gemerkt werden: Wenn man um die ganze Komponente ein Rechteck zieht,
ist der Start-Index immer der Slot oben links in der Ecke.

### Item-Component

Die [ItemComponent](./src/main/kotlin/net/bestlinuxgamers/guiApi/component/essentials/ItemComponent.kt)
ist eine Komponente, welche einen Minecraft-ItemStack abbildet.
Sie muss benutzt werden, um einzelne Items in die Komponenten-Struktur einzubringen.

## Rendering

Vor jedem Rendervorgang (Aktualisierung) des GUI wird die
[GuiComponent](./src/main/kotlin/net/bestlinuxgamers/guiApi/component/GuiComponent.kt)#beforeRender Methode
in allen (Unter-)Komponenten aufgerufen.

### Render-Trigger

Teilweise ist es nötig, die GUI noch nach dem initialen Öffnen zu verändern.
Das kann zum Beispiel bei Animationen, oder bei gewünschten Veränderungen
durch einen Klick, der Fall sein.

#### On-Demand

On-Demand-Rendering bietet eine Möglichkeit, die GUI bei Bedarf zu aktualisieren.
Das Feature muss auf Komponenten-Ebene aktiviert werden und ermöglicht die Nutzung der
[GuiComponent](./src/main/kotlin/net/bestlinuxgamers/guiApi/component/GuiComponent.kt)#triggerReRender Methode.
Sobald diese Methode aufgerufen wird, aktualisiert sich die GUI.

#### Render-Tick

Der Render-Tick stellt eine periodische Routine dar, welche den Aufruf der
[GuiComponent](./src/main/kotlin/net/bestlinuxgamers/guiApi/component/GuiComponent.kt)#onRenderTick Methode
in allen (Unter-)Komponenten und einen anschließenden Rendervorgang zur Folge hat.
Der Tick muss auf der höchsten Komponenten-Ebene aktiviert werden
und die Schnelligkeit muss eingestellt werden.

### Render-Einstellungen

Der Rendervorgang kann in einigen Punkten angepasst werden,
was zu einer niedrigeren Renderzeit führen kann.

#### Static

Wenn eine Komponente als static markiert wurde, wird entgegen aller anderen
Einstellungen keine Aktualisierung vorgenommen.

#### Smart-Render

Dieses Feature erkennt Veränderungen an Komponenten
und ermöglicht es, nur Komponenten mit Veränderungen neu zu rendern.
Smart-Rendering muss auf Komponenten-Ebene aktiviert werden.

## Code-Beispiel

Das folgende Beispiel wurde in Kotlin programmiert.
Genaue Erklärungen, welche die volle Funktionalität der API erläutern,
sind im [Wiki](https://github.com/bestlinuxgamers/GuiApi/wiki) zu finden.

### Gui erstellen

```kotlin
import net.bestlinuxgamers.guiApi.gui.ChestInventoryGui
import net.bestlinuxgamers.guiApi.provider.GuiInstancesProvider
import org.bukkit.entity.Player

class TestGui(player: Player, guiInstancesProvider: GuiInstancesProvider) :
    ChestInventoryGui(
        player,
        "TITLE",
        6, //lines
        guiInstancesProvider.eventDispatcher,
        guiInstancesProvider.schedulerProvider,
        renderTick = false,
        onDemandRender = false
    ) {

    override fun setUp() {
        val component = ItemComponent(ItemStack(Material.STICK))

        component.setClickable { event, clickedComponent ->
            event.whoClicked.sendMessage("You clicked slot $clickedComponent")
        }

        setComponent(component, 0)
    }

    override fun beforeRender(frame: Long) {
    }

    override fun onRenderTick(tick: Long, frame: Long) {
    }
}
```

### Gui nutzen

```kotlin
val gui = TestGui(player, guiInstancesProvicer)

//open
gui.open()

//close
gui.close()
```
