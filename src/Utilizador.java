import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Utilizador {
    private String user;
    private String pass;
    private boolean isDoente;
    private Coords coords;
    private boolean isLoggedIn;
    private ReadWriteLock l;
    private Set<String> contactedUsers;

    private class Coords{
        private int x;
        private int y;

        public Coords(){
            this.x = -1;
            this.y = -1;
        }

        public Coords(int x, int y){
            this.x = x;
            this.y = y;
        }

        public Coords(Coords xy){
            this.x = xy.getX();
            this.y = xy.getY();
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public void setX(int x){
            this.x = x;
        }

        public void setY(int y){
            this.y = y;
        }

        @Override
        public String toString() {
            return "Coordenadas: \n" +
                    "X: " + x +
                    ", Y: " + y +
                    '\n';
        }
    }

    public Utilizador(){
        this.user = "";
        this.pass = "";
        this.isDoente = false;
        this.coords = new Coords();
        this.isLoggedIn = false;
        this.l = new ReentrantReadWriteLock();
        this.contactedUsers = new TreeSet<>();
    }

    public Utilizador(String user, String pass, int x, int y){
        this.user = user;
        this.pass = pass;
        this.isDoente = false;
        this.coords = new Coords(x,y);
        this.isLoggedIn = false;
        this.l = new ReentrantReadWriteLock();
        this.contactedUsers = new TreeSet<>();
    }

    public Utilizador(Utilizador u){
        this.user = u.getUser();
        this.pass = u.getPass();
        this.isDoente = u.getIsDoente();
        this.coords = u.getCoords();
        this.isLoggedIn = u.getIsLoggedIn();
        this.l = u.l;
        this.contactedUsers = new TreeSet<>();
    }

    public String getUser(){
        readLock();
        try {
            return this.user;
        } finally {
            readUnlock();
        }
    }

    public String getPass(){
        readLock();
        try {
            return this.pass;
        } finally {
            readUnlock();
        }
    }

    public boolean getIsDoente(){
        readLock();
        try {
            return this.isDoente;
        } finally {
            readUnlock();
        }
    }

    public Coords getCoords(){
        readLock();
        try {
            return this.coords;
        } finally {
            readUnlock();
        }
    }

    public boolean getIsLoggedIn(){
        readLock();
        try {
            return this.isLoggedIn;
        } finally {
            readUnlock();
        }
    }

    public Set<String> getContactedUsers(){
        readLock();
        try {
            return this.contactedUsers;
        } finally {
            readUnlock();
        }
    }

    public List<String> getContactedUsersList(){
        readLock();
        try {
            return new ArrayList<>(this.contactedUsers);
        } finally {
            readUnlock();
        }
    }


    public void setUser(String user){
        writeLock();
        try {
            this.user = user;
        } finally {
            writeUnlock();
        }
    }

    public void setPass(String pass){
        writeLock();
        try {
            this.pass = pass;
        } finally {
            writeUnlock();
        }
    }

    public void setIsDoente(boolean isDoente){
        writeLock();
        try {
            this.isDoente = isDoente;
        } finally {
            writeUnlock();
        }
    }

    public void setCoords(Coords coords){
        writeLock();
        try {
            this.coords = coords;
        } finally {
            writeUnlock();
        }
    }

    public void setCoords(int x, int y){
        writeLock();
        try {
            this.coords = new Coords(x, y);
        } finally {
            writeUnlock();
        }
    }

    public void setLoggedIn(boolean isLoggedIn){
        writeLock();
        try {
            this.isLoggedIn = isLoggedIn;
        } finally {
            writeUnlock();
        }
    }

    public void comunicaDoenca(){
        setIsDoente(true);
    }

    public void userLogin(){
        setLoggedIn(true);
    }

    public void userLogout(){
        setLoggedIn(false);
    }

    public int getCoordX(){
       readLock();
       try {
           return this.coords.x;
       } finally {
           readUnlock();
       }
    }

    public int getCoordY(){
        readLock();
        try {
            return this.coords.y;
        } finally {
            readUnlock();
        }
    }

    public void readLock(){
        this.l.readLock().lock();
    }

    public void readUnlock(){
        this.l.readLock().unlock();
    }

    public void writeLock(){
        this.l.writeLock().lock();
    }

    public void writeUnlock(){
        this.l.writeLock().unlock();
    }
    
    public static Utilizador deserialize(DataInputStream in) throws IOException{
        String user = in.readUTF();
        String pass = in.readUTF();
        boolean isDoente = in.readBoolean();
        int x = in.readInt();
        int y = in.readInt();

        Utilizador r = new Utilizador(user,pass,x,y);
        r.setIsDoente(isDoente);
        return r;
    }

    public void serialize(DataOutputStream out) throws IOException{
        out.writeUTF((this.user));
        out.writeUTF((this.pass));
        out.writeBoolean(this.isDoente);
        out.writeInt(coords.getX());
        out.writeInt(coords.getY());
    }

    public void adicionaUtilizadoresCruzados(List<String> codUsers) {
        writeLock();
        codUsers.sort(String::compareTo);
        for (String s: codUsers) {
            if (!s.equals(this.user))
                this.contactedUsers.add(s);
        }
        writeUnlock();
    }

    public void adicionaUtilizadorCruzado(String codUser){
        writeLock();
        this.contactedUsers.add(codUser);
        writeUnlock();
    }

    @Override
    public String toString() {
        return "Utilizador: \n" +
                "User = " + user +
                ",\nPass = " + pass  +
                ",\nEstá Infetado = " + isDoente +
                ",\n" + coords.toString();
    }
}
