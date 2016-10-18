package com.opensource.svgaplayer;

import android.graphics.Path;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class SVGAPath implements Serializable {

    private transient final Path mPath = new Path();

    private transient boolean mPathBuilt = false;

    public SVGAPath() {
    }

    String[] items;

    public Path getPath() {
        if (!mPathBuilt) {
            buildPath();
        }
        return mPath;
    }

    final static List<String> VALID_METHODS = Collections.unmodifiableList(
            Arrays.asList(
                    "M", "L", "H",
                    "V", "C", "S",
                    "Q", "R", "A",
                    "Z", "m", "l",
                    "h", "v", "c",
                    "s", "q", "r",
                    "a", "z"));


    void setValues(final String values) {
//        values = values.replace(",", " ");
        items = values.split("[,\\s+]");
    }

    void buildPath() {
        String currentMethod = "";
        ArrayList<SVGAPoint> args = new ArrayList<>();
        String argLast = null;
        for (String item : items) {
            if (item.length() < 1) {
                continue;
            }
            String firstLetter = item.substring(0, 1);
            if (VALID_METHODS.contains(firstLetter)) {
                if (null != argLast) {
                    try {
                        args.add(new SVGAPoint(Float.valueOf(argLast)));
                    } catch (Exception ignore) {
                    }
                }
                this.operate(currentMethod, args);
                args.clear();
                currentMethod = firstLetter;
                argLast = item.substring(1);
            } else {
                if (null != argLast && argLast.trim().length() > 0) {
                    try {
                        args.add(new SVGAPoint(Float.valueOf(argLast), Float.valueOf(item)));
                    } catch (Exception ignore) {
                    }
                    argLast = null;
                } else {
                    argLast = item;
                }
            }
        }
        this.operate(currentMethod, args);
    }

    private void operate(final String method, final List<SVGAPoint> args) {
        SVGAPoint currentPoint = new SVGAPoint(0, 0);

        if (method.equals("M") && args.size() == 1) {
            mPath.moveTo(args.get(0).x, args.get(0).y);
            currentPoint = new SVGAPoint(args.get(0).x, args.get(0).y);
        } else if (method.equals("m") && args.size() == 1) {
            mPath.rMoveTo(args.get(0).x, args.get(0).y);
            currentPoint = new SVGAPoint(currentPoint.x + args.get(0).x, currentPoint.y + args.get(0).y);
        }
        if (method.equals("L") && args.size() == 1) {
            mPath.lineTo(args.get(0).x, args.get(0).y);
        } else if (method.equals("l") && args.size() == 1) {
            mPath.rLineTo(args.get(0).x, args.get(0).y);
        }
        if (method.equals("C") && args.size() == 3) {
            mPath.cubicTo(args.get(0).x, args.get(0).y, args.get(1).x, args.get(1).y, args.get(2).x, args.get(2).y);
        } else if (method.equals("c") && args.size() == 3) {
            mPath.rCubicTo(args.get(0).x, args.get(0).y, args.get(1).x, args.get(1).y, args.get(2).x, args.get(2).y);
        }
        if (method.equals("Q") && args.size() == 2) {
            mPath.quadTo(args.get(0).x, args.get(0).y, args.get(1).x, args.get(1).y);
        } else if (method.equals("q") && args.size() == 2) {
            mPath.rQuadTo(args.get(0).x, args.get(0).y, args.get(1).x, args.get(1).y);
        }
        if (method.equals("H") && args.size() == 1) {
            mPath.lineTo(args.get(0).val, currentPoint.y);
        } else if (method.equals("h") && args.size() == 1) {
            mPath.rLineTo(args.get(0).val, 0);
        }
        if (method.equals("V") && args.size() == 1) {
            mPath.lineTo(currentPoint.x, args.get(0).val);
        } else if (method.equals("v") && args.size() == 1) {
            mPath.rLineTo(0, args.get(0).val);
        }
        if (method.equals("Z") && args.size() == 1) {
            mPath.close();
        } else if (method.equals("z") && args.size() == 1) {
            mPath.close();
        }
    }

}
