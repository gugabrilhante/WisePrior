package com.gustavo.brilhante.ui

object TestTags {
    // Screens
    const val SCREEN_TASK_LIST = "screen_task_list"
    const val SCREEN_TASK_EDITOR = "screen_task_editor"

    // Buttons — Task List
    const val BTN_TASK_LIST_ADD = "btn_task_list_add"

    // Buttons — Task Editor
    const val BTN_TASK_EDITOR_DONE = "btn_task_editor_done"
    const val BTN_TASK_EDITOR_BACK = "btn_task_editor_back"

    // Inputs — Task Editor
    const val INPUT_TASK_EDITOR_TITLE = "input_task_editor_title"
    const val INPUT_TASK_EDITOR_NOTES = "input_task_editor_notes"
    const val INPUT_TASK_EDITOR_URL = "input_task_editor_url"

    // Cards
    const val CARD_TASK_ITEM = "card_task_item"

    // Text / containers
    const val TEXT_EMPTY_STATE = "text_empty_state"
    const val TEXT_TASK_TITLE = "text_task_title"
    const val TEXT_TASK_NOTES = "text_task_notes"

    // Toggles — Task Editor
    const val TOGGLE_TASK_DATE = "toggle_task_date"
    const val TOGGLE_TASK_TIME = "toggle_task_time"
    const val TOGGLE_TASK_URGENT = "toggle_task_urgent"
    const val TOGGLE_TASK_FLAGGED = "toggle_task_flagged"
    const val TOGGLE_TASK_RECURRENCE = "toggle_task_recurrence"

    // Priority segments
    const val SEGMENT_PRIORITY_NONE = "segment_priority_none"
    const val SEGMENT_PRIORITY_LOW = "segment_priority_low"
    const val SEGMENT_PRIORITY_MEDIUM = "segment_priority_medium"
    const val SEGMENT_PRIORITY_HIGH = "segment_priority_high"

    // Section headers — Task Editor
    const val SECTION_TASK_EDITOR_DATETIME = "section_task_editor_datetime"
    const val SECTION_TASK_EDITOR_DETAILS = "section_task_editor_details"
    const val SECTION_TASK_EDITOR_PRIORITY = "section_task_editor_priority"
    const val SECTION_TASK_EDITOR_TAGS = "section_task_editor_tags"
    const val SECTION_TASK_EDITOR_URL = "section_task_editor_url"

    // Tags (chips)
    const val CHIP_TAG_ITEM = "chip_tag_item"

    // Dialogs
    const val DIALOG_TAG_EDITOR = "dialog_tag_editor"
    const val DIALOG_DATE_PICKER = "dialog_date_picker"
    const val DIALOG_TIME_PICKER = "dialog_time_picker"

    // Tag Editor Dialog inputs / buttons
    const val INPUT_TAG_EDITOR_NAME = "input_tag_editor_name"
    const val BTN_TAG_EDITOR_SAVE = "btn_tag_editor_save"
    const val BTN_TAG_EDITOR_CANCEL = "btn_tag_editor_cancel"
    const val BTN_TAG_EDITOR_DELETE = "btn_tag_editor_delete"

    // Recurrence
    const val DROPDOWN_RECURRENCE_UNIT = "dropdown_recurrence_unit"
    const val BTN_TASK_EDITOR_RECURRENCE_DECREMENT = "btn_task_editor_recurrence_decrement"
    const val BTN_TASK_EDITOR_RECURRENCE_INCREMENT = "btn_task_editor_recurrence_increment"

    // Sidebar — already declared as string constants in TaskSidebar.kt; mirrored here
    // so tests can import from a single location.
    const val SIDEBAR_LIST = "sidebar_list"
    const val BTN_SIDEBAR_ADD_TAG = "add_tag_button"

    // Sidebar collection items
    const val SIDEBAR_ITEM_TODAY = "sidebar_item_today"
    const val SIDEBAR_ITEM_SCHEDULED = "sidebar_item_scheduled"
    const val SIDEBAR_ITEM_ALL = "sidebar_item_all"
    const val SIDEBAR_ITEM_FLAGGED = "sidebar_item_flagged"
    const val SIDEBAR_ITEM_COMPLETED = "sidebar_item_completed"
    const val SIDEBAR_TAGS_HEADER = "sidebar_tags_header"
}
