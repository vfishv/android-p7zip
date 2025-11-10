package com.nmmedit.p7zip;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.util.Log;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
        String zipPath = "/sdcard/Download/0.7z";
        try {
            openInArchive(zipPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
    }

    public void openInArchive(String zipPath) throws SevenZipException, FileNotFoundException {
        //测试依赖环境,懒得改注释掉
        RandomAccessFile randomAccessFile = new RandomAccessFile(zipPath, "rw");

        IInArchive inArchive = SevenZip.openInArchive(ArchiveFormat.SEVEN_ZIP, new RandomAccessFileInStream(randomAccessFile));

        ISimpleInArchive simpleInterface = inArchive.getSimpleInterface();
        long s = System.currentTimeMillis();

        for (ISimpleInArchiveItem item : simpleInterface.archiveItems()) {
            String path = item.getPath();
            boolean isFolder = item.isFolder();
            String method = item.getMethod();
            long size = item.getSize();
            long packedSize = item.getPackedSize();
            String isEncrypted = item.isEncrypted() ? "*" : "";
            int itemIndex = item.getItemIndex();
            Log.i(TAG, "---------------------------------------------------------------------------------------------");
            Log.i(TAG, (isFolder ? "+" : "-") + itemIndex + " " + path + " " + size + "/" + packedSize + " " + method + " " + isEncrypted);
            if (isFolder) {

            } else {
                inArchive.extract(new int[]{itemIndex}, false, new IArchiveExtractCallback() {

                    private long writeCount = 0;
                    private long totalCount = 0;

                    @Override
                    public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
                        Log.d(TAG, "getStream: " + index + " " + extractAskMode);
                        return new ISequentialOutStream() {
                            @Override
                            public void close() throws IOException {
                                Log.d(TAG, "close: ");
                            }

                            @Override
                            public int write(ByteBuffer dst, int len) throws SevenZipException {
                                writeCount += len;
                                float percent = writeCount * 100.0f / totalCount;
                                Log.d(TAG, "write: " + len + " " + writeCount + " " + percent + "%");
                                return len;
                            }
                        };
                    }

                    @Override
                    public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {
                        Log.d(TAG, "prepareOperation: " + extractAskMode);

                    }

                    @Override
                    public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
                        Log.d(TAG, "setOperationResult: " + extractOperationResult);

                    }

                    @Override
                    public void setTotal(long total) throws SevenZipException {
                        totalCount = total;
                        Log.d(TAG, "setTotal: " + total);
                    }

                    @Override
                    public void setCompleted(long complete) throws SevenZipException {
                        float percent = complete * 100.0f / totalCount;
                        Log.i(TAG, "setCompleted: " + complete + " " + percent + "%");
                    }
                });
            }
        }
        Log.i(TAG,"endTime " + (System.currentTimeMillis() - s));


//        int numberOfItems = inArchive.getNumberOfItems();
//        for (int num = 3; num < numberOfItems; num++) {
//
//            for (int i = 0; i < inArchive.getNumberOfProperties(); i++) {
//                PropertyInfo propertyInfo = inArchive.getPropertyInfo(i);
//                Object property = inArchive.getProperty(num, propertyInfo.propID);
//                System.out.println(property);
//            }
//            inArchive.extractSlow(num, new ISequentialOutStream() {
//                @Override
//                public int write(byte[] data) throws SevenZipException {
//                    System.out.println(new String(data));
//                    return data.length;
//                }
//            });

//        }

        inArchive.close();
    }

}
