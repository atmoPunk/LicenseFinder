# LicenseFinder

Simple Kotlin app that searches directories for software licenses

Currently searches for MIT, Apache 2.0, LGPL 3.0, GPL 3.0 and BSD 3-Clause licenses

Uses simple text search, does not search binary files

if directory contains file ``LICENSE``, ``LICENCE`` or ``COPYING`` with possible extensions, then it will be designated as the main license

## Building

import project into IntelliJ IDEA, and build artifact FindLicenses:jar

## Usage

```bash
java -jar FindLicenses.jar directory
Licenses found:
MIT
APACHE2
Main license: UNKNOWN # unknown means that file with main license is found, but the license is unknown to the app
```
