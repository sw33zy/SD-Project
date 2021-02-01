import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Utilizadores {
    private Map<String,Utilizador> users;
    private Lock lockUsers = new ReentrantLock();

    public Utilizadores(){
        this.users = new HashMap<>();
    }

    public Utilizadores(Map<String,Utilizador> users){
        setUsers(users);
    }

    public Utilizadores(Utilizadores u){
        this.setUsers(u.getUsers());
    }

    public Map<String, Utilizador> getUsers(){
        Map<String,Utilizador> r = new HashMap<>();
        lockUsers.lock();
        try {
            for (Map.Entry<String, Utilizador> u : this.users.entrySet())
                r.put(u.getKey(), u.getValue());
            return r;
        } finally {
            lockUsers.unlock();
        }
    }

    public void setUsers(Map<String, Utilizador> users){
        this.users = new HashMap<>();
        lockUsers.lock();
        try {
            users.entrySet().forEach(u -> this.users.put(u.getKey(), u.getValue()));
        } finally {
            lockUsers.unlock();
        }
    }

    public Utilizador getUser(String codUser){
        lockUsers.lock();
        try {
            return this.users.get(codUser);
        } finally {
            lockUsers.unlock();
        }
    }

    public boolean userComunicaDoenca(String codUser){
        lockUsers.lock();
        try {
            Utilizador aux = getUser(codUser);
            if (aux != null){
                aux.comunicaDoenca();
                return true;
            }
            return false;
        } finally {
            lockUsers.unlock();
        }
    }

    public void userLogin(String codUser){
        lockUsers.lock();
        try {
            Utilizador aux = getUser(codUser);
            if (aux != null) {
                    aux.userLogin();
            }
        } finally {
            lockUsers.unlock();
        }
    }

    public void userLogout(String codUser){
        lockUsers.lock();
        try {
            Utilizador aux = getUser(codUser);
            if (aux != null) {
                aux.userLogout();
            }
        } finally {
            lockUsers.unlock();
        }
    }

    public void addUser(Utilizador u){
        lockUsers.lock();
        try {
            this.users.put(u.getUser(), u);
        } finally {
            lockUsers.unlock();
        }
    }

    public void addUser(String user, String pass, int x, int y){
        lockUsers.lock();
        try {
            this.users.put(user, new Utilizador(user, pass, x, y));
        } finally {
            lockUsers.unlock();
        }
    }

    public boolean existeUser(String codUser){
        lockUsers.lock();
        try {
            return this.users.containsKey(codUser);
        } finally {
            lockUsers.unlock();
        }
    }

    /*public void adicionaUtilizadorCruzado(Utilizador u, int a, int b) {
        lockUsers.lock();
        try {
            for(Utilizador user: users.values())
                if(user.getCoordX() == a && user.getCoordY() == b)
                user.adicionaUtilizadorCruzado(u.getUser());
        } finally {
            lockUsers.unlock();
        }
    }*/

    public void cruzaUtilizadores(List<String> codUtilizadores) {
        lockUsers.lock();
        List<Utilizador> u = new ArrayList<>();
        for(String codUser: codUtilizadores){
            Utilizador ut = getUser(codUser);
            ut.writeLock();
            u.add(ut);
        }
        lockUsers.unlock();
        for (Utilizador user : u){
            user.adicionaUtilizadoresCruzados(codUtilizadores);
            user.writeUnlock();
        }
    }

    public List<String> getContactedUsersList(String cod){
        this.lockUsers.lock();
        Utilizador u = this.users.get(cod);
        this.lockUsers.unlock();
        if(u != null)
            return u.getContactedUsersList();
        else
            return new ArrayList<>();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Collection<Utilizador> values = this.users.values();
        sb.append("Utilizadores:\n").append(values);
        return sb.toString();
    }
}
