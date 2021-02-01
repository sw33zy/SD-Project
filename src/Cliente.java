import java.io.*;
import java.net.Socket;
import java.util.Random;

import Exceptions.*;

public class Cliente {
    private static Socket socket;
    private static Demultiplexer m;
    private static TaggedConnection.Frame frame;
    /*private static AtualizaLocal atualizaLocal;
    private static Thread modLocal;*/
    static VerificaContacto verCon;

    DataOutputStream out; 
    DataInputStream in; 
    ByteArrayInputStream bi;
    ByteArrayOutputStream bo;

    final static int N = 10;
    static int tag = 1;
    static boolean loggedIn = false;
    static String user;
    static int coordX = -1;
    static int coordY = -1;

    public static String[] parseLine (String userInput) {
        return userInput.split(" ");
    }

   static class AtualizaLocal implements Runnable{
        Demultiplexer m1;
        boolean done;

        public AtualizaLocal(Demultiplexer m1){
           this.m1 = m1;
           this.done = false;
        }

       public void startIt(){
           this.done = false;
       }

        public void endIt(){
            this.done = true;
        }

        @Override
        public void run() {
            Random rand = new Random();
            int modX;
            int modY;
            ByteArrayOutputStream bo;
            DataOutputStream out;
            while(!done){
                if(loggedIn) {
                    if (coordX == 0) {
                        modX = rand.nextInt(2);
                    } else if (coordX == N-1) {
                        modX = rand.nextInt(2) - 1;
                    } else {
                        modX = rand.nextInt(3) - 1;
                    }
                    if (coordY == 0) {
                        modY = rand.nextInt(2);
                    } else if (coordX == N-1) {
                        modY = rand.nextInt(2) - 1;
                    } else {
                        modY = rand.nextInt(3) - 1;
                    }
                    coordX += modX;
                    coordY += modY;
                    if(modX != 0 || modY != 0) {
                        bo = new ByteArrayOutputStream();
                        out = new DataOutputStream(bo);
                        try {
                            out.writeUTF(user);
                            out.writeInt(coordX);
                            out.writeInt(coordY);
                            out.flush();
                        } catch (Exception e) {
                        }
                        byte[] data = bo.toByteArray();
                        try {
                            m1.send(tag, 4, data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        tag++;
                    }

                }/*else{
                    try {
                        c.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }*/
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void register(String input) throws IOException,InterruptedException, UserExistsException, FormatoInvalidoException, TamanhoInvalidoException{
        String[] tokens;
        //Random rand = new Random();
        tokens = parseLine(input);

        bo = new ByteArrayOutputStream();
        out = new DataOutputStream(bo);

        if(tokens.length==4){
            Utilizador newUser = new Utilizador(tokens[0], tokens[1], Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]));
            newUser.serialize(out);
            out.flush();
            byte[] data = bo.toByteArray();
            m.send(tag,1,data);
            data = m.receive(tag);
            tag++;
            bi = new ByteArrayInputStream(data);
            in = new DataInputStream(bi);

            if(!in.readBoolean()){

                if(in.readInt()==0){
                    throw new TamanhoInvalidoException(in.readInt()); 
                }
                else {
                    formatedtext("%45s\n", "Utilizador registado com sucesso!");
                }
            }
            else
                throw new UserExistsException();
            
        } else {
            throw new FormatoInvalidoException();
        }
    }

    public void login(String input) throws IOException, InterruptedException, InfectedUserException, PasswordMissmatch, FormatoInvalidoException, UserAlreadyLoggedInException {
        String[] tokens;
        tokens = parseLine(input);

        bo = new ByteArrayOutputStream();
        out = new DataOutputStream(bo);

        if(tokens.length==2) {
            for (int i = 0; i < 2; i++)
                out.writeUTF(tokens[i]);
            out.flush();
            byte[] data = bo.toByteArray();
            m.send(tag,2,data);
            data = m.receive(tag);
            tag++;
            bi = new ByteArrayInputStream(data);
            in = new DataInputStream(bi);
            if (in.readBoolean()) {
                if(in.readBoolean()) {
                    if(in.readBoolean()) {
                        formatedtext("%46s\n", "Utilizador autenticado com sucesso!");
                        user = in.readUTF();
                        coordX = in.readInt();
                        coordY = in.readInt();
                        loggedIn = true;
                        verCon.setUser(user);
                    /*atualizaLocal.startIt();
                    modLocal = new Thread(atualizaLocal);
                    modLocal.start();*/
                    }else{
                        throw new UserAlreadyLoggedInException();
                    }
                }else{
                    throw new InfectedUserException();
                }
            } else
                throw new PasswordMissmatch();  
        }else {
            throw new FormatoInvalidoException();
        }
    }

    public void move(String input) throws NumberFormatException, IOException, InterruptedException, FormatoInvalidoException, TamanhoInvalidoException{
        String[] tokens;
        tokens = parseLine(input);

        bo = new ByteArrayOutputStream();
        out = new DataOutputStream(bo);

        if (tokens.length == 2) {

            out.writeUTF(user);
            out.writeInt(Integer.parseInt(tokens[0]));
            out.writeInt(Integer.parseInt(tokens[1]));

            out.flush();
            byte[] data = bo.toByteArray();
            m.send(tag,4,data);
            data = m.receive(tag);
            tag++;
            bi = new ByteArrayInputStream(data);
            in = new DataInputStream(bi);
            if(in.readBoolean()){
                formatedtext("%38s\n", "Movido para X:" + (tokens[0]) + " Y:" + (tokens[1]));
            }
            else{
                throw new TamanhoInvalidoException( in.readInt() );     
            }
        } else {
            throw new FormatoInvalidoException();
        }
    }

    public void lotacao(String input) throws NumberFormatException, IOException, InterruptedException,TamanhoInvalidoException, FormatoInvalidoException{
        String[] tokens;
        tokens = parseLine(input);

        bo = new ByteArrayOutputStream();
        out = new DataOutputStream(bo);

        if (tokens.length == 2) {
            
            out.writeInt(Integer.parseInt(tokens[0]));
            out.writeInt(Integer.parseInt(tokens[1]));
            
            out.flush();
            byte[] data = bo.toByteArray();
            m.send(tag,3,data);
            data = m.receive(tag);
            tag++;
            bi = new ByteArrayInputStream(data);
            in = new DataInputStream(bi);
            if(in.readBoolean()){
                int lotacao = in.readInt();
                formatedtext("%40s\n", "Lotação em X:" + (tokens[0]) + " Y:" + (tokens[1]) + " --> " + lotacao);
            }
            else{
                throw new TamanhoInvalidoException( in.readInt() );     
            }
        } else {
            throw new FormatoInvalidoException();
        }
    }

    public void disponibilidade(String input) throws NumberFormatException, IOException, InterruptedException,TamanhoInvalidoException, FormatoInvalidoException{
        String[] tokens;
        tokens = parseLine(input);

        bo = new ByteArrayOutputStream();
        out = new DataOutputStream(bo);

        if (tokens.length == 2) {
            out.writeInt(Integer.parseInt(tokens[0]));
            out.writeInt(Integer.parseInt(tokens[1]));
            out.flush();
            byte[] data = bo.toByteArray();
            m.send(tag,5,data);
            new Thread(new Receiver(tag,m,tokens)).start();
            tag++;
        } else {
            throw new FormatoInvalidoException();
        };
    }

    public void infecao() throws IOException, InterruptedException{
        bo = new ByteArrayOutputStream();
        out = new DataOutputStream(bo);

        out.writeUTF(user);
        out.flush();
        byte[] data = bo.toByteArray();
        m.send(tag,6,data);
        data = m.receive(tag);
        tag++;
        bi = new ByteArrayInputStream(data);
        in = new DataInputStream(bi);
        in.readBoolean();
        formatedtext("%43s\n", "Contração de Doença Notificada");
        formatedtext("%39s\n", "A terminar a sessão...");
        user = "";
        loggedIn = false;
       /* atualizaLocal.endIt();
        modLocal.join();*/
        verCon.setUser(user);
    }

    public void logout()  throws IOException, InterruptedException {
        bo = new ByteArrayOutputStream();
        out = new DataOutputStream(bo);

        out.writeUTF(user);
        out.flush();
        byte[] data = bo.toByteArray();
        m.send(tag,7,data);
        tag++;
    }

    public static void formatedtext(String format, String text){
        System.out.println("---------------------------------------------------------");
        System.out.printf(format,text);
        System.out.println("---------------------------------------------------------");
    }

    private void menuHelpInicial(){
        System.out.println("login <username> <password>                 inicia procedimentos de início de sessão\n" +
                           "register <username> <password> <X> <Y>      inicia procedimentos de registo\n" +
                           "exit                                        termina sessão e encerra o programa\n");
    }

    private void menuHelpLogged(){
        System.out.println("lotacao <CoordenadaX> <CoordenadaY>                 consulta número de pessoas numa localização\n" +
                           "move <CoordenadaX> <CoordenadaY>                    mudar localização\n" +
                           "disponibilidade <CoordenadaX> <CoordenadaY>         consulta quando um local estiver livre\n" +
                           "infecao                                             comunica contração de doença\n" +
                           "exit                                                termina sessão e encerra o programa\n");
    }


    private void commands() throws InterruptedException{
        
        System.out.print("\nBem-vindo ao Alerta Covid!\n\n(execute 'help' para ver os comandos disponíveis)\n> ");

        verCon = new VerificaContacto(0,m);
        new Thread(verCon).start();
        Thread[] threads = {
            new Thread (() -> {
                int i = 0;
                String command;
                user = "";
                BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
                try{
                    while((command = stdin.readLine()) != null){
                    
                        String word = command.contains(" ") ? command.split(" ")[0] : command;

                        try{
                            switch(word){
                                case "help":
                                    menuHelpInicial();
                                    break;
                                case "register":
                                    register(command.split(" ",2)[1]);
                                    break;
                                case "login":
                                    login(command.split(" ",2)[1]);
                                    break;
                                case "exit":
                                    socket.close();
                                    return;
                                default:
                                    formatedtext("%37s\n", "Opção Inválida!!!");
                            }

                        } catch (UserExistsException e) {
                            formatedtext("%53s\n","Um utilizador com esse nome já existe no sistema!");
                        } catch(FormatoInvalidoException e) {
                            formatedtext("%38s\n", "Formato inválido!!!");
                        } catch (InfectedUserException e) {
                            formatedtext("%42s\n", "O utilizador está infectado!");
                        }catch (UserAlreadyLoggedInException e) {
                            formatedtext("%42s\n", "O utilizador já se encontra com a sessão iniciada!");
                        } catch (PasswordMissmatch e) {
                            formatedtext("%39s\n", "Credenciais inválidas!");  
                        } catch (TamanhoInvalidoException e){
                            formatedtext("%49s\n", "Localização inválida! Tamanho do Mapa: " + e.getTamanho());
                        } catch (NumberFormatException e) {
                            formatedtext("%44s\n", "Apenas são permitidos números!");
                        }
                        while(loggedIn) {
                            if(i==0){
                                System.out.print("\nBem vindo, " + user + "\n\n(execute 'help' para ver os comandos disponíveis)\n>");
                                i++;
                            }
                            command = stdin.readLine();
                            word = command.contains(" ") ? command.split(" ")[0] : command;
                            try{
                                switch(word){
                                    case "help":
                                        menuHelpLogged();
                                        break;
                                    case "lotacao":
                                        lotacao(command.split(" ",2)[1]);
                                        break;
                                    case "move":
                                        move(command.split(" ",2)[1]);
                                        break;
                                    case "disponibilidade":
                                        disponibilidade(command.split(" ",2)[1]);
                                        break;
                                    case "infecao":
                                        infecao();
                                        break;
                                    case "exit":
                                        logout();
                                        user = "";
                                        loggedIn = false;
                                        verCon.setUser(user);
                                        break;
                                    default:
                                        formatedtext("%37s\n", "Opção Inválida!!!");
                                }
                            } catch (NumberFormatException e) {
                                formatedtext("%44s\n", "Apenas são permitidos números!");
                            } catch (TamanhoInvalidoException e){
                                formatedtext("%49s\n", "Localização inválida! Tamanho do Mapa: " + e.getTamanho());
                            } catch(FormatoInvalidoException e) {
                                formatedtext("%38s\n", "Formato inválido!!!");
                            }
                            System.out.print("> ");
                        }
                        System.out.print("> ");
                    }
                    socket.close();
                }catch(Exception e){}
            })
        };
    for (Thread t: threads) t.start();
    for (Thread t: threads) t.join();
    }
    
    public static void start(Integer port) throws IOException, InterruptedException{
        Cliente tc = new Cliente();
        socket = new Socket("localhost", port);
        m = new Demultiplexer(new TaggedConnection(socket));
        m.start();

        /*atualizaLocal = new AtualizaLocal(m);
        modLocal = new Thread(atualizaLocal);*/
        tc.commands();
        
    }
    
    public static void main(String[] args) throws IOException, InterruptedException {
        Cliente.start(12347);
    }
}


