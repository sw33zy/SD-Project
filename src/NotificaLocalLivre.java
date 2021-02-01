import java.io.*;

public class NotificaLocalLivre implements Runnable {
    private int tag;
    private int opt;
    private TaggedConnection c;
    private int cordX;
    private int cordY;
    private ISistema sys;

    public NotificaLocalLivre(int tag,int opt,TaggedConnection c, int x, int y, ISistema sys){
        this.tag = tag;
        this.opt = opt;
        this.c = c;
        this.cordX = x;
        this.cordY = y;
        this.sys = sys;
    }


    @Override
    public void run() {
        try {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bo);
        if(cordX<sys.tamMap() && cordY<sys.tamMap()){
                out.writeBoolean(true);
            System.out.println("pedido de lotacao do local:("+cordX+","+cordY+")");
            out.writeBoolean(sys.notificaLocalSeVazio(cordX,cordY));
        } else{
            out.writeBoolean(false);
            out.writeInt(sys.tamMap());
        }
        out.flush();
        byte[] data = bo.toByteArray();
        c.send(tag, opt, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
