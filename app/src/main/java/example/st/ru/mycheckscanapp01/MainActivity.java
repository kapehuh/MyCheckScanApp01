package example.st.ru.mycheckscanapp01;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE=0;
    private static final int MY_PERMISSION_STORAGE=1;

    TextView textView;
    Button button;
    ImageView imageView;

    BarcodeDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //request storage permission
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M&&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSION_STORAGE);
        }

        textView=(TextView) findViewById(R.id.textView);
        button=(Button) findViewById(R.id.button);
        imageView=(ImageView) findViewById(R.id.imageView);

        //init detector
        detector=new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX|Barcode.QR_CODE)
                .build();
        if(!detector.isOperational()){
            textView.setText("Could not set up the detector!");
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //load image
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==RESULT_LOAD_IMAGE&&resultCode==RESULT_OK&&null!=data){
            Uri selectedImage=data.getData();
            String[] filePathColumn={MediaStore.Images.Media.DATA};
            Cursor cursor=getContentResolver().query(selectedImage, filePathColumn, null,null,null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath=cursor.getString(columnIndex);
            cursor.close();

            Bitmap bitmap= BitmapFactory.decodeFile(picturePath);
            imageView.setImageBitmap(bitmap);

            //load data
            processData(bitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_STORAGE:{
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Permission granted!",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this,"Permission not granted!",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
    //process barcode
    private void processData(Bitmap myBitmap){
        Frame frame=new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Barcode> barcodes=detector.detect(frame);

        Barcode thisCode=barcodes.valueAt(0);
        textView.setText(thisCode.rawValue);
    }
}
