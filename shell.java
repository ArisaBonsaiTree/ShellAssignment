import java.io.*;
import java.util.Arrays;

/* HINT GIVEN TO US
    // check if we are going to an absolute or relative directory
    if (!newDirectory.startsWith(fileSeparator))  // relative path
    newDirectory = workingDirectory + newDirectory;
 */

public class shell extends Thread{

    private BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
    private ProcessBuilder processBuilder = new ProcessBuilder();
    private Process process;
    private StringBuilder builder = new StringBuilder();
    private int dotCounter = 0;
    private String[] result;
    String storePath = "/home";

    public static void main(String[] args) throws IOException, InterruptedException {
        new shell().runMe();
    }

    private void runMe() throws InterruptedException, IOException {
        processBuilder.directory(new File("/home"));

        while (true){
            try {
                System.out.print("$> ");

                // "cd .." --> cd[0] ..[1] :: ONLY SPACES
                result = console.readLine().split("\\s");

                if (result[0].equals("")) continue;

                // Commands are loaded into processBuilder
                processBuilder.command(result);

                // Close the application if 'exit' was typed
                if(result[0].equalsIgnoreCase("exit")) System.exit(0);


                // ------------------------------------------------------------

                // If the user types cd --> Some logic we go through
                if(result[0].startsWith("cd")){

                    // Relative path => cd ./
                    // Suppose we are typing cd ./arisa   pwd: /home
                    if (result[1].startsWith(".") && result[1].startsWith("/", 1)) {
                        String cantSplitAnArray = convertArrayToString(result); // REMOVES CD => ./arisa
                        String[] breakUpStringResult = cantSplitAnArray.split("/"); // .[0] arisa[1]

                        String[] dir = getDirectory().split("/");

                        // Adds the current path into the builder
                        for (int i = 1; i < dir.length; i++) { // Add one, since a blank is counted
                            builder.append("/").append(dir[i]);
                        }

                        // Appends the rest
                        for (int i = 1; i < breakUpStringResult.length; i++) {
                            builder.append("/").append(breakUpStringResult[i]);
                        }

                        // Store the path before replacing it
                        storePath = processBuilder.directory().getAbsolutePath();
                        processBuilder.directory(new File(builder.toString()));

                        if(validateDirectoy(builder.toString())){
                            // It's a real directory,
                            builder.setLength(0); // Reset builder
                            continue;
                        }else {
                            // Not a real directory it must be resetted
                            processBuilder.directory(new File(storePath));
                            builder.setLength(0); // Reset builder
                            continue;
                        }
                    }

                    // Go up a directory :: Looking for cd ..
                    if (result[1].startsWith(".") && result[1].startsWith(".", 1)) {
                        goUpADirectory(result, true);
                        continue;
                    }
                    
                    // IF you type cd arisa
                    if(!result[1].startsWith("/")){
                        builder.append(getDirectory()).append("/").append(result[1]);

                        storePath = processBuilder.directory().getAbsolutePath();
                        processBuilder.directory(new File(builder.toString()));

                        if(validateDirectoy(builder.toString())){
                            // It's a real directory,
                            builder.setLength(0); // Reset builder
                            continue;
                        }else {
                            // Reset it
                            processBuilder.directory(new File(storePath));
                            builder.setLength(0); // Reset builder
                            continue;
                        }

                    }


                    // typing /home/arisa/Desktop ========
                    // Store the path
                    storePath = processBuilder.directory().getAbsolutePath();
                    // Place what ever you have after cd
                    processBuilder.directory(new File(result[1]));

                    // Validates to see if it works
                    if(validateDirectoy(result[1])){ // Absolute path  cd home/arisa/Desktop
                        // It's a real directory,
                        builder.setLength(0); // Reset builder Just in case
                        continue;
                    }
                    else {
                        // Not a real directory it must be resetted

                        processBuilder.directory(new File(storePath));
                        builder.setLength(0); // Reset builder
                        continue;
                    }
                } // end of cd [logic]

                else {
                    process = processBuilder.start();
                    printStream();
                }
            }
            catch (Exception e) { // Just catch anything   NOT FOR CD
                System.out.println(result[0] + ": command not found");
                processBuilder.directory(new File(storePath));
                continue;
            }
        }
    }




    // Will try to look for a directory with the given content :: Throw an error if it can't do so
    private boolean validateDirectoy(String directoryPath){
        try{
            getDirectory();
            return true;
        } catch (IOException e) {
            System.out.println("bash: cd: " + result[1] + ": No such file or directory found");
            return false;
        }
    }

    private String convertArrayToString(String[] convertMe){
        // Remove the cd from the array, 'purifies it'
        String[] removeTheCD = Arrays.copyOfRange(convertMe, 1, convertMe.length);
        // Now turn convert an array into a string, HOWEVER NOT IT HAS BRACKETS
        String convertArrayToString = Arrays.toString(removeTheCD);
        // Return the String, but remove the brackets, move it up by one and lower it by one first --> [....] <--last
        return convertArrayToString.substring(1, convertArrayToString.length() - 1);
    }

    private void goUpADirectory(String[] findDots, boolean shouldCount) throws IOException {
        // We only want this to one once, otherwise skip this
        if(shouldCount == true) {
            String dotFinder = convertArrayToString(findDots);
            String[] dotBreaker = dotFinder.split("/");

            for (int i = 0; i < dotBreaker.length; i++) {
                if (dotBreaker[i].equalsIgnoreCase(".."))
                    dotCounter++;
            }
        }


        // Break the directories up "/home/arisa/Desktop" --> [0]home, [1]arisa, [2]Desktop
        String[] dir = getDirectory().split("/");

        if(dir.length == 0){
            // Prevents it from going any lower than '/'
            dotCounter = 0;
        }
        else {

            // '/ + [i]' + '/ + [i]' + ...   :: We reduce the length by one! Removing the last directory!
            for (int i = 0; i < dir.length - 1; i++) {
                builder.append("/").append(dir[i]);
            }

            processBuilder.directory(new File(builder.toString()));

            builder.setLength(0); // Reset string builder
            dotCounter--;
        }
        if(dotCounter > 0){
            String[] dummy = null;
            goUpADirectory(dummy, false);
        }
    }

    private void printStream() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }

    private String getDirectory() throws IOException {
        // Load the command "pwd" into processBuilder to make it Print Working Directory
        processBuilder.command("pwd");

        // Start the process
        process = processBuilder.start();

        String pwd = "";

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;

        // Fill pwd with line
        while ((line = reader.readLine()) != null) {
            pwd = line;
        }
        // Returns /home/arisa/Desktop
        return pwd;
    }
}


