package com.modulo06.echoesmoon.systems;

public class DialogSystem {
    private String[] lines = new String[0];
    private int index = 0;
    private boolean open = false;
    private boolean finished = false;

    public void start(String[] lines) {
        this.lines = lines != null ? lines : new String[0];
        index = 0;
        open = this.lines.length > 0;
        finished = false;
    }

    public void next() {
        if (!open) return;
        index++;
        if (index >= lines.length) {
            open = false;
            finished = true;
        }
    }

    public boolean isOpen() { return open; }
    public boolean isFinished() { return finished; }

    public String line() {
        if (!open || index < 0 || index >= lines.length) return "";
        return lines[index];
    }
}
