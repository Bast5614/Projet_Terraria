# Architecture

## UML

![UML](https://user-images.githubusercontent.com/72931362/123392369-43435500-d59d-11eb-8286-d0fffb3423de.png)

## Packages structure

The first class called is DesktopLauncher (in [`/TERRARIA/desktop`](/TERRARIA/desktop)) which setup the window and then calls the class TerrariaGame (in [`/TERRARIA/core`](/TERRARIA/core)) that directly calls the MainMenuScreen class in the Screens package,

the project is then structured in 2 main packages (in the [`core file`](/TERRARIA/core)):

* Actors : 
  * Entities    (handles the player and all mobs)
  * Inventory   (handles the inventory and craft system)
  * World       (handles the map generator, define every tiles and define every operation from the player on the map)

* Screens       (handles each screens of the game and their inputs)

    
## Libraries
We are using Gradle to manage dependencies
