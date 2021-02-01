import java.util.List;

public class Sistema implements ISistema {
    private Utilizadores users;
    private Mapa mapa;
    private final int tamMapa;

    public Sistema(int tam){
        this.users = new Utilizadores();
        this.tamMapa = tam;
        this.mapa = new Mapa(tamMapa);
    }

    public boolean existeUtilizador(String user){
        return this.users.existeUser(user);
    }

    public void adicionaUtilizador(Utilizador u){
        this.users.addUser(u);
    }

    public Utilizador getUtilizador(String user){
        return this.users.getUser(user);
    }

    public boolean validaCredenciaisUtilizador(Utilizador u, String pass){
        return pass.equals(u.getPass());
    }

    public int lotacaoLocal(int x, int y){
        return this.mapa.getLotacao(x,y);
    }

    public int tamMap(){
        return this.tamMapa;
    }

    public void adicionaAoLocal(Utilizador u, int x, int y){
        boolean r = mapa.adicionaAoLocal(u,x,y);
        if(r) {
            users.cruzaUtilizadores(mapa.getUtilizadoresLocal(x,y));
        }
    }

    public boolean vaiAoLocal(Utilizador u, int a, int b){
        boolean r = mapa.vaiAoLocal(u,a,b);
        if(r) {
            users.cruzaUtilizadores(mapa.getUtilizadoresLocal(a, b));
        }
        return r;
    }

    public boolean notificaLocalSeVazio(int a, int b){return mapa.notificaLocalSeVazio(a,b);}

    public boolean userComunicaDoenca(String codUser){return users.userComunicaDoenca(codUser);}

    public List<String> getContactedUsersList(String cod){
        return users.getContactedUsersList(cod);
    }

    public void userLogin(String codUser){
        users.userLogin(codUser);
    }

    public void userLogout(String codUser){
        users.userLogout(codUser);
    }

    public void removeDoMapa(String codUser){
        Utilizador u = users.getUser(codUser);
        if(u != null)
            mapa.retiraDoLocal(u);
    }

}
