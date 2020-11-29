package coddiers.hackyeah.dziki.ui;

import coddiers.hackyeah.dziki.R;

public enum ModelObject {

    ONE(R.string.one, R.layout.view_one),
    TWO(R.string.two, R.layout.view_two),
    TREE(R.string.tree, R.layout.view_tree),
    FOUR(R.string.four, R.layout.view_four),
    FIVE(R.string.five, R.layout.view_five);

    private int mTitleResId;
    private int mLayoutResId;

    ModelObject(int titleResId, int layoutResId) {
        mTitleResId = titleResId;
        mLayoutResId = layoutResId;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }

}