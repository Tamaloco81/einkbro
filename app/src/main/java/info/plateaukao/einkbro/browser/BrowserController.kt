package info.plateaukao.einkbro.browser

import android.net.Uri
import android.os.Message
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.CustomViewCallback
import info.plateaukao.einkbro.view.Album

interface BrowserController {
    fun updateProgress(progress: Int)
    fun updateTitle(title: String?)
    fun addNewTab(url: String)
    fun showAlbum(albumController: AlbumController)
    fun removeAlbum(albumController: AlbumController)
    fun onUpdateAlbum(album: Album)
    fun showFileChooser(filePathCallback: ValueCallback<Array<Uri>>)
    fun onShowCustomView(view: View?, callback: CustomViewCallback?)
    fun onLongPress(message: Message, event: MotionEvent?)
    fun hideOverview()
    fun addHistory(title: String, url: String)
    fun onHideCustomView(): Boolean
    fun handleKeyEvent(event: KeyEvent): Boolean
    fun loadInSecondPane(url: String): Boolean //void updateTabs(Album album);
}