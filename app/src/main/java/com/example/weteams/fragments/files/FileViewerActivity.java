package com.example.weteams.fragments.files;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weteams.R;
import com.example.weteams.logic.Callbacks;
import com.example.weteams.logic.FileStorage;

import java.io.File;
import java.io.FileOutputStream;

public class FileViewerActivity extends AppCompatActivity {

    public static final String FILE_ID_KEY = "file_id";
    public static final String FILENAME_KEY = "filename";

    private ProgressBar progressBar;
    private TextView textFileView;
    private ImageView imageFileView;
    private ImageView imagePrevPage;
    private ImageView imageNextPage;

    private Bitmap currentBitmap;

    private PdfRenderer pdfRenderer;
    private int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);

        progressBar = findViewById(R.id.progressBar);
        textFileView = findViewById(R.id.textFileView);
        imageFileView = findViewById(R.id.imageFileView);
        imagePrevPage = findViewById(R.id.imagePrevPage);
        imageNextPage = findViewById(R.id.imageNextPage);

        String fileId = getIntent().getStringExtra(FILE_ID_KEY);
        final String filename = getIntent().getStringExtra(FILENAME_KEY);
        setTitle(filename);

        FileStorage.downloadFile(this, fileId, new Callbacks<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                progressBar.setVisibility(View.GONE);

                if (filename.endsWith(".txt")) {
                    openTextFile(bytes);
                } else if (filename.endsWith(".jpg") || filename.endsWith(".png")) {
                    openImageFile(bytes);
                } else if (filename.endsWith(".pdf")) {
                    openPdfFile(bytes);
                } else {
                    Toast.makeText(FileViewerActivity.this, "Cannot open the file", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(FileViewerActivity.this, "Sorry, download failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openTextFile(byte[] bytes) {
        textFileView.setMovementMethod(new ScrollingMovementMethod());
        textFileView.setText(new String(bytes));
        textFileView.setVisibility(View.VISIBLE);
    }

    public void openImageFile(byte[] bytes) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imageFileView.setImageBitmap(bitmap);
        imageFileView.setVisibility(View.VISIBLE);
    }

    public void openPdfFile(byte[] bytes) {
        imageFileView.setVisibility(View.VISIBLE);
        imagePrevPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPage(-1);
            }
        });
        imageNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPage(1);
            }
        });

        try {
            File file = File.createTempFile("temp", "pdf", getCacheDir());
            file.deleteOnExit();

            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(bytes);
            } finally {
                stream.close();
            }

            pdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
            showPage(0);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(FileViewerActivity.this, "Cannot open the file", Toast.LENGTH_SHORT).show();
        }
    }

    public void showPage(int offset) {
        int pageCount = pdfRenderer.getPageCount();
        currentPage += offset;
        if (currentPage < 0) {
            currentPage = 0;
        } else if (currentPage >= pageCount) {
            currentPage = pageCount - 1;
        }
        imagePrevPage.setVisibility(currentPage > 0 ? View.VISIBLE: View.GONE);
        imageNextPage.setVisibility(currentPage < pageCount - 1 ? View.VISIBLE: View.GONE);

        PdfRenderer.Page page = pdfRenderer.openPage(currentPage);
        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        imageFileView.setImageBitmap(bitmap);
        page.close();
    }
}
