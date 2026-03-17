package at.htl.overtale;

import java.util.List;

public class DialogManager {

    private final OvertaleHud hud;
    private List<String> pages;
    private int currentPage = 0;
    private boolean active = false;

    public DialogManager(OvertaleHud hud) {
        this.hud = hud;
    }

    public void startDialog(List<String> texts) {
        pages = texts;
        currentPage = 0;
        active = true;
        hud.showDialogOnly();
        showCurrentPage();
    }

    // Z zum weitergehen im dialog und ENTER zum überspringen
    public void advance() {
        if (!active) return;

        if (!hud.isTypewriterDone()) {
            hud.skipTypewriter();
        } else if (currentPage + 1 < pages.size()) {
            currentPage++;
            showCurrentPage();
        } else {
            active = false;
            hud.hideDialog(); // schaltet dialogPane wieder aus
        }
    }

    public boolean isActive() {
        return active;
    }

    private void showCurrentPage() {
        hud.showDialog(pages.get(currentPage));
    }
}
