package com.ydl.webview.activity

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.ydl.webview.R
import com.ydl.webviewlibrary.FileReaderView
import kotlinx.android.synthetic.main.activity_file_display.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.log


class FileDisplayActivity : AppCompatActivity() {

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_display)
        copy()
        init()
    }

    var filePath: String? = null
    fun init() {
        tv_start.setOnClickListener(View.OnClickListener {
            // 弹出保存图片的对话框
            AlertDialog.Builder(this@FileDisplayActivity)
                .setItems(arrayOf(
                    "加载word文档",
                    "加载txt文件",
                    "加载表格文档",
                    "加载ppt文件",
                    "加载pdf文件"
                ),
                    DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            0 -> {
                                filePath = getFilePath(0)
                                Log.e("yuyu","item 0 -->$filePath")
                                documentReaderView.show(filePath)
                            }
                            1 -> {
                                filePath = getFilePath(1)
                                Log.e("yuyu","item 1 -->$filePath")
                                documentReaderView.show(filePath)
                            }
                            2 -> {
                                filePath = getFilePath(2)
                                Log.e("yuyu","item 2 -->$filePath")
                                documentReaderView.show(filePath)
                            }
                            3 -> {
                                filePath = getFilePath(3)
                                Log.e("yuyu","item 3 -->$filePath")
                                documentReaderView.show(filePath)
                            }
                            4 -> {
                                filePath = getFilePath(4)
                                Log.e("yuyu","item 4 -->$filePath")
                                documentReaderView.show(filePath)
                            }
                            else -> {
                                filePath = getFilePath(0)
                                Log.e("yuyu","item 5 -->$filePath")
                                documentReaderView.show(filePath)
                            }
                        }
                    })
                .show()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (documentReaderView != null) {
            documentReaderView.stop()
        }
    }

    private fun getFilePath(position: Int): String? {
        var path: String? = null
        when (position) {
            0 -> path = getFilesDir().getAbsolutePath().toString() + File.separator + "test.docx"
            1 -> path = getFilesDir().getAbsolutePath().toString() + File.separator + "test.txt"
            2 -> path = getFilesDir().getAbsolutePath().toString() + File.separator + "test.xlsx"
            3 -> path = getFilesDir().getAbsolutePath().toString() + File.separator + "test.pptx"
            4 -> path = getFilesDir().getAbsolutePath().toString() + File.separator + "test.pdf"
        }
        return path
    }

    private fun copy() { // 开始复制
        val path = "file" + File.separator
        copyAssetsFileToAppFiles(path + "test.docx", "test.docx")
        copyAssetsFileToAppFiles(path + "test.pdf", "test.pdf")
        copyAssetsFileToAppFiles(path + "test.pptx", "test.pptx")
        copyAssetsFileToAppFiles(path + "test.txt", "test.txt")
        copyAssetsFileToAppFiles(path + "test.xlsx", "test.xlsx")
    }

    /**
     * 从assets目录中复制某文件内容
     *
     * @param assetFileName assets目录下的文件
     * @param newFileName   复制到/data/data/package_name/files/目录下文件名
     */
    private fun copyAssetsFileToAppFiles(
        assetFileName: String,
        newFileName: String
    ) {
        var `is`: InputStream? = null
        var fos: FileOutputStream? = null
        try {
            `is` = this.getAssets().open(assetFileName)
            fos = this.openFileOutput(newFileName, Context.MODE_PRIVATE)
            var byteCount = 0
            val buffer = ByteArray(1024)
            while (`is`.read(buffer).also { byteCount = it } != -1) {
                fos!!.write(buffer, 0, byteCount)
            }
            fos!!.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                `is`?.close()
                fos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}