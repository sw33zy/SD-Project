import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Receiver implements Runnable{
    private int tag;
    private Demultiplexer m;
    private int cordX;
    private int cordY;

    public Receiver(int tag,Demultiplexer m, String[] tokens){
        this.tag = tag;
        this.m = m;
        this.cordX = Integer.parseInt(tokens[0]);
        this.cordY = Integer.parseInt(tokens[1]);
    }


    @Override
    public void run() {
        try {
            byte[] data = m.receive(tag);
            ByteArrayInputStream bi = new ByteArrayInputStream(data);
            DataInputStream in = new DataInputStream(bi);
            if(in.readBoolean()){
                in.readBoolean();
                Cliente.formatedtext("%40s\n", "O local ("+cordX+","+cordY+") está livre");
            }else{
                Cliente.formatedtext("%49s\n", "Localização inválida! Tamanho do Mapa: " + in.readInt());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
