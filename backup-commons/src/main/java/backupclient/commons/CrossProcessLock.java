package backupclient.commons;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class CrossProcessLock {

    public static CrossProcessLock instance = new CrossProcessLock();
    private final File lock_file;
    private final Object internal_lock = new Object();
    private Thread local_holder;

    private FileLock lock;
    
    private CrossProcessLock() {
        String tmp_dir = System.getProperty("java.io.tmpdir");
        if (! (tmp_dir.endsWith("/")) || tmp_dir.endsWith("\\")) {
            tmp_dir += System.getProperty("file.separator");
        }
        
        lock_file = new File(tmp_dir, "focusbackup.lock~");
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                instance.releaseLock();
            }
        });
    }

    // Will sleep one second after each try
    public boolean tryLock(int max_tries) {
        assert max_tries >= 0;
        if (local_holder == Thread.currentThread()) return true;
        else try {
            int tries=0;
            RandomAccessFile raf = new RandomAccessFile(lock_file, "rw");
            FileChannel channel = raf.getChannel();
            do {
                synchronized (internal_lock) {
                    if (local_holder == null && (lock = channel.tryLock()) != null) {
                        local_holder = Thread.currentThread();
                        return true;
                    }
                }
                Thread.sleep(1000);
            } while (++tries < max_tries);
        } catch (Exception e) {
            e.printStackTrace(); //
        }

        return false;
    }

    public synchronized void releaseLock() {
        if (lock != null) {
            try {
                lock.release();
                lock = null;
                local_holder = null;
            } catch (IOException e) {
                e.printStackTrace(); // TODO use logger. Maybe make api handler singelton?
            }
        }
    }
}
