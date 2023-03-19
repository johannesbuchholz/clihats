# Welcome to CliHats

CliHats is a java library to expose aspects of an existing program to the command line: Extend your program with a command-line interface like putting a hat on.

CliHats offers
- Declarative and functional API
- Automatic selection of commands based on user inputs
- Option parsing
- Automatically generated help pages and error messages

# Get CliHats
Clihats is available on Maven Central. Depending on your build tool, add one of the following dependency declarations to your project.
## Maven
```xml
<dependency>
    <groupId>io.github.johannesbuchholz</groupId>
    <artifactId>clihats</artifactId>
    <version>0.0.1</version>
</dependency>
```

## Gradle
```groovy
implementation 'io.github.johannesbuchholz:clihats:0.0.1'
```
If you want to use CliHats' annotation processor, you may add the following to your dependency section
```groovy
annotationProcessor 'io.github.johannesbuchholz:clihats:0.0.1'
```

# Quickstart

In this example we will use CliHats to make multiple existing methods available through a command-line interface.

Assume the below "Hello, World!" example.

```java
/**
 * A simple "Hello, World!" program to demonstrate the capabilities of CliHats.
 */
public class HelloWorldCliHats {

    /**
     * Simply prints "Hello, World!".
     */
    public static void sayHello() {
        System.out.println("Hello, World!");
    }

    /**
     * Prints a greeting to the specified name.
     * @param name The name to greet.
     * @param polite If true, additionally prints "Nice to meet you!".
     */
    public static void sayHelloToPerson(String name, Boolean polite) {
        System.out.printf("Hello, %s!\n", name);
        if (polite)
            System.out.println("Nice to meet you!");
    }

}
```

We declare a command-line interface by annotating our class with `@CommandLineInterface`.

```
@CommandLineInterface
public class HelloWorldCliHats {
    // ...
}
```

Next, we add the first command to our command-line interface by annotating the method `sayHello` with `@Command`.

```
@Command
public static void sayHello() {
    System.out.println("Hello, World!");
}
```

By default, CliHats will automatically group commands to the first command-line interface it finds in the upwards package tree. In our case this is `HelloWorldCliHats`.

We proceed in a similar fashion with the second method `sayHelloToPerson`. This time, we need CliHats to also set method parameters. We accomplish this by adding `@Option` to each parameter.

```
@Command
public static void sayHelloToPerson(
        @Option(necessity = OptionNecessity.REQUIRED) String name,
        @Option(flagValue = "true", defaultValue = "false") Boolean polite
) {
    System.out.printf("Hello, %s!\n", name);
    if (polite)
        System.out.println("Nice to meet you!");
}
```

Finally, it is left to add a java entrypoint to our program. In order to let CliHats choose commands and parse options, we need to pass the received String arguments to the command-line interface we want to invoke.
For this, we load our previously declared command-line interface with `CliHats.get(HelloWorldCliHats.class)` and run it by calling `execute` passing the given arguments.

```
public static void main(String[] args) {
    CliHats.get(HelloWorldCliHats.class).execute(args);
}
```

The complete class definition now looks like this:

```java
/**
 * A simple "Hello, World!" program to demonstrate the capabilities of CliHats.
 */
@CommandLineInterface
public class HelloWorldCliHats {

    public static void main(String[] args) {
        CliHats.get(HelloWorldCliHats.class).execute(args);
    }

    /**
     * Simply prints "Hello, World!".
     */
    @Command
    public static void sayHello() {
        System.out.println("Hello, World!");
    }

    /**
     * Prints a greeting to the specified name.
     * @param name the name to greet.
     * @param polite if true, additionally prints "Nice to meet you!".
     */
    @Command
    public static void sayHelloToPerson(
            @Option(necessity = OptionNecessity.REQUIRED) String name,
            @Option(flagValue = "true", defaultValue = "false") Boolean polite
    ) {
        System.out.printf("Hello, %s!\n", name);
        if (polite)
            System.out.println("Nice to meet you!");
    }

}
```

Executing the above program without arguments results in a message similar to the following:

```
Help for hello-world-cli-hats
A simple "Hello, World!" program to demonstrate the capabilities of Clihats.

Commands:
say-hello           Simply prints "Hello, World!".
say-hello-to-person Prints a greeting to the specified name.
```

If we run `{"say-hello-to-person", "--help"}` we receive a help message like this:

```
Help for say-hello-to-person                                                    
Prints a greeting to the specified name.                                        

Options:                                                                        
-n --name   (required) The name to greet.                               
-p --polite (flag)     If true, additionally prints "Nice to meet you!".
```

Running `{"say-hello-to-person", "-n", "John", "--polite"}` yields

```
Hello, John!
Nice to meet you!
```

But if we do not pass option `-n` and run `{"say-hello-to-person", "--polite"}`, we receive

```
Error running hello-world-cli-hats: Exception during invocation of command say-hello-to-person: Invalid input arguments:
> Missing required arguments:
    -n
```

The above program is a tiny example mostly using default behaviour. There are options to configure parameter names and aliases, value mapping, parsing behaviour and exception handling.

# Core concept
The general concept follows a rather functional style in which a command-line interface is represented by a set of methods. Each method models one task like printing a program version, loading data from a file or sending a web-request. A task may require additional input like a filename, a web-address or credentials. These are provided by the user via options.

Using CliHats, a command-line interface consists of one `Commander`. The commander possesses a collection of `Command` objects. Each command is responsible for one task represented by an `Instruction` and a list of `AbstractOptionParser` objects. Option parsers extract parameters from the command-line arguments and make them available to the command being executed.

Although CliHats offers these structures as public classes which may be constructed and called "by hand", CliHats also comes with an annotation processor, automatically creating commanders, commands and option parsers based on annotations. One may fall back to manually defining a commander when more control over the configuration is required.

As an example, the generated Commander of the above "Hello, World!" setup looks like this:

```
Commander.forName("hello-world-cli-hats")
        .withDescription("A simple \"Hello, World!\" program to demonstrate the capabilities of CliHats.")
        .withCommands(
                Command.forName("say-hello")
                        .withInstruction(args -> HelloWorldCliHats.sayHello())
                        .withDescription("Simply prints \"Hello, World!\"."),
                Command.forName("say-hello-to-person")
                        .withInstruction(args -> HelloWorldCliHats.sayHelloToPerson((String) args[0], (Boolean) args[1]))
                        .withDescription("Prints a greeting to the specified name.")
                        .withParsers(
                                OptionParsers.valued("-n").withAliases("--name").isRequired(true).withDescription("the name to greet."),
                                OptionParsers.flag("-p").withAliases("--polite").withFlagValue("true").withDefault("false").withMapper(new BooleanMapper()).withDescription("if true, additionally prints \"Nice to meet you!\"."))))
```
# Build your cli
This section describes how to put all the pieces together.

## Defining command-line interfaces - `@CommandLineInterface`
A class annotated with `@CommandLineInterface` defines a command-line interface serving as anchor for attaching commands. Per default, the name is set to the hyphenated class name.

## Defining commands - `@Command`
A `public static void` method annotated with `@Command` defines a command. Each command needs to be attached to one command-line interface in order to be executable by CliHats.
CliHats automatically attaches commands to the first command-line interface within the upwards package tree of the command containing class. Alternatively, explicit attaching is possible via parameter `cli`.
A command method must not define any primitive parameter.  
Per default, the command name is equal to the hyphenated name of the annotated method. It is also possible to explicitly set the name using parameter `name`.

## Defining options - `@Option`
A method parameter annotated with `@Option` defines an option. Options only take effect on methods annotated with `@Command`. It is not required to declare annotate all method parameter. Non-option parameters are set to `null` whenever the respective command is invoked by CliHats.
If a command is invoked by CliHats, each registered option parser scans the available input arguments and provides a value to its respective method parameter.

### Option types
Currently, there are three different option types available: valued options, flag options and positional options.
- Valued option extract named user defined values like `--name "John"` or `--numbers=1,2,3,4`.
- Flag options are named switches providing one of two predefined values. Think of something like `--dry-run` or `--verbose`.
- Positional options extract user defined values from a specific index. In this sense, the command `mkdir ./tmp` takes `./tmp` as a positional argument from index 0. These parsers are invoked last.

The parser type is automatically deduced from the option configuration. A positional options is created if the parameter `position` is set to a non-negative integer. A flag option is assumed if the parameter `flagValue` is set to a non-empty string. Otherwise, a valued option is created.

### Option names and aliases
Valued and flag options may be named individually via parameter `name`. This parameter takes an array of String values, where the first entry denotes the name. All following names are used as aliases.
If `name` is not set, the option name is automatically set to `-` plus the first character of the parameter name. In that case, the alias name is also automatically set to the hyphenated parameter name prefixed with `--`.
Positional options are not callable by their name and therefore only possess a name for internal identification.

### Option necessity
Each option possesses a `necessity` determining CliHats behaviour in case the respective option is missing.
- `OPTIONAL` The default for each Option. Results in the parameter value `null` if the option is not present.
- `REQUIRED` If set, missing the respective option aborts the execution with an error message.
- `PROMPT` A missing option with this necessity lets CliHats ask for manual user input over the command line.
- `MASKED_PROMPT` Like PROMPT but hiding typed characters.

If manual user input from a console is requested, CliHats tries to call `System.console()` which may fail if there is no console available to the running jvm.

### Value mapping
By default, option parsers provide their values as `java.lang.String`. If a command method requires other parameter types, a `Mapper` can be specified via the `mapper` parameter. CliHats is able to recognize a number of commonly used types and to automatically configure a suiting mapper. Currently implemented standard mappers are available for these types: 
- `Path`
- `Boolean`
- `Integer`
- `Double`
- `Float` 
- `LocalDate` 
- `LocalDateTime`
- `BigDecimal`
Explicitly declaring a mapper overrides this behaviour.

It is noteworthy that a missing non-required option skips value mapping and the respective parameter is directly set to `null`.

### Default values
If an option is missing, their respective parameter is set to `null`. However, it is possible to declare a custom default value using parameter `defaultValue` which then behaves as if the user had provided that value. In particular, this means that value mapping applies. For flag options, the parameter `flagValue` behaves similarly.

Default values are only relevant for non-required options.

## Configuring help pages
There are two ways to define the content of help pages: Either add javadoc comments to a command method or provided a description using the parameter `description`. Using the parameter overrides the javadoc description.
The javadoc of a command-annotated method may also contain `@param` tags to conveniently provide descriptions for a command's options.

Analogously, use javadoc or the parameter `description` when defining help pages for a command-line interface declaration.

CliHats triggers help printing for a command-line or for a particular command if the last passed option is equal to `--help`. Alternatively, passing zero arguments to the command-line interface also calls for help.

## Exception handling
Using the method `execute(String[] args)` on the `Cli` object returned from `CliHats.get(Class<?> commandLineInterface)` includes CliHats' automatic exception handling. That is, the cli catches exceptions, prints an appropriate message to `System.err` and exits the jvm with a non-zero exit code. If custom exception handling is desired, use `executeWithThrows(String[] args)` instead and handle thrown exceptions manually.

It is worth mentioning that help calls also result in an exception. In that case, CliHats exception handling prints help to `System.out` and exits the jvm normally with code `0`. Modelling help calls as an exception enables custom processing of help-calls when using `executeWithThrows(String[] args)`.

## Use case examples
This section aims to provide common use case examples when defining options.

### Receive a username
Receiving a username from the command-line may be done by declaring an option as follows:
```
@Option(necessity = OptionNecessity.PROMPT) String name
```
One now may declare the name by providing `-n <name>` or `--name <name>`. If not providing the above option, CliHats will ask the user for manual input.

### Receive a password
Receiving a password from the command-line and not showing already typed characters, one may declare an option as follows:
```
@Option(necessity = OptionNecessity.MASKED_PROMPT) String password
```
One now may declare the password by providing `-p <name>` or `--password <name>`. If one does not provide the above option, CliHats will ask for manual input, hiding typed characters.

Note: Be aware that CliHats handles user inputs from command-line arguments and from console input as String objects. There are discussions about how to handle sensitive data like passwords regarding immutability of String objects and them remaining accessible in memory until garbage collection is performed. It is encouraged to use `char[]` for password handling in order to minimize the duration in which sensitive data remains in memory by emptying said array. For now, CliHats still uses String objects for transporting user input data.

### Receive a boolean flag
A simple switch-like option may be defined as follows
```
@Option(flagValue = "true", defaultValue = "false") Boolean flag
```
Dropping `defaultValue` results in passing `null` to the parameter whenever the flag is not present.

Note: CliHats will automatically use a value mapper for some commonly used types like `java.lang.Boolean`. The specified String values from this example are then transformed using said mapper.

### Load file contents
Obtaining some file content as `InputStream` might look like this:
First define a public mapper class extending `AbstractValueMapper`.
```java
public class InputStreamMapper extends AbstractValueMapper<InputStream> {

        @Override
        public InputStream map(String filePath) {
            Path p = Path.of(filePath);
            if (!Files.isRegularFile(p)) {
                throw new IllegalArgumentException("Path does not point to a regular file " + p);
            }
            try {
                return Files.newInputStream(p);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not create input stream: " + e.getMessage(), e);
            }
        }
        
    }
```

Then, declare an option using the above mapper.
```
@Option(mapper = InputStreamMapper.class) InputStream file
```

Make sure to close the opened InputStream when processing is done.

### Receive a list of String
Collecting multiple values into a list argument is possible by defining a suiting mapper.

A mapper returning multiple `String` objects might be defined as follows.

```java
public static class ListMapper extends AbstractValueMapper<List<String>> {
        
        private static final String separator = ",";

        @Override
        public List<String> map(String str) {
            return Arrays.stream(str.split(separator))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

}
```

Then, declare an option using the above mapper.
```
@Option(mapper = ListMapper.class) List<String> list
```

# Build configuration
CliHats uses an annotation processor to automatically create Commander, Command and Option objects and conveniently make it available to your code through the clas `CliHats`. The process of generating the required source code may be configured.

## Logging
CliHats uses Slf4j with the Simple provider and one single logger instance named `CliHats`. You may configure the log level used during processing by adding the jvm argument
```text
-Dorg.slf4j.simpleLogger.log.CliHats=<YOUR DESIRED LOG LEVEL>
```
