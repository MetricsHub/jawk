# AWK in Java

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

## Getting Started

Add Jawk in the list of dependencies in your [Maven **pom.xml**](https://maven.apache.org/pom.html):

```xml
<dependencies>
  <!-- [...] -->
  <dependency>
    <groupId>org.metricshub</groupId>
    <artifactId>jawk</artifactId>
    <version>${project.version}</version>
  </dependency>
</dependencies>
```

## Examples

### Invoke AWK scripts files on input files

```java
/**
 * Executes the specified AWK script
 * <p>
 * @param scriptFile File containing the AWK script to execute
 * @param inputFileList List of files that contain the input to be parsed by the AWK script
 * @return the printed output of the script as a String
 * @throws ExitException when the AWK script forces its exit with a specified code
 * @throws IOException on I/O problems
 */
private String runAwk(File scriptFile, List<String> inputFileList) throws IOException, ExitException {

    AwkSettings settings = new AwkSettings();

    // Set the input files
    settings.getNameValueOrFileNames().addAll(inputFileList);

    // Create the OutputStream, to collect the result as a String
    ByteArrayOutputStream resultBytesStream = new ByteArrayOutputStream();
    settings.setOutputStream(new PrintStream(resultBytesStream));

    // Sets the AWK script to execute
    settings.addScriptSource(new ScriptFileSource(scriptFile.getAbsolutePath()));

    // Execute the awk script against the specified input
    Awk awk = new Awk();
    awk.invoke(settings);

    // Return the result as a string
    return resultBytesStream.toString(StandardCharsets.UTF_8);

}
```

### Execute AWK script (as String) on String input

```java
/**
 * Executes the specified script against the specified input
 * <p>
 * @param script AWK script to execute (as a String)
 * @param input Text to process (as a String)
 * @return result as a String
 * @throws ExitException when the AWK script forces its exit with a specified code
 * @throws IOException on I/O problems
 */
private String runAwk(String script, String input) throws IOException, ExitException {

    AwkSettings settings = new AwkSettings();

    // Set the input files
    settings.setInput(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

    // We force \n as the Record Separator (RS) because even if running on Windows
    // we're passing Java strings, where end of lines are simple \n
    settings.setDefaultRS("\n");

    // Create the OutputStream, to collect the result as a String
    ByteArrayOutputStream resultBytesStream = new ByteArrayOutputStream();
    settings.setOutputStream(new UniformPrintStream(resultBytesStream));

    // Sets the AWK script to execute
    settings.addScriptSource(new ScriptSource("Body", new StringReader(script), false));

    // Execute the awk script against the specified input
    Awk awk = new Awk();
    awk.invoke(settings);

    // Return the result as a string
    return resultBytesStream.toString(StandardCharsets.UTF_8);

}
```

## Javadoc

* [AwkSettings](apidocs/org/metricshub/jawk/util/AwkSettings.html)
* [Awk](apidocs/org/metricshub/jawk/Awk.html)

## Java Scripting API (JSR 223)

**Jawk** can be invoked via the JSR 223 scripting API (J2SE 6).
