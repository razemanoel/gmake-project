package com.example.app.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.app.R;
import com.example.app.entities.Mail;
import com.example.app.repositories.MailRepository;
import com.example.app.viewmodels.MailViewModel;
import com.example.app.viewmodels.MailViewModelFactory;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;

import java.util.Collections;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComposeMailFragment extends Fragment {
    private static final String TAG = "ComposeMailFragment";

    private EditText toEditText, subjectEditText, bodyEditText;
    private Button sendButton, saveDraftButton;
    private MailViewModel mailViewModel;
    private MailRepository repository;
    private Mail initialMail;
    private boolean isEditingDraft = false;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_compose_mail, container, false);

        // ▶ Toolbar back arrow
        MaterialToolbar toolbar = view.findViewById(R.id.composeTopAppBar);
        toolbar.setNavigationOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );

        // ▶ Find views
        toEditText      = view.findViewById(R.id.toEditText);
        subjectEditText = view.findViewById(R.id.subjectEditText);
        bodyEditText    = view.findViewById(R.id.bodyEditText);
        sendButton      = view.findViewById(R.id.sendButton);
        saveDraftButton = view.findViewById(R.id.saveDraftButton);

        // ▶ Setup repo + ViewModel
        repository    = new MailRepository(requireContext());
        mailViewModel = new ViewModelProvider(this,
                new MailViewModelFactory(repository))
                .get(MailViewModel.class);

        // ▶ Prefill if editing a draft
        if (getArguments() != null && getArguments().containsKey("mail")) {
            initialMail = new Gson()
                    .fromJson(getArguments().getString("mail"), Mail.class);
            boolean wasDraft = initialMail.getLabels() != null &&
                    initialMail.getLabels().stream()
                            .anyMatch(l -> "drafts".equalsIgnoreCase(l.getId()));
            if (wasDraft) {
                toEditText     .setText(initialMail.getToUsername());
                subjectEditText.setText(initialMail.getSubject());
                bodyEditText   .setText(initialMail.getBody());
                isEditingDraft = true;
            }
        }

        // ▶ Send button
        saveDraftButton.setOnClickListener(v -> {
            String to  = toEditText.getText().toString().trim();
            String sub = subjectEditText.getText().toString().trim();
            String bd  = bodyEditText.getText().toString().trim();

            if (to.isEmpty() && sub.isEmpty() && bd.isEmpty()) {
                Toast.makeText(getContext(),
                        "Draft is empty — nothing saved",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            saveDraftButton.setEnabled(false);

            // If we cleared “To” on an existing draft, keep the old value:
            if (isEditingDraft && to.isEmpty()) {
                to = initialMail.getToUsername();
            }

            // Build the draft object
            Mail draft = (isEditingDraft ? initialMail : new Mail());
            draft.setToUsername(to);
            draft.setSubject(sub);
            draft.setBody(bd);
            draft.setIsDraft(true);

            if (isEditingDraft) {
                // —— UPDATE only ——
                repository.updateMail(draft, new Callback<Mail>() {
                    @Override
                    public void onResponse(Call<Mail> call, Response<Mail> resp) {
                        if (!resp.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    "Failed to update draft (code " + resp.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                            saveDraftButton.setEnabled(true);
                            return;
                        }
                        Toast.makeText(getContext(),
                                "Draft updated",
                                Toast.LENGTH_SHORT).show();
                        // tell Inbox to refresh
                        Bundle result = new Bundle();
                        result.putBoolean("shouldRefresh", true);
                        getParentFragmentManager()
                                .setFragmentResult("inboxRefresh", result);
                        // go back
                        requireActivity()
                                .getSupportFragmentManager()
                                .popBackStack();
                    }

                    @Override
                    public void onFailure(Call<Mail> call, Throwable t) {
                        Toast.makeText(getContext(),
                                "Failed to update draft: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        saveDraftButton.setEnabled(true);
                    }
                });
            } else {
                // —— FIRST SAVE —— (exactly as before)
                repository.createDraft(draft, new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                        if (!r.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    "Failed to save draft (code " + r.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                            saveDraftButton.setEnabled(true);
                            return;
                        }
                        // parse new draft so we can update it next time
                        try {
                            String json = r.body().string();
                            initialMail = new Gson().fromJson(json, Mail.class);
                            isEditingDraft = true;
                        } catch (Exception ex) {
                            Log.w(TAG, "Could not parse new draft", ex);
                            isEditingDraft = true;
                        }
                        Toast.makeText(getContext(),
                                "Draft saved",
                                Toast.LENGTH_SHORT).show();
                        Bundle result = new Bundle();
                        result.putBoolean("shouldRefresh", true);
                        getParentFragmentManager()
                                .setFragmentResult("inboxRefresh", result);
                        requireActivity()
                                .getSupportFragmentManager()
                                .popBackStack();
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> c, Throwable t) {
                        Toast.makeText(getContext(),
                                "Failed to save draft: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        saveDraftButton.setEnabled(true);
                    }
                });
            }
        });

        return view; ///best
    }
}
