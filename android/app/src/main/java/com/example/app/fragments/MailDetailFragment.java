package com.example.app.fragments;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.app.entities.Label;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;

import com.example.app.R;
import com.example.app.api.LabelAPI;
import com.example.app.auth.SessionManager;
import com.example.app.entities.BlacklistResponse;
import com.example.app.entities.Mail;
import com.example.app.entities.MailLabel;
import com.example.app.helpers.LabelMenuHelper;
import com.example.app.repositories.LabelRepository;
import com.example.app.repositories.MailRepository;
import com.example.app.utils.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.example.app.utils.DateUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MailDetailFragment extends Fragment {
    private static final String TAG = "MailDetailFragment";

    private Mail currentMail;
    private MailRepository repo;

    private LabelRepository labelRepo;

    private TextView subjectTextView, fromTextView, toTextView,
            timestampTextView, bodyTextView;
    private LinearLayout labelsContainer;
    private ImageButton deleteButton, starButton, spamButton, unreadButton,restoreButton,labelsButton;

    private Map<String,Integer> blacklistIds = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mail_detail, container, false);

        getParentFragmentManager().setFragmentResultListener(
                "inboxRefresh",
                getViewLifecycleOwner(),
                (key, bundle) -> {
                    repo.fetchMailById(currentMail.getId(), new Callback<Mail>() {
                        @Override public void onResponse(Call<Mail> c, Response<Mail> r) {
                            if (r.isSuccessful() && r.body() != null) {
                                currentMail = r.body();
                                displayLabels(currentMail.getLabels());
                            }
                        }
                        @Override public void onFailure(Call<Mail> c, Throwable t) { }
                    });
                }
        );

        // 1️⃣ Init Repo
        repo = new MailRepository(requireContext());
        labelRepo = new LabelRepository(
                RetrofitClient.getAuthenticatedInstance(requireContext())
                        .create(LabelAPI.class),
                new SessionManager(requireContext()).getUserId()
        );




        // 2️⃣ Find Views
        deleteButton  = view.findViewById(R.id.deleteButton);
        starButton    = view.findViewById(R.id.starButton);
        spamButton    = view.findViewById(R.id.spamButton);
        unreadButton  = view.findViewById(R.id.unreadButton);
        restoreButton = view.findViewById(R.id.restoreButton);
        labelsButton   = view.findViewById(R.id.labelsButton);


        LinearLayout restoreContainer = view.findViewById(R.id.restoreContainer);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        subjectTextView   = view.findViewById(R.id.subjectTextView);
        fromTextView      = view.findViewById(R.id.fromTextView);
        toTextView        = view.findViewById(R.id.toTextView);
        timestampTextView = view.findViewById(R.id.timestampTextView);
        bodyTextView      = view.findViewById(R.id.bodyTextView);
        labelsContainer   = view.findViewById(R.id.labelsContainer);

        // 3️⃣ Deserialize Mail
        if (getArguments() == null || !getArguments().containsKey("mail")) {
            Log.e(TAG, "No mail in arguments");
            return view;
        }
        currentMail = new Gson()
                .fromJson(getArguments().getString("mail"), Mail.class);

        if (hasLabel("trash")) {
            restoreContainer.setVisibility(View.VISIBLE);
        } else {
            restoreContainer.setVisibility(View.GONE);
        }

        // 4️⃣ Populate UI & labels
        subjectTextView.setText(currentMail.getSubject());
        fromTextView.setText("From: " + currentMail.getFromUsername());
        toTextView.setText("To: " + currentMail.getToUsername());
        // to keep “Sent:” in front
        timestampTextView.setText("Sent: " + DateUtils.formatMailTs(currentMail.getTimestamp()));
        bodyTextView.setText(currentMail.getBody());
        displayLabels(currentMail.getLabels());

        // 5️⃣ Auto-mark as “read” if it was unread
        if (hasLabel("unread")) {
            repo.toggleReadMail(currentMail, new Callback<Void>() {
                @Override public void onResponse(Call<Void> c, Response<Void> r) {
                    if (r.isSuccessful()) {
                        // update locally & UI
                        currentMail.getLabels().removeIf(l -> l.getId().equalsIgnoreCase("unread"));
                        currentMail.getLabels().add(new MailLabel("read","Read"));
                        displayLabels(currentMail.getLabels());
                        // notify list to refresh
                        Bundle result = new Bundle();
                        result.putBoolean("shouldRefresh", true);
                        getParentFragmentManager().setFragmentResult("inboxRefresh", result);
                    }
                }
                @Override public void onFailure(Call<Void> c, Throwable t) { /* ignore */ }
            });
        }

        // 6️⃣ Button callbacks
        deleteButton.setOnClickListener(v -> trashOrDelete());
        restoreButton.setOnClickListener(v -> restoreMail());
        starButton.setOnClickListener(v ->
                toggleLabel(new MailLabel("star", "Star")));


        spamButton.setOnClickListener(v -> {
            boolean wasSpam = hasLabel("spam");
            toggleLabel(new MailLabel("spam","Spam"));

            String combined = currentMail.getSubject() + "\n" + currentMail.getBody();
            List<String> urls = extractUrls(combined);

            if (!wasSpam) {
                // newly marked spam → add to blacklist and store ID
                for (String url : urls) {
                    repo.addToBlacklist(url, new Callback<BlacklistResponse>() {
                        @Override public void onResponse(Call<BlacklistResponse> c, Response<BlacklistResponse> r) {
                            if (r.isSuccessful() && r.body() != null) {
                                blacklistIds.put(url, r.body().getId());
                            }
                        }
                        @Override public void onFailure(Call<BlacklistResponse> c, Throwable t) {
                            Log.e(TAG, "Failed to add to blacklist: " + url, t);
                        }
                    });
                }
            } else {
                // un-spam → remove from blacklist using stored IDs
                for (String url : urls) {
                    Integer id = blacklistIds.get(url);
                    if (id != null) {
                        repo.removeFromBlacklist(id, new Callback<Void>() {
                            @Override public void onResponse(Call<Void> c, Response<Void> r) { }
                            @Override public void onFailure(Call<Void> c, Throwable t) {
                                Log.e(TAG, "Failed to remove from blacklist: " + url, t);
                            }
                        });
                        blacklistIds.remove(url);
                    }
                }
            }
        });

        unreadButton.setOnClickListener(v -> markUnread());
        labelsButton.setOnClickListener(v -> showLabelPicker());
        return view;
    }

    private void restoreMail() {
        if (!hasLabel("trash")) {
            Toast.makeText(getContext(), "Not in Trash", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> ids = new ArrayList<>();
        for (MailLabel l : currentMail.getLabels()) {
            if (!l.getId().equalsIgnoreCase("trash")) {
                ids.add(l.getId());
            }
        }
        repo.updateMailLabels(currentMail.getId(), ids, new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) {
                    currentMail.getLabels().removeIf(l -> l.getId().equalsIgnoreCase("trash"));
                    displayLabels(currentMail.getLabels());
                    Toast.makeText(getContext(), "Mail restored", Toast.LENGTH_SHORT).show();
                    Bundle result = new Bundle();
                    result.putBoolean("shouldRefresh", true);
                    getParentFragmentManager().setFragmentResult("inboxRefresh", result);
                } else {
                    Toast.makeText(getContext(), "Restore failed: " + r.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> c, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> extractUrls(String text) {
        List<String> urls = new ArrayList<>();
        Pattern p = Pattern.compile("((https?://)?(www\\.)?[\\w.-]+\\.[a-zA-Z]{2,}(\\S*)?)");
        Matcher m = p.matcher(text);
        while (m.find()) urls.add(m.group());
        return urls;
    }

    private void displayLabels(List<MailLabel> labels) {
        labelsContainer.removeAllViews();
        if (labels != null) {
            for (MailLabel label: labels) {
                TextView chip = new TextView(getContext());
                chip.setText(label.getName());
                chip.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
                chip.setPadding(24,12,24,12);

                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(40f);
                bg.setColor(getColorForLabel(label.getId()));
                chip.setBackground(bg);

                chip.setClickable(true);
                chip.setOnClickListener(v -> toggleLabel(label));

                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                p.setMargins(8,0,8,0);
                chip.setLayoutParams(p);

                labelsContainer.addView(chip);
            }
        }

        // update icons
        starButton.setImageResource(
                hasLabel("star")
                        ? android.R.drawable.btn_star_big_on
                        : android.R.drawable.btn_star_big_off
        );
        spamButton.setImageResource(
                hasLabel("spam")
                        ? android.R.drawable.ic_dialog_alert
                        : android.R.drawable.ic_dialog_info
        );

        unreadButton.setImageResource(
                hasLabel("read")
                        ? R.drawable.ic_markread
                        : R.drawable.ic_markunread
        );
    }



    private void toggleLabel(final MailLabel clicked) {
        List<String> ids = new ArrayList<>();
        boolean removed = false;
        if (currentMail.getLabels() != null) {
            for (MailLabel l: currentMail.getLabels()) {
                if (l.getId().equalsIgnoreCase(clicked.getId())) {
                    removed = true;
                } else {
                    ids.add(l.getId());
                }
            }
        }
        if (!removed) ids.add(clicked.getId());

        repo.updateMailLabels(currentMail.getId(), ids, new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) {
                    // refresh full Mail and UI
                    repo.fetchMailById(currentMail.getId(), new Callback<Mail>() {
                        @Override public void onResponse(Call<Mail> c2, Response<Mail> r2) {
                            if (r2.isSuccessful() && r2.body() != null) {
                                currentMail = r2.body();
                                displayLabels(currentMail.getLabels());
                            }
                        }
                        @Override public void onFailure(Call<Mail> c2, Throwable t) { }
                    });
                }
            }
            @Override public void onFailure(Call<Void> c, Throwable t) { }
        });
    }

    private void showLabelPicker() {
        // fetch them as LiveData and observe once
        labelRepo.getAllLabels().observe(getViewLifecycleOwner(), labels -> {
            if (labels == null || labels.isEmpty()) {
                Toast.makeText(requireContext(), "No labels available", Toast.LENGTH_SHORT).show();
                return;
            }

            // filter out the fixed ones:
            List<Label> custom = new ArrayList<>();
            for (Label lbl : labels) {
                if (!LabelMenuHelper.isFixedLabel(lbl.getName())) {
                    custom.add(lbl);
                }
            }

            if (custom.isEmpty()) {
                Toast.makeText(requireContext(), "You don’t have any custom labels yet", Toast.LENGTH_SHORT).show();
                return;
            }

            // build names + checked array
            CharSequence[] names = new CharSequence[custom.size()];
            boolean[] checked      = new boolean[custom.size()];
            for (int i = 0; i < custom.size(); i++) {
                Label lbl = custom.get(i);
                names[i] = lbl.getName();
                // pre-check if this mail already has that label
                checked[i] = hasLabel(lbl.getId());
            }

            new AlertDialog.Builder(requireContext())
                    .setTitle("Choose Labels")
                    .setMultiChoiceItems(names, checked, (dlg, which, isChecked) -> {
                        checked[which] = isChecked;
                    })
                    .setPositiveButton("OK", (dlg, which) -> {
                        // 1. Start with all of the mail’s existing labels
                        List<String> allIds = new ArrayList<>();
                        for (MailLabel ml : currentMail.getLabels()) {
                            allIds.add(ml.getId());
                        }

                        // 2. For each custom label, toggle its presence
                        for (int i = 0; i < custom.size(); i++) {
                            String id = custom.get(i).getId();
                            if (checked[i]) {
                                // if checked, add it if not already there
                                if (!allIds.contains(id)) {
                                    allIds.add(id);
                                }
                            } else {
                                // if unchecked, remove it
                                allIds.remove(id);
                            }
                        }

                        // 3. Send the merged list back to the server
                        repo.updateMailLabels(currentMail.getId(), allIds, new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> resp) {
                                if (resp.isSuccessful()) {
                                    // reload mail & UI
                                    repo.fetchMailById(currentMail.getId(), new Callback<Mail>() {
                                        @Override public void onResponse(Call<Mail> c2, Response<Mail> r2) {
                                            if (r2.isSuccessful() && r2.body() != null) {
                                                currentMail = r2.body();
                                                displayLabels(currentMail.getLabels());
                                            }
                                        }
                                        @Override public void onFailure(Call<Mail> c2, Throwable t) { }
                                    });
                                    // notify inbox to refresh
                                    Bundle result = new Bundle();
                                    result.putBoolean("shouldRefresh", true);
                                    getParentFragmentManager().setFragmentResult("inboxRefresh", result);
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> c, Throwable t) {
                                Toast.makeText(requireContext(),
                                        "Failed to update labels",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    })

                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private boolean hasLabel(String id) {
        if (currentMail.getLabels() == null) return false;
        for (MailLabel l : currentMail.getLabels()) {
            if (l.getId().equalsIgnoreCase(id)) return true;
        }
        return false;
    }

    private void trashOrDelete() {
        boolean inTrash = hasLabel("trash");

        if (inTrash) {
            repo.deleteMail(currentMail.getId(), new Callback<Void>() {
                @Override public void onResponse(Call<Void> c, Response<Void> r) {
                    if (r.isSuccessful()) {
                        Toast.makeText(getContext(),
                                "Mail permanently deleted",
                                Toast.LENGTH_SHORT).show();
                        requireActivity()
                                .getSupportFragmentManager()
                                .popBackStack();
                    }
                }
                @Override public void onFailure(Call<Void> c, Throwable t) {
                    Toast.makeText(getContext(),
                            "Delete failed: "+t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            List<String> ids = new ArrayList<>();
            if (currentMail.getLabels() != null) {
                for (MailLabel l: currentMail.getLabels()) ids.add(l.getId());
            }
            ids.add("trash");
            repo.updateMailLabels(currentMail.getId(), ids, new Callback<Void>() {
                @Override public void onResponse(Call<Void> c, Response<Void> r) {
                    if (r.isSuccessful()) {
                        Toast.makeText(getContext(),
                                "Moved to Trash",
                                Toast.LENGTH_SHORT).show();
                        requireActivity()
                                .getSupportFragmentManager()
                                .popBackStack();
                    }
                }
                @Override public void onFailure(Call<Void> c, Throwable t) { }
            });
        }
    }

    private void markUnread() {
        if (!hasLabel("read")) {
            Toast.makeText(getContext(), "Already unread", Toast.LENGTH_SHORT).show();
            return;
        }
        repo.toggleReadMail(currentMail, new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) {
                    Toast.makeText(getContext(),
                            "Marked Unread",
                            Toast.LENGTH_SHORT).show();
                    // tell inbox to refresh
                    Bundle result = new Bundle();
                    result.putBoolean("shouldRefresh", true);
                    getParentFragmentManager().setFragmentResult("inboxRefresh", result);
                    requireActivity()
                            .getSupportFragmentManager()
                            .popBackStack();
                }
            }
            @Override public void onFailure(Call<Void> c, Throwable t) { }
        });
    }



    private int getColorForLabel(String id) {
        switch (id.toLowerCase()) {
            case "sent":     return ContextCompat.getColor(requireContext(), R.color.label_green_light);
            case "read":     return ContextCompat.getColor(requireContext(), R.color.label_green);
            case "received": return ContextCompat.getColor(requireContext(), R.color.label_blue);
            case "spam":     return ContextCompat.getColor(requireContext(), R.color.label_red);
            default:         return ContextCompat.getColor(requireContext(), R.color.label_gray);
        }
    }
}
