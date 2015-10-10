# Untitled Java 68HC11 emulator
2006-2007 Paul Kratt

This is the source code for my Java 68HC11 emulator. It was developed in my spare time beginning late Spring 2006. The last significant changes made to it occurred around February 2007. Approximately 99% of the 68HC11 opcodes are supported, with the exceptions being TEST and SWI. Hardware interrupts are not yet supported, although the basic structure for them is there which is used to support emulator breakpoints. Some instructions (DAA for example) are not implemented correctly, and I haven't isolated all of these. All of the code in the project was written by me unless otherwise noted in source code comments. This is the only Java implementation of an HC11 emulator that I am aware of. Basic programs written in assembly will work and debug just fine, compiled C or C++ code is more likely to run into an incorrectly handled instruction.

Loading executables in ELF format is reccomended, although S19 is accepted as well. A sample program is provided in the resources directory. This will output scrolling text to the LCD.

The program was written under the NetBeans IDE, and its project folder is what has been included here. The visual GUI editor was used for the user interface of the application, so NetBeans is required to modify that. However, the program can also be compiled using Apache ANT. A compiled .JAR is provided in the dist folder.

The program was modeled after the FOX11 development board, which was what MSOE was using for assembly courses in Spring 2006. They stopped using the 6811 after that point. The keypad attachment for this device is emulated, as well as the LED's (as text) and VERY BASIC support for the LCD screen. For the LCD screen, any code that calls the LCD functions built into the FOX11 buffalo rom should work just fine. To use these functions however, a dump of the ROM on the board is required. A dump (extracted from memory) is provided in the Resources folder, as well as a compiled program that uses it. I am not aware of what license exists on the ROM data, but they're not selling the boards anymore so I don't see any harm in including it. The file config.ini allows you to define the paths to binary files to be loaded upon launch of the emulator.

The source code to this application is released under the MIT License. This applies only to the code, it does apply to any binary data included with the code such as sample applications, images, or rom data. Basically, if the file doesn't have my name in it, it's not covered by the license.

![Screenshot of emulator]
(https://raw.githubusercontent.com/Sappharad/Java-68HC11-Emulator/master/Resources/hc11emu_osx.png)