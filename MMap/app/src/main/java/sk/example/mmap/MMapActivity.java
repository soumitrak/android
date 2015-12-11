package sk.example.mmap;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Random;

// Memory mapped array list of int
class MListi {
    private int _size = 0;
    private final IntBuffer _buffer;

    public MListi(IntBuffer buffer) {
        _buffer = buffer;
    }

    public void put(int i) {
        _buffer.put(i);
        ++_size;
    }

    public int get(int index) {
        return _buffer.get(index);
    }

    public int size() {
        return _size;
    }
}

public class MMapActivity extends AppCompatActivity {

    private File directory = null;
    private static final String fileName = "mmap.dat";
    private static final long BUFFER_SIZE = 1024 * 1024 * 400;

    private FileChannel getFileChannel() throws IOException {
        System.out.println("Directory is " + directory.getAbsolutePath());

        File file = new File(directory, fileName);
        file.delete();
        FileChannel fc = new RandomAccessFile(file, "rw").getChannel();

        // file.deleteOnExit();
        // System.out.println("Deleting file " + directory.getName() + "/" + fileName + " status " + new File(directory, fileName).delete());

        return fc;
    }

    private MappedByteBuffer getNextBuffer(FileChannel fileChannel) throws IOException {
        long currentSize = fileChannel.size();
        long newSize = currentSize + BUFFER_SIZE;
        System.out.println("Truncating file from " + currentSize + " to " + newSize + " size.");
        fileChannel.truncate(newSize);
        System.out.println("mmap");
        MappedByteBuffer mmap = fileChannel.map(FileChannel.MapMode.READ_WRITE, currentSize, BUFFER_SIZE);
        System.out.println("mmap done");
        return mmap;
    }

    private void testMList(int count) throws IOException {
        FileChannel fc = getFileChannel();
        MListi list = new MListi(getNextBuffer(fc).asIntBuffer());
        System.out.println("Adding " + count + " random int into array.");
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            list.put(random.nextInt());
        }
        System.out.println("Iterating array now.");
        for (int i = 0; i < list.size(); ++i)
            list.get(i);
        fc.close();
        System.out.println("Done testing MListi.");
    }

    private void testArrayList(int count) {
        System.out.println("Testing arraylist");
        ArrayList<Integer> list = new ArrayList<Integer>();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            list.add(random.nextInt());
        }
        System.out.println("Iterating array now.");
        for (int i = 0; i < list.size(); ++i)
            list.get(i);
        System.out.println("Done testing arraylist.");
    }

    private void testMMap(int count) throws IOException {
        testMList(count);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mmap);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final int numIntegers = 1000000 * 100;
        directory = getExternalFilesDir(null);

        RelativeLayout.LayoutParams row2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        row2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        View on = (View) findViewById(R.id.onheap);
        on.setLayoutParams(row2);

        on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    testArrayList(numIntegers);
                    Snackbar.make(view, "Success!!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(view, "Failure!!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        row2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        row2.addRule(RelativeLayout.BELOW, on.getId());
        View off = (TextView) findViewById(R.id.offheap);
        off.setLayoutParams(row2);

        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    testMMap(numIntegers);
                    Snackbar.make(view, "Success!!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(view, "Failure!!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mmap, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
