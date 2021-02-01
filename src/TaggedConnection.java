import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable {

    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Lock rl = new ReentrantLock();
    private final Lock wl = new ReentrantLock();

    public static class Frame {
        public final int tag;
        public final int opt;
        public final byte[] data;
        public Frame(int tag, int opt,byte[] data) { this.tag = tag; this.opt = opt; this.data = data; }
    }

    public TaggedConnection(Socket socket) throws IOException {
        this.dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void send(Frame frame) throws IOException {
        try {
            wl.lock();
            this.dos.writeInt(frame.tag);
            this.dos.writeInt(frame.opt);
            this.dos.writeInt(frame.data.length);
            this.dos.write(frame.data);
            this.dos.flush();
        }
        finally {
            wl.unlock();
        }
    }

    public void send(int tag,int opt, byte[] data) throws IOException {
        this.send(new Frame(tag,opt,data));
    }

    public Frame receive() throws IOException {
        int tag;
        int opt;
        byte[] data;
        try {
            rl.lock();
            tag = this.dis.readInt();
            opt = this.dis.readInt();
            int n = this.dis.readInt();
            data = new byte[n];
            this.dis.readFully(data);
        }
        finally {
            rl.unlock();
        }
        return new Frame(tag,opt,data);
    }

    @Override
    public void close() throws IOException {
        this.dis.close();
        this.dos.close();
    }
}
