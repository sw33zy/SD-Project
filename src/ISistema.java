import java.util.List;

public interface ISistema {

    boolean existeUtilizador(String user);

    void adicionaUtilizador(Utilizador u);

    Utilizador getUtilizador(String user);

    boolean validaCredenciaisUtilizador(Utilizador u, String pass);

    int lotacaoLocal(int x, int y);

    int tamMap();

    void adicionaAoLocal(Utilizador u, int x, int y);

    boolean vaiAoLocal(Utilizador u, int a, int b);

    boolean notificaLocalSeVazio(int a, int b);

    boolean userComunicaDoenca(String codUser);

    List<String> getContactedUsersList(String cod);

    void removeDoMapa(String codUser);

    void userLogin(String codUser);

    void userLogout(String codUser);
}
