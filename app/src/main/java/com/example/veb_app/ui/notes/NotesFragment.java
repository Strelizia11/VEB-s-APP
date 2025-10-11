package com.example.veb_app.ui.notes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.veb_app.R;
import com.example.veb_app.databinding.FragmentNotesBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class NotesFragment extends Fragment {
    private boolean isProcessingList = false;
    private boolean isInList = false;
    private boolean isEditingMode = false;

    private FragmentNotesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotesViewModel notesViewModel =
                new ViewModelProvider(this).get(NotesViewModel.class);

        binding = FragmentNotesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNotes;
        notesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Setup FAB click handler
        FloatingActionButton fabAddNote = binding.fabAddNote;
        if (fabAddNote != null) {
            fabAddNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCreateNoteDialog();
                }
            });
        }

        // Setup search functionality
        TextInputEditText etSearch = root.findViewById(R.id.et_search);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchNotes(s.toString());
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        // Setup back to home button
        MaterialButton btnBackToHome = root.findViewById(R.id.btn_back_to_home);
        if (btnBackToHome != null) {
            btnBackToHome.setOnClickListener(v -> {
                // Use the activity's nav controller for consistent navigation
                if (getActivity() != null) {
                    androidx.navigation.fragment.NavHostFragment navHostFragment = (androidx.navigation.fragment.NavHostFragment) getActivity().getSupportFragmentManager()
                            .findFragmentById(R.id.nav_host_fragment_content_main);
                    if (navHostFragment != null) {
                        androidx.navigation.NavController navController = navHostFragment.getNavController();
                        navController.navigate(R.id.nav_home);
                    }
                }
            });
        }

        // Load existing notes from NotesManager
        loadExistingNotes();
        
        // Show back button if there are notes (indicating user came from home)
        showBackButtonIfNeeded();
        
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload notes when returning to this fragment
        loadExistingNotes();
        // Update back button visibility
        showBackButtonIfNeeded();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showCreateNoteDialog() {
        showCreateNoteDialog(null);
    }

    private void showCreateNoteDialog(Note existingNote) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_note, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(d -> {
            isEditingMode = false;
        });
        dialog.show();

        // Get dialog views
        TextInputEditText etTitle = dialogView.findViewById(R.id.et_note_title);
        TextInputEditText etBody = dialogView.findViewById(R.id.et_note_body);
        
        // Pre-fill fields if editing existing note
        if (existingNote != null) {
            isEditingMode = true;
            etTitle.setText(existingNote.getTitle());
            etBody.setText(existingNote.getBody());
            // Set cursor to end for editing
            etBody.setSelection(etBody.getText().length());
        }
        MaterialButton btnBold = dialogView.findViewById(R.id.btn_bold);
        MaterialButton btnItalic = dialogView.findViewById(R.id.btn_italic);
        MaterialButton btnUnderline = dialogView.findViewById(R.id.btn_underline);
        MaterialButton btnStrikethrough = dialogView.findViewById(R.id.btn_strikethrough);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Formatting button states
        final boolean[] isBold = {false};
        final boolean[] isItalic = {false};
        final boolean[] isUnderline = {false};
        final boolean[] isStrikethrough = {false};

        // Bold button
        btnBold.setOnClickListener(v -> {
            isBold[0] = !isBold[0];
            btnBold.setBackgroundColor(isBold[0] ? getResources().getColor(R.color.sage_green_light) : 0);
            btnBold.setTextColor(isBold[0] ? getResources().getColor(R.color.white) : getResources().getColor(R.color.sage_green));
            applyFormattingToSelectionOrNewText(etBody, isBold[0], isItalic[0], isUnderline[0], isStrikethrough[0], "bold");
        });

        // Italic button
        btnItalic.setOnClickListener(v -> {
            isItalic[0] = !isItalic[0];
            btnItalic.setBackgroundColor(isItalic[0] ? getResources().getColor(R.color.sage_green_light) : 0);
            btnItalic.setTextColor(isItalic[0] ? getResources().getColor(R.color.white) : getResources().getColor(R.color.sage_green));
            applyFormattingToSelectionOrNewText(etBody, isBold[0], isItalic[0], isUnderline[0], isStrikethrough[0], "italic");
        });

        // Underline button
        btnUnderline.setOnClickListener(v -> {
            isUnderline[0] = !isUnderline[0];
            btnUnderline.setBackgroundColor(isUnderline[0] ? getResources().getColor(R.color.sage_green_light) : 0);
            btnUnderline.setTextColor(isUnderline[0] ? getResources().getColor(R.color.white) : getResources().getColor(R.color.sage_green));
            applyFormattingToSelectionOrNewText(etBody, isBold[0], isItalic[0], isUnderline[0], isStrikethrough[0], "underline");
        });

        // Strikethrough button
        btnStrikethrough.setOnClickListener(v -> {
            isStrikethrough[0] = !isStrikethrough[0];
            btnStrikethrough.setBackgroundColor(isStrikethrough[0] ? getResources().getColor(R.color.sage_green_light) : 0);
            btnStrikethrough.setTextColor(isStrikethrough[0] ? getResources().getColor(R.color.white) : getResources().getColor(R.color.sage_green));
            applyFormattingToSelectionOrNewText(etBody, isBold[0], isItalic[0], isUnderline[0], isStrikethrough[0], "strikethrough");
        });

        // Add TextWatcher to apply formatting to new text and handle smart lists
        etBody.addTextChangedListener(new android.text.TextWatcher() {
            private int lastCursorPosition = 0;
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                lastCursorPosition = etBody.getSelectionStart();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isFormatting || isProcessingList) return;
                
                // Handle Enter key for smart lists - simplified approach (works in both create and edit mode)
                if (count == 1 && before == 0 && s.charAt(start) == '\n') {
                    handleEnterKey(s.toString(), start, etBody);
                    return;
                }
                
                // Only apply formatting if text was added (not deleted) and not in editing mode
                if (count > before && !isEditingMode) {
                    int newTextStart = start;
                    int newTextEnd = start + count;
                    
                    // Apply active formatting to new text
                    if (isBold[0] || isItalic[0] || isUnderline[0] || isStrikethrough[0]) {
                        isFormatting = true;
                        applyFormattingToNewText(etBody, newTextStart, newTextEnd, isBold[0], isItalic[0], isUnderline[0], isStrikethrough[0]);
                        isFormatting = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // Don't restore cursor position if we're applying formatting, processing lists, or editing
                if (!isFormatting && !isProcessingList && !isEditingMode) {
                    // Keep cursor at the end for normal typing
                    if (s.length() > 0) {
                        etBody.setSelection(s.length());
                    }
                }
            }
        });

        // Save button
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String originalBody = etBody.getText().toString();
            android.util.Log.d("SaveNote", "Original body before trimming: '" + originalBody + "'");
            String body = trimEmptyLines(originalBody);
            android.util.Log.d("SaveNote", "Body after trimming: '" + body + "'");

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a note title", Toast.LENGTH_SHORT).show();
                return;
            }

            if (body.isEmpty()) {
                Toast.makeText(getContext(), "Please enter note content", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create and save the note with formatted text
            // First get the original formatted text
            SpannableString originalFormatted = new SpannableString(etBody.getText());
            
            // If the body was trimmed, we need to adjust the SpannableString accordingly
            SpannableString formattedBody;
            if (body.equals(originalBody)) {
                // No trimming occurred, use original formatted text
                formattedBody = originalFormatted;
            } else {
                // Trimming occurred, create new SpannableString from trimmed text
                formattedBody = new SpannableString(body);
            }
            
            android.util.Log.d("SaveNote", "Formatted body: '" + formattedBody.toString() + "'");
            
            if (existingNote != null) {
                // Update existing note
                existingNote.setTitle(title);
                existingNote.setBody(body);
                existingNote.setFormattedBody(formattedBody);
                NotesManager.getInstance().updateNote(existingNote);
                updateNoteDisplay(existingNote);
                Toast.makeText(getContext(), "Note updated: " + title, Toast.LENGTH_SHORT).show();
            } else {
                // Create new note
                Note newNote = new Note(title, body, formattedBody);
                NotesManager.getInstance().addNote(newNote);
                addNoteToDisplay(newNote);
                Toast.makeText(getContext(), "Note saved: " + title, Toast.LENGTH_SHORT).show();
            }
            
            dialog.dismiss();
            isEditingMode = false;
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            isEditingMode = false;
        });
    }

    private void handleEnterKey(CharSequence text, int enterPosition, TextInputEditText editText) {
        isProcessingList = true;
        
        // Find the current line
        String currentText = text.toString();
        int lineStart = findLineStart(currentText, enterPosition);
        String currentLine = currentText.substring(lineStart, enterPosition);
        
        // Debug: Log the current line
        android.util.Log.d("SmartList", "Enter key pressed at position: " + enterPosition);
        android.util.Log.d("SmartList", "Current line: '" + currentLine + "'");
        android.util.Log.d("SmartList", "Is in list: " + isInList);
        
        // Check if current line starts with a list pattern
        ListInfo listInfo = detectListPattern(currentLine);
        
        if (listInfo != null) {
            // Continue the list
            android.util.Log.d("SmartList", "Detected list pattern: " + listInfo.type + " " + listInfo.number);
            isInList = true;
            continueList(listInfo, enterPosition, editText);
        } else {
            // Check if we're in a list and current line is empty (cancel list)
            if (isInList && currentLine.trim().isEmpty()) {
                // Cancel the list by adding a blank line
                android.util.Log.d("SmartList", "Canceling list - was in list and current line is empty");
                cancelList(enterPosition, editText);
                isInList = false;
            } else if (enterPosition > 0) {
                // Check if previous line was a list
                String previousLine = getPreviousLine(currentText, enterPosition);
                ListInfo prevListInfo = detectListPattern(previousLine);
                android.util.Log.d("SmartList", "Previous line: '" + previousLine + "'");
                android.util.Log.d("SmartList", "Current line empty: " + currentLine.trim().isEmpty());
                
                if (prevListInfo != null) {
                    isInList = true;
                    android.util.Log.d("SmartList", "Set isInList to true based on previous line");
                }
            }
        }
        
        isProcessingList = false;
    }
    
    private int findLineStart(String text, int position) {
        int start = position - 1;
        while (start >= 0 && text.charAt(start) != '\n') {
            start--;
        }
        return start + 1;
    }
    
    private String getPreviousLine(String text, int position) {
        int lineStart = findLineStart(text, position);
        if (lineStart > 0) {
            int prevLineStart = findLineStart(text, lineStart - 1);
            return text.substring(prevLineStart, lineStart - 1);
        }
        return "";
    }
    
    private ListInfo detectListPattern(String line) {
        line = line.trim();
        
        // Check for numbered list (1. 2. 3. etc.) - more flexible pattern
        if (line.matches("^\\d+\\.\\s?.*")) {
            String number = line.replaceAll("^(\\d+)\\.\\s?.*", "$1");
            return new ListInfo("numbered", Integer.parseInt(number));
        }
        
        // Check for bullet points (- or •) - more flexible pattern
        if (line.matches("^[-•]\\s?.*")) {
            return new ListInfo("bullet", 0);
        }
        
        return null;
    }
    
    private void continueList(ListInfo listInfo, int position, TextInputEditText editText) {
        String continuation = "";
        
        if (listInfo.type.equals("numbered")) {
            continuation = (listInfo.number + 1) + ". ";
        } else if (listInfo.type.equals("bullet")) {
            continuation = "• ";
        }
        
        // Insert the continuation after the newline
        android.text.Editable editable = editText.getText();
        editable.insert(position + 1, continuation);
        editText.setSelection(position + 1 + continuation.length());
    }
    
    private void cancelList(int position, TextInputEditText editText) {
        // Add an extra newline to create a blank line and exit list mode
        android.text.Editable editable = editText.getText();
        editable.insert(position + 1, "\n");
        editText.setSelection(position + 2);
    }
    
    private static class ListInfo {
        String type;
        int number;
        
        ListInfo(String type, int number) {
            this.type = type;
            this.number = number;
        }
    }

    private void applyFormattingToSelectionOrNewText(TextInputEditText editText, boolean bold, boolean italic, boolean underline, boolean strikethrough, String formatType) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        
        // If text is selected, apply formatting to selection
        if (start != end && start >= 0 && end >= 0) {
            applyFormattingToSelection(editText, start, end, bold, italic, underline, strikethrough, formatType);
        }
        // If no text is selected, the formatting will be applied to new text via TextWatcher
    }
    
    private void applyFormattingToSelection(TextInputEditText editText, int start, int end, boolean bold, boolean italic, boolean underline, boolean strikethrough, String formatType) {
        SpannableString spannable = new SpannableString(editText.getText());
        
        // Remove existing formatting from selection only for the specific format type being toggled
        if (formatType.equals("bold")) {
            StyleSpan[] boldSpans = spannable.getSpans(start, end, StyleSpan.class);
            for (StyleSpan span : boldSpans) {
                if (span.getStyle() == android.graphics.Typeface.BOLD) {
                    spannable.removeSpan(span);
                }
            }
        } else if (formatType.equals("italic")) {
            StyleSpan[] italicSpans = spannable.getSpans(start, end, StyleSpan.class);
            for (StyleSpan span : italicSpans) {
                if (span.getStyle() == android.graphics.Typeface.ITALIC) {
                    spannable.removeSpan(span);
                }
            }
        } else if (formatType.equals("underline")) {
            UnderlineSpan[] underlineSpans = spannable.getSpans(start, end, UnderlineSpan.class);
            for (UnderlineSpan span : underlineSpans) {
                spannable.removeSpan(span);
            }
        } else if (formatType.equals("strikethrough")) {
            StrikethroughSpan[] strikethroughSpans = spannable.getSpans(start, end, StrikethroughSpan.class);
            for (StrikethroughSpan span : strikethroughSpans) {
                spannable.removeSpan(span);
            }
        }
        
        // Apply new formatting based on format type and current state
        if (formatType.equals("bold") && bold) {
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (formatType.equals("italic") && italic) {
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (formatType.equals("underline") && underline) {
            spannable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (formatType.equals("strikethrough") && strikethrough) {
            spannable.setSpan(new StrikethroughSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        editText.setText(spannable);
        editText.setSelection(start, end);
    }
    
    private void applyFormattingToNewText(TextInputEditText editText, int start, int end, boolean bold, boolean italic, boolean underline, boolean strikethrough) {
        SpannableString spannable = new SpannableString(editText.getText());
        
        // Apply all active formatting styles to new text
        if (bold) {
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (italic) {
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (underline) {
            spannable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (strikethrough) {
            spannable.setSpan(new StrikethroughSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        editText.setText(spannable);
        // Keep cursor at the end of the text
        editText.setSelection(spannable.length());
    }

    private void addNoteToDisplay(Note note) {
        // Hide the default text
        binding.textNotes.setVisibility(View.GONE);

        // Create note container
        View noteContainer = createNoteContainer(note);
        
        // Add to the notes container at the beginning (most recent first)
        LinearLayout notesContainer = binding.getRoot().findViewById(R.id.notes_container);
        notesContainer.addView(noteContainer, 0); // Insert at position 0 (top)
    }

    private void updateNoteDisplay(Note note) {
        // Find and update the existing note container
        LinearLayout notesContainer = binding.getRoot().findViewById(R.id.notes_container);
        
        // Find the note container by looking for the one with matching note data
        for (int i = 0; i < notesContainer.getChildCount(); i++) {
            View child = notesContainer.getChildAt(i);
            if (child instanceof LinearLayout && child.getTag() != null) {
                Note existingNote = (Note) child.getTag();
                if (existingNote.getId() == note.getId()) {
                    // Remove old container
                    notesContainer.removeViewAt(i);
                    
                    // Create new container with updated content
                    View newNoteContainer = createNoteContainer(note);
                    notesContainer.addView(newNoteContainer, i);
                    break;
                }
            }
        }
    }

    private View createNoteContainer(Note note) {
        // Create main note container
        LinearLayout noteContainer = new LinearLayout(getContext());
        noteContainer.setOrientation(LinearLayout.VERTICAL);
        noteContainer.setPadding(24, 24, 24, 24); // Increased padding for better spacing
        
        // Use different background based on pin status
        if (note.isPinned()) {
            noteContainer.setBackgroundResource(R.drawable.pinned_note_container_background);
            android.util.Log.d("PinColor", "Using pinned note background for pinned note");
        } else {
            noteContainer.setBackgroundResource(R.drawable.note_container_background);
            android.util.Log.d("PinColor", "Using regular note background for unpinned note");
        }
        
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(16, 16, 16, 16); // Increased margins between notes
        noteContainer.setLayoutParams(containerParams);

        // Create title container
        LinearLayout titleContainer = new LinearLayout(getContext());
        titleContainer.setOrientation(LinearLayout.VERTICAL);
        titleContainer.setPadding(16, 16, 16, 16); // Increased padding for title container
        titleContainer.setBackgroundResource(R.drawable.title_note_container_background);
        
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, 0, 0, 12); // Increased margin between title and body
        titleContainer.setLayoutParams(titleParams);

        // Create title text
        TextView titleText = new TextView(getContext());
        titleText.setText(note.getTitle());
        titleText.setTextSize(18);
        titleText.setTextColor(getResources().getColor(R.color.sage_green));
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        titleText.setGravity(android.view.Gravity.CENTER);
        titleText.setPadding(16, 8, 16, 8); // Add padding inside title text
        titleContainer.addView(titleText);

        // Create body text with preserved formatting
        TextView bodyText = new TextView(getContext());
        bodyText.setText(note.getFormattedBody());
        bodyText.setTextSize(14);
        bodyText.setTextColor(getResources().getColor(R.color.black));
        bodyText.setPadding(16, 12, 16, 16); // Add more padding for better spacing
        bodyText.setLineSpacing(4, 1.1f); // Add line spacing for better readability

        // Add title container and body to note container
        noteContainer.addView(titleContainer);
        noteContainer.addView(bodyText);

        // Set tag to identify this note container
        noteContainer.setTag(note);

        // Add long press listener for context menu
        noteContainer.setOnLongClickListener(v -> {
            showNoteContextMenu(note, noteContainer);
            return true;
        });

        return noteContainer;
    }

    // Note class
    public static class Note {
        private String title;
        private String body;
        private SpannableString formattedBody;
        private boolean isPinned;
        private long id;

        public Note(String title, String body, SpannableString formattedBody) {
            this.title = title;
            this.body = body;
            this.formattedBody = formattedBody;
            this.isPinned = false;
            this.id = System.currentTimeMillis(); // Simple ID generation
        }

        public Note(String title, String body, SpannableString formattedBody, boolean isPinned, long id) {
            this.title = title;
            this.body = body;
            this.formattedBody = formattedBody;
            this.isPinned = isPinned;
            this.id = id;
        }

        public String getTitle() { return title; }
        public String getBody() { return body; }
        public SpannableString getFormattedBody() { return formattedBody; }
        public boolean isPinned() { return isPinned; }
        public long getId() { return id; }
        
        public void setPinned(boolean pinned) { this.isPinned = pinned; }
        public void setTitle(String title) { this.title = title; }
        public void setBody(String body) { this.body = body; }
        public void setFormattedBody(SpannableString formattedBody) { this.formattedBody = formattedBody; }
    }

    private String trimEmptyLines(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Debug logging
        android.util.Log.d("TrimEmptyLines", "Original text: '" + text + "'");
        android.util.Log.d("TrimEmptyLines", "Original length: " + text.length());
        
        // Split into lines
        String[] lines = text.split("\n", -1);
        android.util.Log.d("TrimEmptyLines", "Number of lines: " + lines.length);
        
        // Find first non-empty line
        int start = 0;
        while (start < lines.length && lines[start].trim().isEmpty()) {
            android.util.Log.d("TrimEmptyLines", "Skipping empty line at start: " + start);
            start++;
        }
        
        // Find last non-empty line
        int end = lines.length - 1;
        while (end >= start && lines[end].trim().isEmpty()) {
            android.util.Log.d("TrimEmptyLines", "Skipping empty line at end: " + end);
            end--;
        }
        
        android.util.Log.d("TrimEmptyLines", "Start index: " + start + ", End index: " + end);
        
        // If all lines are empty, return empty string
        if (start > end) {
            android.util.Log.d("TrimEmptyLines", "All lines are empty, returning empty string");
            return "";
        }
        
        // Join the non-empty lines
        StringBuilder result = new StringBuilder();
        for (int i = start; i <= end; i++) {
            if (i > start) {
                result.append("\n");
            }
            result.append(lines[i]);
        }
        
        String trimmed = result.toString();
        android.util.Log.d("TrimEmptyLines", "Trimmed text: '" + trimmed + "'");
        android.util.Log.d("TrimEmptyLines", "Trimmed length: " + trimmed.length());
        
        return trimmed;
    }

    private void showNoteContextMenu(Note note, View noteContainer) {
        PopupMenu popupMenu = new PopupMenu(getContext(), noteContainer);
        popupMenu.getMenuInflater().inflate(R.menu.note_context_menu, popupMenu.getMenu());
        
        // Update pin/unpin text based on current state
        if (note.isPinned()) {
            popupMenu.getMenu().findItem(R.id.action_pin).setTitle("Unpin");
        } else {
            popupMenu.getMenu().findItem(R.id.action_pin).setTitle("Pin");
        }
        
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                editNote(note, noteContainer);
                return true;
            } else if (itemId == R.id.action_delete) {
                deleteNote(note, noteContainer);
                return true;
            } else if (itemId == R.id.action_pin) {
                togglePinNote(note, noteContainer);
                return true;
            }
            return false;
        });
        
        popupMenu.show();
    }

    private void editNote(Note note, View noteContainer) {
        // Show the same dialog as create note but pre-filled
        showCreateNoteDialog(note);
    }

    private void deleteNote(Note note, View noteContainer) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Remove the note container from the layout
                    LinearLayout notesContainer = binding.getRoot().findViewById(R.id.notes_container);
                    notesContainer.removeView(noteContainer);
                    
                    // Show the default text if no notes remain
                    if (notesContainer.getChildCount() == 1) { // Only the default text view
                        binding.textNotes.setVisibility(View.VISIBLE);
                    }
                    
                    Toast.makeText(getContext(), "Note deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void togglePinNote(Note note, View noteContainer) {
        boolean wasPinned = note.isPinned();
        boolean isNowPinned = !wasPinned;

        android.util.Log.d("TogglePin", "Note was pinned: " + wasPinned + ", now pinned: " + isNowPinned);

        LinearLayout notesContainer = binding.getRoot().findViewById(R.id.notes_container);

        if (isNowPinned) {
            // If pinning a new note, first unpin any existing pinned note
            unpinExistingNote(notesContainer);
            
            // Now pin the new note
            note.setPinned(true);
            NotesManager.getInstance().updateNote(note);
            
            // Remove the old container and create a new one with updated pin status
            notesContainer.removeView(noteContainer);
            View newNoteContainer = createNoteContainer(note);
            
            // Add pinned note at the top (after default text if visible)
            int insertIndex = binding.textNotes.getVisibility() == View.VISIBLE ? 1 : 0;
            notesContainer.addView(newNoteContainer, insertIndex);
            Toast.makeText(getContext(), "Note pinned", Toast.LENGTH_SHORT).show();
        } else {
            // Unpinning the note
            note.setPinned(false);
            NotesManager.getInstance().updateNote(note);
            
            // Remove the old container and create a new one with updated pin status
            notesContainer.removeView(noteContainer);
            View newNoteContainer = createNoteContainer(note);
            
            // Add unpinned note at the end
            notesContainer.addView(newNoteContainer);
            Toast.makeText(getContext(), "Note unpinned", Toast.LENGTH_SHORT).show();
        }
    }

    private void unpinExistingNote(LinearLayout notesContainer) {
        // Find and unpin any existing pinned note
        for (int i = 0; i < notesContainer.getChildCount(); i++) {
            View child = notesContainer.getChildAt(i);
            if (child.getTag() instanceof Note) {
                Note existingNote = (Note) child.getTag();
                if (existingNote.isPinned()) {
                    android.util.Log.d("UnpinExisting", "Unpinning existing pinned note");
                    
                    // Unpin the existing note
                    existingNote.setPinned(false);
                    NotesManager.getInstance().updateNote(existingNote);
                    
                    // Remove the old container and create a new one
                    notesContainer.removeView(child);
                    View newNoteContainer = createNoteContainer(existingNote);
                    
                    // Add the unpinned note at the end
                    notesContainer.addView(newNoteContainer);
                    
                    break; // Only one note can be pinned at a time
                }
            }
        }
    }

    private void searchNotes(String query) {
        if (binding == null) return;
        
        LinearLayout notesContainer = binding.getRoot().findViewById(R.id.notes_container);
        if (notesContainer == null) return;
        
        // If search query is empty, show all notes
        if (query.trim().isEmpty()) {
            for (int i = 0; i < notesContainer.getChildCount(); i++) {
                View child = notesContainer.getChildAt(i);
                child.setVisibility(View.VISIBLE);
            }
            return;
        }
        
        // Search through notes and hide/show based on query
        String searchQuery = query.toLowerCase().trim();
        for (int i = 0; i < notesContainer.getChildCount(); i++) {
            View child = notesContainer.getChildAt(i);
            if (child.getTag() instanceof Note) {
                Note note = (Note) child.getTag();
                String title = note.getTitle().toLowerCase();
                String body = note.getBody().toLowerCase();
                
                // Show note if title or body contains search query
                if (title.contains(searchQuery) || body.contains(searchQuery)) {
                    child.setVisibility(View.VISIBLE);
                } else {
                    child.setVisibility(View.GONE);
                }
            }
        }
    }

    private void loadExistingNotes() {
        if (binding == null) return;
        
        LinearLayout notesContainer = binding.getRoot().findViewById(R.id.notes_container);
        if (notesContainer == null) return;
        
        // Clear existing notes from the container
        notesContainer.removeAllViews();
        
        // Get all notes from NotesManager
        java.util.List<Note> allNotes = NotesManager.getInstance().getAllNotes();
        
        if (allNotes.isEmpty()) {
            // Show empty state
            binding.textNotes.setVisibility(View.VISIBLE);
            return;
        }
        
        // Hide default text
        binding.textNotes.setVisibility(View.GONE);
        
        // Sort notes: pinned first, then by ID (most recent first)
        java.util.List<Note> sortedNotes = new java.util.ArrayList<>(allNotes);
        sortedNotes.sort((note1, note2) -> {
            // Pinned notes first
            if (note1.isPinned() && !note2.isPinned()) return -1;
            if (!note1.isPinned() && note2.isPinned()) return 1;
            
            // Then by ID (most recent first)
            return Long.compare(note2.getId(), note1.getId());
        });
        
        // Add notes to container in sorted order
        for (Note note : sortedNotes) {
            View noteContainer = createNoteContainer(note);
            notesContainer.addView(noteContainer);
        }
    }

    private void showBackButtonIfNeeded() {
        if (binding == null) return;
        
        MaterialButton btnBackToHome = binding.getRoot().findViewById(R.id.btn_back_to_home);
        if (btnBackToHome != null) {
            // Always show back button - user can navigate back to home from notes
            // This ensures the side nav Home button and back button both work
            btnBackToHome.setVisibility(View.VISIBLE);
        }
    }
}
