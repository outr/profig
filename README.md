# profig
Powerful configuration management for Scala (JSON, properties, command-line arguments, and environment variables)

# 1.0.0

* [X] Merge support
    * [X] Defaults support (only apply if value doesn't already exist)
* [X] Load command-line arguments
* [X] Load environment variables
* [X] Load properties
* [X] Loading case classes
* [X] Storing case classes
* [X] Trait for application startup (JVM and Scala.js)
* [ ] Loading
    * [ ] JSON
        * [X] Directly
        * [ ] From Disk (JVM-only)
        * [ ] From ClassLoader
    * [ ] Properties
        * [X] Directly
        * [ ] From Disk (JVM-only)
        * [ ] From ClassLoader
    * [ ] Automatic lookup of default locations
* [ ] Save to disk
* [ ] Support change notification
* [ ] Document classes
* [ ] Document README