import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Mapa {
    ReentrantLock l =  new ReentrantLock();
    private Local[][] locais;

    private class Local{
        ReadWriteLock lock;
        Condition estaLivre;
        List<String> codUsers;
        int lotacao;

        public Local(){
            this.lock = new ReentrantReadWriteLock();
            this.estaLivre = this.lock.writeLock().newCondition();
            this.codUsers = new ArrayList<>();
            this.lotacao = 0;
        }

        public List<String> getCodUsers(){
            return this.codUsers;
        }

        public void removeUtilizador(String codUser){
            codUsers.remove(codUser);
            lotacao--;
        }

        public void adicionaUtilizador(String codUser){
            codUsers.add(codUser);
            lotacao++;
        }
    }

    public Mapa(int n){
        locais = new Local[n][n];
        for(int i=0;i<n;i++)
            for(int j=0;j<n;j++)
                this.locais[i][j] = new Local();
    }

    public boolean adicionaAoLocal(Utilizador u, int a, int b){
        l.lock();
        if((a>=0 && a<locais.length) &&(b>=0 && b<locais.length)) {
            Local loc = locais[a][b];
            loc.lock.writeLock().lock();
            l.unlock();
            try {
                loc.adicionaUtilizador(u.getUser());
                return true;
            }finally {
                loc.lock.writeLock().unlock();
            }
        }
        else {
            l.unlock();
            return false;
        }
    }

    public int getLotacao(int a, int b){
        int r = -1;
        l.lock();
        if((a>=0 && a<locais.length) &&(b>=0 && b<locais.length)) {
            Local loc = locais[a][b];
            loc.lock.readLock().lock();
            l.unlock();
            r = loc.lotacao;
            loc.lock.readLock().unlock();
        }else l.unlock();
        return r;
    }

    public boolean vaiAoLocal(Utilizador u, int a, int b){
        l.lock();
        if((a>=0 && a<locais.length) &&(b>=0 && b<locais.length)) {
            u.readLock();
            int x = u.getCoordX();
            int y = u.getCoordY();
            String codU = u.getUser();
            u.readUnlock();
            Local lfrom = locais[x][y];
            Local lto = locais[a][b];
            l.unlock();
            if(x < a){
                lfrom.lock.writeLock().lock();
                lto.lock.writeLock().lock();
            }else if(x > a){
                lto.lock.writeLock().lock();
                lfrom.lock.writeLock().lock();
            }else if (y <= b){
                lfrom.lock.writeLock().lock();
                lto.lock.writeLock().lock();
            }else{
                lto.lock.writeLock().lock();
                lfrom.lock.writeLock().lock();
            }
            lfrom.removeUtilizador(codU);
            lfrom.lock.writeLock().unlock();
            lto.adicionaUtilizador(u.getUser());
            lto.lock.writeLock().unlock();
            u.writeLock();
            u.setCoords(a,b);
            u.writeUnlock();
            lfrom.lock.writeLock().lock();
            if(lfrom.lotacao == 0) {
                lfrom.estaLivre.signalAll();
            }
            lfrom.lock.writeLock().unlock();
            return true;
        }
        else {
            l.unlock();
            return false;
        }
    }

    public void retiraDoLocal(Utilizador u){
        l.lock();
        u.readLock();
        int x = u.getCoordX();
        int y = u.getCoordY();
        u.readUnlock();
        Local loc = locais[x][y];
        loc.lock.writeLock().lock();
        l.unlock();
        loc.removeUtilizador(u.getUser());
        if(loc.lotacao == 0) {
            loc.estaLivre.signalAll();
        }
        loc.lock.writeLock().unlock();
    }

    public boolean notificaLocalSeVazio(int a, int b){
        l.lock();
        if ((a >= 0 && a < locais.length) && (b >= 0 && b < locais.length)) {
            Local loc = locais[a][b];
            l.unlock();
            loc.lock.writeLock().lock();
            try {
                while (loc.lotacao != 0) {
                    try {
                        loc.estaLivre.await();
                    } catch (InterruptedException e) {

                    }
                }
                return true;
            } finally {
                loc.lock.writeLock().unlock();
            }
        } else {
            l.unlock();
            return false;
        }
    }

    public List<String> getUtilizadoresLocal(int a, int b){
        List<String> r = new ArrayList<>();
        l.lock();
        if((a>=0 && a<locais.length) &&(b>=0 && b<locais.length)){
            Local loc = locais[a][b];
            loc.lock.readLock().lock();
            l.unlock();
            r.addAll(loc.getCodUsers());
            loc.lock.readLock().unlock();
        }else l.unlock();
        return  r;
    }
}
