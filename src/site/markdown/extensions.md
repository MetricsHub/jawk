# Jawk - _Extension Facility_

AWK, while an excellent text processing language, is limited as a general purpose language. For example, it would be impossible to create a socket or display a simple GUI without external assistance either from the shell or via extensions to AWK itself (i.e., gawk). To overcome this limitation, an extension facility is added to **Jawk** .

The Jawk extension facility allows for arbitrary Java code to be called as AWK functions in a Jawk script. These extensions can come from the user (developer) or 3rd party providers (i.e., the Jawk project team). And, Jawk extensions are opt-in. In other words, the `-ext` flag is required to use Jawk extensions and extensions must be explicitly registered to the Jawk instance via the `-Djawk.extensions` property (except for core extensions bundled with Jawk ).

Jawk extensions support **blocking**. You can think of blocking as a tool for extension event management. A Jawk script can block on a collection of blockable services, such as socket input availability, database triggers, user input, GUI dialog input response, or a simple fixed timeout, and, together with the `**-ni**` option, action rules can act on block events instead of input text, leveraging a powerful AWK construct originally intended for text processing, but now can be used to process blockable events. A sample enhanced echo server script is included in this article. It uses blocking to handle socket events, standard input from the user, and timeout events, all within the 47-line script (including comments).

Extensions must operate within Jawk 's memory model. Therefore, extensions must use strings, numbers, and associative arrays to interface with Jawk scripts. For example, the socket creation extension (bundled with Jawk ) passes a string handle back to the caller for referal to the newly created socket.

We will first go over an example of using the extensions bundled with Jawk . Then, we'll cover creating a new extension from scratch.

## Bundled Extensions

Jawk comes bundled with the following extensions:

* **CoreExtension** - A set of extensions which make integrating other extensions into Jawk easier, and development of Jawk scripts easier.
* **StdinExtension** - Extension to look for and read input from stdin, since it is expected extensions run extensions with the -ni option.

Please refer to their individual JavaDocs for a description of their APIs.

### Creating Extensions

Here, we build `FileExtension.java`. The extension module consists of the following extensions:

* FileCreationBlock - Block until any of the files are created.
* FileInfo - Return information about a file. For now, it just returns the last modified time of the file as a String.

The extension's FileCreationBlock will poll to check for the existence of a file via a separate thread, but will appear to simply block from the Jawk script perspective. And, a FileExists function would naturally fit into this extension. However, it is already implemented in the CoreExtension module.

The code for `FileExtension.java` is as follows:

```java
package org.metricshub.jawk.ext;

import org.metricshub.jawk.ext.JawkExtension;
import org.metricshub.jawk.jrt.*;
import org.metricshub.jawk.NotImplementedError;

import java.io.*;
import java.util.*;

// to run:
// java -Djawk.extensions="org.jawk.ext.FileExtension"
//         ... -jar jawk.jar -ext -ni -f {script.awk}

public class FileExtension extends AbstractExtension implements JawkExtension {

  private static final int POLLING_DELAY = 300;

  public String getExtensionName() { return "File Support"; }
  public String[] extensionKeywords() {
    return new String[] {
        "FileCreationBlock",    // i.e., $0 = FileCreationBlock(aa, str, etc)
        "FileInfo",        // i.e., FileInfo(map, "filename")
    };
  }

  private final Map<String,FileWatcher> file_watchers = new HashMap<String,FileWatcher>();
  private BulkBlockObject file_creation_blocker;

  public final void init(VariableManager vm, JRT jrt) {
    super.init(vm, jrt);
    file_creation_blocker = new BulkBlockObject("FileCreation", file_watchers, vm);
  }

  private final class FileWatcher extends Thread implements Blockable {
    private final String filename;
    private final File file;
    private boolean found = false;
    private FileWatcher(String filename) {
        this.filename = filename;
        this.file = new File(filename);
        start();
    }
    public final void run() {
        while (true) {
            if (file.exists())
                synchronized(file_creation_blocker) {
                    found = true;
                    file_creation_blocker.notify();
                    return;
                }
            try { Thread.sleep(POLLING_DELAY) ; } catch (InterruptedException ie) {}
        }
    }
    public final boolean willBlock(BlockObject bo) {
        return ! found;
    }
  }

  public final int[] getAssocArrayParameterPositions(String extension_keyword, int arg_count) {
    if (extension_keyword.equals("FileInfo"))
        return new int[] {0};
    else
        return super.getAssocArrayParameterPositions(extension_keyword, arg_count);
  }

  public Object invoke(String keyword, Object[] args) {
    if (keyword.equals("FileCreationBlock")) {
        for (Object arg : args)
            populateFileWatchers(arg);
        return file_creation_blocker.populateHandleSet(args, vm);
    }
    else if (keyword.equals("FileInfo")) {
        checkNumArgs(args, 2);
        return fileinfo((AssocArray) args[0], toAwkString(args[1]));
    }
    else
        throw new NotImplementedError(keyword);
    // never reached
    return null;
  }

  private final void populateFileWatchers(Object arg) {
    if (arg instanceof AssocArray) {
        AssocArray aa = (AssocArray) arg;
        for (Object o : aa.keySet())
            populateFileWatchers(o);
    } else {
        String str = arg.toString();
        if (! str.equals(""))
            if (file_watchers.get(str) == null || ! file_watchers.get(str).isAlive())
                file_watchers.put(str, new FileWatcher(str));
    }
  }

  private final int fileinfo(AssocArray aa, String filename) {
    File file = new File(filename);
    long last_modified = file.lastModified();
    if (last_modified 0L) {
        Date date = new Date(last_modified);
        aa.put("last modified", date.toString());
        return 1;
    } else {
        aa.put("last modified", "");
        return 0;
    }
  }
}
```

Most of the code registering itself to Jawk via the JawkExtension interface is fairly easy to follow. extensionKeywords() returns the set of extension functions to be accepted by the Jawk parser. And, invoke() maps extension keywords to Java methods which do the work. init() and getAssocArrayParameterPositions() require some context with regard to FileCreationBlock and will be discussed below.

Likewise, the code handling "FileInfo" is easy to follow. "FileInfo" maps to the fileinfo() function via invoke. The fileinfo() method, then, computes `File.lastModified()` for the specified file and populates this result into an associative array. Then, 1 is returned if successful, 0 is returned otherwise (i.e., when lastModified() returns 0L).

How did invoke() know the first parameter is an associative array? getAssocArrayParameterPositions() enforces this by returning int[] {0} for the "FileInfo" extension, telling the Jawk parser to treat the first (0th) argument as an associative array. As a result, passing in a string as the first parameter causes a SemanticException. Note that for all other extension keywords, getAssocArrayParameterPositions() refers to the superclass implementation, which simply returns an empty array (meaning, there are no assumptions on which parameters are associative arrays).

Walking through the code for "FileCreationBlock", on the other hand, is more involved because "FileCreationBlock" requires the following supporting containers and classes:

* `Map<String,FileWatcherfile> watchers` - a map of handles (string filenames) to FileWatchers (objects which poll for the creation of the file).
* `BulkBlockObject file_creation_blocker` - a convenience class which makes it easy to block on a set of handles/Blockables (file_watchers).
* `class FileWatcher` - a Blockable implementation, as required by the BulkBlockObject constructor for file_creation_blocker. FileWatcher spawns a thread which polls for the creation of the particular file. When the file is created, a "found" flag is set to true and the file_creation_blocker is notified (via Object.notify()) to unblock, allowing the script to proceed. Jawk will, then, refer to the "FileCreation" string (parameter) passed into the BulkBlockObject constructor of file_creation_blocker to return that, along with the handle, separated by `OFS`.

With all of these support classes, not much more is required of filecreationblock(), except to simply populate file_watchers with FileWatcher objects for files the user wishes to monitor and to trigger file_creation_block to block on the set of FileWatchers in file_watchers.

Using BulkBlockObject makes developing blocking services easier than if it were done using BlockObjects and providing your own block() method. Extensions requiring multiplexing of potentially many blockables, like FileCreationBlock and Socket*Block, use BulkBlockObject, while blockable services which don't (like StdinBlock and Timeout) use BlockObject.

This was just a high level description of the FileExtension, giving a brief introduction to some of the extension constructs and services required to formulate a useful blocking extension. If you wish to develop Jawk extensions, we recommend reading some of the extension source code bundled with Jawk , such as StdinExtension and SocketExtension, to get a better feel for writing extensions.
