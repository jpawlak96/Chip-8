# CHIP-8-Emulator
Basic implementation of chip-8[^1] emulator in Java. Currently, all interactions happen at the terminal

### Changelog
* 18.03.2023: Emulator support only few instructions needed to run the simplest program - IBM Logo 
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
[^1]: https://en.wikipedia.org/wiki/CHIP-8
