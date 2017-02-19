
package com.example.yokoshogi;
import java.util.Arrays;

public class Position implements Comparable<Position>,Cloneable {

    private int[] location;
    private boolean reverseFlg;

    private Position ( Builder builder) {
        this.location = new int[] {builder.row,builder.column};
        this.reverseFlg = builder.reverseFlg;
    }
    public static class Builder {
        private int row;
        private int column;
        private boolean reverseFlg;

        public Position.Builder setRow ( int row ) {
            this.row = row;
            return this;
        }

        public Position.Builder setColumn ( int column ) {
            this.column = column;
            return this;
        }

        public Position.Builder setReverse ( boolean reverse ) {
            this.reverseFlg = reverse;
            return this;
        }
        public Position build() {
            return new Position (this);
        }
    }

    public int[] getLocation () {
        return location;
    }

    @Override
    public int compareTo(Position o) {
        return  this.getIndexOnBoard () - o.getIndexOnBoard ();
    }

    @Override
    public boolean equals ( Object obj ) {
        if (obj == null) {
            return false;
        }
        return Arrays.equals (location,((Position)obj).getLocation ());
    }

    @Override
    public int hashCode () {
        return  this.getRow () * Ban.ROW_COUNT + getColumn ();
    }

    int getIndexOnBoard () {
        if (this.reverseFlg) {
            Position position = this.getPointSymmetry ();
            return position.getRow () * Ban.ROW_COUNT + position.getColumn ();
        }else {
            return getRow () * Ban.ROW_COUNT + getColumn ();
        }
    }
    public int getRow () {
        return location[0];
    }
    public int getColumn () {
        return location[1];
    }
    void setRow (int row) {
        location[0] = row;
    }
    void setColumn (int column) {
        location[1] = column;
    }

    public Position copy () {
        Builder builder = new Builder ();
        builder.setColumn ( this.getColumn () );
        builder.setRow ( this.getRow () );
        builder.setReverse ( this.reverseFlg );
        return builder.build ();
    }

    @Override
    public String toString () {
        return "ヨコ" + String.valueOf ( getColumn () ) + "タテ" + String.valueOf ( getRow () );
    }
    //MoveAndIterateの反転用
    Position getReverse( boolean enemyFlg ) {
        int i;
        if (enemyFlg) {
            i = 1;
        }else{
            i = -1;
        }
        Builder builder = new Builder ();
        builder.setColumn ( i * this.getColumn () );
        builder.setRow ( i * this.getRow () );
        builder.setReverse ( this.reverseFlg );
        return builder.build ();
    }
    public Position getPointSymmetry () {
        Position position = this.copy ();
        position.setColumn ( Ban.COLUMN_COUNT - this.getColumn () - 1);
        position.setRow ( Ban.ROW_COUNT - this.getRow () - 1);
        return position;
    }

    public void setReverseFlg ( boolean reverseFlg ) {
        this.reverseFlg = reverseFlg;
    }
}
