import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable{
    public static List<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.socket  = socket;

            // socket.getOutputStream(): raw byte data transfers from server to client
            // OutputStreamWriter: char to byte encoding
            // BufferedWriter: reduce low-level write operations, store data in internal buffer in chunk and then send it
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUsername + " has entered the chat");
        } catch (IOException e) {
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    @Override
    public void run() {
        while(socket.isConnected()) {
            try {
                String msg = bufferedReader.readLine();
                broadcastMessage(msg);
            } catch (IOException e) {
                closeAll(socket, bufferedWriter, bufferedReader);
            }
        }
    }

    private void broadcastMessage(String msg) {
        for(ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(msg);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeAll(socket, bufferedWriter, bufferedReader);
            }
        }
    }

    private void closeAll(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
        removeClient(this);
        try {
            if(bufferedReader != null) {
                bufferedReader.close();
            }
            if(bufferedWriter != null) {
                bufferedWriter.close();
            }
            if(socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        broadcastMessage("SERVER: " + clientHandler.clientUsername + " has left the chat");
    }
}
