# Welcome to CliHats

![Maven Central](https://img.shields.io/maven-central/v/io.github.johannesbuchholz/clihats)
![GitHub repo size](https://img.shields.io/github/repo-size/johannesbuchholz/clihats)

<!-- tag::readme-intro[] -->
CliHats - The java library to easily expose code to the command line. Building a command-line interface becomes as simple as putting on a hat.
<!--  end::readme-intro[] -->

<!--  tag::readme-schema[] -->
<pre>
                    public static void main(String[] args)
                                       |
                                       |
                                       V
                                  |---------|     
             -------------------- | <b>CliHats</b> | --------------------    
             |                    |---------|                    |                                
             |                         |                         V                     
             |                         |    public static void <b>upload</b>(String name, String pw, Path... files)                              
             V                         |  
public static void <b>status</b>();           |
                                       V
                     public static void <b>loadConfig</b>(Path configPath)                  
                                
</pre>
<!--  end::readme-schema[] -->

CliHats offers
<!-- tag::readme-offers[] -->
- Declarative and functional API
- Automatic selection of commands based on user inputs
- Option parsing
- Automatically generated help pages and error messages
<!-- end::readme-offers[] -->

## Quickstart
Create an entry point mapping to your command line interface.
```java
/**
 * The Quickstart example of CliHats.
 */
@CommandLineInterface
public class CliHatsQuickstart {

    public static void main(String[] args) {
        CliHats.get(CliHatsQuickstart.class).execute(args);
    }

    /**
     * For some name, prints "Hello, {name}!" multiple times.
     * @param name The name to greet.
     * @param count The number of greetings.
     */
    @Command
    public static void sayHello(
            @Argument String name, 
            @Argument(type = OPERAND, necessity = REQUIRED) Integer count
    ) {
        for (int i = 0; i < count; i++)
            System.out.println("Hello, " + name + "!");
    }

}
```

Compile and run your command by running the above entrypoint with `say-hello --name John 4`
```
Hello, John!
Hello, John!
Hello, John!
Hello, John!
```

Alternatively, get the command help page by running `say-hello --help`
```
Help for say-hello                                                              

Synopsis:
say-hello [-n <value>] COUNT

Prints "Hello {name}" multiple count.                                           

Arguments:                                                                      
-n    --name            The name to greet.      
COUNT        (required) The number of greetings.
```

## Why CliHats?

In contrary to other parameter parsers like [JCommander](https://github.com/cbeust/jcommander), CliHats directly links java methods to the command-line.

This is advantageous when orchestrating multiple functionalities of your code neatly behind one command line interface while the api is deduced from your java methods.
Let CliHats care for argument parsing, choosing the right command and generating help pages while you design your access points: API first becomes a breeze.

To make a long story short:
- CliHats provides a functional view on building a command-line interface.
- CliHats is a cli-builder. You declare the api and CliHats builds corresponding links from input arguments to methods.
- CliHats is slim, uses plain java and is basically reflection-free.
- CliHats lets you adopt a command-line interface in no time, potentially not even requiring you to touch your current code's logic.

### When to use CliHats?
If you want to offer your users a toolset of methods behind one single java API, CliHats will come in handy. 
This is especially true, if the methods posses relatively simple signatures with few parameters and are easily deducible from String values.

## Get CliHats
CliHats is available on [Maven Central](https://mvnrepository.com/artifact/io.github.johannesbuchholz/clihats).

### Maven
```xml
<dependency>
    <groupId>io.github.johannesbuchholz</groupId>
    <artifactId>clihats</artifactId>
    <version>1.0.1-2-296fee82-SNAPSHOT</version>
</dependency>
```

### Gradle
```groovy
implementation group: 'io.github.johannesbuchholz', name: 'clihats', version: '1.0.1-2-296fee82-SNAPSHOT'
```

## Documentation
The [documentation](https://johannesbuchholz.github.io/clihats/doc.html) contains a quickstart example and details on how to use and configure CliHats to your needs. 