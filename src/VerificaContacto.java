import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VerificaContacto implements Runnable {
    private int tag;
    private Demultiplexer m;
    private String user;

    public VerificaContacto(int tag, Demultiplexer m) {
        this.tag = tag;
        this.m = m;
        this.user = "";
    }

    public void setUser(String user){
        this.user = user;
    }


    @Override
    public void run() {
        try {
            for (; ; ) {
                byte[] data = m.receive(tag);
                ByteArrayInputStream bi = new ByteArrayInputStream(data);
                DataInputStream in = new DataInputStream(bi);
                List<String> l = new ArrayList<>();
                int size = in.readInt();
                for (int i = 0; i<size;i++)
                    l.add(in.readUTF());
                if (l.contains(this.user)) {
                    Cliente.formatedtext("%47s\n","Entrou em contacto com um infectado!");
                }
        }
    }catch(IOException e){
            e.printStackTrace();
        } catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}
