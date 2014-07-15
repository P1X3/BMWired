## About
BMWinterface is a project for android devices running 4.x+, whose sole purpose is to interface with I-Bus in BMW vehicles.

## Building
Add [usb-serial-for-android](https://github.com/mik3y/usb-serial-for-android) to project dependency list. Build.

## How to use
1. Install the application
2. Create desktop widget
3. Insert USB interface and allow activity to run when notification appears

## Usage notes
* Widget is not required

## Implemented features
* Volume Up&Down: Tested
* Next&Previous: Not tested

## FAQ

**Q:** What interfaces can be used?

**A:** BMWinterface has been designed for [IBUS interface from Rolf Resler](http://www.reslers.de/IBUS/). It may work with other interfaces, but they have to be added to probe list, tested, and then added to release build. 

**Vednor ID:** 0x10C4

**ProductID:** 0x8584
