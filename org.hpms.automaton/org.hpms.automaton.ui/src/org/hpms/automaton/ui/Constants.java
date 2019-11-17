package org.hpms.automaton.ui;

interface Constants {

   String ICON_PATH_NEW    = "icons/new.gif";
   String ICON_PATH_RENAME = "icons/rename.gif";
   String ICON_PATH_DELETE = "icons/delete.png";

   String ICON_KEY_NEW     = "org.hpms.automaton.editors.icons.new";
   String ICON_KEY_RENAME  = "org.hpms.automaton.editors.icons.rename";
   String ICON_KEY_DELETE  = "org.hpms.automaton.editors.icons.delete";

   String TRANSITION_ELEMENT     = "transition";
   String TRANSITION_SOURCE_ATTR = "from";
   String TRANSITION_EVENT_ATTR  = "event";
   String TRANSITION_DEST_ATTR   = "futur";
   String SHORTCUT_ELEMENT       = "shortcut";
   String SHORTCUT_EVENT_ATTR    = "event";
   String SHORTCUT_DEST_ATTR     = "futur";
   String ON_ENTRY_ELEMENT       = "on-entry";
   String ON_ENTRY_STATE_ATTR    = "state";
   String ON_ENTRY_ACTION_ATTR   = "action";
   String ON_EXIT_ELEMENT        = "on-exit";
   String ON_EXIT_STATE_ATTR     = "state";
   String ON_EXIT_ACTION_ATTR    = "action";
   String PSEUDO_STATE_ANY       = "*";
}
