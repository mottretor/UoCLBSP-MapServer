
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

class ThreadedEchoServer {

    static final int PORT = 1978;

    public static void main(String args[]) {
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            // new thread for a client
            System.out.println("Started new client");
            new EchoThread(socket).start();
        }
    }
}

class EchoThread extends Thread {

    protected Socket socket;

    public EchoThread(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public void run() {
        InputStream inp = null;
        BufferedReader brinp = null;
        DataOutputStream out = null;
        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }
        String line;
        while (true) {
            try {
                line = brinp.readLine();
                StringBuilder raw = new StringBuilder();
                raw.append("" + line);
                boolean isPost = line.startsWith("POST");
                int contentLength = 0;
                while (!(line = brinp.readLine()).equals("")) {
                    raw.append('\n' + line);
                    if (isPost) {
                        final String contentHeader = "Content-Length: ";
                        if (line.startsWith(contentHeader)) {
                            contentLength = Integer.parseInt(line.substring(contentHeader.length()));
                        }
                    }
                }
                StringBuilder body = new StringBuilder();
                if (isPost) {
                    int c = 0;
                    for (int i = 0; i < contentLength; i++) {
                        c = brinp.read();
                        body.append((char) c);

                    }
                }
                raw.append(body.toString());
                System.out.println(body);

                // send response
                out.writeBytes("HTTP/1.1 200 OK\n");
                out.writeBytes("Content-Type: text/html\n");
                out.writeBytes("\n");
                out.writeBytes(new Date().toString());
                if (isPost) {
                    out.writeBytes("<br><u>" + body.toString() + "</u>");
                } else {
                    out.writeBytes("<form method='POST'>");
                    out.writeBytes("<input name='name' type='text'/>");
                    out.writeBytes("<input type='submit'/>");
                    out.writeBytes("</form>");
                }
                //
                // do not in.close();
                out.flush();
                out.close();
                socket.close();
//                if ((line == null) || line.equalsIgnoreCase("QUIT")) {
//                    socket.close();
//                    return;
//                } else {
//                    out.writeBytes(line + "\n");
//                    out.flush();
//                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
