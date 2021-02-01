import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class ServerWorker implements Runnable {
    private Socket socket;
    private ISistema sys;
    private TaggedConnection c;

    private TaggedConnection.Frame frame;
    DataInputStream in;
    DataOutputStream out;
    ByteArrayInputStream bi;
    ByteArrayOutputStream bo;
    

    public ServerWorker (Socket socket, ISistema sys, TaggedConnection c) {
        this.socket = socket;
        this.sys = sys;
        this.c = c;
    }

    public void register() throws IOException{
        Utilizador newUser = Utilizador.deserialize(in);
        boolean existe = sys.existeUtilizador(newUser.getUser());
        out.writeBoolean(existe);
        if(!existe) {
            if(newUser.getCoordX()<=sys.tamMap() && newUser.getCoordY()<=sys.tamMap()){
                out.writeInt(1);
                sys.adicionaUtilizador(newUser);
                sys.adicionaAoLocal(newUser, newUser.getCoordX(), newUser.getCoordY());
                System.out.println(newUser.toString());
            }
            else{
                out.writeInt(0);
                out.flush();
                out.writeInt(sys.tamMap());
            }
        }
                        
        out.flush();
        byte[] data = bo.toByteArray();
        c.send(frame.tag, frame.opt, data);
                        
    }

    public void login() throws IOException{
        String user = in.readUTF();
        String pass = in.readUTF();
        Utilizador storedUser = sys.getUtilizador(user);
        if(storedUser == null) {
            out.writeBoolean(false);
        }
        else{
            System.out.println(storedUser);
            if(pass.equals(storedUser.getPass())) {
                out.writeBoolean(true);
                if(!storedUser.getIsDoente()) {
                    out.writeBoolean(true);
                    if(!storedUser.getIsLoggedIn()) {
                        sys.userLogin(user);
                        out.writeBoolean(true);
                        out.writeUTF(storedUser.getUser());
                        out.writeInt(storedUser.getCoordX());
                        out.writeInt(storedUser.getCoordY());
                    }
                    else
                        out.writeBoolean(false);
                }
                else
                    out.writeBoolean(false);
            }
            else
                out.writeBoolean(false);
        }
        out.flush();
        byte[] data = bo.toByteArray();
        c.send(frame.tag, frame.opt, data);
    }

    public void lotacao() throws IOException{
        int x = in.readInt();
        int y = in.readInt();
        if(x<sys.tamMap() && y<sys.tamMap()){
            out.writeBoolean(true);
            System.out.println("pedido de consulta da lotacao de ("+x+","+y+")");
            out.writeInt(sys.lotacaoLocal(x,y));                         
        }
        else{
            out.writeBoolean(false);
            out.writeInt(sys.tamMap());
        }
        out.flush();
        byte[] data = bo.toByteArray();
        c.send(frame.tag, frame.opt, data);
    }

    public void move() throws IOException{
        String user = in.readUTF();
        int x = in.readInt();
        int y = in.readInt();
        if(x<sys.tamMap() && y<sys.tamMap()){
            out.writeBoolean(true);
            System.out.println("mudança de local do utilizador: "+user+" para ("+x+","+y+")");
            sys.vaiAoLocal(sys.getUtilizador(user),x,y);
        }else{
            out.writeBoolean(false);
            out.flush();
            out.writeInt(sys.tamMap());
        }
        out.flush();
        byte[] data = bo.toByteArray();
        c.send(frame.tag, frame.opt, data);
    }

    public void disponibilidade() throws IOException{
        int x = in.readInt();
        int y = in.readInt();
        new Thread(new NotificaLocalLivre(frame.tag, frame.opt, c, x, y, sys)).start();
    }

    public void infecao() throws IOException{
        String user = in.readUTF();
        System.out.println("notificada contração de doença do utilizador "+user);
        sys.userComunicaDoenca(user);
        sys.removeDoMapa(user);
        out.writeBoolean(true);
        out.flush();
        byte[] data = bo.toByteArray();
        c.send(frame.tag, frame.opt, data);
        Server.notificaTodosUsers(sys.getContactedUsersList(user));
    }

    public void logout() throws IOException{
        String user = in.readUTF();
        System.out.println("pedido para terminar sessão do utilizador "+user);
        sys.userLogout(user);
    }

    // @TODO
    @Override
    public void run() {
        try {
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            while (true) {
                frame = c.receive();
                //int option = in.readInt();
                int option= frame.opt;
                bi = new ByteArrayInputStream(frame.data);
                in = new DataInputStream(bi);
                bo = new ByteArrayOutputStream();
                out = new DataOutputStream(bo);
                switch (option) {
                    case 1:
                        register();
                        break;
                    case 2:
                        login();
                        break;
                    case 3:
                        lotacao();
                        break;
                    case 4:
                        move();
                        break;
                    case 5:
                        disponibilidade();
                        break;
                    case 6:
                        infecao();
                        break;
                    case 7:
                        logout();
                        break;
                    case 0:
                        throw new EOFException();
                }
            }

        } catch (EOFException e) {
            System.out.println("Connection closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class Server {
    final static int WORKERS_PER_CONNECTION = 3;
    final static int N = 10;
    private static List<TaggedConnection> cons;

    public static void  notificaTodosUsers(List<String> l) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bo);
        out.writeInt(l.size());
        for(String s : l)
            out.writeUTF(s);
        out.flush();
        byte[] data = bo.toByteArray();
        for (TaggedConnection c : cons)
            c.send(0,0,data);
    }

    public static void main (String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12347);
        ISistema sys = new Sistema(N);
        cons = new ArrayList<>();

        while (true) {
            Socket socket = serverSocket.accept();
            TaggedConnection c = new TaggedConnection(socket);
            cons.add(c);
            for(int i=0; i<WORKERS_PER_CONNECTION;i++) {
                Thread worker = new Thread(new ServerWorker(socket, sys, c));
                worker.start();
            }
        }
    }

}