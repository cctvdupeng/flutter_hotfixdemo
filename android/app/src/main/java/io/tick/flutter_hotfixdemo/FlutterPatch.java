package io.tick.flutter_hotfixdemo;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerLoadResult;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;

import java.io.File;
import java.lang.reflect.Field;

import io.flutter.view.FlutterMain;

public class FlutterPatch {

    private static final String TAG = "FlutterPatch";

    private FlutterPatch() {
    }

    public static void flutterPatchInit() {
        try {
            String abi = getAbi();
            String relativePath = "lib/" + abi;
            Log.e("FlutterPatch", "flutterPatchInit() relativePath  " + getAbi());
            String libPath = findLibraryFromTinker(ApplicationContextProvider.context,
                    relativePath, "libapp.so");
            Log.e("FlutterPatch", "flutterPatchInit() called   " + libPath);
            Field field = FlutterMain.class.getDeclaredField("sAotSharedLibraryName");
            field.setAccessible(true);
            field.set(null, libPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAbi() {
        String abi;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            abi = Build.CPU_ABI;
        } else {
            abi = Build.SUPPORTED_ABIS[0];
        }
        return abi;
    }

    public static String findLibraryFromTinker(Context context, String relativePath, String libName)
            throws UnsatisfiedLinkError {
        final Tinker tinker = Tinker.with(context);

        libName = libName.startsWith("lib") ? libName : "lib" + libName;
        libName = libName.endsWith(".so") ? libName : libName + ".so";
        String relativeLibPath = relativePath + "/" + libName;
        if (tinker.isEnabledForNativeLib() && tinker.isTinkerLoaded()) {
            TinkerLoadResult loadResult = tinker.getTinkerLoadResultIfPresent();
            if (loadResult.libs == null) {
                return libName;
            }
            for (String name : loadResult.libs.keySet()) {
                if (!name.equals(relativeLibPath)) {
                    continue;
                }
                String patchLibraryPath = loadResult.libraryDirectory + "/" + name;
                File library = new File(patchLibraryPath);
                if (!library.exists()) {
                    continue;
                }
                //whether we check md5 when load
                boolean verifyMd5 = tinker.isTinkerLoadVerify();
                if (verifyMd5 && !SharePatchFileUtil.verifyFileMd5(library,
                        loadResult.libs.get(name))) {
                    tinker.getLoadReporter().onLoadFileMd5Mismatch(library,
                            ShareConstants.TYPE_LIBRARY);
                } else {
//                    System.load(patchLibraryPath);
                    TinkerLog.i(TAG, "findLibraryFromTinker success:" + patchLibraryPath);
                    return patchLibraryPath;
                }
            }
        }
        return libName;
    }
}
