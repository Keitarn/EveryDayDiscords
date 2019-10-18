import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class Loader {
    private Map<String, Command> commands = new HashMap<>();
    private HashMap<String, TreeSet<String>> messages = new HashMap<String, TreeSet<String>>();
    private TreeSet<Long> default_channel = new TreeSet<Long>();
    String path;

    Loader(Map<String, Command> commands, HashMap<String, TreeSet<String>> messages, String path, TreeSet<Long> default_channel) {
        this.commands = commands;
        this.messages = messages;
        this.default_channel = default_channel;
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
            // check to make sure you have valid data
            array[0] = array[0].replaceAll(" ", "");

            switch (array[0]) {
//                case "Message":
//                    if (!save) {
//                        mapMessage(array[1]);
//                    }
//                    break;
//                case "Serveur":
//                    if (save) {
//                        saveChanel(array[1]);
//                    } else {
//                        recupChanel(array[1]);
//                    }
//                    break;
//                case "Path":
//                    if (!save) {
//                        recupPath(array[1]);
//                    }
//                    break;
                case "Path_Copy" :
                    System.out.println(array[1]);
                    array[1] = array[1].replace(" \"", "");
                    array[1] = array[1].replace("\"", "");
                    path_Copy = array[1];
                    System.out.println(path_Copy);
                    break;
                case "Path_Image" :
                    array[1] = array[1].replace(" \"", "");
                    array[1] = array[1].replace("\"", "");
                    path_Image = array[1];
                    System.out.println(path_Image);
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

//    private void saveMessage(String fileName) {
//        final File fichier = new File(path.get("pathConfig") + "\\" + fileName);
//        try {
//
//            fichier.createNewFile();
//
//            final FileWriter writer = new FileWriter(fichier);
//            try {
//                for (Map.Entry commandes : messages.entrySet()) {
//                    for (String messages : (TreeSet<String>) commandes.getValue()) {
//                        writer.write(commandes.getKey().toString() + " : \"" + messages + "\"\n");
//                    }
//                }
//
//
//            } finally {
//                writer.close();
//            }
//        } catch (Exception e) {
//            System.out.println("Impossible de creer le fichier");
//        }
//    }

//    private void saveChanel(String fileName) {
//        final File fichier = new File(path.get("pathConfig") + "\\" + fileName);
//        try {
//
//            fichier.createNewFile();
//
//            final FileWriter writer = new FileWriter(fichier);
//            try {
//                for (Long elem : default_channel) {
//                    writer.write(elem.toString() + "\n");
//                }
//
//            } finally {
//                // quoiqu'il arrive, on ferme le fichier
//                writer.close();
//            }
//        } catch (Exception e) {
//            System.out.println("Impossible de creer le fichier");
//        }
//    }

//    private void mapMessage(String fileName) throws IOException {
//        final File fichier = new File(path.get("pathConfig")  + "\\" + fileName);
//        fichier.createNewFile();
//        FileReader input = new FileReader(path.get("pathConfig")  + "\\" + fileName);
//        BufferedReader bufRead = new BufferedReader(input);
//        String myLine = null;
//
//        while ((myLine = bufRead.readLine()) != null) {
//            String[] array = myLine.split(":");
//            array[0] = array[0].replaceAll(" ", "");
//            array[1] = array[1].replaceAll(" \"", "");
//            array[1] = array[1].replaceAll("\"", "");
//            messages.putIfAbsent(array[0], new TreeSet<String>());
//            messages.get(array[0]).add(array[1]);
//        }
//    }
//
//    private void recupChanel(String fileName) throws IOException {
//        final File fichier = new File(path.get("pathConfig")  + "\\" + fileName);
//        fichier.createNewFile();
//        FileReader input = new FileReader(path.get("pathConfig")  + "\\" + fileName);
//        BufferedReader bufRead = new BufferedReader(input);
//        String myLine = null;
//
//        while ((myLine = bufRead.readLine()) != null) {
//            default_channel.add(new Long(myLine));
//        }
//    }
//
//    private void recupPath(String fileName) throws IOException {
//        final File fichier = new File(path.get("pathConfig")  + "\\" + fileName);
//        fichier.createNewFile();
//        FileReader input = new FileReader(path.get("pathConfig")  + "\\" + fileName);
//        BufferedReader bufRead = new BufferedReader(input);
//        String myLine = null;
//
//        while ((myLine = bufRead.readLine()) != null) {
//            String[] array = myLine.split(":",2);
//            array[0] = array[0].replaceAll(" ", "");
//            array[1] = array[1].replaceAll(" \"", "");
//            array[1] = array[1].replaceAll("\"", "");
//            path.putIfAbsent(array[0], array[1]);
//        }
//    }
}
