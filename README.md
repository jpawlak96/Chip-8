# CHIP-8-Emulator
![blinky_screenshot.jpg](assets%2Fblinky_screenshot.jpg)

Basic implementation of Chip-8[^1] emulator in Java 17. Library used to handle screen, time and keyboard is JavaFX.

### Requirement
* Java 17+
* Maven

### Usage
```bash
mvn clean javafx:run
```

### Changelog
* 18.03.2023: Emulator support only few instructions needed to run the simplest program - IBM Logo.
    > List of current instructions:
    > * 00E0 (clear screen)
    > * 1NNN (jump to NNN)
    > * 6XNN (set NN in register X)
    > * 7XNN (add NN to register X)
    > * ANNN (set NNN in index register)
    > * DXYN (draw N rows indicated by index register)

    Screen dump after executing of the program:
    ```
                                                                    
                                                                    
                                                                    
                                                                    
                                                                    
                                                                    
                                                                    
                                                                    
                ######## #########   #####         #####            
                                                                    
                ######## ########### ######       ######            
                                                                    
                  ####     ###   ###   #####     #####              
                                                                    
                  ####     #######     ####### #######              
                                                                    
                  ####     #######     ### ####### ###              
                                                                    
                  ####     ###   ###   ###  #####  ###              
                                                                    
                ######## ########### #####   ###   #####            
                                                                    
                ######## #########   #####    #    #####            
                                                                    
                                                                    
                                                                    
                                                                    
                                                                    
                                                                    
                                                                    
                                                                    
                                                                    
    ```
* 19.03.2023: Added processor tests. Creating the tests helped improve the 7XNN instruction to be more consistent with the documentation.
* 20.03.2023: Incrementation of program count moved to fetch step. Good practice to keep code clean.
* 21.03.2023: All 36 chip-8 commands were implemented. ROM "test_opcode.ch8" works correctly:
  ```
  ████████████████████████████████████████████████████████████████
  █   █ █ ██   █ █ ██████   █   ██   █ █ █████   ██  █   █ █ █████
  ██  ██ ███ █ █  ███████ █ █  ███ █ █  ██████   ██ ██ █ █  ██████
  ███ █ █ ██ █ █ █ ██████ █ █ ████ █ █ █ █████ █ ███ █ █ █ █ █████
  █   █ █ ██   █ █ ██████   █   ██   █ █ █████   ██ ██   █ █ █████
  ████████████████████████████████████████████████████████████████
  █ █ █ █ ██   █ █ ██████   █   ██   █ █ █████   █   █   █ █ █████
  █   ██ ███ █ █  ███████   █ █ ██ █ █  ██████   █ ███ █ █  ██████
  ███ █ █ ██ █ █ █ ██████ █ █ █ ██ █ █ █ █████ █ █   █ █ █ █ █████
  ███ █ █ ██   █ █ ██████   █   ██   █ █ █████   █   █   █ █ █████
  ████████████████████████████████████████████████████████████████
  ██  █ █ ██   █ █ ██████   █  ███   █ █ █████   █   █   █ █ █████
  ██ ███ ███ █ █  ███████   ██ ███ █ █  ██████   █  ██ █ █  ██████
  ███ █ █ ██ █ █ █ ██████ █ ██ ███ █ █ █ █████ █ █ ███ █ █ █ █████
  ██ ██ █ ██   █ █ ██████   █   ██   █ █ █████   █   █   █ █ █████
  ████████████████████████████████████████████████████████████████
  █   █ █ ██   █ █ ██████   █   ██   █ █ █████   ██  █   █ █ █████
  ███ ██ ███ █ █  ███████   ███ ██ █ █  ██████ ████ ██ █ █  ██████
  ███ █ █ ██ █ █ █ ██████ █ █  ███ █ █ █ █████  ████ █ █ █ █ █████
  ███ █ █ ██   █ █ ██████   █   ██   █ █ █████ ████ ██   █ █ █████
  ████████████████████████████████████████████████████████████████
  █   █ █ ██   █ █ ██████   █   ██   █ █ █████   █   █   █ █ █████
  █   ██ ███ █ █  ███████   ██  ██ █ █  ██████ ████  █ █ █  ██████
  ███ █ █ ██ █ █ █ ██████ █ ███ ██ █ █ █ █████  ████ █ █ █ █ █████
  █   █ █ ██   █ █ ██████   █   ██   █ █ █████ ███   █   █ █ █████
  ████████████████████████████████████████████████████████████████
  ██ ██ █ ██   █ █ ██████   █ █ ██   █ █ █████  ██ █ █   █ █ █████
  █ █ ██ ███ █ █  ███████   █   ██ █ █  ███████ ███ ██ █ █  ██████
  █   █ █ ██ █ █ █ ██████ █ ███ ██ █ █ █ ██████ ██ █ █ █ █ █ █████
  █ █ █ █ ██   █ █ ██████   ███ ██   █ █ █████   █ █ █   █ █ █████
  ████████████████████████████████████████████████████████████████
  ████████████████████████████████████████████████████████████████
  ```
* 22.02.2023: 
  * After painstaking research and debugging, finally found issues with faulty instructions! 
  * Added JavaFX. Now emulator looks much better :)
  * Added keyboard and sound support, I am able to play all games.


* 23.02.2023: 
  * File chooser allows to select ROM before emulation. Rummaging through the source code is no longer needed to run programs.
  * Refactor Window class by moving code to separate classes. Code looks more readable

[^1]: https://en.wikipedia.org/wiki/CHIP-8
