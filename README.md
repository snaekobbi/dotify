[![Build Status](https://travis-ci.org/joeha480/dotify.svg)](https://travis-ci.org/joeha480/dotify)

**Future releases of Dotify CLI will be found at https://github.com/brailleapps/dotify-cli**

Note: Dotify will be split up into smaller repos (at https://github.com/brailleapps) very soon. This repo will only be retained for archival purposes.
For more information, see https://github.com/joeha480/dotify/issues/175


# Dotify
Dotify Braille Translation System is an open source Braille translator written in Java.  Dotify is designed for collaborative, open source braille software development.

## Features
Dotify delivers a unique set of abilities that, once combined, no other Braille production framework can offer today.

### Java Based
Dotify is a pure Java framework which makes it suitable for various applications including workstations, servers and embedded systems without changes to the code. Run on a server today and embed the same code in a device tomorrow.

### Designed for High-Volume, Automated Use
Dotify has been designed for high-volume, automated use from the very beginning, prioritizing reliability, speed and error free output over a "best effort" approach typically utilized in end-user systems.

### Designed for High Quality Braille
Dotify does not compromise with braille quality in order to fit a set of predetermined design restrictions. If a feature is required for a locale, we aim to make it possible to implement. Dotify does not tell you what good Braille should be like.

### Open Source
Dotify is Open Source, this means that everyone has the right to use it and change it.

### Modular
Dotify aims at a modular design of functional units that can be replaced or integrated into other solutions individually. The core components are compatible with both OSGi and SPI environments and are available for download from [the central repository](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.daisy.dotify%22).

## Performance
About 10 seconds/book or 70 braille pages/second (based on a selection of novels, utilizing a single core of a modern PC and including JVM startup).
