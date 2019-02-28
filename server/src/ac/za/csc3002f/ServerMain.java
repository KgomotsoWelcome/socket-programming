package ac.za.csc3002f;

public class ServerMain {
    public static void main(String[] args){
        Server server = new Server(
                Integer.parseInt(System.getenv("CHAT_SERVICE_PORT")));
        server.start();
    }
}
