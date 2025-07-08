package com.example.app.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Collections;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.app.R;
import com.example.app.entities.Mail;
import com.example.app.repositories.MailRepository;
import com.example.app.viewmodels.MailViewModel;
import com.example.app.viewmodels.MailViewModelFactory;
import com.google.gson.Gson;

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

        // find views
        toEditText      = view.findViewById(R.id.toEditText);
        subjectEditText = view.findViewById(R.id.subjectEditText);
        bodyEditText    = view.findViewById(R.id.bodyEditText);
        sendButton      = view.findViewById(R.id.sendButton);
        saveDraftButton = view.findViewById(R.id.saveDraftButton);

        // setup repo + ViewModel
        repository = new MailRepository(requireContext());
        mailViewModel = new ViewModelProvider(this,
                new MailViewModelFactory(repository))
                .get(MailViewModel.class);

        // ▶ Callbacks
        Callback<ResponseBody> postCb = new Callback<>() {
            @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                Toast.makeText(getContext(), "Mail sent!", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
            @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                Log.e(TAG, "Send error", t);
                Toast.makeText(getContext(),
                        "Send failed: "+t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                sendButton.setEnabled(true);
            }
        };

        Callback<Mail> updateCb = new Callback<>() {
            @Override public void onResponse(Call<Mail> c, Response<Mail> r) {
                // inform inbox to refresh
                Bundle result = new Bundle();
                result.putBoolean("shouldRefresh", true);
                getParentFragmentManager().setFragmentResult("inboxRefresh", result);
            }
            @Override public void onFailure(Call<Mail> c, Throwable t) {
                Log.e(TAG, "Update error", t);
            }
        };

        // ▶ Prefill if editing a draft
        if (getArguments() != null && getArguments().containsKey("mail")) {
            initialMail = new Gson()
                    .fromJson(getArguments().getString("mail"), Mail.class);
            boolean wasDraft = initialMail.getLabels() != null &&
                    initialMail.getLabels().stream()
                            .anyMatch(l -> "drafts".equalsIgnoreCase(l.getId()));

            if (wasDraft) {
                if (initialMail.getToUsername() != null)
                    toEditText.setText(initialMail.getToUsername());
                subjectEditText.setText(initialMail.getSubject());
                bodyEditText   .setText(initialMail.getBody());
            } else {
                // not a draft → ignore
                initialMail = null;
            }
        }

        // ▶ Send button
        sendButton.setOnClickListener(v -> {
            String to  = toEditText.getText().toString().trim();
            String sub = subjectEditText.getText().toString().trim();
            String bd  = bodyEditText.getText().toString().trim();
            if (to.isEmpty() || sub.isEmpty() || bd.isEmpty()) {
                Toast.makeText(getContext(),
                        "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            sendButton.setEnabled(false);

            // build Mail
            Mail mail = isEditingDraft ? initialMail : new Mail();
            mail.setToUsername(to);
            mail.setSubject(sub);
            mail.setBody(bd);
            mail.setIsDraft(false);

            if (isEditingDraft) {
                // first update content
                repository.updateMail(mail, new Callback<Mail>() {
                    @Override public void onResponse(Call<Mail> c, Response<Mail> r) {
                        // then clear “drafts” & assign sent/received on backend
                        updateCb.onResponse(c, r);
                        postCb.onResponse(null, null);
                    }
                    @Override public void onFailure(Call<Mail> c, Throwable t) {
                        updateCb.onFailure(c, t);
                        postCb.onFailure(null, t);
                    }
                });
            } else {
                mailViewModel.sendMail(mail, postCb);
            }
        });

        // ▶ Save Draft button
        saveDraftButton.setOnClickListener(v -> {
            String to  = toEditText.getText().toString().trim();
            String sub = subjectEditText.getText().toString().trim();
            String bd  = bodyEditText.getText().toString().trim();
            if (to.isEmpty() && sub.isEmpty() && bd.isEmpty()) {
                Toast.makeText(getContext(),
                        "Draft is empty — nothing saved", Toast.LENGTH_SHORT).show();
                return;
            }
            saveDraftButton.setEnabled(false);

            // fall back to original “to” if user cleared it
            if (isEditingDraft && to.isEmpty()) {
                to = initialMail.getToUsername();
            }

            // build draft Mail
            Mail draft = isEditingDraft ? initialMail : new Mail();
            draft.setToUsername(to);
            draft.setSubject(sub);
            draft.setBody(bd);
            draft.setIsDraft(true);

            // upsert content → then force-assign “drafts” label
            Callback<ResponseBody> contentCb = new Callback<>() {
                @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                    repository.updateMailLabels(
                            draft.getId(),
                            Collections.singletonList("drafts"),
                            new Callback<Void>() {
                                @Override public void onResponse(Call<Void> c2, Response<Void> r2) {
                                    Toast.makeText(getContext(),
                                            "Draft saved", Toast.LENGTH_SHORT).show();
                                    requireActivity().getSupportFragmentManager().popBackStack();
                                }
                                @Override public void onFailure(Call<Void> c2, Throwable t2) {
                                    Toast.makeText(getContext(),
                                            "Label error: "+t2.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    saveDraftButton.setEnabled(true);
                                }
                            }
                    );
                }
                @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                    Toast.makeText(getContext(),
                            "Save failed: "+t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    saveDraftButton.setEnabled(true);
                }
            };

            if (isEditingDraft) {
                repository.updateMail(draft, new Callback<Mail>() {
                    @Override public void onResponse(Call<Mail> c, Response<Mail> r) {
                        contentCb.onResponse(null, null);
                    }
                    @Override public void onFailure(Call<Mail> c, Throwable t) {
                        contentCb.onFailure(null, t);
                    }
                });
            } else {
                repository.createDraft(draft, contentCb);
            }
        });

        return view;
    }
}
