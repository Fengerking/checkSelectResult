package com.afei.camera2getpreview;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.Rect;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Size;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.afei.camera2getpreview.util.Camera2Proxy;
import com.afei.camera2getpreview.util.ColorConvertUtil;
import com.afei.camera2getpreview.util.FileUtil;
import com.afei.camera2getpreview.util.NativeLibrary;

import java.io.File;
import java.nio.ByteBuffer;

public class CameraFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "CameraFragment";

    private ImageView       mCloseIv;
    private ImageView       mSwitchCameraIv;
    private ImageView       mTakePictureIv;
    private Camera2View     mCameraView;
    private Camera2Proxy    mCameraProxy;

    public static final int     DLG_CONFIRM        = 1;
    public static final int     DLG_RESULT         = 2;
    private int         m_nQstNum     = 10;
    private int         m_nIndex      = 0;
    private int         m_nResult[][] = new int[m_nQstNum][3];
    private int         m_nCheckR[][] = new int[m_nQstNum][2];

    protected dlgParam  m_dlgParam          = null;

    public class dlgParam {
        public int nType            = 0;
        public int nView            = 0;
        public int nIcon            = R.drawable.ic_launcher_foreground;
        public String strTitle      = null;
        public String strOK         = null;
        public String strCancel     = null;
        public String strOther      = null;

        public View   dlgView     = null;
    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            mCameraProxy.openCamera();
            mCameraProxy.setPreviewSurface(texture);
            // 根据相机预览设置View大小，避免显示变形
            Size previewSize = mCameraProxy.getPreviewSize();
            mCameraView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera, null);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        mCloseIv = rootView.findViewById(R.id.toolbar_close_iv);
        mSwitchCameraIv = rootView.findViewById(R.id.toolbar_switch_iv);
        mTakePictureIv = rootView.findViewById(R.id.take_picture_iv);
        mCameraView = rootView.findViewById(R.id.camera_view);
        mCameraProxy = mCameraView.getCameraProxy();

        mCloseIv.setOnClickListener(this);
        mSwitchCameraIv.setOnClickListener(this);
        mTakePictureIv.setOnClickListener(this);
        mCameraProxy.setImageAvailableListener(mOnImageAvailableListener);

        for (int i = 0; i < m_nQstNum; i++) {
            m_nResult[i][0] = 0; m_nResult[i][1] = 0; m_nResult[i][2] = 0;
            m_nCheckR[i][0] = 0; m_nCheckR[i][1] = 0;
        }
        m_dlgParam = new dlgParam();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCameraView.isAvailable()) {
            mCameraProxy.openCamera();
        } else {
            mCameraView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraProxy.releaseCamera();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_close_iv:
                getActivity().finish();
                break;
            case R.id.toolbar_switch_iv:
                //mCameraProxy.switchCamera();
                showResult();
                break;
            case R.id.take_picture_iv:
                mIsShutter = true;
                //showCheckResult();
                break;
        }
    }

    private byte[] mYuvBytes;
    private boolean mIsShutter;

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image == null) {
                return;
            }
            saveImageFile(image);

           // checkResult(image);

            image.close();
        }
    };

    private void saveImageFile (Image image) {
        int width = mCameraProxy.getPreviewSize().getWidth();
        int height = mCameraProxy.getPreviewSize().getHeight();
        if (mYuvBytes == null) {
            // YUV420 大小总是 width * height * 3 / 2
            mYuvBytes = new byte[width * height * 3 / 2];
        }

        // YUV_420_888
        Image.Plane[] planes = image.getPlanes();

        // Y通道，对应planes[0]
        // Y size = width * height
        // yBuffer.remaining() = width * height;
        // pixelStride = 1
        ByteBuffer yBuffer = planes[0].getBuffer();
        int yLen = width * height;
        yBuffer.get(mYuvBytes, 0, yLen);
        // U通道，对应planes[1]
        // U size = width * height / 4;
        // uBuffer.remaining() = width * height / 2;
        // pixelStride = 2
        ByteBuffer uBuffer = planes[1].getBuffer();
        int pixelStride = planes[1].getPixelStride(); // pixelStride = 2
        for (int i = 0; i < uBuffer.remaining(); i+=pixelStride) {
            mYuvBytes[yLen++] = uBuffer.get(i);
        }
        // V通道，对应planes[2]
        // V size = width * height / 4;
        // vBuffer.remaining() = width * height / 2;
        // pixelStride = 2
        ByteBuffer vBuffer = planes[2].getBuffer();
        pixelStride = planes[2].getPixelStride(); // pixelStride = 2
        for (int i = 0; i < vBuffer.remaining(); i+=pixelStride) {
            mYuvBytes[yLen++] = vBuffer.get(i);
        }

        if (mIsShutter) {
            mIsShutter = false;

            // save yuv data
            String yuvPath = FileUtil.SAVE_DIR + System.currentTimeMillis() + ".yuv";
            FileUtil.saveBytes(mYuvBytes, yuvPath);

            // save bitmap data
            String jpgPath = yuvPath.replace(".yuv", ".jpg");
            Bitmap bitmap = ColorConvertUtil.yuv420pToBitmap(mYuvBytes, width, height);
            FileUtil.saveBitmap(bitmap, jpgPath);

            byte[] result = new byte[64];
            int nRC = NativeLibrary.checkSelectResult(mYuvBytes, width, height, result);
            if (nRC > 0) {
                for (int i = 0; i < nRC; i++) {
                    m_nCheckR[i][0] = result[i*2];
                    m_nCheckR[i][1] = result[i*2+1];
                }
            }
            mTakePictureIv.postDelayed(()->showCheckResult(), 2);
        }
    }


    private void showCheckResult () {
        initDlgParam();
        m_dlgParam.nType = DLG_CONFIRM;
        m_dlgParam.nIcon = R.drawable.ic_launcher_foreground;
        m_dlgParam.nView = R.layout.dlg_confirm;
        m_dlgParam.strTitle = "结果确认正确吗？";
        m_dlgParam.strCancel = "取消";
        m_dlgParam.strOK = "确定";
        showNoteDialog ();
    }

    private void showResult () {
        initDlgParam();
        m_dlgParam.nType = DLG_RESULT;
        m_dlgParam.nIcon = R.drawable.ic_launcher_foreground;
        m_dlgParam.nView = R.layout.dlg_confirm;
        m_dlgParam.strTitle = "统计结果";
        m_dlgParam.strOK = "确定";
        showNoteDialog ();
    }

    protected void initDlgParam () {
        m_dlgParam.nType = 0;
        m_dlgParam.nIcon = R.drawable.ic_launcher_foreground;
        m_dlgParam.nView = 0;
        m_dlgParam.strTitle = null;
        m_dlgParam.strCancel = null;
        m_dlgParam.strOK = null;
        m_dlgParam.strOther = null;
    }

    protected void showNoteDialog() {
        final View noteView = LayoutInflater.from(getActivity()).inflate(m_dlgParam.nView,null);
        m_dlgParam.dlgView = noteView;
        AlertDialog.Builder dlgNote = new AlertDialog.Builder(getActivity()){
            public AlertDialog create() {
                onDlgCreate();
                return super.create();
            }
            public AlertDialog show() {
                onDlgShow ();
                return super.show();
            }
        };

        dlgNote.setIcon(m_dlgParam.nIcon);
        if (m_dlgParam.strTitle != null)
            dlgNote.setTitle(m_dlgParam.strTitle);
        dlgNote.setView(noteView);

        if (m_dlgParam.strCancel != null) {
            dlgNote.setNeutralButton(m_dlgParam.strCancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            onDlgCnacel();
                        }
                    });
        }

        if (m_dlgParam.strOK != null) {
            dlgNote.setPositiveButton(m_dlgParam.strOK,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            onDlgOK();
                        }
                    });
        }

        dlgNote.show();
    }

    protected void onDlgCreate () {
        if (m_dlgParam.nType == DLG_CONFIRM) {
            ListView lvConfirm = (ListView)m_dlgParam.dlgView.findViewById(R.id.lvConfirm);
            String[] strResult = new String[]{
                    "1、 赞成 " + m_nCheckR[0][0] + "     反对 " + m_nCheckR[0][1],
                    "2、 赞成 " + m_nCheckR[1][0] + "     反对 " + m_nCheckR[1][1],
                    "3、 赞成 " + m_nCheckR[2][0] + "     反对 " + m_nCheckR[2][1],
                    "4、 赞成 " + m_nCheckR[3][0] + "     反对 " + m_nCheckR[3][1],
                    "5、 赞成 " + m_nCheckR[4][0] + "     反对 " + m_nCheckR[4][1],
                    "6、 赞成 " + m_nCheckR[5][0] + "     反对 " + m_nCheckR[5][1],
                    "7、 赞成 " + m_nCheckR[6][0] + "     反对 " + m_nCheckR[6][1],
            };
            lvConfirm.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, strResult));
        }
        else if (m_dlgParam.nType == DLG_RESULT) {
            ListView lvConfirm = (ListView)m_dlgParam.dlgView.findViewById(R.id.lvConfirm);
            String[] strResult = new String[]{
                    "1、 赞成 " + m_nResult[0][0] + "     反对 " + m_nResult[0][1] + "     没有 " + m_nResult[0][2],
                    "2、 赞成 " + m_nResult[1][0] + "     反对 " + m_nResult[1][1] + "     没有 " + m_nResult[1][2],
                    "3、 赞成 " + m_nResult[2][0] + "     反对 " + m_nResult[2][1] + "     没有 " + m_nResult[2][2],
                    "4、 赞成 " + m_nResult[3][0] + "     反对 " + m_nResult[3][1] + "     没有 " + m_nResult[3][2],
                    "5、 赞成 " + m_nResult[4][0] + "     反对 " + m_nResult[4][1] + "     没有 " + m_nResult[4][2],
                    "6、 赞成 " + m_nResult[5][0] + "     反对 " + m_nResult[5][1] + "     没有 " + m_nResult[5][2],
                    "7、 赞成 " + m_nResult[6][0] + "     反对 " + m_nResult[6][1] + "     没有 " + m_nResult[6][2],
            };
            lvConfirm.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, strResult));
        }
    }

    protected void onDlgShow () {
    }

    protected void onDlgOK () {
        if (m_dlgParam.nType == DLG_CONFIRM) {
            for (int i = 0; i < m_nQstNum; i++) {
                if (m_nCheckR[i][0] == 1) {
                    m_nResult[i][0]++;
                }
                else if (m_nCheckR[i][1] == 1) {
                    m_nResult[i][1]++;
                }
                else {
                    m_nResult[i][2]++;
                }
            }
            m_nIndex++;
        }
        else if (m_dlgParam.nType == DLG_RESULT) {
            m_nIndex = 0;
        }
    }

    protected void onDlgCnacel () {
    }


}
