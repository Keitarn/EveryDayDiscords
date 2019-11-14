import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class Loader {
    private Map<String, Command> commands = new HashMap<>();
    private HashMap<String, TreeSet<String>> messages = new HashMap<String, TreeSet<String>>();
    String path;

    Loader(Map<String, Command> commands, HashMap<String, TreeSet<String>> messages, String path) {
        this.commands = commands;
        this.messages = messages;
        this.path = path;
    }

    public Map<String, Command> getCommands() {
        return commands;
    }

    public HashMap<String, TreeSet<String>> getMessages() {
        return messages;
    }

    public Properties LectureParam() throws IOException {
        FileReader input = new FileReader(path+ "\\" + "config.txt");
        BufferedReader bufRead = new BufferedReader(input);
        String myLine = null;
        String client = "";
        String login_BDD = "";
        String mdp_BDD = "";
        String nom_BDD = "";
        String path_BDD = "";
        String path_Image = "";
        String path_Copy = "";

        while ((myLine = bufRead.readLine()) != null) {
            String[] array = myLine.split(":" , 2);
            array[0] = array[0].replaceAll(" ", "");
            switch (array[0]) {
                case "Path_Image" :
                    array[1] = array[1].replace(" \"", "");
                    array[1] = array[1].replace("\"", "");
                    path_Image = array[1];
                    break;
                case "BDD_path" :
                    array[1] = array[1].replace(" ", "");
                    path_BDD = array[1];
                    break;
                case "BDD_nom" :
                    array[1] = array[1].replace(" ", "");
                    nom_BDD = array[1];
                    break;
                case "BDD_mdp" :
                    array[1] = array[1].replace(" ", "");
                    mdp_BDD = array[1];
                    break;
                case "BDD_login" :
                    array[1] = array[1].replace(" ", "");
                    login_BDD = array[1];
                    break;
                case "Discord" :
                    array[1] = array[1].replace(" ", "");
                    client = array[1];
                    break;
            }
        }
        Properties properties = new Properties(login_BDD,mdp_BDD,client,nom_BDD,path_BDD, path_Image, path_Copy);
        return properties;
    }
}
