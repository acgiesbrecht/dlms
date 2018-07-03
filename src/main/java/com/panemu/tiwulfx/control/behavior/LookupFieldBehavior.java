/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.control.behavior;

import com.panemu.tiwulfx.control.LookupField;
import static com.panemu.tiwulfx.control.behavior.TypeAheadFieldBehavior.KEY_BINDINGS;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import java.util.ArrayList;
import java.util.List;
import static javafx.scene.input.KeyCode.*;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class LookupFieldBehavior<T> extends BehaviorBase<LookupField<T>> {

    public static final String ACTION_SHOW_SUGGESTION = "showSuggestion";
    public static final String ACTION_SHOW_SEARCH_WINDOW = "showSearchWindow";

    public LookupFieldBehavior(LookupField<T> c) {
        super(c, KEY_BINDINGS);
    }
    protected static final List<KeyBinding> KEY_BINDINGS = new ArrayList<KeyBinding>();

    static {
        KEY_BINDINGS.add(new KeyBinding(ENTER, ACTION_SHOW_SEARCH_WINDOW).ctrl());
        KEY_BINDINGS.add(new KeyBinding(SPACE, ACTION_SHOW_SUGGESTION).ctrl());
        // However, we want to consume other key press / release events too, for
        // things that would have been handled by the InputCharacter normally
        KEY_BINDINGS.add(new KeyBinding(null, KeyEvent.KEY_PRESSED, "Consume"));
    }

    @Override
    protected void callAction(String string) {
        if (ACTION_SHOW_SEARCH_WINDOW.equals(string)) {
            getControl().showLookupDialog();
        } else if (ACTION_SHOW_SUGGESTION.equals(string)) {
            if (getControl().isShowingSuggestion()) {
                getControl().hideSuggestion();
            }
            getControl().showSuggestion();
        } else {
            super.callAction(string);
        }
    }
}
