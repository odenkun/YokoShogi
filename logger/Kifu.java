package com.example.yokoshogi.logger;

import com.example.yokoshogi.logger.Sashite;

import java.util.ArrayList;


public class Kifu {
    private ArrayList<Sashite> sashiteArrayList;
    public Kifu () {
        sashiteArrayList = new ArrayList<>();
    }
    public void add (Sashite sashite) {
        sashiteArrayList.add(sashite);
    }

    public int getCount() {
        return sashiteArrayList.size ();
    }

    public Sashite getLatest() {
        return sashiteArrayList.get(sashiteArrayList.size() - 1);
    }
}
