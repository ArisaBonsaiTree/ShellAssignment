import java.io.*;
import java.util.Arrays;

// TODO: Perhaps prevent broken directories
// TODO: Refactor
// TODO: ..   ../..  ../../..
// TODO BUG   cd /arisa --> sends you up a directory

/* HINT GIVEN TO US
    // check if we are going to an absolute or relative directory
    if (!newDirectory.startsWith(fileSeparator))  // relative path
    newDirectory = workingDirectory + newDirectory;
 */
public class ImprovedTerminal extends Thread{

    private BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
    private ProcessBuilder processBuilder = new ProcessBuilder();
    private Process process;
    private StringBuilder builder = new StringBuilder();
    private int dotCounter = 0;


    // I don't like working in main, so I made an object to run a method that isn't static
    public static void main(String[] args) throws IOException, InterruptedException {
        new ImprovedTerminal().runMe();
    }


    private void runMe() throws InterruptedException, IOException {
        while (true){
            try {
                System.out.print("$> ");

                // "cd .." --> cd[0] ..[1] :: ONLY SPACES
                String[] result = console.readLine().split("\\s");

                // Inputting \n or Enter will just send you back
                if (result[0].equals("")) {
                    continue;
                }

                // Commands are loaded into processBuilder
                processBuilder.command(result);

                // Close the application if 'exit' was typed
                if(result[0].equalsIgnoreCase("exit"))
                    System.exit(0);


                // If the user types cd --> Some logic we go through
                if(result[0].startsWith("cd")){

                    // cd ./ -- Relaltive path?
                    if(result[1].startsWith(".") && result[1].startsWith("/", 1)){

                        String rudendent = convertArrayToString(result);


                        String[] breakUpDir = rudendent.split("/");

                        String[] dir = getDirectory().split("/");

                        for (int i = 0; i < dir.length; i++) {
                            builder.append("/").append(dir[i]);
                        }

                        for (int i = 0; i < breakUpDir.length; i++) {
                            builder.append("/").append(breakUpDir[i]);
                        }

                        processBuilder.directory(new File(builder.toString()));
                        builder.setLength(0); // Reset string builder

                        continue;
                    }

                    // Go up a directory :: Looking for cd ..
                    if(result[1].startsWith(".") && result[1].startsWith(".",1)){
                        goUpADirectory(result, true);
                        continue;
                    }


                    // Absolute path
                    for(int i = 1; i < result.length; i++){
                        // cd /home/arisa/Desktop/Folder
                        builder.append("/").append(result[i]);
                        processBuilder.directory(new File(builder.toString()));
                        builder.setLength(0); // Reset string builder
                    }


                    //processBuilder.directory(new File("/home"));
                    continue;
                }
                // BREAK THIS UP
                // ====================================================

                // Activate process Builder
                process = processBuilder.start();

                // Runs nearly everything EXCEPT cd..
                printStream();
            }
            // TODO: Fix error handling to specific errors
            catch (IOException e) { // Just catch anything
                processBuilder.directory(new File("/home"));
                continue;
            }
        }
    }

    // Some magic to allow us to splice things + removed brackets and such
    private String convertArrayToString(String[] convertMe){
        // Remove the cd from the array, 'purifies it'
        String[] removeTheCD = Arrays.copyOfRange(convertMe, 1, convertMe.length);

        // Now turn convert an array into a string, HOWEVER NOT IT HAS BRACKETS
        String convertArrayToString = Arrays.toString(removeTheCD);

        // Return the String, but remove the brackets, move it up by one and lower it by one first --> [....] <--last
        return convertArrayToString.substring(1, convertArrayToString.length() - 1);
    }


    private void goUpADirectory(String[] findDots, boolean shouldCount) throws IOException {
        // We only want this to run one time to find the amount of ".."
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

        // '/ + [i]' + '/ + [i]' + ...
        for (int i = 0; i < dir.length - 1; i++) {
            builder.append("/").append(dir[i]);
        }

        processBuilder.directory(new File(builder.toString()));
        builder.setLength(0); // Reset string builder
        dotCounter--;
        if(dotCounter > 0){
            String[] dummy = null;
            goUpADirectory(dummy, false);
        }

    }


    public void printStream() throws IOException {
        // Step 4) Output Stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            // Step 5) Output the contents
            System.out.println(line);
        }
    }

    public String getDirectory() throws IOException {
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


