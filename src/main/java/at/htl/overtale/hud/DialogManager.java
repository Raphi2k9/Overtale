package at.htl.overtale.hud;

import java.util.List;

public class DialogManager {

    private final OvertaleHud _hud;
    private List<String> _pages;
    private int _currentPage = 0;
    private boolean _active = false;
    private Runnable _onComplete = null;

    public DialogManager(OvertaleHud hud) {
        _hud = hud;
    }

    public void startDialog(List<String> texts) {
        startDialog(texts, null);
    }

    public void startDialog(List<String> texts, Runnable onComplete) {
        _pages = texts;
        _currentPage = 0;
        _active = true;
        _onComplete = onComplete;
        _hud.showDialogOnly();
        showCurrentPage();
    }

    // Z zum weitergehen im dialog und ENTER zum überspringen
    public void advance() {
        if (!_active) return;

        if (!_hud.isTypewriterDone()) {
            _hud.skipTypewriter();
        } else if (_currentPage + 1 < _pages.size()) {
            _currentPage++;
            showCurrentPage();
        } else {
            _active = false;
            _hud.hideDialog();
            if (_onComplete != null) {
                _onComplete.run();
                _onComplete = null;
            }
        }
    }

    public boolean isActive() {
        return _active;
    }

    private void showCurrentPage() {
        _hud.showDialog(_pages.get(_currentPage));
    }
}
