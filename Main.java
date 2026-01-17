import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

class Parser {
    enum WriteType
    {
        NoWrite,
        Overwrite,
        Append
    };
    String commandName;
    String[] args;

    WriteType writeType;
    String filePath;

    public boolean parse(String input)
    {
        //splits the command line to command and arguments
        ArrayList<String> splits = new ArrayList<>(Arrays.asList(input.split("\\s+"))); //(using \s+ instead of \s to tolerate multiple consecutive spaces)
        writeType = WriteType.NoWrite;
        filePath = null;
        if (splits.contains(">")) {
            writeType = WriteType.Overwrite;
            int idx = splits.indexOf(">");
            if (idx + 1 <splits.size()) { // in case of there's a file name
                filePath = splits.get(idx+1);
                splits = new ArrayList<>(splits.subList(0,idx)); //removing the > operator and the file name from the list
            }
        }
        else if (splits.contains(">>")) {
            writeType = WriteType.Append;
            int idx = splits.indexOf(">>");
            if (idx + 1 < splits.size()) {
                filePath = splits.get(idx+1);
                splits = new ArrayList<>(splits.subList(0,idx));
            }
        }

        commandName = splits.get(0).toLowerCase();
        splits.remove(0);
        args = splits.toArray(String[]::new); //putting the rest of the array to the args array
        return true;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }
}

class Terminal
{
    public static String commandOutput;
    Parser parser = new Parser();
    String currentPath = System.getProperty("user.dir");
    public String pwd()
    {
        return currentPath;
    }

    public void cd(String[] args)
    {
        // Home condition
        if (args.length == 0) {
            currentPath = System.getProperty("user.home");
        }
        else
        {
            if (args[0].equals(".."))
            {
                Path parent = Paths.get(currentPath).getParent();
                if (parent != null) currentPath = parent.toString();
            }
            else
            {
                String absolutePath = normalizePath(args[0]);
                if (new File(absolutePath).exists())
                {
                    currentPath = absolutePath;
                }
                else {
                    System.out.println("Error: INVALID PATH");
                }
            }
        }
    }

    public String ls()
    {
        File[] directories = new File(currentPath).listFiles();
        String res = "";
        for (int i = 0; i < directories.length; i++)
        {
            String relative = directories[i].toString().replace(currentPath, ""); //removing the parent path part
            System.out.println(relative);
            res += relative + "\r\n";
        }

        return res;
    }

    public void mkdir(String[] args)
    {
        if (args.length > 0)
        {
            for (int i = 0; i < args.length; i++)
            {
                new File(normalizePath(args[i])).mkdirs();
            }

        }
        else
        {
            System.out.println("Error: INSUFFICIENT ARGUMENTS");
        }
    }

    public void rmdir(String[] args)
    {
        if (args.length > 0)
        {
            if (args[0].equals("*"))
            {
                File[] dir = new File(currentPath).listFiles();
                for (int i=0;i<dir.length;i++)
                {
                    dir[i].delete();
                }
                System.out.println("deleted all empty children");
                return;
            }
            File f = new File(normalizePath(args[0]));
            if (f.isDirectory())
            {
                if (f.delete()) // delete only works on empty directories BY DEFAULT
                {
                    System.out.println("Directory successfully deleted");
                }
                else
                {
                    System.out.println("Directory has children");
                }
            }
            else
            {
                System.out.println("Error: Directory does not exist");
            }

        }
        else
        {
            System.out.println("Error: INSUFFICIENT ARGUMENTS");
        }
    }

    public void touch(String[] args)
    {
        if (args.length > 0)
        {
            File f = new File(normalizePath(args[0]));

            try
            {
                if (f.createNewFile())
                {
                    System.out.println("File created successfully");
                }
                else
                {
                    System.out.println("Error: File already exists");
                }
            }
            catch (IOException e) {
                System.out.println("Error: FAILED TO CREATE FILE");
            }

        }
        else
        {
            System.out.println("Error: INSUFFICIENT ARGUMENTS");
        }
    }

    public void cp(String[] args) throws IOException
    {
        if (args.length != 2) {
            System.out.println("Error: INVALID NUMBER OF ARGUMENTS");
            return;
        }

        Path file1 = Paths.get(normalizePath(args[0]));

        if (!Files.exists(file1)) {
            System.out.println("Error: Trying to copy a non-existing file");
            return;
        }

        if (Files.isDirectory(file1)) {
            System.out.println("Error: Can't copy directories");
            return;
        }

        Path file2 = Paths.get(normalizePath(args[1]));

        //If the destination file doesn't exist
        if (file2.getParent() == null) {
            System.out.println("Error: can't navigate");
            return;
        }

        boolean existed = Files.exists(file2);

        Files.copy(file1, file2, REPLACE_EXISTING);

        //Overrides the file if it exists
        if (existed) System.out.println("File is overwritten successfully");

        //Create the file if it doesn't exist
        else System.out.println("File is created and copied successfully");
    }

    public void cpr (String args[])
    {
        //using AtomicBoolean rather than boolean because lambda expression only takes a copy of variables and not a reference
        AtomicBoolean success = new AtomicBoolean(true);

        if (args.length != 2) {
            System.out.println("Error: INVALID NUMBER OF ARGUMENTS");
            return;
        }
        Path sourceDir = Paths.get(normalizePath(args[0]));
        Path targetDir = Paths.get(normalizePath(args[1]));
        if (!Files.exists(sourceDir)) {
            System.out.println("Error: Trying to copy a non-existing directory");
            return;
        }
        if (!Files.isDirectory(sourceDir)) {
            System.out.println("Error: Source path is not a directory");
            return;
        }
        try {
            Files.walk(sourceDir).forEach( sourcePathIterator -> { //lambda expression for action functional interface to avoid creating an unnecessary class
                try
                {
                    Path targetPathIterator = targetDir.resolve(sourceDir.relativize(sourcePathIterator)); //get us from sourceDir to sourcePathIterator and returns the path in targetPathIterator

                    if (Files.isDirectory(sourcePathIterator)) {
                        if (!Files.exists(targetPathIterator))
                        {
                            Files.createDirectories(targetPathIterator);
                        }
                    }
                    else Files.copy(sourcePathIterator, targetPathIterator, REPLACE_EXISTING);

                }
                catch (IOException e) {
                    e.printStackTrace();
                    success.set(false);
                }
            });
        }
        catch (IOException e) {
            e.printStackTrace();
            success.set(false);
        }
        if (success.get()) System.out.println("Directory copied successfully");
        else System.out.println("Error: something went wrong");
    }
    public void rm(String[] args)
    {
        if (args.length > 0)
        {
            File f = new File(normalizePath(args[0]));
            if (f.isFile())
            {
                if (f.delete()) // delete only works on empty directories BY DEFAULT
                {
                    System.out.println("File successfully deleted");
                }
                else
                {
                    System.out.println("ERROR: Failed to delete file");
                }
            }
            else
            {
                System.out.println("Error: File does not exist");
            }

        }
        else
        {
            System.out.println("Error: INSUFFICIENT ARGUMENTS");
        }
    }
    public String cat(String[] args) {
        StringBuilder result = new StringBuilder();
        if (args.length != 1 && args.length != 2) return "Error: Invalid number of arguments";
        for (String arg : args)
        {
            File f = new File(normalizePath(arg));
            if (!f.exists()) return ("Error: " + arg + " NOT FOUND");
            if (!f.isFile()) return ("Error: " + arg + " NOT A FILE");
            try (Scanner reader = new Scanner(f))
            {
                while (reader.hasNextLine())
                    result.append(reader.nextLine()).append("\n"); //StringBuilder to avoid creating a string at each concatenation
            } catch (IOException e)
            {
                return ("Error: FAILED TO READ " + arg);
            }
        }
        return result.toString();
    }

    public String wc(String[] args) {
        StringBuilder result = new StringBuilder();
        if (args.length <= 0) return("Error: Invalid number of arguments");
        for (String arg : args)
        {
            File f = new File(normalizePath(arg));
            int lineCount = 0;
            int wordCount = 0;
            int charCount = 0;

            if (!f.exists()) return ("Error: FILE " + arg + " NOT FOUND");
            if (!f.isFile()) return("Error: " + arg + " IS NOT A FILE");

            try (Scanner reader = new Scanner(f))
            {
                while (reader.hasNextLine())
                {
                    String line = reader.nextLine();
                    lineCount++;
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) wordCount += trimmed.split("\\s+").length; //to avoid counting empty lines as a word
                    charCount += line.length();
                    if (reader.hasNextLine()) charCount++; //to count the \n character at the end of each line if exists
                }
                result.append(lineCount).append(" ").append(wordCount).append(" ").append(charCount).append(" ").append(arg).append("\n"); //StringBuilder to avoid creating a string at each concatenation
            }
            catch (IOException e) {
                return "Error: FAILED TO READ FILE -> " + arg + "\n";
            }
        }
        return result.toString();
    }

    public void zip(String[] args) {
        // We need at least 2 things
        if (args.length < 2) {
            System.out.println("Error: zip needs an archive name and at least one file to compress.");
            return;
        }

        //name of our zip file
        String zipFileName = args[0];

        try {
            //  writes to the physical zip file
            FileOutputStream fileOutputStream = new FileOutputStream(normalizePath(zipFileName));

            //tool from java.util.zip =  formats the data correctly
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

            for (int i = 1; i < args.length; i++) {
                File fileToZip = new File(normalizePath(args[i]));

                // If a file doesn't exist
                if (!fileToZip.exists()) {
                    System.out.println("Warning: File not found, skipping: " + args[i]);
                    continue;
                }

                addFileToZip(fileToZip, fileToZip.getName(), zipOutputStream);
            }

            //close
            zipOutputStream.close();
            fileOutputStream.close();
            System.out.println("Successfully created " + zipFileName);

        } catch (IOException e) {
            System.out.println("Error: Something went wrong while creating the zip file.");
        }
    }

    //helper method handles (recursion)
    private void addFileToZip(File file, String entryName, ZipOutputStream zipOutputStream) throws IOException {
        // check if directory
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (File childFile : children) {
                // the method calls itself for each item in the folder = recursion
                addFileToZip(childFile, entryName + "/" + childFile.getName(), zipOutputStream);
            }
            return;
        }


        FileInputStream fileInputStream = new FileInputStream(file);
        // A ZipEntry = label file inside
        ZipEntry zipEntry = new ZipEntry(entryName);
        zipOutputStream.putNextEntry(zipEntry);


        byte[] bytes = new byte[1024];
        int length;
        while ((length = fileInputStream.read(bytes)) >= 0) {
            zipOutputStream.write(bytes, 0, length);
        }

        fileInputStream.close();
    }


    // unzip
    public void unzip(String[] args) {
        // name?
        if (args.length < 1) {
            System.out.println("Error: unzip needs the name of the archive to extract.");
            return;
        }

        String zipFileName = args[0];
        File destinationDir = new File(currentPath); // Extract to current location

        try {
            // read
            FileInputStream fileInputStream = new FileInputStream(normalizePath(zipFileName));
            ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);

            // zipentry = file inside
            ZipEntry zipEntry = zipInputStream.getNextEntry();


            while (zipEntry != null) {
                File newFile = new File(destinationDir, zipEntry.getName());

                // cr folder
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    // parent folder? oracle documentation of java
                    new File(newFile.getParent()).mkdirs();

                    // write file content
                    FileOutputStream fileOutputStream = new FileOutputStream(newFile);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.close();
                }
                // next item
                zipEntry = zipInputStream.getNextEntry();
            }

            // close stream
            zipInputStream.closeEntry();
            zipInputStream.close();
            fileInputStream.close();
            System.out.println("Successfully extracted " + zipFileName);

        } catch (IOException e) {
            System.out.println("Error: Something went wrong during extraction.");
        }
    }

    public void chooseCommandAction(String input) throws IOException
    {
        parser.parse(input);

        switch (parser.commandName)
        {
            case "pwd":
                commandOutput = pwd();
                break;
            case "cd":
                commandOutput = null;
                cd(parser.getArgs());
                break;
            case "ls":
                commandOutput = ls();
                break;
            case "mkdir":
                commandOutput = null;
                mkdir(parser.getArgs());
                break;
            case "rmdir":
                commandOutput = null;
                rmdir(parser.getArgs());
                break;
            case "touch":
                commandOutput = null;
                touch(parser.getArgs());
                break;
            case "cp":
                commandOutput = null;
                if (parser.getArgs().length > 0 && parser.getArgs()[0].equals("-r"))
                    cpr(Arrays.copyOfRange(parser.getArgs(), 1, parser.getArgs().length));
                else cp(parser.getArgs());
                break;
            case "rm":
                commandOutput = null;
                rm(parser.getArgs());
                break;
            case "cat":
                commandOutput = cat(parser.getArgs());
                break;
            case "wc":
                commandOutput = wc(parser.getArgs());
                break;
            case "zip":
                commandOutput = null;
                zip(parser.getArgs());
                break;
            case "unzip":
                commandOutput = null;
                unzip(parser.getArgs());
                break;
            default:
                System.out.println("Error: COMMAND NOT FOUND");
        }
        handleRedirection();
    }

    public void handleRedirection() throws IOException {
        if (parser.writeType == Parser.WriteType.NoWrite || commandOutput == null) return;
        String targetFile = normalizePath(parser.filePath);
        File file = new File(targetFile);
        if (!file.exists()) file.createNewFile();
        if (parser.writeType == Parser.WriteType.Overwrite) Files.writeString(file.toPath(), commandOutput);
        else if (parser.writeType == Parser.WriteType.Append) Files.writeString(file.toPath(), commandOutput, StandardOpenOption.CREATE ,java.nio.file.StandardOpenOption.APPEND); //if the file doesn't exist it creates it
    }
    public String normalizePath(String path)
    {
        Path p = Paths.get(path);
        if (p.isAbsolute()) {
            return path;
        }
        else
        {
            return Paths.get(currentPath, path).toString();
        }
    }
}
public class Main {
    static Terminal t = new Terminal();

    public static void main(String[] args) throws IOException
    {
        Scanner input = new Scanner(System.in);
        Terminal t = new Terminal();
        while (true) {
            String command = input.nextLine();
            if (command.equals("exit")) break;
            try {
                t.chooseCommandAction(command);
                if (t.commandOutput != null)    System.out.println(t.commandOutput);
            }
            catch (Exception e) {
                System.out.println("Error during command: " + command);
                e.printStackTrace();
            }

        }
        /*
        String[] testCommands = {
                "pwd",
                "mkdir testDir",
                "cd testDir",
                "touch file1.txt",
                "ls",
                "pwd > output.txt",
                "ls >> output.txt",
                "cat file1.txt",
                "wc file1.txt",
                "cd ..",
                "cp testDir/file1.txt copy.txt",
                "rm copy.txt",
                "zip archive.zip testDir/file1.txt",
                "unzip archive.zip",
                "rmdir testDir",
                "ls"
        };
        for (String command : testCommands) {
            System.out.println("\n> " + command);
            try {
                t.chooseCommandAction(command);
                if (t.commandOutput != null)    System.out.println(t.commandOutput);
            }
             catch (Exception e) {
                System.out.println("Error during command: " + command);
                e.printStackTrace();
            }
        }
    */
    }
}